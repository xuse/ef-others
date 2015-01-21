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

import jef.http.client.HttpEngine;
import jef.http.client.HttpTask;
import jef.http.client.HttpTask.TaskState;
import jef.http.client.support.Proxy;
import jef.tools.StringUtils;
import jef.ui.ConsoleConversation;
import jef.ui.ConsoleShell;

public class TaskBatchModifyConversation extends ConsoleConversation<String>{
	HttpEngine engine;
	public TaskBatchModifyConversation(ConsoleShell app,HttpEngine engine) {
		super(app);
		this.engine=engine;
	}

	
	protected void execute() {
		boolean isPaused=engine.isPause();
		if(!isPaused)engine.pause();
		engine.pause();
		boolean editQueue=getInputBoolean("修改队列?",true);
		boolean editError=getInputBoolean("修改错误任务?",true);
		String pattern=getInput("筛选：url匹配:(*匹配，空表示全部)");
		int n=0;
		if(pattern.indexOf('*')>-1 || pattern.indexOf('?')>-1){
			String proxy=getInput("修改代理为：(空表示不变,null表示清空)");
			String cookie=getInput("修改Cookie为：(空表示不变,null表示清空)");
			boolean isAll=StringUtils.isEmpty(pattern);
			if("null".equalsIgnoreCase(proxy))proxy=" ";

			if(editQueue){
				for(HttpTask t:engine.getQueued()){
					if(isAll||StringUtils.contains(t.getUrl().toString(), pattern, true)){
						modifyTask(t,proxy,cookie);
						n++;
					}
				}
			}
			if(editError){
				for(HttpTask t:engine.getErrors()){
					if(isAll||StringUtils.contains(t.getUrl().toString(), pattern, true)){
						if(modifyTask(t,proxy,cookie))n++;
					}
				}
			}
		}else{
			String urlReplace=getInput("url匹配字串替换为：");
			if(StringUtils.isNotEmpty(urlReplace)){
				int maxCount=getInputInt("最大修改数量：");
				if(maxCount==0)maxCount=99999;
				if(maxCount>0){
					if(editQueue){
						for(HttpTask t:engine.getQueued()){
							String url=t.getUrl().toString();
							if(url.indexOf(pattern)>-1){
								if(t.setUrlIfAvalible(url.replace(pattern, urlReplace))){
									n++;
								}
							}
							if(n>maxCount)break;
						}
					}
					if(editError){
						for(HttpTask t:engine.getErrors()){
							String url=t.getUrl().toString();
							if(url.indexOf(pattern)>-1){
								if(t.setUrlIfAvalible(url.replace(pattern, urlReplace))){
									n++;
								}
							}
							if(n>maxCount)break;
						}
					}	
				}
			}	
		}
		prompt("成功修改了"+n+"个任务。");
		if(!isPaused)engine.resume();	
	}

	private boolean modifyTask(HttpTask t,String proxy,String cookie) {
		boolean flag=false;
		if (t.getState() == TaskState.POSTING || t.getState() == TaskState.PROCESSING) {
			return flag;
		}else{
			if(!StringUtils.isEmpty(proxy)){
				flag=true;
				t.getOptions().setProxy(Proxy.create(proxy));
			}
			if(!StringUtils.isEmpty(cookie)){
				flag=true;
				if("null".equalsIgnoreCase(cookie)){
					t.getOptions().setCookie(null);							
				}else{
					t.getOptions().setCookie(cookie);	
				}
			}
			return flag;	
		}
	}
}
