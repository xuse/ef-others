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
import java.net.UnknownHostException;

import jef.common.log.LogUtil;
import jef.tools.Exceptions;

/**
 * Socket连接会话的抽象类
 * 
 * @author Administrator
 *
 */
public abstract class MessageClient {
	protected String server;
	protected int port = getDefaultPort();
	protected String user;
	protected String password;
	protected BufferedReader sockin;
	protected PrintWriter sockout;
	protected Socket client;
	private OutputStream os;
	
	protected abstract boolean isDebug();
	
	public MessageClient(String server, String user, String password) {
		this.server = server;
		this.user = user;
		this.password = password;
	}

	protected abstract int getDefaultPort();
	
	protected void sendLine(String string) {
		sockout.println(string);
		if(isDebug())System.out.println(string);
	}

	/**
	 * 服务器是否连接
	 * @return
	 */
	public boolean isOpen(){
		return client!=null && client.isConnected();
	}

	public void initConnectObjs() throws UnknownHostException, IOException {
		client = new Socket(server, port);
		InputStream is = client.getInputStream();
		sockin = new BufferedReader(new InputStreamReader(is));
		os = client.getOutputStream();
		sockout = new PrintWriter(os, true);
		String str=sockin.readLine();
		if(isDebug()){
			LogUtil.show(str);	
		}
	}
	protected OutputStream getRawOutput(){
		sockout.flush();//为了保证同步，要先帅新PrintStream中的缓存
		return os;
	}

	public synchronized void close() {
		if (client != null) {
			sendLine("QUIT");
			String str;
			try {
				str = sockin.readLine();
				if(isDebug())LogUtil.show(str);
			} catch (IOException e1) {
				Exceptions.log(e1);
			}
			try {
				client.close();
			} catch (IOException e) {
				Exceptions.log(e);
			}
			sockin = null;
			sockout = null;
			client = null;
		}
	}

	protected void startReadThread() {
		Thread t = new Thread() {
			
			public void run() {
				while (client != null && client.isConnected()) {
					try {
						String str = sockin.readLine();
						if (str == null)
							break;
						LogUtil.show(str);
						processServerMsg(str);
					} catch (IOException e) {
						break;
					}
				}
				LogUtil.show("Socket is closed.");
			}
		};
		t.setDaemon(true);
		t.start();
	}

	abstract protected void processServerMsg(String str);

	abstract protected void login() throws IOException,AuthenticationException;

}
