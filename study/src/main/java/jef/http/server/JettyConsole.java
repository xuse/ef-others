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
package jef.http.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import jef.common.Callback;
import jef.common.Configuration.ConfigItem;
import jef.common.Entry;
import jef.common.log.LogUtil;
import jef.http.server.jetty.JettyServer;
import jef.tools.Assert;
import jef.tools.StringUtils;
import jef.tools.ThreadUtils;
import jef.tools.reflect.ClassLoaderUtil;
import jef.ui.ConsoleConversation;
import jef.ui.console.AbstractConsoleShell;
import jef.ui.console.ShellResult;

public class JettyConsole extends AbstractConsoleShell {
	
	public JettyConsole() {
		super(null);
	}
	
	
	private Map<Integer, WebServer> wsMap = new HashMap<Integer, WebServer>();
	private Entry<String, String> lastClosed;

	public static void main(String... addargs) {
		System.setProperty("common.debug.adapter", "false");
		JettyConsole c = new JettyConsole();
		c.start(addargs);
	}

	/**
	 * 主要方法，开始一个控制台程序
	 * @param args
	 */
	public void start(String... args) {
		try {
			initApplication(args);
		} catch (Exception e1) {
			LogUtil.exception(e1);
			return;
		}
		System.out.print(getPrompt());
		new InputListener().start();//开启本地键盘输入等待线程
		
		while (keepgoing) {
			ThreadUtils.doWait(lock); //waiting for cmd
			if(cmd!=null){
//				if(debug)System.out.println("为命令"+cmd+"加锁");
				lock.lock();
				ShellResult result=perform(cmd, true);
//				if(debug)System.out.println("为命令"+cmd+"解锁");
				cmd=null;
				lock.unlock();
				if(result.needProcess()){
					LogUtil.show("ConsoleApplication\u662F\u9876\u5C42Shell\uFF0C\u4E0D\u80FD\u629B\u51FA\u547D\u4EE4\u3002");
					LogUtil.show(result.getCmd());
				}
				if(result==ShellResult.TERMINATE)break;
			}
		}
		try {
			closeApplication();
		} catch (Exception e1) {
			LogUtil.exception(e1);
		}
	}
	/**
	 * 处理命令
	 * @param str
	 * @param source
	 * @return
	 */
	protected ShellResult performCommand(String str,String... source) {
		ShellResult result=ShellResult.CONTINUE;
		if ("q".equalsIgnoreCase(str) || "exit".equalsIgnoreCase(str)) {
			return ShellResult.TERMINATE;
		} else {
			try {
				result=performMyCommand(str,source);
			} catch (Exception e) {
				LogUtil.exception(e);
			}
		}
		return result;
	}
	
	protected ConfigItem getEnvironmentEnum(String arg0) {
		return null;
	}

	protected String getEnvironmentFileName() {
		return null;
	}

