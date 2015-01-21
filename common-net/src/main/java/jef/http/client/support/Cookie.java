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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 在Http客户端中描述一个Cookie信息
 * @author Administrator
 *
 */
public class Cookie  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6281001261095478712L;
	//Cookie中本身的Domain信息
	private String domain;
	//路径
	private String path;
	//过期日期
	private Date expires;
	//内容
	private String content;
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Date getExpires() {
		return expires;
	}
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	public String getContent() {
		return content;
	}
	
	private void addingCookie(Map<String, String> map, String[] items) {
		for(String arg :items){
			int n=arg.indexOf('=');
			if(n>-1){
				map.put(arg.substring(0,n).trim(), arg.substring(n+1).trim());
			}else{
				map.put(arg.trim(), null);
			}
		}
	}
	
	
	public String toString() {
		return this.path+"\t"+this.content;
	}
	public void addContent(String[] cookieItems) {
		Map<String,String> map=new HashMap<String,String>();
		if(this.content!=null){
			addingCookie(map,this.content.split(";"));	
		}
		addingCookie(map,cookieItems);
		StringBuilder sb=new StringBuilder();
		for(String key: map.keySet()){
			if(sb.length()>0){
				sb.append("; ");
			}
			sb.append(key.trim()).append("=");
			String value=map.get(key);
			if(value!=null){
				sb.append(value);
			}
		}
		this.content = sb.toString();

	}
	public final void setContent(String content) {
		this.content = content;
	}
}
