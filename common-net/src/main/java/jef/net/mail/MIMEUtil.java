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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import jef.tools.Exceptions;
import jef.tools.StringUtils;
import jef.tools.io.Charsets;
import jef.tools.io.WriterOutputStream;
import jef.tools.string.JefStringReader;
import jef.tools.support.JefBase64;

public class MIMEUtil {
	/**
	 * 解码邮件MIME头等位置的编码文本，目前只支持rfc2047=?B?str?=格式的文本
	 * @param source
	 * @return
	 */
	public static String decodeRfc2047(String source){
		if(StringUtils.isEmpty(source))return "";
		JefStringReader reader =new JefStringReader(source);
		StringBuilder sb = new StringBuilder(); 
		int c;
		char[] WEN_HAO=new char[]{'?'};
		try {
			while ((c = reader.read()) != -1) {
				if (c == (int)'=' && reader.nextChar()==(int)'?') { //处理rfc2047即(=?b?str?=)格式的编码
					reader.read();
					String charSet=new String(reader.readUntilCharIs(WEN_HAO));
					charSet=Charsets.getStdName(charSet);
					reader.omit(1);
					char way=(char)reader.read();
					reader.omit(1);
					String code=reader.readUntillKey("?=");
					if(way=='B' || way=='b'){
						sb.append(new String(JefBase64.decodeFast(code),charSet));	
					}else{
						sb.append(decodeQP(code,charSet));
					}
					
					reader.omit(2);
				} else {
					sb.append((char) c);
				}
			}
		} catch (IOException e) {
			Exceptions.log(e);
		}
		reader.close();
		return sb.toString();
	}

	/**
	 * 邮件MIME头等位置的文本编码，目前只支持rfc2047=?B?str?=格式的文本
	 * @param source
	 * @param charSet
	 * @return
	 */
	public static String encodeRfc2047(String source, String charSet) {
		if(StringUtils.isEmpty(source))return "";
		if (StringUtils.isEmpty(source))
			return source;
		if (!StringUtils.hasAsian(source))
			return source;
		StringBuilder sb = new StringBuilder();
		sb.append("=?");
		sb.append(charSet);
		sb.append("?B?");
		try {
			sb.append(JefBase64.encode(source.getBytes(charSet)));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		sb.append("?=");
		return sb.toString();
	}
	
	/**
	 * QP编码
	 * @param str
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public static String encodeQP(String str,String charset) throws IOException {
		int count = 0;
		if (str == null) {
			return null;
		}
		byte[] bytes = str.getBytes(charset);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(str.length()*2);
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i];
			if (b < 0) {
				b = 256 + b;
			}
			count++;
			if (PRINTABLE_CHARS.get(b)) {
				if (count == 76) {
					count = 0;
					count++;
					buffer.write(ESCAPE_CHAR);
					buffer.write(CHUNK_SEPARATOR);
				}
				buffer.write(b);
			} else {
				count = encodeQuotedPrintable(b, buffer, count);
			}
		}
		return new String(buffer.toByteArray(), "US-ASCII");
	}
	
	/**
	 * 
	 * @param str
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static void encodeQPAndoutput(String str,String charset,PrintWriter pw) throws IOException {
		int count = 0;
		if (str == null) {
			return;
		}
		byte[] bytes = str.getBytes(charset);
		WriterOutputStream buffer=new WriterOutputStream(pw);
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i];
			if (b < 0) {
				b = 256 + b;
			}
			count++;
			if (PRINTABLE_CHARS.get(b)) {
				if (count >= 76) {
					count = 0;
					count++;
					buffer.write(ESCAPE_CHAR);
					buffer.write(CHUNK_SEPARATOR);
				}
				buffer.write(b);
			} else {
				count = encodeQuotedPrintable(b, buffer, count);
			}
		}
	}

	/**
	 * QP解码
	 * @param source
	 * @param charset 编码，为null时默认使用UTF-8
	 * @return 解码后的文本
	 * @throws UnsupportedEncodingException 
	 */
	public static String decodeQP(String source,String charset) throws UnsupportedEncodingException{
		if(StringUtils.isEmpty(source))return "";
		source = source.replaceAll("=\r\n", "");
		//qpStr = qpStr.replaceAll("=\n", "");
		char[] bytes = source.toCharArray();
		return decodeQP(bytes,charset); 
		
	}
	
	/**
	 * QP解码
	 * @param bytes 字节组
	 * @param charset 编码，为null时默认使用UTF-8
	 * @return 解码后的文本
	 * @throws UnsupportedEncodingException
	 */
	public static String decodeQP(char[] bytes, String charset) throws UnsupportedEncodingException {
		if(charset==null)charset="UTF-8";
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i];
			if (b == ESCAPE_CHAR) {
				try {
					int u = Character.digit(bytes[++i], 16);
					int l = Character.digit(bytes[++i], 16);
					if (u == -1 || l == -1) {
						continue;
					}
					buffer.write((char) ((u << 4) + l));
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new IllegalArgumentException("Invalid quoted-printable encoding");
				}
			} else {
				buffer.write(b);
			}
		}
		return new String(buffer.toByteArray(), charset);
	}

	/*
	 * 单字符QP编码
	 * @param b
	 * @param buffer
	 * @throws IOException
	 */
	private static final int encodeQuotedPrintable(int b, OutputStream buffer, int count) throws IOException {
		if (count >= 76) {
			count = 0;
			count++;
			buffer.write(ESCAPE_CHAR);
			buffer.write(CHUNK_SEPARATOR);
		}
		char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
		char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
		
		buffer.write(ESCAPE_CHAR);
		count++;
		buffer.write(hex1);
		buffer.write(hex2);
		count++;
//		if (count == 76) {
//			count = 0;
//			count++;
//			buffer.write(ESCAPE_CHAR);
//			buffer.write(CHUNK_SEPARATOR);
//		}
		return count;
	}
	
	private static final BitSet PRINTABLE_CHARS = new BitSet(256);
	static {
		for (int i = 33; i <= 60; i++) {
			PRINTABLE_CHARS.set(i);
		}
		for (int i = 62; i <= 126; i++) {
			PRINTABLE_CHARS.set(i);
		}
		PRINTABLE_CHARS.set('\t');
		PRINTABLE_CHARS.set(' ');
	}
	private static final byte ESCAPE_CHAR = '=';
	private static final byte[] CHUNK_SEPARATOR = "\r\n".getBytes();
}
