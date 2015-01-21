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

import jef.tools.IOUtils;

public class RawMessage extends Message{
	private byte[] message;

	
	public InputStream getContent() {
		return new ByteArrayInputStream(message);
	}

	
	public long getContentLength() {
		return message.length;
	}

	
	public byte getMessageCharSet() {
		throw new UnsupportedOperationException();
	}

	
	public byte getMessageType() {
		return RAW_MESSAGE;
	}

	public RawMessage(String str){
		message=str.getBytes();
	}
	
	/**
	 * 这个构造方法会等待全部数据载入内存
	 * @param in
	 * @throws IOException
	 */
	public RawMessage(InputStream in) throws IOException{
		message=IOUtils.toByteArray(in);
		in.close();
	}
	
	public RawMessage(byte[] message){
		this.message=message;
	}

	
	public String toString() {
			String msg = new String(message);
			return msg;
	}
}
