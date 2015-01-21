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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import jef.common.Entry;
import jef.common.log.LogUtil;
import jef.tools.JXB;
import jef.tools.string.RegexpUtils;

import org.xml.sax.SAXException;

public class HostManager{
	public static String HOST_FILE = "hosts.xml";
	private static HashMap<String, HostProfile> hosts;
	private static List<Entry<String,Pattern>> wildcardHosts=new ArrayList<Entry<String,Pattern>>();
	
	public static HostProfile getHost(String host) {
		if(hosts==null)init();
		HostProfile p=hosts.get(host);
		if(p!=null)return p;
		
		for(Entry<String,Pattern> key:wildcardHosts){
			if(key.getValue().matcher(host).matches()){
				return hosts.get(key.getKey());
			}
		}
		return null;
	}

	public static HashMap<String, HostProfile> getAll() {
		if(hosts==null)init();
		return hosts;
	}

	private static synchronized void init() {
		if (hosts == null) {
			File file=new File(HOST_FILE);
			if(file.exists()){
				try {
					loadFromFile(file);
				} catch (IOException e) {
					LogUtil.show(e);
				}
			}
			if(hosts==null){
				hosts=getDefaultProfile();
			}
		}
	}
	
	/**
	 * 创建一个内建的缺省站点简要表
	 * @return
	 */
	public static final HashMap<String, HostProfile> getDefaultProfile(){
		List<HostProfile> hs=new ArrayList<HostProfile>(15);
		HostProfile me=new HostProfile("www.baidu.com");
		me.setMaxCount(5);
		hs.add(me);
		
		HashMap<String, HostProfile> map=new HashMap<String,HostProfile>();
		for(HostProfile h:hs){
			map.put(h.getHost(), h);
		}
		return map;
	}
	

	// 将配置写入文本文件
	public static void saveToFile(File file) throws IOException {
		if(hosts==null)init();
		if (file == null)
			file = new File(HOST_FILE);
		JXB.saveObjectToXML(hosts, file);
	}

	// 将配置从文本文件读入
	@SuppressWarnings("unchecked")
	public static void loadFromFile(File file) throws IOException {
		if (file == null)
			file = new File(HOST_FILE);
		if(!file.exists()){
			LogUtil.show("File not found:"+ file.getAbsolutePath());
			return;
		}
		try {
			hosts = (HashMap<String, HostProfile>) JXB.loadObjectFromXML(file);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		} catch (InstantiationException e) {
			throw new IOException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new IOException(e.getMessage());
		}
		//将所有的通配站点挑出来
		wildcardHosts.clear();
		for(String pro:hosts.keySet()){
			if(pro.indexOf('*')>-1){
				wildcardHosts.add(new Entry<String,Pattern>(pro,RegexpUtils.simplePattern(pro, false, true, true, false)));
			}
		}
	}

	public static void add(HostProfile host) {
		hosts.put(host.getHost(), host);
		String pro=host.getHost();
		if(pro.indexOf('*')>-1){
			wildcardHosts.add(new Entry<String,Pattern>(pro,RegexpUtils.simplePattern(pro, false, true, true, false)));
		}
	}
	
	public static boolean remove(String key){
		HostProfile p=hosts.remove(key);
		if(key.indexOf('*')>-1){
			wildcardHosts.remove(key);
		}
		return p!=null;
	}

	public static void setNotSupportBreakPoint(URL url) throws IOException {
		HostProfile p=getHost(url.getHost());
		if(p==null){
			p=new HostProfile(url.getHost());
			p.setSupportBreakPoint(false);
			add(p);
			saveToFile(null);
		}
	}
	
	
}
