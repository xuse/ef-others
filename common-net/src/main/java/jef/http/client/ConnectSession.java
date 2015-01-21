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

import jef.concurrent.Listenable;


public abstract class ConnectSession implements Serializable,Listenable{
	private static final long serialVersionUID = 1L;
	transient private int costTime=0; // 下载时间计数，记录下载耗费的时间
	transient protected HttpTask dlTask;
	
	public int getCostTime() {
		return costTime;
	}

	public void setCostTime(int costTime) {
		this.costTime = costTime;
	}

	public abstract String getCompletedPercent();

	public abstract long getCompletedLength();

	public abstract boolean isRunning();

	public abstract String getErrorMessage();
	

	
	protected ConnectSession(HttpTask dlTask){
		this.dlTask=dlTask;
	}

	public void setDLTask(HttpTask dlTask) {
		this.dlTask = dlTask;
	}

	public ConnectOptions getConnectOptions() {
		return dlTask.getOptions();
	}

	public URL getUrl() {
		return dlTask.getUrl();
	}

	public void addLog(String msg){
		dlTask.addLog(msg);
	}

	public abstract String getLength();
}
