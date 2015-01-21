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
package jef.net.socket.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.apache.commons.lang.ArrayUtils;

public abstract class Message {
	//一个消息的构成
	//MsgFlag
	//MsgHeader
	//MsgContent 
	//    MsgPart1 (MsgIndexPart)
	//    MsgPart2
	//MsgIndexPart 是一个特殊的Part，长度为10的倍数，记录了所有Part的MsgHeader
	public static final byte[] JEF_MESSAGE_FLAG={0x00,0x0D,0x0A,0x0D,0x0A,0x00};
	public static final int LENGTH_MSG_FLAG=6;
	public static final int LENGTH_MSG_HEADER=10; //头长度10位 byte+byte+long

	
	public static final byte RAW_MESSAGE=0x7F;//简单文本
	public static final byte STRING_MESSAGE=0x01;//简单文本
	public static final byte STREAM_MESSAGE=0x02;//数据流
	public static final byte PART_FILENAME=0x03;//文本，表示一个文件名

	public static final byte MULTI_PART_MESSAGE=0x10; //由多个部分构成的数据流
	public static final byte FILE_MESSAGE=0x11;//由简单文本+数据流两个部分构成
	public static final byte SERIALIZABLE_OBJECT_MESSAGE=0x12;//由类名+数据流两个部分构成
	
	public static final byte FORMAT_ZIPPED=0x21;//数据流
	/**
	 * 得到一个字节，标记消息的类型
	 * @return
	 */
	public abstract byte getMessageType(); 
	
	/**
	 * 得到一个字节，标记消息的编码
	 * @return
	 */
	public abstract byte getMessageCharSet();
	
	/**
	 * 得到一个long，(8个字节),
	 * 标记消息内容的长度(不含消息标志和消息头)
	 * 对于MultiPart，是索引区的长度而不是整个消息的长度(不含消息标志和消息头)
	 * @return
	 */
	public abstract long getContentLength();
	/**
	 * 得到消息内容数据流，不含10 byte的MessageHeader
	 * @return
	 * @throws IOException
	 */
	public abstract InputStream getContent()throws IOException; 
	
	public MessageHeader getHeader(){
		MessageHeader mh=new MessageHeader();
		mh.setMessageType(this.getMessageType());
		mh.setCharset(this.getMessageCharSet());
		mh.setContentLength(this.getContentLength());
		return mh;
	}
	
	public static boolean isJefMessage(byte[] buffer) {
		if(buffer.length>6){
			buffer=ArrayUtils.subarray(buffer, 0, 6);
		}
		return ArrayUtils.isEquals(buffer, Message.JEF_MESSAGE_FLAG);
	}
	
	/**
	 * 尝试从输入流中读取消息头，如果消息头可解析则返回消息头
	 * 如果消息头不可解析，则将输入流回归到开始状态，并返回null
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static MessageHeader getMessageHeader(PushbackInputStream in) throws IOException {
		byte[] msgBuf=new byte[10];
		
		int len=in.read(msgBuf,0,Message.LENGTH_MSG_FLAG);
		if(len<6 || !isJefMessage(msgBuf)){
			in.unread(msgBuf,0,len);
			return null;
		}
		
		len=in.read(msgBuf);
		if(len<Message.LENGTH_MSG_HEADER){
			in.unread(JEF_MESSAGE_FLAG);
			in.unread(msgBuf,0,len);
			return null;
		}
		MessageHeader mh=MessageHeader.load(msgBuf, 0);
		return mh;
	}

	public byte[] getFlagAndHeader() {
		byte[] headBuf=new byte[Message.LENGTH_MSG_FLAG+Message.LENGTH_MSG_HEADER];
		System.arraycopy(Message.JEF_MESSAGE_FLAG, 0, headBuf, 0, Message.LENGTH_MSG_FLAG);
		getHeader().putBytes(headBuf, Message.LENGTH_MSG_FLAG);
		return headBuf;
	}

	public void close() {
	}
}
