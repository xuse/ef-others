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
package jef.net.socket;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jef.common.log.FileLogger;
import jef.common.log.LogUtil;
import jef.tools.DateUtils;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.JefConfiguration;
import jef.tools.ThreadUtils;

/**
 * 使用NIO编写的Socket服务器
 * @author jiyi
 */

public class SocketServer {
	public static final int GOBAL_SESSION_EXPIRES = 600000;
	private Selector selector;
	private int port;
	private boolean shutdown = false;
	public boolean debug = false;
	private FileLogger fg;
	private SocketAction handler;
	private boolean enableSession;

	public boolean isEnableSession() {
		return enableSession;
	}

	public void setEnableSession(boolean enableSession) {
		this.enableSession = enableSession;
	}

	protected boolean isShutdown() {
		return shutdown;
	}

	protected void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

	// 当前控制中的所有会话
	Map<String, SocketSession> sessions = new ConcurrentHashMap<String, SocketSession>();

	// 用于清理过期Session的线程
	class ExpireSessionCleanner extends Thread {
		
		public void run() {
			while (true) {
				for (Iterator<SocketSession> iter = sessions.values().iterator(); iter.hasNext();) {
					SocketSession session = iter.next();
					if (System.currentTimeMillis() > session.getExpireTime()) {
						log("Removing expired Session:" + session.getUser() + "[" + DateUtils.format(session.getStartTime()) + "]-[" + DateUtils.format(session.getExpireTime()) + "]");
						iter.remove();// 移除过期的Session
					}
				}
				ThreadUtils.doSleep(5000);//每五秒运行一次
			}
		}
	}

	/**
	 * 构造方法
	 * 
	 * @param port
	 * @throws IOException
	 */
	public SocketServer(int port) throws IOException {
		ServerSocketChannel server = ServerSocketChannel.open();
		ServerSocket ss=server.socket();
		ss.setPerformancePreferences(1, 2, 0);
		Selector sel = Selector.open();
		server.socket().bind(new InetSocketAddress(port));
		server.configureBlocking(false);
		server.register(sel, SelectionKey.OP_ACCEPT);
		selector = sel;
		this.port = port;
		this.fg = new FileLogger(new File(JefConfiguration.get(JefConfiguration.Item.LOG_PATH) + "/socket" + DateUtils.formatDate(new Date()) + ".log"));
		fg.setLogDate(true);
	}

	public void log(String msg) {
		fg.log(msg);
	}

	/**
	 * 开始服务
	 */
	public void start() {
		Thread server = new Thread() {
			ExecutorService executor;
			
			public void run() {
				executor = Executors.newCachedThreadPool();// 多线程处理器
				log("Socket Servered listen on prot " + port);
				// 启动守护线程
				if(enableSession){
					Thread cleanner = new ExpireSessionCleanner();
					cleanner.setDaemon(true);
					cleanner.start();	
				}
				while (!shutdown) {
					try {
						selector.select();
						Set<SelectionKey> set=selector.selectedKeys();
						//int size=set.size();
						Iterator<SelectionKey> iter = set.iterator();
						while (iter.hasNext()) {
							SelectionKey key = iter.next();
							iter.remove();
							handleKey(key);
						}
						//System.out.println("debug: now let's do next select.("+size+")");
					} catch (IOException e) {
						Exceptions.log(e);
					}
				}
				// 提示线程池关闭
				executor.shutdown();
				// 清理Session，但被线程所持有的session依然存在
				sessions.clear();
				for (SelectionKey key : selector.keys()) {// 关闭所有IO处理中的通道
					IOUtils.closeQuietly(key.channel());
				}
				try {
					selector.close();
				} catch (IOException e) {
					Exceptions.log(e);
				}
			}

			// 处理事件
			protected void handleKey(SelectionKey key) {
				try {
					if (key.isAcceptable()) { // 处理接入请求
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel channel = server.accept();
						channel.configureBlocking(false);
						SelectionKey wkey = channel.register(selector, SelectionKey.OP_READ);
						SocketExchange exchange = new SocketExchange(wkey);
						wkey.attach(exchange);
						if (debug){
							fg.log(exchange.getUser() + " is accepted!");
						}
					} else if (key.isReadable()) { // 接收信息
						SocketExchange exchange = (SocketExchange) key.attachment();
						exchange.processRead(); // 需要添加异常处理
						if (exchange.getStatus() == SocketExchange.PROCESSING) {
							Runnable action = prepareRunner(exchange);
							if (action != null) {
								executor.execute(action);
							} else {
								exchange.setStatus(SocketExchange.FINISH);
							}
						}
					} else if (key.isWritable()) { // 发送信息
						SocketExchange exchange = (SocketExchange) key.attachment();
						if (exchange.getStatus() == SocketExchange.FINISH) {
							exchange.close();
							if (debug){
								fg.log(exchange.getUser() + " is closed!");
							}
						}else{
							exchange.processWrite();	
						}
					}else{
						System.out.println("a key of "+key+" ("+key.interestOps()+") is skipped.");
					}
				} catch (Exception e) {//如果一个socket出现异常则丢弃这个连接。
					SocketExchange att=(SocketExchange)key.attachment();
					fg.log((att==null)?"":att.getUser()+e.getMessage());
					Exceptions.log(e);
					if(att!=null)att.close();
					key.cancel();
				}
			}

			private Runnable prepareRunner(final SocketExchange exchange) {
				if(enableSession){
					SocketSession session = sessions.get(exchange.getUser());
					if (session == null) {
						session = new SocketSession();
						session.setUser(exchange.getUser());
						session.setStartTime(System.currentTimeMillis());
						sessions.put(session.getUser(), session);
					}
					// 设置Session的下次过期时间
					session.setExpireTime(System.currentTimeMillis() + GOBAL_SESSION_EXPIRES);	
				}
				return new Runnable() {
					public void run() {
						getHandler().doAction(exchange);
					}
				};
			}
		};
		// 启动服务线程
		server.start();
	}

	public SocketAction getHandler() {
		if (handler == null) {
			handler = new DefaultSocketAction();
			handler.setServer(this);
		}
		return handler;
	}

	public void setHandler(SocketAction handler) {
		this.handler = handler;
		handler.setServer(this);
	}

	public void stop() {
		if (shutdown)
			return;
		this.shutdown = true;
		try {
			SocketChannel socket = SocketChannel.open();
			socket.connect(new InetSocketAddress("localhost", port));
			socket.close();
		} catch (IOException e) {
			LogUtil.show("Can't connect to localhost:"+port);
			Exceptions.log(e);
		}
	}
	
	public static String getUser(Socket socket){
		return socket.getInetAddress().getHostAddress()+":"+socket.getPort();
	}
}
