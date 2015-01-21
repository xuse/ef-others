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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import jef.tools.IOUtils;

/**
 * 压缩格式的数据流文件
 * @author Administrator
 */
class ZipedStreamMessage extends StreamMessage{
	File ziped;
	
	public ZipedStreamMessage(InputStream in){
		super(null,-1);
		try{
			ziped=File.createTempFile("~gz", ".gz");
			GZIPOutputStream out=new GZIPOutputStream(new FileOutputStream(ziped));
			IOUtils.copy(in, out, true);
			in.close();
			this.length=ziped.length();
			this.stream=new FileInputStream(ziped);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	
	public byte getMessageCharSet() {//nothing
		return FORMAT_ZIPPED;//1表示是ZIPED
	}

	
	public void close() {
		if(ziped.exists()){
			ziped.delete();
			//System.out.println("删除临时压缩文件:"+ ziped.getAbsolutePath());
		}
	}
	
}
