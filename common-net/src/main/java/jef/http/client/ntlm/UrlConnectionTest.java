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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import jef.net.AuthenticationException;
import jef.tools.IOUtils;

public class UrlConnectionTest {
	public static void main(String...strings) throws IOException, AuthenticationException{
		URL u=new URL("HTTP://www.baidu.com");
		String proxyHost="hzproxy.asiainfo-linkage.com";
		String domain="ailk";
		String user="jiyi";
		String password="aaa_123";
		int proxyPort=8080;
		Proxy p=new Proxy(java.net.Proxy.Type.HTTP,new InetSocketAddress(proxyHost, proxyPort));
		URLConnection con=u.openConnection(p);
		NTLM ntlm=new NTLM();
		
		con.setRequestProperty("Proxy-Authorization", "NTLM "+ntlm.getType1Message("jiyi", domain));
		con.connect();
		
		String challage=con.getHeaderField("Proxy-Authenticate").substring(6);
		System.out.println(challage);
		String key=ntlm.getType3Message(user, password, "jiyi", domain, challage);
		con.getInputStream().close();
		con=u.openConnection(p);
		con.setRequestProperty("Proxy-Authorization", "NTLM "+key);
		con.connect();
		IOUtils.copy(con.getInputStream(), System.out, false);
	}

}