	/**
	 * 解析参数
	 * -p number 用来指定端口
	 * -n string 用来指定context path
	 * -c string 指定web root目录
	 * -j string 指定工程目录（通过找寻WEB-INF目录来定位web目录）
	 * -i 当前工程即为工程目录，即运行当前工程
	 * -m 指定maven模式来加载类。需要解析pom.xml文件
	 * @param args
	 * @throws Exception
	 */
	protected void initApplication(String... args) throws Exception {
		LogUtil.show("Welcome to Jef Jetty Conosle.");
		LogUtil.show("Command start [port]. to staring servers");
		LogUtil.show("Command stop  [port]. to stop severs");
		LogUtil.show("Command restart[port]. to restart servers");
		LogUtil.show("Command web. to view started servers.");
		LogUtil.show("Command q.   to exit Jetty Console.");
		if (args.length > 0) {
			int port = 80;
			String context = null;
			String contextName = null;
			boolean isMaven=false;
			for (int i = 0; i < args.length; i++) {
				String a = args[i];
				if ("-p".equals(a)) {						
					port = StringUtils.toInt(args[i + 1], 80);
					i++;
				} else if ("-n".equals(a)) {
					contextName = args[++i];
				} else if ("-c".equals(a)) {// context
					context = args[++i];
				} else if ("-j".equals(a)) {
					File root = new File(args[++i]);
					if(!root.exists()){
						System.out.println("Directory not exist:"+ root.getAbsolutePath());
					}
					File f=findWebinf(root);
					Assert.notNull(f, "Can not find the root driectory of web application.");
					context = f.getParent();
				} else if ("-i".equals(a)) {
					File f = new File(ClassLoaderUtil.getCodeSource(JettyConsole.class).getPath());
					while (f != null && !"WEB-INF".equals(f.getName())) {
						f = f.getParentFile();
					}
					if(f==null){
						f=findWebinf(new File(System.getProperty("user.dir")));
					}
					Assert.notNull(f, "Can not find the root driectory of web application.");
					context = f.getParent();
				} else if ("-m".equals(a)) {
					isMaven=true;
				} else {
					context = a;
				}
			}
			if (port > 0 && context != null) {
				this.startWebServer(port, new File(context), contextName,isMaven);
			}
		} else {// 从注册表读取上次的Web服务器配置
			Preferences p = Preferences.userRoot().node("JettyServer");
			for (String s : p.keys()) {
				String value = p.get(s, null);
				if (value != null) {
					String[] params = StringUtils.split(value, "*");
					this.startWebServer(Integer.valueOf(s), new File(params[0]), params[1],false);
				}
			}
		}
	}

	
	protected void closeApplication() throws Exception {
		for (WebServer ws : wsMap.values()) {
			ws.stop();
			ws = null;
		}
		wsMap.clear();
	}
	/*
	 * 低层查找优先的算法。IOUtils.findFile是深度优先算法。
	 */
	private File findWebinf(File root){
		List<File> dirs=new ArrayList<File>();
		for(File f:root.listFiles()){
			if(f.isDirectory()){
				if("WEB-INF".equals(f.getName())){
					return f;
				}else{
					dirs.add(f);
				}
			}
		}
		for(File f:dirs){
			File folder=findWebinf(f);
			if(folder!=null)return folder;
		}
		return null;
	}

	// 选择服务器会话
	class ChooseSessionConversation extends ConsoleConversation<jef.http.server.WebServer> {
		int port;
		public ChooseSessionConversation(int port) {
			super(JettyConsole.this);
			this.port=port;
		}

		protected WebServer executeCall() {
			if (wsMap.isEmpty())
				return null;
			status();
			if (wsMap.size() == 1) {
				return wsMap.values().iterator().next();
			} else {
				if(port<1 || port>65535){
					port = super.getInputInt("which port do you want to operate?");
				}
				WebServer ws = wsMap.get(port);
				return ws;
			}
		}
	}

	// 启动信息会话
	class AskStartConversation extends ConsoleConversation<Object[]> {
		private File path;
		private String context;

		public AskStartConversation(File path, String context) {
			super(JettyConsole.this);
			this.path = path;
			this.context = context;
		}

		@Override
		protected Object[] executeCall() {
			if (path == null) {
				if (lastClosed != null) {
					path = new File(super.getInputWithDefaultValue("WebRoot is?", lastClosed.getKey()));
				} else {
					path = new File(super.getInput("WebRoot is?"));
				}
				if (!path.exists()) {
					System.out.println(path.getAbsolutePath() + " does not exist");
					return new Object[] { null, context };
				}
			}
			if (path.isDirectory() && context == null) {
				if (lastClosed != null) {
					context = super.getInputWithDefaultValue("Context path is?", lastClosed.getValue());
				} else {
					context = super.getInput("Context path is?");
				}
			}
			if(context!=null && !context.startsWith("/")){
				context="/"+context;
			}
			return new Object[] { path, context };
		}
	};

