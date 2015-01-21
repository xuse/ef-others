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
import java.util.HashMap;
import java.util.Map;

import jef.common.Entry;
import jef.http.client.support.CommentEntry;
import jef.http.client.support.Proxy;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 站点简要表
 * @author Administrator
 *
 */
public class HostProfile implements Serializable,Comparable<HostProfile>{
	private static final long serialVersionUID = 6957537064790318640L;
	
	private String host;
	private String filenameParam;
	private int maxCount=4;
	private Proxy proxy;
	private boolean allowRedirect=false;
	private boolean autoCookie=true;
	private boolean supportBreakPoint=true;
	private Object attachment;
	private Entry<String,String>[] customHeader;
	
	public Entry<String, String>[] getCustomHeader() {
		return customHeader;
	}
	public void setCustomHeader(Entry<String, String>[] customHeader) {
		this.customHeader = customHeader;
	}
	public boolean isSupportBreakPoint() {
		return supportBreakPoint;
	}
	public void setSupportBreakPoint(boolean supportBreakPoint) {
		this.supportBreakPoint = supportBreakPoint;
	}
	public final boolean isAutoCookie() {
		return autoCookie;
	}
	public final void setAutoCookie(boolean autoCookie) {
		this.autoCookie = autoCookie;
	}
	public boolean isAllowRedirect() {
		return allowRedirect;
	}
	public void setAllowRedirect(boolean allowRedirect) {
		this.allowRedirect = allowRedirect;
	}
	
	/**
	 * @param name
	 * @param value
	 */
	public void addCustomHeader(String name,String value){
		CommentEntry[] newHeader=new CommentEntry[customHeader.length+1];
		System.arraycopy(customHeader, 0, newHeader, 0, customHeader.length);
		newHeader[customHeader.length]=new CommentEntry(name,value);
		this.customHeader=newHeader;
	}

	/**
	 * 构造
	 * @return
	 */
	public String getHost() {
		return host;
	}
	/**
	 * 构造
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}
	public Proxy getProxy() {
		return proxy;
	}
	public void setProxy(Proxy proxy) {
		if(proxy==null || proxy.toString().length()==0){
			this.proxy=null;
		}else{
			this.proxy = proxy;	
		}
	}
	public void setProxy(String proxy) {
		if(StringUtils.isNotBlank(proxy)){
			this.proxy = Proxy.create(proxy);	
		}else{
			this.proxy=null;
		}
	}
	public Object getAttachment() {
		return attachment;
	}
	public void setAttachment(Object postDown) {
		this.attachment = postDown;
	}
	public HostProfile(){}
	public HostProfile(String string) {
		this.host=string;
	}
	public int getMaxCount() {
		return maxCount;
	}
	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}
	
	public final String getFilenameParam() {
		return filenameParam;
	}
	public final void setFilenameParam(String filenameParam) {
		this.filenameParam = filenameParam;
	}
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append(StringUtils.rightPad(host, 25));
		sb.append(" ").append(maxCount);
		sb.append(" ").append(StringUtils.rightPad(ObjectUtils.toString(proxy),16));
		sb.append(" ").append((attachment!=null)?attachment.toString():"");
		return sb.toString();
	}
	
	public int compareTo(HostProfile o) {
		String[] h1=StringUtils.split(host,".");
		String[] h2=StringUtils.split(o.host,".");
		int length=Math.min(h1.length, h2.length);
		for(int i=1;i<=length;i++){
			String s1=h1[h1.length-i];
			String s2=h2[h2.length-i];
			int n=s1.compareTo(s2);
			if(n!=0)return n;
		}
		return Integer.valueOf(h1.length).compareTo(h2.length);
	}
	public Map<String, String> getCustomHeaderMap() {
		if(customHeader==null)return null;
		Map<String,String> map=new HashMap<String,String>();
		for(Entry<String,String> e:customHeader){
			map.put(e.getKey(), e.getValue());	
		}
		return map;
	}
	public static boolean breakPoint(HostProfile host2) {
		if(host2==null)return true;
		return host2.supportBreakPoint;
	}
}
