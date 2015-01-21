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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jef.net.socket.message.FileMessage.FileNamePart;

public class MultiPartMessage extends Message {
	protected List<Message> parts=new ArrayList<Message>();

	
	public InputStream getContent() throws IOException {
		List<InputStream> ss=new ArrayList<InputStream>();
		ss.add(getIndex());//消息索引区
		for(Message m:parts){
			ss.add(m.getContent());//各个部分
		}
		SequenceInputStream in=new SequenceInputStream(Collections.enumeration(ss));
		return in;
	}

	
	public byte getMessageCharSet() {
		return 0;
	}

	
	public byte getMessageType() {
		return MULTI_PART_MESSAGE;
	}

	/**
	 * 获取整体的消息头数据流
	 * @param index
	 * @throws IOException
	 */
	private ByteArrayInputStream getIndex() throws IOException {
		short indexLength=(short)(parts.size()*10);
		byte[] index=new byte[indexLength];
		int offset=0;
		for(Message msg:parts){
			if(msg instanceof StringMessage ||msg instanceof StreamMessage){
				MessageHeader item=new MessageHeader();
				item.setMessageType(msg.getMessageType());
				item.setCharset(msg.getMessageCharSet());
				item.setContentLength(msg.getContentLength());
				item.putBytes(index,offset);
				offset+=10;
			}else{
				throw new IOException("Error Message part");
			}
		}
		return new ByteArrayInputStream(index);
	}
	

	public void addParts(List<Message> parts2) {
		for(Message part: parts2){
			addPart(part);
		}
	}
	
	public void addPart(Message part){
		if(part.getMessageType()==FILE_MESSAGE){
			FileMessage fm=(FileMessage)part;
			parts.addAll(fm.parts);
		}else if(part.getMessageType()==STREAM_MESSAGE || part.getMessageType()==STRING_MESSAGE){
			parts.add(part);
		}else if(part.getMessageType()==PART_FILENAME || part.getMessageType()==SERIALIZABLE_OBJECT_MESSAGE){
			parts.add(part);
		}else{
			throw new IllegalArgumentException("the part is a "+part.getClass().getName()+ part.getMessageType());
		}
	}

	/**
	 * 對於多部分消息，其长度不是消息本身的长度，而是内部所有消息头的长度。
	 * 多部消息会先将所有的消息头返回，然后需要根据消息头猪哥向后读取消息
	 */
	public long getContentLength() {
		return parts.size()*10;
	}
	public List<Message> getParts() {
		return parts;
	}

	public Message getPart(int index) {
		Message part=parts.get(index);
		if(part instanceof FileNamePart){
			part=new FileMessage((FileNamePart)part,(StreamMessage)parts.get(index+1));
		}
		return part;
	}

	
	public void close() {
		for(Message part:parts){
			part.close();
		}
	}
	
	
}
