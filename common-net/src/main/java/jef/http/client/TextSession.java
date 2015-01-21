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

import java.io.IOException;
import java.math.BigDecimal;

import jef.http.client.HttpTask.TaskState;
import jef.http.client.support.HttpConnection;
import jef.http.client.support.PostMethod;
import jef.tools.StringUtils;

class TextSession extends ConnectSession {
	private static final long serialVersionUID = 3640385803172463765L;
	private long uploadLength;
	private long postBytes;
	private int contentLen;
	private HttpConnection con;
	
	public HttpConnection getCon() {
		return con;
	}

	public TextSession(HttpTask dlTask, HttpConnection con){
		   super(dlTask);
		   this.con=con;
	}
	
	public int getContentLen() {
		return contentLen;
	}
	public void setContentLen(int contentLen) {
		this.contentLen = contentLen;
	}
	public long getUploadLength() {
		return uploadLength;
	}
	public void setUploadLength(int uploadLength) {
		this.uploadLength = uploadLength;
	}
	
	
	public long getCompletedLength() {
		if(dlTask.getState().equals(TaskState.POSTING)){//计算上传字节
			return postBytes;
		}else{
			return 0;	
		}
	}
	
	
	public String getCompletedPercent() {
		long completedTot = getCompletedLength();
		if(dlTask.getState().equals(TaskState.POSTING)){//计算上传字节
			if(this.uploadLength>0){
				return new BigDecimal(completedTot).divide(new BigDecimal(this.uploadLength), 2, BigDecimal.ROUND_HALF_EVEN).divide(new BigDecimal(0.01), 0, BigDecimal.ROUND_HALF_EVEN).toString();	
			}else{
				return "UN";	
			}
		}else{
			return "0";	
		}
	}
	
	public String getErrorMessage() {
		return null;//异常直接上刨出
	}
	
	public boolean isRunning() {
		return false;
	}

	public void send() throws IOException {
		//连接或上传数据
		if(getConnectOptions().getMethod() instanceof PostMethod){
			uploadLength=((PostMethod)getConnectOptions().getMethod()).getLength();
		}
		if (con == null){
			dlTask.setState(TaskState.POSTING);
			con =HttpConnection.createConnection(getUrl(),getConnectOptions(),true);
		}
		contentLen=con.getContentLength();
	}

	
	public String getLength() {
		return StringUtils.formatSize(contentLen);
	}
}
