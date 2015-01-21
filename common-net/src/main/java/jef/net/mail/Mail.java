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
package jef.net.mail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jef.common.log.LogUtil;
import jef.http.client.support.FilePart;
import jef.http.client.support.HtmlPart;
import jef.http.client.support.NormalPart;
import jef.http.client.support.Part;
import jef.http.server.MultipartStream;
import jef.jre5support.Headers;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.io.CountInputStream;
import jef.tools.string.StringParser;
import jef.tools.support.JefBase64;

/**
 * 描述一个MIME邮件对象
 * @author Administrator
 *
 */
public class Mail {
	private MailSummary summary;
	private Part[] parts;
	
	/**
	 * 获得邮件摘要信息
	 * @return
	 */
	public MailSummary getSummary() {
		return summary;
	}

	/**
	 * 设置邮件摘要信息
	 * 
	 * @param header
	 * @see MailSummary
	 */
	public void setSummary(MailSummary header) {
		this.summary = header;
	}

	/**
	 * 返回邮件的各个Part
	 * 返回的Part包括三种类型
	 * <li>{@link jef.http.client.support.NormalPart} :纯文字的Part </li>
	 * <li>{@link jef.http.client.support.HtmlPart}:HTML格式的Part</li>
	 * <li>{@link jef.http.client.support.FilePart}:文件附件</li>

	 * @return Part[]    邮件中的所有Part
	 */
	public Part[] getParts() {
		return parts;
	}
	
	public static String readLine(InputStream in) throws IOException{
		StringBuilder sb=new StringBuilder();
		int c;
		int match=0;
		while((c=in.read())>-1){
			if(c==MultipartStream.FIELD_SEPARATOR[match]){
				match++;
				if(match==2){
					break;
				}else{
					continue;	
				}
			}else{
				sb.append((char)c);
				match=0;
			}
		}
		return sb.toString();
	}

	/**
	 * 从eml格式的文件中读取。即通过解析文件来生成对象
	 * @param 输入文件
	 * @return Mail    返回类型
	 * @throws
	 */
	public static Mail loadMail(File in) throws IOException{
		return loadMail(new FileInputStream(in));
	}
	
	/**
	 * 从eml格式的流中读取。即通过解析流来生成对象
	 * @param 输入流
	 * @return Mail    返回类型
	 * @throws
	 */
	public static Mail loadMail(InputStream ins) throws IOException{
		CountInputStream in=new CountInputStream(ins);
		//read headers
		String str=readLine(in);
		if(str.startsWith("+OK"))str=readLine(in);
		String key = null;
		Headers map = new Headers();
		while(str!=null && str.length()>0){
			if (str.equals(".")) {
				break;
			} else if (str.startsWith(" ") || str.startsWith("\t")) {// || str.indexOf(":") == -1
				if (key == null)
					continue;
				String lastValue = map.getFirst(key);
				lastValue = lastValue.concat(str.substring(1));
				map.put(key, new String[]{lastValue});
			} else {
				key = StringUtils.substringBefore(str, ":").toLowerCase();
				String value = StringUtils.substringAfter(str, ":").trim();
				map.add(key, value);
			}
			str=readLine(in);
		}
		//process contents
		Mail m=createMail(map, in);
		in.close();
		MailSummary s = new MailSummary();
		s.setHeaders(map);
		m.setSummary(s);
		s.setId(new MailId());
		s.getId().setSize((int) in.getSize());
		return m;
	}
	
	private Mail(){}
	
	//FIXME: 其实邮件一定是文本编码的，这里不应该用stream,而是用reader，这样后续得到的char[]就可以用fastDecoder来解码了
	public static Mail createMail(Headers headers, InputStream stream) throws IOException {
		String contType = headers.getFirst("Content-Type");
		String encoding = headers.getFirst("Content-Transfer-Encoding");
		String charset = StringUtils.substringAfter(contType, "charset=");
		if (charset != null) {
			if (charset.startsWith("\""))
				charset = StringUtils.substringBetween(charset, "\"");
		}
		if (contType == null) {
			contType = "text/plain";
		}
		Mail mail = new Mail();
		if (contType.toLowerCase().startsWith("multipart/")) {
			String boud = StringUtils.substringAfter(contType, "boundary=");
			if (boud.startsWith("\""))
				boud = StringUtils.substringBetween(boud, "\"");
			MultipartStream ms = new MultipartStream(stream, boud.getBytes(), 4096);
			List<Part> ps=new ArrayList<Part>();
			processMultiParts(ms, ps);
			mail.parts=ps.toArray(new Part[ps.size()]);
		} else {
			String content=decode(new String(IOUtils.toByteArray(stream),charset), encoding, charset);
			mail.parts=new Part[]{new NormalPart("", content)};
		}
		return mail;
	}

