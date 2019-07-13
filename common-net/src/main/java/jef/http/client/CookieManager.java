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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xml.sax.SAXException;

import jef.common.log.LogUtil;
import jef.http.client.support.Cookie;
import jef.tools.ArrayUtils;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.JXB;
import jef.tools.StringUtils;

public class CookieManager implements Serializable{
	private static final long serialVersionUID = -4474061474173390959L;
	//记录所有的Cookie
	private Map<String,Domain> domains=new ConcurrentHashMap<String,Domain>();
	//记录所有的通配Cookie Domain;
	private List<Domain> wildcardDomains=new ArrayList<Domain>();
	transient boolean isChanged=false;
	public static boolean DEBUG_MODE=true;
	
	/**
	 * 根据URL获取有效的Cookie
	 * @param url
	 * @return
	 */
	String getCookie(URL url){
		String host=url.getHost().toLowerCase();
		Domain domain=domains.get(host);
		if(domain==null){
			for(Domain wildcard:wildcardDomains){
				if(wildcard!=null && host.endsWith(wildcard.name.substring(1))){
					domain=wildcard;
					break;
				}
			}
		}
		if(domain==null)return null;
		List<Cookie> enable=new ArrayList<Cookie>();
		for(Cookie cookie:domain.cookies){
			if("/".equals(cookie.getPath())){
				enable.add(cookie);
			}else if(url.getPath().indexOf(cookie.getPath())>-1){
				enable.add(cookie);
			}
		}
		if(enable.isEmpty())return null;
		StringBuilder sb=new StringBuilder();
		sb.append(enable.get(0).getContent());
		for(int i=1;i<enable.size();i++){
			sb.append("; ");
			sb.append(enable.get(i).getContent());			
		}
		String str=sb.toString();
		//System.out.println("站点:" +url+"cookie:"+ str);
		return str;
	}
	
	public void putCookie(Domain domain) {
		Domain old=domains.put(domain.getName(), domain);
		if(old==null && domain.name.startsWith(".")){
			wildcardDomains.add(domain);
		}
	}

	/**
	 * 将得到的Cookie字串写入
	 * @param host
	 * @param cookieStr
	 */
	public void setCookie(String host,String cookieStr,boolean isNew){
		//System.out.println("Set:" +host+"cookie:"+ cookieStr);
		Cookie cookie=new Cookie();
		String[] args=cookieStr.split("; ");
		
		//Do Filter
		List<String> filtered=new ArrayList<String>();
		for(String cstr:args){
			if(cstr.startsWith("domain=")){
				String value=StringUtils.substringAfter(cstr, "=");
				cookie.setDomain(value.toLowerCase());
			}else if(cstr.startsWith("expires=")){
				//目前所有Cookie暂时都不设过期时间
			}else if(cstr.startsWith("path=")){
				String value=StringUtils.substringAfter(cstr, "=");
				cookie.setPath(value);
			}else if(cstr.startsWith("HttpOnly")){
				
			}else{
				filtered.add(cstr.trim());
			}
		}
		args=filtered.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
		
		//check Cookie
		if(args.length==0)return;
		if(cookie.getDomain()==null)cookie.setDomain(host);
		if(cookie.getPath()==null)cookie.setPath("/");
		
		//adding cookie
		Domain domain=domains.get(cookie.getDomain());
		if(domain==null){//之前没有则新创建一个
			domain=new Domain(cookie.getDomain());
			domains.put(cookie.getDomain(), domain);
			if(domain.name.startsWith(".")){
				wildcardDomains.add(domain);
			}
		}
		domain.updateCookie(cookie, args, isNew);
		isChanged=true;
	}
	
	public Collection<String> domains(){
		return domains.keySet();
	}

	public boolean removeCookie(String name) {
		Domain domain= domains.get(name);
		if(domain!=null){
			if(domain.name.startsWith("."))wildcardDomains.remove(domain);
			domains.remove(name);
			isChanged=true;
			return isChanged;
		}else{
			return false;
		}
	}
	
	public void clearCookies() {
		domains.clear();
		wildcardDomains.clear();
		isChanged=true;
	}
	
	public int size(){
		return domains.size();
	}
	
	public Domain getDomainCookie(String domain) {
		return domains.get(domain);
	}
	
	/**
	 * 描述一个域下面的所有Cookie
	 * @author Administrator
	 *
	 */
	public static class Domain implements Serializable{
		private static final long serialVersionUID = -2617442576037310433L;
		String name; 
		List<Cookie> cookies=new ArrayList<Cookie>();
		public Domain(String domain){
			this.name=domain;
		}
		
		public String toString() {
			StringBuilder sb=new StringBuilder();
			for(Cookie cookie: cookies){
				sb.append(name).append(' ');
				sb.append(StringUtils.rightPad(cookie.getPath(),10));
				sb.append("\n\t");
				sb.append(cookie.getContent());
			}
			return sb.toString();
		}

		/**
		 * 输入Cookie基本信息，查找并更新Cookie
		 * @param newCookie ,Cookie基本信息，其实只包含host\Expire\
		 * @param cookieItems
		 * @param isNew
		 */
		public void updateCookie(Cookie newCookie, String[] cookieItems,boolean isNew) {
			for(Cookie old:cookies){
				//旧的找到了，更新Cookie;
				if(old.getPath().equals(newCookie.getPath())){
					if(isNew)old.setContent(null);
					old.addContent(cookieItems);
					old.setExpires(newCookie.getExpires());
					return;
				}
			}
			newCookie.addContent(cookieItems);
			cookies.add(newCookie);
		}
		public String getName() {
			return name;
		}
		void setName(String name) {
			this.name = name;
		}
		public List<Cookie> getCookies() {
			return cookies;
		}
		protected void setCookies(List<Cookie> cookies) {
			this.cookies = cookies;
		}
		public String getRootCookie(){
			StringBuilder sb=new StringBuilder();
			for(Cookie c: this.cookies){
				if(c.getPath().equals("/")){
					sb.append(c.getContent());		
				}
			}
			return sb.toString();
		}
		
		public Domain(){
		}
	}
	/**
	 * 存为XML
	 * @return
	 */
	public File saveXml() {
		File file=new File("Cookies.xml");
		JXB.saveObjectToXML(new Object[]{this.domains,this.wildcardDomains}, file);
		return file;
	}

	/**
	 * 从XML中读入
	 */
	@SuppressWarnings("unchecked")
	public void loadXml() {
		File file=new File("Cookies.xml");
		if(!file.exists()){
			LogUtil.show("File not exist:"+file.getAbsolutePath());
			return;
		}
		try {
			Object[] dummy=(Object[]) JXB.loadObjectFromXML(file);
			this.domains=(Map<String, Domain>) dummy[0];
			this.wildcardDomains=(List<Domain>) dummy[1];
		} catch (SAXException e) {
			Exceptions.log(e);
		} catch (IOException e) {
			Exceptions.log(e);
		} catch (InstantiationException e) {
			Exceptions.log(e);
		} catch (IllegalAccessException e) {
			Exceptions.log(e);
		}
	}
	
	public Map<String, Domain> getDomains() {
		return domains;
	}

	public void setDomains(Map<String, Domain> domains) {
		this.domains = domains;
	}

	public List<Domain> getWildcardDomains() {
		return wildcardDomains;
	}

	public void setWildcardDomains(List<Domain> wildcardDomains) {
		this.wildcardDomains = wildcardDomains;
	}
	public void save() {
		if(this.isChanged){
			IOUtils.saveObject(this, new File("Cookies.manager"));
			this.isChanged = false;	
		}
	}
}
