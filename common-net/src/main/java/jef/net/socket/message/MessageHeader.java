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

import org.apache.commons.lang.ArrayUtils;

import jef.tools.ByteUtils;

public class MessageHeader {
	private byte messageType;
	private byte charset;
	private long contentLength;
	
	public byte getCharset() {
		return charset;
	}
	public void setCharset(byte charset) {
		this.charset = charset;
	}
	public byte getMessageType() {
		return messageType;
	}
	public void setMessageType(byte messageType) {
		this.messageType = messageType;
	}
	public long getContentLength() {
		return contentLength;
	}
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}
	//写入到指定的缓冲区
	public void putBytes(byte[] index, int offset) {
		index[offset]=messageType;
		index[offset+1]=charset;
		ByteUtils.putLong(index, contentLength, offset+2);
	}
	/**
	 * 从缓冲区读取生成一个MessageHeader。如果长度小于10则会异常
	 * @param buf
	 * @param offset
	 * @return
	 */
	public static MessageHeader load(byte[] buf,int offset){
		MessageHeader i=new MessageHeader();
		i.messageType=buf[offset];
		i.charset=buf[offset+1];
		i.contentLength=ByteUtils.getLong(buf, offset+2);
		return i;
	}
	
	public static final byte[] MULTI_PART_TYPES=new byte[]{
		Message.FILE_MESSAGE,
		Message.MULTI_PART_MESSAGE,
		
		
	};
	public static final byte[] SINGLE_PART_TYPES=new byte[]{
		Message.STRING_MESSAGE,
		Message.STREAM_MESSAGE,
		Message.PART_FILENAME,
		Message.SERIALIZABLE_OBJECT_MESSAGE
	};
	
	public boolean isMultiPart() {
		return ArrayUtils.contains(MULTI_PART_TYPES, messageType);
	}
	public boolean isSinglePart() {
		return ArrayUtils.contains(SINGLE_PART_TYPES, messageType);
	}
	public boolean isZipedStream() {
		return messageType==Message.STREAM_MESSAGE && charset==Message.FORMAT_ZIPPED;
	}
	public boolean isString(){
		return messageType==Message.STRING_MESSAGE || messageType==Message.PART_FILENAME;
	}
}
