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
package jef.http.client.ntlm;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import jef.tools.Exceptions;

public class SocketNtlmTest {

	private static byte[] str2bytes(String str) {
		char[] chars = str.toCharArray();
		byte[] bytes = new byte[chars.length];

		int i;
		for (i = 0; i < chars.length; i++) {
			bytes[i] = (byte) (chars[i] & 0xFF);
		}
		return bytes;
	}

	static void detectProxyAuth(String proxy,int port,InetSocketAddress target,String user,String password,String host,String domain) {
		String auth_method = null;

		try {
			Socket sock = new Socket(proxy, port);
			InputStream sp_in = sock.getInputStream();
			OutputStream sp_out = sock.getOutputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(sp_in));
			BufferedOutputStream buff_out = new BufferedOutputStream(sp_out, 1460);

			/* authenticate against a proxy host */
			String connect = "GET " + target.getAddress().getHostName()  + " HTTP/1.1\r\n\r\n";
			send(buff_out,connect);

			/* read the response */
			String header;
			while ((header = reader.readLine()) != null) {
				if (header.length() == 0) {
					break; /* end of headers */
				}
				header.trim();
				System.out.println("Recv:" + header);
			}

			sock.close();
			sock = null;

			//发送初次的NTLM验证
			NTLM ntlm = new NTLM();

			String s = ntlm.getResponseFor(null, user, password, host,domain);
			sock = new Socket(proxy, port);
			sp_in = sock.getInputStream();
			sp_out = sock.getOutputStream();
			reader = new BufferedReader(new InputStreamReader(sp_in));
			buff_out = new BufferedOutputStream(sp_out, 1460);

			/* authenticate against a proxy host */
			connect = "GET "+target.getAddress().getHostName() +" HTTP/1.1\r\n" +
					"Proxy-Authorization: NTLM " + s + "\r\n\r\n";
			send(buff_out,connect);

			// String header;
			header = "";
			while ((header = reader.readLine()) != null) {
				if (header.length() == 0) {
					break; /* end of headers */
				}
				header.trim();
				System.out.println(header);

				/* authenticate against a proxy host */
				if (header.toLowerCase().startsWith("proxy-authenticate")) {
					auth_method = header.substring(header.indexOf(":") + 6).trim();
				}
			}

			String s1 = ntlm.getResponseFor(auth_method, user, password, "", domain);

			// sock = new Socket(proxy, port);
			sp_in = sock.getInputStream();
			sp_out = sock.getOutputStream();
			reader = new BufferedReader(new InputStreamReader(sp_in));
			buff_out = new BufferedOutputStream(sp_out, 1460);

			/* authenticate against a proxy host */
			connect = "GET "+target.getAddress().getHostName()+" HTTP/1.1\r\n";

			String keepalive = "Proxy-Connection: Keep-Alive\r\n\r\n";
			String AuthorizationStr = "Proxy-Authorization: NTLM " + s1 + "\r\n\r\n";
			/* send the CONNECT call */
			send(buff_out,connect,AuthorizationStr,keepalive);
			
			/* read the response */
			// String header;
			header = "";
			while ((header = reader.readLine()) != null) {
				header.trim();
//				IO
			}

			/* close the connection now since we don't need it */
			sock.close();
			sock = null;
			// return;
		} catch (Exception e) {
			Exceptions.log(e);
		}
	}

	private static void send(BufferedOutputStream buff_out,String... connect) throws IOException {
		
		for(String s:connect){
			System.out.println("send:"+s);
			buff_out.write(str2bytes(s), 0, s.length());	
		}
		buff_out.flush(); /* no more to write */
	}

	public static void main(String arg[]) {
		InetSocketAddress baidu=new InetSocketAddress("http://www.baidu.com/",80);
		detectProxyAuth("hzproxy.asiainfo-linkage.com",8080,baidu,"jiyi","aaa_123","jiyi","ailk");
	}
}
