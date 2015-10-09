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
package jef.net.ftp.client;


import java.io.IOException;

import jef.common.log.LogUtil;
import jef.tools.JefConfiguration;
import jef.tools.JefConfiguration.Item;

/**
 * 继承jef.inner.sun.net.ftp.FtpClient，扩充了一些功能
 * @author Administrator
 *
 */
final class FtpClient extends jef.inner.sun.net.ftp.AbstractFtpClientImp {
	private boolean debug=JefConfiguration.getBoolean(Item.DB_DEBUG,false);
	public String execute(String cmd) throws IOException {
		super.issueCommandCheck(cmd);
		StringBuilder sb=new StringBuilder();
		for(Object o:super.serverResponse){
			sb.append(o);
		}
		String data=sb.toString();
		if(debug){
			LogUtil.show(">"+data);
		}
		return data;
	}
	
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void rd(String path) throws IOException{
		String cmd="RMD " + path;
		super.issueCommandCheck(cmd);
	}
	
	public void delete(String path) throws IOException{
		String cmd="DELE " + path;
		issueCommandCheck(cmd);
	}
	
	public String[] getResponse(){
		String[] array=new String[super.serverResponse.size()];  
		for(int i=0;i<super.serverResponse.size();i++){
			array[i]=(String)super.serverResponse.elementAt(i);
		}
		return array;
	}

	@Override
	protected int issueCommand(String s) throws IOException {
		if(debug)LogUtil.show(s);
		return super.issueCommand(s);
	}
}
