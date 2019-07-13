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
package jef.http.client.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jef.common.wrapper.Paginator;
import jef.http.client.HostManager;
import jef.http.client.HostProfile;
import jef.http.client.support.PostDownCfg;
import jef.tools.Exceptions;
import jef.tools.StringUtils;
import jef.ui.ConsoleShell;

public class HostEditConversation extends AbstractEditConversation<HostProfile> {
	public HostEditConversation(ConsoleShell app) {
		super(app);
	}

	protected void callSave() {
		try {
			HostManager.saveToFile(null);
		} catch (IOException e) {
			Exceptions.log(e);
		}
	}

	protected boolean edit(HostProfile host) {
		String name = getInput("Host name: (current:"+host.getHost()+")").toLowerCase();
		if (name.indexOf("//") > -1) {
			try {
				URL u = new URL(name);
				name = u.getHost().toLowerCase();
			} catch (MalformedURLException e) {
				Exceptions.log(e);
			}
		}
		if(StringUtils.isNotEmpty(name))host.setHost(name);
		
		host.setAllowRedirect(getInputBoolean("允许重定向?", host.isAllowRedirect()));
		host.setAutoCookie(getInputBoolean("允许自动管理Cookie?", host.isAutoCookie()));
		
		int n=getInputInt("最大连接数:(Current:" + host.getMaxCount()+")");
		if(n>-1)	host.setMaxCount(n);
		
		String proxy=getInput("代理服务器:(Current:" + StringUtils.toString(host.getProxy())+"),输入null表示无");
		if(StringUtils.isEmpty(proxy)){
		}else if(proxy.equals("null")){
			host.setProxy("");
		}else{
			host.setProxy(proxy);
		}
		
		//host.setUseProxyPool(getInputBoolean("使用代理池?", false));
		if(getInputBoolean("修改高级设置？",host.getAttachment()!=null)){
			PostDownCfg cfg=inputPostDownCfg((PostDownCfg)host.getAttachment());
			
			if(cfg!=null){
				if(cfg==DELETE){
					host.setAttachment(null);
				}else{
					host.setAttachment(cfg);	
				}
			}
		}
		prompt(host.getHost());
		prompt("MaxCount:"+host.getMaxCount());
		prompt("Proxy:"+StringUtils.toString(host.getProxy()));
		prompt("AllowDirect:"+host.isAllowRedirect());
		prompt("AutoCookie:"+host.isAutoCookie());
		prompt("Attachment:"+host.getAttachment());
		return getInputBoolean("OK?",true);
	}

	private static PostDownCfg DELETE=new PostDownCfg();
	private PostDownCfg inputPostDownCfg(PostDownCfg cfg) {
		if(cfg==null)cfg=new PostDownCfg();
		cfg.setHasSubmitField(false);
		
		int type=getInputInt("高级类型0-Post,1-Track 2-Remove(Current:" + cfg.getType()+")");
		if(type==2){
			return DELETE;
		}
		if(type!=1 && type!=0){
			return null;
		}
		cfg.setType(type);
		
		if(type==1){//tract html to get url
			String tmp=getInput("路径匹配：(用/开头,(*)通配) (Current:" + cfg.getRawUrl() + ")");
			if(StringUtils.isNotEmpty(tmp)){
				cfg.setRawUrl(tmp);	
			}
			
			tmp=getInput("追踪的Xpath: (Current:"+ cfg.getAction() +")");
			if(StringUtils.isNotEmpty(tmp)){
				cfg.setAction(tmp);	
			}
		}else{//post to a new url
			String tmp=getInput("路径匹配：(用/开头,(*)通配)(Current:" + cfg.getRawUrl()+")");
			if(StringUtils.isNotEmpty(tmp)){
				cfg.setRawUrl(tmp);	
			}
			
			tmp=getInput("提交URL：(Current:" + cfg.getAction() +")");
			if(StringUtils.isNotEmpty(tmp)){
				cfg.setAction(tmp);
			}
			
			tmp=getInput("Code field name:(Current:"+ cfg.getCodeField()+")");
			if(StringUtils.isNotEmpty(tmp)){
				cfg.setCodeField(tmp);	
			}
			
			tmp=getInput("添加Field(,分隔)(Current:" + cfg.getAppendField()+")");
			if(StringUtils.isNotEmpty(tmp)){
				cfg.setAppendField(tmp);	
			}
			
			tmp=getInput("添加Value(,分隔),支持{code}: (Current:"+ cfg.getAppendValue()+")");
			if(StringUtils.isNotEmpty(tmp)){
				cfg.setAppendValue(tmp);	
			}
		}
		return cfg;
	}

	private static final HostProfile[] EMPTY=new HostProfile[0];
	/**
	 * 更新分页器中的数据
	 */
	protected void load(){
		int cutPage = -1;
		if(p!=null)cutPage=p.getCurrentPage();
		HashMap<String, HostProfile> hosts = HostManager.getAll();
		List<HostProfile> profile=Arrays.asList(hosts.values().toArray(EMPTY));
		p= new Paginator<HostProfile>(profile,10);
		if(cutPage>-1)p.setPage(cutPage);
		
	}
	

	
	protected void createNew() {
		HostProfile host = new HostProfile();
		if(edit(host)){
			HostManager.add(host);
			load();
		}
	}
	
	protected boolean delete(HostProfile toDel) {
		return HostManager.remove(toDel.getHost());
	}

	
	protected void endConversation() {
		try {
			HostManager.loadFromFile(null);
		} catch (IOException e) {
			Exceptions.log(e);
		}
	}
}
