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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.List;

import jef.tools.IOUtils;
import jef.tools.StringUtils;

public class MultipartPostMethod extends PostMethod {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5966724455548330720L;
	public static final int HEADER_PART_SIZE_MAX = 10240;
	protected static final int DEFAULT_BUFSIZE = 4096;
	
	public static final String boundary = "------------------7d71b526e00e4";
	private static final String FILE_PART_HEADER = "--------------------7d71b526e00e4\r\n" + "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n"
			+ "Content-Type: application/octet-stream\r\n\r\n";	// 每个文件部分的开头
	private static final String NORMAL_PART_HEADER = "--------------------7d71b526e00e4\r\n" + "Content-Disposition: form-data; name=\"%s\"\r\n\r\n";// 每个普通数据段开头
	private static final String REQUEST_TAIL = "--------------------7d71b526e00e4--\r\n";// 结尾

	
	public void doMethod(URLConnection conn) throws IOException {
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		// conn.setRequestProperty("Content-Length",String.valueOf(strHeader.length() + partLength + strTail.length()));
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		sendMessage(parts,out);
	}
	
	static void sendMessage(List<Part> parts,OutputStream out) throws IOException{
		for (Part part : parts) {
			if (part instanceof FilePart) {
				FilePart fPart = (FilePart) part;
				out.write(String.format(FILE_PART_HEADER, fPart.getName(), fPart.getFilename()).getBytes());// 写头
				IOUtils.copy(fPart.getInputStream(), out, false);
			} else {
				NormalPart np = (NormalPart) part;
				out.write(String.format(NORMAL_PART_HEADER, np.getName()).getBytes());
				out.write(np.getContent().getBytes());
			}
			out.write(StringUtils.CRLF);
		}
		out.write(REQUEST_TAIL.getBytes());
		out.close();
	}
	
	
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("Content-Type: multipart/form-data; boundary=" + boundary);
		sb.append(StringUtils.CRLF_STR);
		
		for (Part part : parts) {
			if (part instanceof FilePart) {
				FilePart fPart = (FilePart) part;
				sb.append(String.format(FILE_PART_HEADER, fPart.getName(), fPart.getFilename()));
				sb.append("(RAW file content)");
				sb.append(StringUtils.CRLF_STR);
			} else {
				NormalPart np = (NormalPart) part;
				sb.append(String.format(NORMAL_PART_HEADER, np.getName()));
				sb.append(np.getContent());
			}
			sb.append(StringUtils.CRLF_STR);
		}
		sb.append(REQUEST_TAIL);
		return sb.toString();
	}
	
	
}
