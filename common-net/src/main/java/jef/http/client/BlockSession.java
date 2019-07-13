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
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;

import jef.http.client.HttpTask.TaskState;
import jef.http.client.support.HttpConnection;
import jef.http.client.support.ThreadState;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.ThreadUtils;

/**
 * 每个Session对应一个Http任务
 * @author Administrator
 */
public class BlockSession extends ConnectSession{
	private static final long serialVersionUID = 3801524122558194318L;
	private DLThread[] dlThreads; // 任务的线程集
	private long totalLength; // 下载文件长度
	private String fileTmppath;//下载中的文件(将被加上后缀)
	
	
	//预连结重用功能
	//setCon将一个可用的预连存储下来。
	//pick将一个可用的预连结取走,注意凡是预连接都是不含Partion参数的连接，不能被多块下载重用
	transient private HttpConnection preConnection;
	
	public synchronized HttpConnection pickCon() {
		HttpConnection tempVar=preConnection;
		preConnection=null;
		return tempVar;
	}
	public synchronized void setCon(HttpConnection con) {
		if(con!=null && con.isConnected()){
			this.preConnection = con;
			this.totalLength=con.getContentLength();
		}
			
		
	}
	
	//构造器
	public BlockSession(int contentLen, int threadQut,String tmpPath,HttpTask dltask, HttpConnection con) {
		super(dltask);
		this.totalLength=contentLen;
		this.fileTmppath=tmpPath;
		if(con!=null &&!con.supportBreakPoint()){
			threadQut=1;
		}
		if(threadQut==1)setCon(con);//只有单线程下载才可以利用之前建立的连接。
		dlThreads=new DLThread[threadQut];
	}
	
	public long getTotalLength() {
		return totalLength;
	}
	public void setTotalLength(long length) {
		this.totalLength=length;
	}
	
	String getFileTmppath() {
		return fileTmppath;
	}
	
	
	public String getCompletedPercent() {
		long completedTot = getCompletedLength();
		if(totalLength!=0){
			return new BigDecimal(100*completedTot/totalLength).toString();	
		}else{
			return "UN";
		}
		
	}
	
	
	public long getCompletedLength() {
		long completedTot = 0;
		for (DLThread t : dlThreads) {
			if (t != null)
				completedTot += t.getReadByte();
		}
		return completedTot;
	}
	
	public boolean isRunning() {
		for (DLThread t : dlThreads) {
			if (t != null) {
				if (ThreadState.PROCESSING.equals(t.getThreadState()))
					return true;
			}
		}
		return false;
	}
	
	private boolean isPause() {
		boolean flag=false;
		for (DLThread t : dlThreads) {
			if (t != null) {
				if (ThreadState.PAUSE!=t.getThreadState()){
					return false;
				}else{
					flag=true;
				}
					
			}
		}
		return flag;
	}
	
	
	
	public String getErrorMessage() {
		StringBuilder sb=new StringBuilder();
		for (DLThread t : dlThreads) {
			if (t != null){
				if (ThreadState.ERROR.equals(t.getThreadState()))	{
					sb.append(t.getErrorMessage()).append("\n");
				}
			}
		}
		if(sb.length()==0)return null;
		return sb.toString();
	}
	
	//恢复运行各个线程
	public void resumeDownload(HttpEngine engine) {
		for (int i = 0; i <dlThreads.length ; i++) {
			dlThreads[i].setSession(this);
			engine.executeInPool(dlThreads[i]);
		}
		waitToFinished();
	}
	
	private void waitToFinished() {
		//等待各个下载线程完成
		while(this.isRunning()){
			ThreadUtils.doWait(this);
		}
		doFinished();
	}

