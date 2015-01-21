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
package jef.http.client;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jef.common.Entry;
import jef.http.client.support.CommentEntry;
import jef.http.client.support.HttpMethod;
import jef.http.client.support.Proxy;
import jef.http.client.support.UserAgent;
import jef.tools.ArrayUtils;
import jef.tools.StringUtils;

public class ConnectOptions implements Serializable {
	private static final long serialVersionUID = -4417849201726291559L;
	
	/**
	 * 重复文件处理策略：检查长度
	 */
	public static final int DUP_CHECKL_LENGTH=0;
	/**
	 * 重复文件处理策略：下载
	 */
	public static final int DUP_DOWNLOAD=1;
	/**
	 * 重复文件处理策略：不下载
	 */
	public static final int DUP_SKIP=2;

	
	public static final ConnectOptions DEFAULT = new ConnectOptions();
	
	/**
	 * 代理服务器
	 */
	private Proxy proxy;
	
	/**
	 * 允许重定向
	 */
//	public boolean allowRedirect;
	private int _allowRedirect=-1;//-1自动 1允许 0不允许
	
	/**
	 * Connection: Keep-Alive or Close
	 */
	private boolean keepSessionOnServer = false;
	
	/**
	 * 当没有指定引用页时自动编造引用页
	 */
	private boolean autoGenerateRefer = true;
	
	/**
	 *如果目标文件已存在的处理策略 
	 */
	private int dupFileOption=DUP_CHECKL_LENGTH;
	
	/**
	 * 重试次数
	 */
	private int retry=3;
	/**
	 * 定义cookie
	 */
	private String cookie;
	/**
	 * 引用页
	 */
	private String reference;
	/**
	 * 浏览器和操作系统类型
	 */
	private String userAgent = UserAgent.IE7;//指明浏览器类型
	/**
	 * 备用URL
	 */
	private String[] shiftUrl;
	/**
	 * 方法
	 */
	private HttpMethod method = HttpMethod.DEFAULT;
	/**
	 * 网站身份认证
	 */
	private String authorization;
	/**
	 * 自定义Http头
	 */
	private Entry<String,String>[] customHeader;
	
	//状态数据，目前正在重试第几个备选url
	transient protected int shiftTo=0;

	public Entry<String, String>[] getCustomHeader() {
		return customHeader;
	}
	public Map<String, String> getCustomHeaderMap() {
		if(customHeader==null)return null;
		Map<String,String> map=new HashMap<String,String>();
		for(Entry<String,String> e:customHeader){
			map.put(e.getKey(), e.getValue());	
		}
		return map;
	}
	public void setCustomHeader(Entry<String, String>[] customHeader) {
		this.customHeader = customHeader;
	}
	
	@SuppressWarnings("unchecked")
	public void setCustomHeader(Map<String,String> map) {
		List<Entry<String,String>> list=new ArrayList<Entry<String,String>>();
		for(String key:map.keySet()){
			list.add(new Entry<String,String>(key,map.get(key)));
		}
		customHeader=(list.toArray(new Entry[]{}));
	}

	/**
	 * @param name
	 * @param value
	 */
	public void addCustomHeader(String name,String value){
		if(customHeader==null){
			this.customHeader=new CommentEntry[1];
			customHeader[0]=new CommentEntry(name,value);
		}else{
			CommentEntry[] newHeader=new CommentEntry[customHeader.length+1];
			System.arraycopy(customHeader, 0, newHeader, 0, customHeader.length);
			newHeader[customHeader.length]=new CommentEntry(name,value);
			this.customHeader=newHeader;
		}
	}
	
	public String nextShift(){
		if(shiftUrl==null)return null;
		if(shiftTo>=shiftUrl.length)return null;
		String url=shiftUrl[shiftTo];
		shiftTo++;
		return url;
	}

	public final String[] getShiftUrl() {
		return shiftUrl;
	}

	public final void setShiftUrl(String[] shiftUrl) {
		this.shiftUrl = shiftUrl;
	}

	public int isDupFileOption() {
		return dupFileOption;
	}
	private final static String[] DUP_STRINGS=new String[]{
		"CheckLength",
		"DownAgain",
		"Skip"
	};

	public static final String NO_COOKIE = "NO_COOKIE";
	
	public String getDupFileOption() {
		return DUP_STRINGS[dupFileOption];
	}
	public void setDupFileOption(String dupFileOption) {
		int index=ArrayUtils.indexOf(DUP_STRINGS, dupFileOption);
		if(ArrayUtils.INDEX_NOT_FOUND!=index){
			this.dupFileOption=index;
		}
	}
	public void setDupFileOption(int dupFileOption) {
		this.dupFileOption = dupFileOption;
	}

	/**
	 * 空构造
	 */
	public ConnectOptions(){};

	/**
	 * 构造
	 * @param proxy 代理服务器
	 */
	public ConnectOptions(Proxy proxy) {
		setProxy(proxy);
	}
	/**
	 * 构造
	 * @param cookie Cookie
	 * @param reference 引用页
	 */
	public ConnectOptions(String cookie, String reference) {
		this.reference = reference;
		this.cookie = cookie;
	}
	
	/**
	 * 要执行的HTTP方法(和参数)
	 * @param method
	 */
	public ConnectOptions(HttpMethod method) {
		this.method = method;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public boolean isAutoGenerateRefer() {
		return autoGenerateRefer;
	}

	public void setAutoGenerateRefer(boolean autoGenerateRefer) {
		this.autoGenerateRefer = autoGenerateRefer;
	}

	public String getReference(URL url) {
		if (reference != null)
			return reference;
		if(url==null)return null;
		if (autoGenerateRefer) {
			return StringUtils.substringBeforeLast(url.toString(), "/");
		}
		return null;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public boolean isKeepSessionOnServer() {
		return keepSessionOnServer;
	}

	public void setKeepSessionOnServer(boolean keepSessionOnServer) {
		this.keepSessionOnServer = keepSessionOnServer;
	}

	public String getProxyString(){
		return StringUtils.toString(proxy);
	}
	public void setProxy(Proxy proxy){
		if(proxy!=null && proxy.isValid()){
			this.proxy=proxy;
		}else{
			this.proxy=null;
		}
	}
	public Proxy getProxy() {
		return proxy;
	}
	
	public String getAuthorization() {
		return authorization;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}
	public int getRetry() {
		return retry;
	}
	public void setRetry(int retry) {
		this.retry = retry;
	}
	public final int isAllowRedirect() {
		return _allowRedirect;
	}
	public final void setAllowRedirect(boolean allowRedirect) {
		this._allowRedirect = allowRedirect?1:0;
	}
	public String  getProxyLogin(){
		if(proxy==null)return null;
		return proxy.getProxyLogin();
	}
}
