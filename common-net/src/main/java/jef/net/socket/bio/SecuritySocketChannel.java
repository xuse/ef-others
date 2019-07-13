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
package jef.net.socket.bio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.crypto.SecretKey;

import jef.tools.ArrayUtils;
import jef.tools.IOUtils;
import jef.tools.ThreadUtils;
import jef.tools.security.EncrypterUtil;
import jef.tools.security.EncrypterUtil.Transport;
import jef.tools.security.IChannel;
import jef.tools.security.KeyConsult;


/**
 * 使用DH算法，每次实例化协商密钥构成的安全通道
 * 
 * 
 * 
 * 
 * 
 * 消息规范
 * 00 01 nn nn 内容
 * 00 02 nn nn nn nn 内容
 * 消息类型 长度word 
 * 
 * @author Jiyi
 */
public class SecuritySocketChannel implements IChannel{
//	private static int timeout=10000;
	
	private String defaultAlgorithm = "DES";		//采用DES PADDING算法，明文和密文长度相等
	private Socket sc;
	private ObjectInputStream rawIn;
	private ObjectOutputStream rawOut;
	
	
	private SecretKey key;

	public Socket getSocket() {
		return sc;
	}


	public SecuritySocketChannel(Socket socket, OutputStream out,InputStream in) {
		this.sc=socket;
		try{
			this.rawOut=new ObjectOutputStream(out==null?sc.getOutputStream():out);
			this.rawIn=new ObjectInputStream(in==null?sc.getInputStream():in);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public SecuritySocketChannel(Socket socket) {
		this.sc=socket;
		try{
			this.rawOut=new ObjectOutputStream(sc.getOutputStream());
			this.rawIn=new ObjectInputStream(sc.getInputStream());
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}


	/**
	 * 通过接收接下来的通信完成密钥协商。 
	 * 该方法会阻塞，直到协商完成，生成加密信道对象。
	 * 或者抛出异常
	 * @param sc
	 * @return
	 * @throws IOException 
	 */
	public static SecuritySocketChannel receiveConsult(Socket sc) throws IOException{
		final SecuritySocketChannel channel=new SecuritySocketChannel(sc);
		
		byte bf[]=new byte[1024];
		int byteReceived=channel.rawIn.read(bf);
		KeyConsult c=new KeyConsult(ArrayUtils.subarray(bf, 0, byteReceived));
		c.send(new Transport(){
			public void send(byte[] encoded) throws IOException{
				channel.rawOut.write(encoded);
			}
		});

		channel.key=c.generateKey(channel.defaultAlgorithm);
		return channel;
	}
	
	/**
	 * 发起和对方通信协商密钥。
	 * 阻塞方法，在协商完毕前该方法不会返回。
	 * 如果协商成功，返回。如果过程中有任何失败，会抛出IOException异常。
	 * @throws IOException 
	 */
	public void doConsult() throws IOException {
		KeyConsult c=new KeyConsult();
		c.send(new Transport(){
			public void send(byte[] encoded) throws IOException {
				rawOut.write(encoded);
			}
		});
//		System.out.println("sending con..");
		byte[] bf=new byte[1024];
		int byteReceived=rawIn.read(bf);
		c.receive(bf,0,byteReceived);
		key=c.generateKey(this.defaultAlgorithm);
		ThreadUtils.doSleep(500);
	}

	/**
	 * 读取消息，解密
	 * @return
	 * @throws IOException 
	 */
	public byte[] read() throws IOException {
		int flag=rawIn.read();
		if(flag!=0){
			throw new IllegalStateException();
		}
		int word=rawIn.read();
		int len;
		if(word==1){
			len=rawIn.readUnsignedShort();	//读2字节
		}else if(word==2){
			len=rawIn.readInt();//读4字节
		}else{
			throw new IllegalStateException("size invalid!"); 
		}
		byte[] message=IOUtils.toByteArray(rawIn, len);
		return EncrypterUtil.decrypt(new ByteArrayInputStream(message), key);
	}
	
	/**
	 * 发送消息，加密
	 * @param encode
	 * @throws IOException
	 */
	public void write(byte[] source) throws IOException {
		byte[] encode=EncrypterUtil.encrypt(source, key);
		rawOut.writeByte(0);
		if(encode.length<65536){
			rawOut.writeByte(1);
			rawOut.writeShort(encode.length);
		}else{
			rawOut.writeByte(2);
			rawOut.writeInt(encode.length);
		}
		rawOut.write(encode);
	}
	
	public void close() throws IOException {
		sc.close();
	}
}