	//创建并运行各个线程
	public void startDownload(HttpEngine engine) {
		int threadQut=dlTask.getThreadQut();
		
		//创建各个下载线程并运行
		if (threadQut == 1) {
			dlThreads[0] = new DLThread(this, 0, 0, -1);//单线程下不告目标长度，线程以此来判断是否单独下载（总觉得理解不便）
			dlThreads[0].start();
		} else {// 开始分块，和java的含头去尾的Range表示形式不同，Http Range是含头含尾的，也就是说一个长度500的文件，实际Range是0~499
			long subLen = (totalLength / threadQut) + 1;//每块长度
			long pos = 0;
			
			for (int i = 0; i < threadQut; i++) {
				long endPos= pos + subLen;
				if (endPos > totalLength)endPos = totalLength;
				dlThreads[i] = new DLThread(this, i, pos, endPos);
				dlThreads[i].start();
				pos += subLen;
			}
		}
		waitToFinished();
	}
	
	/**
	 * 执行完成后的检查和对象返回工作
	 */
	 void doFinished(){
		boolean error=false;
		File file = new File(getFileTmppath());//获得已经下载的临时文件
		if(isPause()){
			dlTask.isBreak=0;
			dlTask.setState(TaskState.BREAK);
			for(DLThread th:this.dlThreads){
				th.setThreadState(ThreadState.PROCESSING);
			}
			return;
		}
		String err= getErrorMessage();
		if(err!=null){
			dlTask.setReturnType(ReturnType.ERROR_MESSAGE);
			dlTask.setReturnObj(err) ;
			error=true;
		}else{
			file=rename(file);
			//开始返回下载对象
			if(!file.exists() || !file.isFile()){
				throw new RuntimeException("\u4E0B\u8F7D\u5B8C\u6210\uFF0C\u4F46\u662F\u627E\u4E0D\u5230\u4E0B\u8F7D\u540E\u7684\u6587\u4EF6." + file.getAbsolutePath());
			}
			if(HttpEngine.DEBUG_MODE)addLog("\u4E0B\u8F7D\u4E3A\u6587\u4EF6\uFF1A" + file.getAbsolutePath());
			
			try{
				if(dlTask.getReturnType().equals(ReturnType.STREAM)){
					dlTask.setReturnObj(new FileInputStream(file));
				}else if(dlTask.getReturnType().equals(ReturnType.FILE)){
					dlTask.setReturnObj(file);
				}else{
					String msg="Block\u6A21\u5F0F\u53EA\u80FD\u8FD4\u56DEStream\u6216\u8005file.";
					addLog(msg);
					dlTask.setReturnType(ReturnType.ERROR_MESSAGE);
					dlTask.setReturnObj(msg);
				}	
			}catch(IOException e){
				Exceptions.log(e);
				dlTask.setReturnType(ReturnType.ERROR_MESSAGE);
				dlTask.setReturnObj(e.getMessage());
				error=true;
			}
		}
		if(error){
			if(file.exists() && getCompletedLength()<1000)file.delete();
			dlTask.setState(TaskState.ERROR);
		}else{
			dlTask.setState(TaskState.DONE);
		}
	}
	
	//将下载临时文件重命名为目标文件
	private File rename(File file) {
		if(!file.exists()){
			throw new RuntimeException("\u4E0B\u8F7D\u5DE5\u4F5C\u6587\u4EF6\u4E22\u5931\u3002" + file.getAbsolutePath());
		}
		if(!file.getAbsolutePath().equalsIgnoreCase(dlTask.filepath)){//如果临时文件和目标文件不同名
			File newFile=new File(dlTask.filepath);
			newFile=IOUtils.escapeExistFile(newFile);//确保目标文件名有效
			if(file.renameTo(newFile)){
				return newFile;	
			}else{
				addLog(file.getAbsolutePath()+"\u6539\u540D\u5931\u8D25\uFF0C\u8BF7\u624B\u5DE5\u4FEE\u590D.");
				return file;
			}
		}
		return file;
	}
	
	public String getLength() {
		return StringUtils.formatSize(totalLength);
	}
	public boolean isBreak() {
		return dlTask.isBreak!=0;
	}
}
