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
package jef.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import jef.common.log.LogUtil;
import jef.tools.support.JefBase64;
import jef.ui.ConsoleConversation;
import jef.ui.console.AbstractConsoleShell;

import org.apache.commons.lang.StringUtils;

/**
 * 可以在控制台上用命令行方式进行socket会话
 * @author Administrator
 *
 */
public class SocketConversation extends ConsoleConversation<String>{
	public SocketConversation(AbstractConsoleShell app, String url, int port) {
		super(app);
		this.server=url;
		this.port=port;
	}

	
	protected void execute() {
		if(!init()){
			return;
		}
		Thread t=new Thread(){
			
			public void run() {
				while(client!=null && client.isConnected()){
					try {
						String str=sockin.readLine();
						prompt(str);	
						if(str.startsWith("334 ")){
							String code=StringUtils.substringAfter(str, "334");
							System.out.println(new String(JefBase64.decodeFast(code)));
						}
					} catch (IOException e) {
						LogUtil.exception(e);
						break;
					}	
				}
				prompt("Socket was closed already.");
			}
		};
		t.setDaemon(true);
		t.start();
		while(true){
			String str=getInput("SOCKET:");
			sockout.println(str);	
			if(str.equalsIgnoreCase("QUIT")){
				break;
			}
		}
		close();
	}
	
	private void close() {
		if(client!=null){
			try {
				client.close();
			} catch (IOException e) {
				LogUtil.exception(e);
			}
			client=null;
		}
	}



	Socket client = null;
	BufferedReader sockin;
	PrintWriter sockout;
	String server;
	int port;
	public boolean init(){
		try{
			if(port<=0)port=25;
			client = new Socket(server, port);
			InputStream is = client.getInputStream();
			sockin = new BufferedReader(new InputStreamReader(is));
			OutputStream os = client.getOutputStream();
			sockout = new PrintWriter(os, true);	
		}catch(IOException e){
			LogUtil.exception(e);
			return false;
		}
		return true;
	}
}
