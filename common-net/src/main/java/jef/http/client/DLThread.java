/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jef.http.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

import jef.common.log.LogUtil;
import jef.http.client.support.HttpConnection;
import jef.http.client.support.HttpException;
import jef.http.client.support.PostMethod;
import jef.http.client.support.ThreadState;
import jef.tools.Assert;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.ThreadUtils;

class DLThread extends Thread implements Serializable {
	private static final long serialVersionUID = -3317849201046281359L;
	private static int BUFFER_SIZE = 81920;
	private static int BUFFER_READY_WRITE=(int)(BUFFER_SIZE * 0.9);
	
	private int id; // 线程编号
	//private long startPos; // 块开始位置
	private long endPos; // 块结束位置
	private long curPos; // 块当前位置
	private long readByte; // 目前已获取字节数
	private ThreadState threadState;
	private String errorMessage;
	
	transient private BlockSession session;
	transient byte[] buf = new byte[BUFFER_SIZE];
	transient int buf_count = 0;

	/**
	 * 当恢复状态时使用
	 */
	void setSession(BlockSession session) {
		this.session = session;
		buf= new byte[BUFFER_SIZE];
		buf_count = 0;
	}

	public DLThread(BlockSession session, int id, long startPos, long endPos) {
		this.session = session;
		this.id = id;
		this.curPos = startPos;
		//this.startPos =startPos;
		this.endPos = endPos;
		if (HttpEngine.DEBUG_MODE) {
			session.addLog("Bolck generated:" + startPos + "-" + endPos + "/" + session.getTotalLength());
		}
		threadState = ThreadState.PROCESSING;
		readByte = 0;
	}

	public void run() {
		Assert.notNull(session);
		RandomAccessFile fos = null;

		File file = new File(session.getFileTmppath());
		HttpConnection con = null;
		try {
			con = connectServer();
			// 打开文件并定位
			fos = new RandomAccessFile(file, "rw");
			fos.seek(curPos);
			
			while (con.getTransferPos() < endPos || endPos < 0) {//尚未完成时，继续接收数据
				int n = fetchData(fos, con);
				if (n == -1)break;
				if(session.isBreak()){
					writeToFile(fos);
					threadState = ThreadState.PAUSE;
					ThreadUtils.doNotifyAll(session);
					return;
				}
			}
			Assert.isFalse(buf_count > 0);
			if (HttpEngine.DEBUG_MODE) {
				session.addLog("Download thread " + id + " finished.");
			}
			if (session.getTotalLength()>-1 && con.getTransferPos() > session.getTotalLength()) {
				session.addLog(session.getUrl() + " thread " + id + " ends: transPos=" + con.getTransferPos() + ",length=" + session.getTotalLength());
				session.addLog("Warn: file is possibly damaged:" + session.getFileTmppath());
			}
			threadState = ThreadState.DONE;
		} catch (Throwable ex) {
			session.addLog(ex.getMessage());
			this.errorMessage = StringUtils.exceptionStack(ex, "jef");
			threadState = ThreadState.ERROR;
		} finally {
			IOUtils.closeQuietly(con);
			IOUtils.closeQuietly(fos);
		}
		ThreadUtils.doNotifyAll(session);
	}

	/**
	 * @param fos
	 * @param con
	 * @return >0本次获得的字节数， 0 本次没有获得数据，允许继续尝试 -1 终止循环，认为下载成功
	 * @throws IOException
	 *             在文件存取操作上抛出异常，下载中止。
	 * @throws HttpException
	 *             在网络连接操作上抛出异常，下载中止。
	 */