	private static void processMultiParts(MultipartStream ms, List<Part> ps) throws IOException {
		ms.skipPreamble();
		do {
			String partHead = MIMEUtil.decodeRfc2047(ms.readHeaders());
			Map<String, String> partHeaders = StringParser.tokeyMaps(StringParser.extractKeywords(partHead, ";:= \r\n\t", false), "name", "filename", "Content-Type", "Content-Transfer-Encoding", "charset", "boundary");
			String contType = StringUtils.lowerCase(partHeaders.get("Content-Type"));
			String encoding = partHeaders.get("Content-Transfer-Encoding");
			String charset = partHeaders.get("charset");
			// if(charset!=null &&
			// charset.startsWith("\""))charset=StringUtils.substringBetween(charset,
			// "\"");
			if (contType == null || contType.startsWith("text/plain")) {// parse
				ByteArrayOutputStream out = readBodyData(ms);
				String content = decode(new String(out.toByteArray(),charset), encoding, charset);
				if (content != null) {
					ps.add(new NormalPart(contType, content));
				}
			} else if (contType.equals("text/html")) {
				ByteArrayOutputStream out = readBodyData(ms);
				String content = decode(new String(out.toByteArray(),charset), encoding, charset);
				if (content != null) {
					ps.add(new HtmlPart( content));
				}
			} else if(partHeaders.containsKey("boundary")) {
				String secBound = partHeaders.get("boundary");
				MultipartStream sub = ms.subMultiParts(secBound);
				processMultiParts(sub, ps);
			} else {// save to a file
				String fileName = partHeaders.get("filename");
				if (StringUtils.isEmpty(fileName))fileName=partHeaders.get("name");
				if (StringUtils.isEmpty(fileName)){
					fileName="unknown.part";	
				}
				File tmpFile = File.createTempFile("~up", "." + StringUtils.substringAfterLast(fileName, "."));
				if ("base64".equalsIgnoreCase(encoding)) {
					ByteArrayOutputStream out = readBodyData(ms);
					byte[] data=out.toByteArray();
					IOUtils.saveAsFile(tmpFile, false,JefBase64.decodeFast(data,0,data.length));
				} else {
					FileOutputStream out = new FileOutputStream(tmpFile);
					ms.readBodyData(out);
					out.flush();
					out.close();
				}
				FilePart fp = new FilePart(fileName, tmpFile);
				ps.add(fp);
			}
		} while (ms.readBoundary());
	}

	private static ByteArrayOutputStream readBodyData(MultipartStream ms) throws IOException {
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		ms.readBodyData(out);
		out.flush();
		out.close();
		return out;
	}

	/*
	 * 解码 base64 & QP
	 */
	private static String decode(String out, String encoding, String charset) {
		String content = null;
		try {
			if ("base64".equalsIgnoreCase(encoding)) {
				content = new String(JefBase64.decodeFast(out), charset);
			} else if("quoted-printable".equalsIgnoreCase(encoding)){ //QP编码
				content=MIMEUtil.decodeQP(out,charset);
			}else{
				content = out;
			}
		} catch (UnsupportedEncodingException e) {
			LogUtil.exception(e);
		}
		return content;
	}
	public static class MailId {
		private int num;
		private int size;

		public int getNum() {
			return num;
		}

		public void setNum(int num) {
			this.num = num;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public String toString() {
			return num + ":" + size;
		}
	}

	public static class MailSummary {
		MailId id;
		Headers headers;

		public MailId getId() {
			return id;
		}

		public void setId(MailId id) {
			this.id = id;
		}

		public Headers getHeaders() {
			return headers;
		}

		public void setHeaders(Headers headers) {
			this.headers = headers;
		}

		public String getSubject() {
			String tmp = headers.getFirst("Subject");
			return MIMEUtil.decodeRfc2047(tmp);
		}

		public String getFrom() {
			String from = headers.getFirst("From");
			return MIMEUtil.decodeRfc2047(from);
		}

		public String getTo() {
			return MIMEUtil.decodeRfc2047(headers.getFirst("To"));
		}

		public String getCc() {
			return MIMEUtil.decodeRfc2047(headers.getFirst("Cc"));
		}

		public boolean hasAttachment() {
			return headers.getFirst("Content-type").toLowerCase().startsWith("multipart");
		}

		@Override
		public String toString() {
			return headers.toString();
		}
		
	}
}
