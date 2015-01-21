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

import java.net.MalformedURLException;
import java.net.URL;

import jef.common.log.LogUtil;
import jef.common.wrapper.Paginator;
import jef.http.client.HttpEngine;
import jef.http.client.HttpTask;
import jef.http.client.support.Proxy;
import jef.tools.StringUtils;
import jef.tools.reflect.BeanUtils;
import jef.ui.ConsoleShell;

public class TaskEditConversation extends AbstractEditConversation<HttpTask>{
	private boolean isErr;
	private HttpEngine engine;
	public TaskEditConversation(ConsoleShell app,boolean isErr,HttpEngine engine) {
		super(app);
		this.engine=engine;
		this.isErr=isErr;
	}

	
	protected void callSave() {
		prompt("\u65E0\u9700\u4FDD\u5B58");
		//do nothing
	}

	
	protected void createNew() {
		prompt("\u4E0D\u652F\u6301\u65B0\u5EFA\u64CD\u4F5C");
		//do nothing
	}

	
	protected boolean delete(HttpTask toDel) {
		if(isErr){
			return engine.removeError(toDel);	
		}else{
			return engine.removeQueued(toDel);
		}
	}

	
	protected boolean edit(HttpTask obj) {
		String tmp=getInput("Url:" + obj.getUrl());
		if(StringUtils.isNotEmpty(tmp)){
			try {
				BeanUtils.setFieldValue(obj, "url", new URL(tmp));
			} catch (MalformedURLException e) {
				LogUtil.exception(e);
			}
		}
		//代理
		tmp=getInput("Proxy  (Current:" +obj.getOptions().getProxyString()+") input null to set null");
		if(StringUtils.isNotEmpty(tmp)){
			if(tmp.equalsIgnoreCase("null")){
				obj.getOptions().setProxy(null);
			}else{
				obj.getOptions().setProxy(Proxy.create(tmp));
			}
		}
		
		tmp=getInput("Reference (Current:" + obj.getOptions().getReference(obj.getUrl())+")");
		if(StringUtils.isNotEmpty(tmp)){
			obj.getOptions().setReference(tmp);
		}
		
		tmp=getInput("Cookie (Current:" + obj.getOptions().getCookie() +")");
		if(StringUtils.isNotEmpty(tmp)){
			obj.getOptions().setCookie(tmp);
		}
		
		tmp=getInput("KeepSession (Current: "+ obj.getOptions().isKeepSessionOnServer() +")");
		if(StringUtils.isNotEmpty(tmp)){
			obj.getOptions().setKeepSessionOnServer(StringUtils.toBoolean(tmp, false));
		}
		
		tmp=getInput("Allow Redirect (Current: "+ obj.getOptions().isAllowRedirect() +")");
		if(StringUtils.isNotEmpty(tmp)){
			obj.getOptions().setAllowRedirect(StringUtils.toBoolean(tmp, false));
		}
		
		tmp=getInput("UserAgent (Current: "+ obj.getOptions().getUserAgent()+")");
		if(StringUtils.isNotEmpty(tmp)){
			obj.getOptions().setUserAgent(tmp);
		}
		
		tmp=getInput("LocalPath (Current: "+ obj.getFilepath()+")");
		if(StringUtils.isNotEmpty(tmp)){
			BeanUtils.setFieldValue(obj, "filepath", tmp);
		}
		
		LogUtil.show(obj.getOptions().getCustomHeaderMap());
		if(getInputBoolean("edit custom header:",false)){
			
		}
		
		
		
		//自定义HTTP头
		
		
		
		

		return false;
	}

	
	protected void load() {
		int cutPage = -1;
		if(p!=null)cutPage=p.getCurrentPage();
		if(this.isErr){
			p= new Paginator<HttpTask>(engine.getErrors(),15);	
		}else {
			p= new Paginator<HttpTask>(engine.getQueued(),15);
		}
		if(cutPage>-1)p.setPage(cutPage);
		
	}

	
	protected void endConversation() {
		
	}


}
