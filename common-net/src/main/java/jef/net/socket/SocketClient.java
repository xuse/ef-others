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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import jef.net.socket.message.Message;
import jef.net.socket.message.MessageBuilder;
import jef.tools.Exceptions;
import jef.tools.IOUtils;

public class SocketClient {
	public static int LOCAL_PORT_DEFAULT = 0;

	private String remoteHost;
	private int remotePort;
	private int localPort = LOCAL_PORT_DEFAULT;
	Socket socket;

	public SocketClient(String host, int port) {
		this.remoteHost = host;
		this.remotePort = port;
	}

	public void setLocalPort(int local) {
		if (socket != null) {
			throw new UnsupportedOperationException("you must set localport before connected.");
		}
		this.localPort = local;
	}

	public static SocketClient connectTo(String host, int port, int localPort) {
		SocketClient client = new SocketClient(host, port);
		client.setLocalPort(localPort);
		client.connect();
		// System.out.println("Opening remote:" + host+":"+port);
		return client;
	}

	/**
	 * 请求消息
	 * 
	 * @param msg
	 * @return
	 * @throws IOException
	 */
	public Message request(Message message) throws IOException {
		InputStream in = message.getContent();
		OutputStream out = socket.getOutputStream();
		// System.out.println("DEBUG: sending"+message.getClass().getName()+" "+message.toString());
		if (message.getMessageType() != Message.RAW_MESSAGE) {
			out.write(message.getFlagAndHeader());
		}
		IOUtils.copy(in, out, false);
		out.flush();
		// out.close();//不能关闭，这将造成连接被关闭哦
		in.close();
		// socket.shutdownOutput();
		// 根据返回内容构造消息，注意传输超时问题
		Message response = MessageBuilder.build(new PushbackInputStream(socket.getInputStream(), 16));
		return response;

	}

	/**
	 * 异步请求发送消息
	 * 
	 * @param msg
	 *            发送的消息
	 * @param callback
	 *            消息返回后调用的对象
	 */
	public void submit(Message msg, SocketCallback callback) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 开启连接
	 */
	public void connect() {
		try {
			if (localPort > 0) {
				socket = new Socket(remoteHost, remotePort, InetAddress.getLocalHost(), localPort);
			} else {
				socket = new Socket(remoteHost, remotePort);
			}
		} catch (UnknownHostException e) {
			Exceptions.log(e);
		} catch (IOException e) {
			Exceptions.log(e);
		}
	}

	public boolean isConnected() {
		return (socket != null && socket.isConnected());
	}

	/**
	 * 关闭连接
	 */
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			Exceptions.log(e);
		} finally {
			socket = null;
		}
	}

	public interface SocketCallback {
		public void onResponse(Message response);
	}

	// public static void main(String...strings ) throws IOException{
	// test1();
	// }
	// public static void test2() throws IOException{
	// BigDataBuffer bb=BigDataBuffer.load(new File("c:/temp"));
	// Message response=MessageBuilder.build(new
	// PushbackInputStream(bb.getAsStream()));
	// System.out.println(response.getClass().getSimpleName());
	// System.out.println(response.toString());
	// if(response instanceof FileMessage){
	// FileMessage fm=(FileMessage)response;
	// IOUtils.saveAsFile(fm.getFileContent(), new File("c://rec" +
	// fm.getFileName()));
	// }
	//		
	// }
	// public static void test1() throws IOException{
	// SocketClient sc=SocketClient.connectTo("127.0.0.1", 7777);
	// //Message request=new FileMessage(new File("e:/document.rar"));
	// Message request=new SerializableObjectMessage(new DateSpan(new
	// Date(),DateUtils.adjustDate(new Date(), 0, 0, 3)));
	// //byte[] mmm="华东感发生法".getBytes("UTF-8");
	// //System.out.print(mmm.length);
	// //request=new StreamMessage(new ByteArrayInputStream(mmm),mmm.length);
	// Message msg=sc.request(request);
	// System.out.println("=============返回===============");
	// System.out.println(msg.getClass().getSimpleName());
	// System.out.println(msg.toString());
	// sc.close();
	// }
}
