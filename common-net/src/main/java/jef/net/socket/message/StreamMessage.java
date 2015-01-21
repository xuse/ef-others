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

import java.io.File;
import java.io.InputStream;

import jef.tools.IOUtils;


public class StreamMessage extends Message{
	protected InputStream stream;
	protected long length;
	File tmpFile;

	
	public InputStream getContent() {
		return stream;
	}
	
	public StreamMessage(InputStream in,long length){
		this.stream=in;
		this.length=length;
	}
	
	
	public byte getMessageCharSet() {//nothing
		return 0;
	}

	
	public byte getMessageType() {
		return STREAM_MESSAGE;
	}

	
	public long getContentLength() {
		return length;
	}

	
	public void close() {
		IOUtils.closeQuietly(stream);
		if(tmpFile!=null && tmpFile.exists()){
			tmpFile.delete();
		}
	}
	
}
