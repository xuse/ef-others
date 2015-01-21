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

import java.io.File;
import java.io.IOException;

import jef.common.log.LogUtil;
import jef.ui.ConsoleConversation;
import jef.ui.console.AbstractConsoleShell;

import org.apache.commons.lang.StringUtils;

/**
 * 可以在控制台上用命令行方式进行socket会话
 * @author Administrator
 */
public class FtpConversation extends ConsoleConversation<String> {

	Ftp client = null;
	String server;
	String current;
	String localDir="c:/";
	int port;

	public FtpConversation(AbstractConsoleShell app, String url, int port) {
		super(app);
		this.server = url;
		this.port = port;
	}

	
	protected void execute() {
		if(StringUtils.isEmpty(server)){
			this.server=getInput("Ftp Host:");
			this.port=getInputInt("Ftp Port:");
		}
		if(StringUtils.isEmpty(server)){
			return;
		}
		if (!init()) {
			return;
		}
		while (true) {
			String str = getInput(current + ">");
			if (str.equalsIgnoreCase("close") || str.equalsIgnoreCase("bye")) {
				break;
			}
			innerExecute(str);
		}
		close();
	}

	private void innerExecute(String str) {
		try {
			if (str.startsWith("get ")) {
				String arg=StringUtils.substringAfter(str, "get ").trim();
				client.download(arg,new File(localDir+arg));
				prompt("File saved at: " + localDir+arg);
			} else if (str.startsWith("put ")) {
				String arg=StringUtils.substringAfter(str, "put ").trim();
				client.upload(new File(arg));
				prompt("upload finished");
			} else if (str.startsWith("download ")) {
				String arg=StringUtils.substringAfter(str, "download ").trim();
				client.download(arg,new File(localDir+arg));
				prompt("File saved at: " + localDir+arg);
			} else if (str.startsWith("upfiles ")) {
				String args=StringUtils.substringAfter(str, "upfiles ").trim();
				File dir=new File(args);
				if(!dir.exists() || dir.isFile()){
					prompt("The target is not a directory:"+ dir.getAbsolutePath());
					return;
				}
				client.upload(dir.listFiles());
				prompt("upload finished");
			} else if (str.startsWith("upload ")) {
				String[] args=StringUtils.substringAfter(str, "upload ").trim().split(" ");
				if(args.length>1){
					client.doUpload(new File(args[0]), args[1]);	
				}else{
					client.upload(new File(args[0]));
				}
				prompt("upload finished");
			} else if (str.startsWith("md ")) {
				String arg=StringUtils.substringAfter(str, "md ").trim();
				client.createDir(arg);
			} else if (str.startsWith("rd ")) {
				String arg=StringUtils.substringAfter(str, "rd ").trim();
				client.rd(arg);
			} else if (str.equals("cdup")) {
				client.cdUp();
				current = client.pwd();
			} else if (str.startsWith("cd ")) {
				String arg=StringUtils.substringAfter(str, "cd ").trim();
				client.cd(arg);
				current = client.pwd();
			} else if (str.equals("asc") || str.equals("ascii")) {
				client.ascii();
				prompt("TYPE set to A");
			} else if (str.equals("bin") || str.equals("binary")) {
				client.binary();
				prompt("TYPE set to I");
			} else if (str.equals("pwd") || str.equals("cd")) {
				current = client.pwd();
				prompt(current);
			} else if (str.equals("list")) {
				prompt(client.listAsString());
			} else if (str.startsWith("list ")) {
				String arg=StringUtils.substringAfter(str, "list ").trim();
				prompt(client.list(arg));
			} else if (str.equals("ls")) {
				LogUtil.show(client.dir(""));
			} else if (str.startsWith("ls ")) {
				String arg=StringUtils.substringAfter(str, "ls ").trim();
				LogUtil.show(client.dir(arg));
			}else{
				LogUtil.show(client.execute(str));
			}
		} catch (IOException e) {
			LogUtil.exception(e);
		}
	}

	private void close() {
		if (client != null) {
			try {
				client.close();
			} catch (IOException e) {
				LogUtil.exception(e);
			}
			client = null;
		}
	}

	public boolean init() {
		if (port <= 0)
			port = 21;
		try {
			String user = getInput("User:");
			String pass = getInput("Password:");
			client = new FtpImpl(server, port, user, pass);
			client.connect();
			current = client.pwd();
			return true;
		} catch (Exception e) {
			LogUtil.exception(e);
			return false;
		}
	}
}
