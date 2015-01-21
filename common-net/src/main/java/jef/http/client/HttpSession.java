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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import jef.common.JefSerializable;
import jef.common.MessageCollector;
import jef.common.log.FileLogger;
import jef.http.client.HttpTask.TaskState;
import jef.tools.JefConfiguration;

/**
 * 本类是线程安全类。但为了性能考虑，本类中不设置任何同步方法和锁，
 * 所有线程安全由内部类各自实现。因此本类中的公共变量应当全部都是线程安全类。
 * @author Administrator
 */
class HttpSession implements JefSerializable{
	private static final String logPath=JefConfiguration.get(JefConfiguration.Item.HTTP_DOWNLOAD_PATH)+"/httpEngine.log";
	
	private static final long serialVersionUID = 4301372475600241841L;
	
	//队列中任务(线程安全)
    Queue<HttpTask> tasks=new ConcurrentLinkedQueue<HttpTask>();
    //当有多个POST任务使用同一个目标URL时，POST任务无法区分
    //处理中任务(线程安全)
    Map<String, HttpTask> processing=new ConcurrentHashMap<String, HttpTask>();
    //错误任务(线程安全)
    Map<String, HttpTask> errorTasks=new ConcurrentHashMap<String, HttpTask>();
    //已完成任务数(线程安全)
    AtomicInteger finished = new AtomicInteger(0);
    //Waiting(线程安全)
    transient Map<Thread, HttpTask> waitings=new ConcurrentHashMap<Thread, HttpTask>();
    //尚未记录到日志文件中的临时日志,数量不应太大,每次session保存前日志都会写入日志文件，因此不需要序列化保存
    transient MessageCollector memLog=new MessageCollector();;
	
	/**
	 * 保存日志
	 */
	public synchronized void saveLog(){
		if(memLog.isEmpty())return;
		FileLogger fl=new FileLogger(new File(logPath));
		fl.setLogDate(true);
		fl.log(memLog);
		memLog.clear();
	}
	/**
	 * 添加日志
	 * @param msg
	 */
	public synchronized void addLog(String msg){
		memLog.addMessage(msg);
	}

	public void init() {
		waitings=new ConcurrentHashMap<Thread, HttpTask>();
		memLog=new MessageCollector();
	}
	public HttpTask getTask(String id) {
		if(errorTasks.containsKey(id))return errorTasks.get(id);
		if(processing.containsKey(id))return processing.get(id);
		for(HttpTask t: tasks){
			if(t.getId().equals(id)){
				return t;
			}
		}
		return null;
	}
	public synchronized void removeTask(HttpTask t) {
		if(t.getState()==TaskState.QUEUED || t.getState()==TaskState.BREAK){
			tasks.remove(t);
		}else if(t.getState()==TaskState.ERROR){
			errorTasks.remove(t.getId());
		}
	}
	
}
