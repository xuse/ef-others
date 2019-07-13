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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import jef.tools.ArrayUtils;
import jef.tools.IOUtils;

public class FileMessage extends MultiPartMessage{
	public byte getMessageType() {
		return FILE_MESSAGE;
	}

	public static final String[] KeepTypes=new String[]{"exe","com","jpg","png","zip","rar","r00","7z","gif","gz"};
	
	//从文件直接构造
	public FileMessage(File file)throws IOException{
		if(!file.exists()){
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		if(file.isDirectory()){
			throw new FileNotFoundException(file.getAbsolutePath()+" is not a file.");
		}
		parts.add(new FileNamePart(file.getName()));
		String ext=IOUtils.getExtName(file.getName());
		if(ArrayUtils.contains(KeepTypes, ext)){
			parts.add(new StreamMessage(new FileInputStream(file),file.length()));	
		}else{
			ZipedStreamMessage zipm=new ZipedStreamMessage(new FileInputStream(file));
			parts.add(zipm);
			//System.out.print("将要发送的文件压缩:"+ file.length()+"->"+zipm.getContentLength());
		}
	}

	//从消息构造
	public FileMessage(FileNamePart fileName,StreamMessage stream) {
		parts.add(fileName);
		parts.add(stream);
	}
	
	public String getFileName(){
		String fileName=parts.get(0).toString();
		return fileName;
	}
	
	public InputStream getFileContent() throws IOException{
		return parts.get(1).getContent();
	}
	
	public static class FileNamePart extends StringMessage{
		public FileNamePart(String message){
			super(message);
		}
		
		public FileNamePart(byte[] message, String charSet) {
			super(message, charSet);
		}

		public byte getMessageType() {
			return PART_FILENAME;
		}
	}
}
