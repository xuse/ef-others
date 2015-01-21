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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import jef.common.log.LogUtil;
import jef.tools.Assert;
import jef.tools.string.CharsetName;

public class StringMessage extends Message{
	private String charSet = "UTF-8"; //默认UTF8编码
	private byte[] message;

	
	public InputStream getContent() {
		return new ByteArrayInputStream(message);
	}
	
	
	public String toString() {
		return getMessage();
	}

	public StringMessage(String message){
		try {
			this.message=message.getBytes(charSet);
		} catch (UnsupportedEncodingException e) {
			LogUtil.exception(e);
		}
	}
	
	public StringMessage(String message,String charSet) throws UnsupportedEncodingException{
		this.charSet=charSet;
		this.message=message.getBytes(charSet);
	}

	public String getCharSet() {
		return charSet;
	}

	public StringMessage(byte[] message,String charSet){
		this.charSet=charSet;
		this.message=message;
	}
	
	public String getMessage()  {
		try {
			return new String(message,charSet);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	
	public byte getMessageCharSet() {
		Charset c=Charset.forName(charSet);
		Assert.notNull(c);
		return (byte)CharsetName.getOrder(c);
	}

	
	public byte getMessageType() {
		return STRING_MESSAGE;
	}

	
	public long getContentLength() {
		return message.length;
	}

	
	public void close() {
		message=null;
	}
	
}
