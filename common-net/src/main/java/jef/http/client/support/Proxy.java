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

import java.io.Serializable;
import java.net.InetSocketAddress;

import jef.tools.StringUtils;

/**
 * 描述一个HTTP类型的代理
 * @author Administrator
 *
 */
public class Proxy implements Serializable{
	private static final long serialVersionUID = -7899405987405611389L;
	public static final Proxy DIRECT_CONNECT=new Proxy();
	/**
	 * 代理服务器地址
	 */
	private String proxyHost;
	/**
	 * 代理服务器端口
	 */
	private int proxyPort;
	/**
	 * 代理服务器验证字符串<br>
	 * 用户名:密码
	 */
	private String proxyLogin;
	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
	public int getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * 根据字符串创建代理
	 * @param proxy
	 * @return
	 */
	public static Proxy create(String proxy){
		if(StringUtils.isBlank(proxy))return null;
		Proxy p=new Proxy();
		if(proxy.indexOf("@")>-1){
			String loginStr=StringUtils.substringBeforeLast(proxy, "@");
			proxy=StringUtils.substringAfterLast(proxy, "@");
			p.proxyLogin=loginStr;
		}
		if (proxy.indexOf(":") > -1) {
			String port = StringUtils.substringAfter(proxy, ":");
			p.proxyHost = StringUtils.substringBefore(proxy, ":");
			p.proxyPort= StringUtils.toInt(port, -1);
		}else{
			p.proxyHost =proxy;
			p.proxyPort= -1;
		}
		if(p.isValid())return p;
		return null;
	}
	
	public Proxy(){}
	
	
	public String toString() {
		if(!isValid())return "";
		String proxyStr=proxyHost+":"+proxyPort;
		if(proxyLogin!=null){
			return proxyLogin+"@"+proxyStr;
		}else{
			return proxyStr;	
		}
	};
	
	public java.net.Proxy toJavaProxy(){
		if(!isValid())return null;
		return new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
	}
	
	public boolean isValid(){
		if(StringUtils.isEmpty(proxyHost) || proxyPort==0)return false;
		return true;
	}
	public String getProxyUser() {
		if(proxyLogin==null)return null;
		return StringUtils.substringBefore(proxyLogin, ":");
	}
	public String getProxyPassword() {
		if(proxyLogin==null)return null;
		return StringUtils.substringAfter(proxyLogin, ":");
	}
	public String getProxyLogin() {
		return this.proxyLogin;
	}
	
}
