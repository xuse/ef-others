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

import jef.common.log.LogUtil;
import jef.tools.Exceptions;

public class ProgressWatcher extends Thread{
	private HttpSession session;
	private int interval=1000;
	private boolean shutdown=false;
	
	public ProgressWatcher(HttpSession session){
		this.session=session;
	}

	public void shutdown(){
		shutdown=true;
	}
	
	private void displayProgress() {
		for(HttpTask t:session.processing.values()){
			LogUtil.show(t.getUrl()+t.getProgress());
		}
	}
	
	//waitings
	
	
	
	public void run() {
		while(!shutdown){
			displayProgress();
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				Exceptions.log(e);
			}
		}
	}
	
	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
}