	int fetchData(RandomAccessFile fos, HttpConnection con) throws IOException, HttpException {
		int len = -1;
		try {
			int max=BUFFER_SIZE - buf_count;//缓存器剩余容量
			len = con.getDownloadStream().read(buf, buf_count, max);
			if (len > 0) {
				buf_count += len;
				readByte += len;
			}
		} catch (IOException ee) {// 获取数据超时的场合
			writeToFile(fos);// 不管怎么说先把缓存中的数据写掉
			session.addLog(ee.getMessage());
			boolean conntected = reconnect(fos, con,ee);
			while (!conntected && con.canRetry()) {
				conntected = reconnect(fos, con,ee);
			}
			if (conntected) {
				return 0;
			} else {
				throw new HttpException(408,ee.getMessage() + ", Retries exceed.");// 通过抛出异常来终止循环
			}
		}
		
		if (len == -1) {// 当服务器没有数据返回时
			if (buf_count > 0)
				writeToFile(fos);// 不管怎么说先把缓存中的数据写掉
			
			if (endPos < 0) {// 无论服务器是否支持断点续传，此时应该得知文件长度，如果不知道，那么就认为无数据返时下载结束。
				return -1;
			} else {
				if(con.getTransferPos()>=endPos){
					session.addLog("DEBUG: this should never happened! Is there bug in position calcaution? "+this.getPosDebugString());
				}
				if(con.supportBreakPoint()){
					// 在已知目标文件长度时，如果发现此时下载没有完成，即认为发生“断流”现象，尝试重新连接服务器
					boolean conntected = reconnect(fos, con,null);
					while (!conntected && con.canRetry()) {
						conntected = reconnect(fos, con,null);
					}
					if (conntected) {
						return 0;
					} else {
						throw new HttpException(999,"Can't connect to server, Retries exceed.");// 通过抛出异常来终止循环
					}	
				}else{
					//此时可能是服务器端长度计算问题，暂时先终止下载，记录日志，待检查文件完整性后再做处理
					if(con.canRetry()){
						con.addRetry();
						return 0;		
					}else{
						session.addLog(con.getURL()+" Download terminate at "+curPos+"/"+endPos+", please check local file:"+session.getFileTmppath()+" for the integrity");
						return -1;
					}
				}
			}
		} else {// 正常获取数据的场合
			if ((endPos>0 && con.getTransferPos() >= endPos) || buf_count >=BUFFER_READY_WRITE ) {
				writeToFile(fos);
			}
			return len;
		}
	}

	private void writeToFile(RandomAccessFile fos) throws HttpException{
		if (buf_count == 0)return;
		try {
			fos.write(buf, 0, buf_count);
			curPos += buf_count;
			buf_count = 0;
		} catch (IOException e) {
			session.addLog("Error at writing the file buffer?");
			throw new HttpException(e.getMessage());
		}
	}

	/**
	 * 重新连接
	 * @param con
	 * @param fos
	 * @return
	 * @throws IOException
	 */
	private boolean reconnect(RandomAccessFile fos, HttpConnection con,IOException e) throws IOException {
		if (endPos > 0 && con.supportBreakPoint()) {// 从上次下载到的地方继续
			boolean isPost = (session.getConnectOptions().getMethod() instanceof PostMethod);
			String msg = "Disconnect, retry:" + ((isPost) ? session.getConnectOptions().getReference(session.getUrl()) : session.getUrl().toString());
			if(e!=null)msg+="\n"+StringUtils.exceptionSummary(e);
			msg = msg + "\n"+getPosDebugString();
			session.addLog(msg);
			return con.reconnect(curPos, endPos,session);
		} else { // 没有获得本次下载长度，之前下载的内容只能作废(此处以长度判断不准确，
			String msg = "Disconnect, not support breakpoint, re-download:" + session.getUrl();
			LogUtil.show(msg);
			session.addLog(msg);
			this.curPos =  0;
			//this.startPos =0;
			if (con.reconnect(-1, -1,session)) {
				if (curPos == 0)
					fos.seek(0);// 由于不支持断点续传，变为重新下载 ;
				return true;
			} else {
				return false;
			}
		}
	}

	//线程初次连接服务器
	private HttpConnection connectServer() throws IOException {
		HttpConnection con = session.pickCon();
		if (con == null) {
			con = HttpConnection.createConnection(session.getUrl(), session.getConnectOptions(), false);
			if(curPos>0){
				if(!con.supportBreakPoint()){
					String msg = "\u7531\u4E8E\u4E0D\u652F\u6301\u65AD\u70B9\u7EED\u4F20\uFF0C\u6587\u4EF6\u91CD\u65B0\u4E0B\u8F7D:" + session.getUrl();
					LogUtil.show(msg);
					session.addLog(msg);
					this.curPos = 0;
				}
				con.setRequestRangeAndConnect(curPos,endPos,session);	
			}else{
				con.connect();	
			}
		}
		if (session.getTotalLength() < 0) {// 如果之前没能获得下载长度，则将此次连结获得的下载长度写入
			endPos = con.getContentLength();
			session.setTotalLength(endPos);
		} else {//如果是206内容
			con.checkRange(id,curPos,endPos,session);
		}
		return con;
	}


	public long getReadByte() {
		return readByte;
	}

	public ThreadState getThreadState() {
		return threadState;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getPosDebugString(){
		return this.curPos+"-"+this.endPos+"(AlreadyRead:"+readByte+")";
	}

	protected final void setThreadState(ThreadState threadState) {
		this.threadState = threadState;
	}
}