	protected ShellResult performMyCommand(String str, String... addargs) {
		if (str.equals("web") || str.equals("server")) {// 显示服务器状态
			status();
			return ShellResult.CONTINUE;
		} else if (str.equals("r") || str.startsWith("r ")) {// 重启指定服务器
			String arg = StringUtils.substringAfter(str, "r ").trim();
			doRestart(arg);
			return ShellResult.CONTINUE;
		} else if (str.equals("restart") || str.startsWith("restart ")) {// 重启指定服务器
			String arg = StringUtils.substringAfter(str, "restart ").trim();
			doRestart(arg);
			return ShellResult.CONTINUE;
		} else if (str.equals("start") || str.startsWith("start ")) {
			String arg = StringUtils.substringAfter(str, "start ").trim();
			final int port = StringUtils.toInt(arg, 80);
			if (port > 0) {
				AskStartConversation c = new AskStartConversation(null, null);
				c.setCallback(new Callback<Object[], Exception>() {
					public void call(Object[] object) throws Exception {
						if (object[0] == null)
							return;
						JettyConsole.this.startWebServer(port, (File) object[0], (String) object[1],false);
					}
				});
				c.start();
			} else {
				LogUtil.show("Port number must be >0 and <65535");
			}
			return ShellResult.CONTINUE;
		} else if (str.equals("stop")||str.startsWith("stop ")) { // 停止服务器
			String arg = StringUtils.substringAfter(str, "stop ").trim();
			int port = StringUtils.toInt(arg, 0);
			ChooseSessionConversation c = new ChooseSessionConversation(port);
			c.setCallback(new Callback<WebServer, Exception>() {
				public void call(WebServer object) throws Exception {
					if (object == null)
						return;
					Integer port = object.getPort();
					lastClosed = new Entry<String, String>(object.getDefaultRoot(), object.getDefaultContext());
					wsMap.remove(port).stop();
					Preferences.userRoot().node("JettyServer").remove(String.valueOf(port));
					LogUtil.show("Web Server at port " + port + " stopped.");
				}
			});
			c.start();
			return ShellResult.CONTINUE;
		} else {
			LogUtil.show("Invalid Command.");
			return ShellResult.CONTINUE;
		}

	}

	private void doRestart(String arg) {
		int port = StringUtils.toInt(arg, 0);
		ChooseSessionConversation c = new ChooseSessionConversation(port);
		c.setCallback(new Callback<WebServer, Exception>() {
			public void call(WebServer object) throws Exception {
				if (object == null)
					return;
				Integer port = object.getPort();
				String context = object.getDefaultContext();
				String root = object.getDefaultRoot();
				wsMap.remove(port).stop();
				LogUtil.show("Web Server at port " + port + " stopped.");
				startWebServer(port, new File(root), context,false);
			}
		});
		c.start();
	}

	private void status() {
		for (WebServer ws : wsMap.values()) {
			LogUtil.show((ws == null) ? "Web server not started" : ws.toString());
		}
	}

	public String getPrompt() {
		return ">";
	}

	private void startWebServer(int port, File file, String contextName,boolean isMaven) {
		if (port < 1)
			port = 80;
		WebServer ws = wsMap.get(port);
		if (ws != null) {
			JettyServer server=(JettyServer)ws;
			LogUtil.show("Web Server is already running. will be restarted..");
			try {
				if(server.isStarted()){
					stopServer(server);
				}
				server.appendAppContext(contextName, file.getAbsolutePath(), null);
				LogUtil.show("The new context ["+contextName+"] is appended. restarting serever...");
				server.doStart();
			} catch (IOException e) {
				LogUtil.exception(e);
			}
			return;
		}
		if (file == null || !file.exists()) {
			LogUtil.error("file not exist:" + file.getAbsolutePath());
			return;
		}
		if (file.isDirectory()) {
			File f = new File(file, "WEB-INF");
			if (f.exists() && f.isDirectory()) {
				f = new File(f, "web.xml");
				if (f.exists() && f.isFile()) {
					if (contextName == null) {
						contextName = file.getParentFile().getName();
					}
					ws = new JettyServer(port, file, contextName);
				}
			}
		}
		if (ws == null)
			ws = new JettyServer(port, file, contextName);
		((JettyServer)ws).setMavenStart(isMaven);
		try{
			ws.start();	
			wsMap.put(port, ws);
		}catch(Throwable e){
			LogUtil.exception("attemt to start server for ["+file.getAbsolutePath()+"] failure!" ,e);
		}
		Preferences.userRoot().node("JettyServer").put(String.valueOf(port), file.getAbsolutePath() + "*" + contextName);
	}

	private void stopServer(JettyServer server) {
		server.doStop();
		int n=0;
		while(!server.isStopped()){
			n++;
			ThreadUtils.doSleep(500);
			if(n>20){
				throw new RuntimeException("Can not stop server!");
			}
		}
	}
	public void exit() {
		throw new UnsupportedOperationException();
	}
}
