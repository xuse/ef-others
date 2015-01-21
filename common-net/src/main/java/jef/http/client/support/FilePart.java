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
package jef.http.client.support;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jef.tools.IOUtils;

/**
 * 描述一个mime的文件数据，在http客户端或者邮件客户端中使用
 * 
 * 一个文件（附件）部分，具有文件长度，文件名等属性
 * 
 * @author Administrator
 *
 */
public class FilePart implements Part {
	private static final long serialVersionUID = -6704088622136713404L;
	private String name;
	private String filename;
	private InputStream in;
	private long length;

	public String getFilename() {
		return filename;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public InputStream getInputStream() {
		return in;
	}

	public String getName() {
		return name;
	}

	public FilePart(String fieldName, File file) throws IOException {
		this.name = fieldName;
		this.in = new BufferedInputStream(new FileInputStream(file));
		this.filename = file.getName();
		this.length = file.length();
	}

	public FilePart(String fieldName, InputStream in, String fileName, long length) {
		this.name = fieldName;
		this.filename = fileName;
		this.in = in;
		this.length=length;
	}

	public void copyTo(File file) throws IOException {
		IOUtils.copy(in, new FileOutputStream(file), true);
		
	}
}
