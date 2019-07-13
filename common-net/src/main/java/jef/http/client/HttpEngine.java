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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import com.google.common.base.Charsets;

import jef.common.Entry;
import jef.common.log.FileLogger;
import jef.common.log.LogUtil;
import jef.http.client.HttpTask.TaskState;
import jef.http.client.support.CommentEntry;
import jef.http.client.support.HttpException;
import jef.http.client.support.Proxy;
import jef.http.client.ui.CookieEditConversation;
import jef.http.client.ui.HostEditConversation;
import jef.http.client.ui.TaskBatchModifyConversation;
import jef.http.client.ui.TaskEditConversation;
import jef.tools.ArrayUtils;
import jef.tools.Assert;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.JefConfiguration;
import jef.tools.StringUtils;
import jef.tools.ThreadUtils;
import jef.tools.algorithm.Sorts;
import jef.tools.string.StringParser;
import jef.ui.ConsoleShell;

/**
 * Http引擎接口类，包含一个任务调度线程。
 * 
 * @author Administrator
 * 
 */
public class HttpEngine {
	public static boolean DEBUG_MODE;
	public static final Set<String> DEBUG_HOSTS=new HashSet<String>();
	
	private static int MAX_INSTANCE_QUT = 12;
	private static int MAX_INSTANCE_PER_HOST = 5;
	private final String DEFAULT_SAVE_PATH = JefConfiguration.get(JefConfiguration.Item.HTTP_DOWNLOAD_PATH, "c:/download");
	private static String SESSION_FILENAME = "http.session";
	
	// 所有的回调对象(使用弱引用，防止内存泄漏)
	Map<String, WeakReference<TaskHandler>> handlers = new ConcurrentHashMap<String, WeakReference<TaskHandler>>();

	private String downloadPath = DEFAULT_SAVE_PATH;
	private static CookieManager cookieManager;
	private HttpSession session;

	private ExecutorService pool = Executors.newCachedThreadPool();// 线程池
	private TaskScheduler scheduler; // 调度器

	public static String getCookie(URL url) {
		if (cookieManager == null)
			return null;
		return cookieManager.getCookie(url);

	}

	public static void setCookie(String host, String cookieStr, boolean isNew) {
		cookieManager.setCookie(host, cookieStr, isNew);
	}

	// 注册一个回调对象
	public void registeCallback(TaskHandler callback) {
		// 清理所有已經失效的callback
		for (Iterator<String> iter = handlers.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			TaskHandler call = handlers.get(key).get();
			if (call == null) {// 弱引用已经失效
				handlers.remove(key);
			}
			if (call == callback)
				throw new IllegalArgumentException("The callback has been registed in name:" + key);
		}

		if (!handlers.containsKey(callback.getName())) {
			handlers.put(callback.getName(), new WeakReference<TaskHandler>(callback));
			if (resumer != null) {
				resumer.getReadyFor(callback.getName());
				startResume();
			}
		}
	}

	public HttpTask[] getErrors(){
		return session.errorTasks.values().toArray(EMPTY);
	}
	
	public HttpTask[] getProcessing(){
		return session.processing.values().toArray(EMPTY);
	}
	
	public HttpTask[] getQueued(){
		return session.tasks.toArray(EMPTY);
	}
	
	/**
	 * 阻塞器。当从磁盘读取上次的任务时，由于回调对象(Callback)并非持久化在磁盘上，此时HttpTask中指向的回调对象即失效。
	 * 因此当启动后，阻塞器就被创建以防止下载任务继续进行，每次有callback被注册时，就检查回调对象条件，一旦满足，阻塞器即消失。
	 */
	ResumeSession resumer;

	class ResumeSession {
		// 记录正在等的任务们
		List<String> waiting = new ArrayList<String>();

		// 构造
		ResumeSession() {
			for (HttpTask t : session.processing.values()) {
				if (t.getCallback() != null) {
					if (!waiting.contains(t.getCallback())) {
						waiting.add(t.getCallback());
					}
				}
			}
			for (HttpTask t : session.tasks) {
				if (t.getCallback() != null) {
					if (!waiting.contains(t.getCallback())) {
						waiting.add(t.getCallback());
					}
				}
			}
			if (!waiting.isEmpty() && DEBUG_MODE) {
				LogUtil.show("上次启动后一些任务的回调对象尚未初始化，这些对象是：" + toString());
			}
		}

		// 是否就绪
		public boolean isReady() {
			return waiting.isEmpty();
		}

		// 移除
		public void getReadyFor(String name) {
			waiting.remove(name);
		}

		
		public String toString() {
			return StringUtils.join(waiting, ",");
		}

	}

	private void startResume() {
		if (resumer == null) {
			startScheduler();
		} else if (resumer.isReady()) {
			if (DEBUG_MODE)
				LogUtil.show("任务所需的回调对象均已获得，任务管理器开始工作.");
			resumer = null;
			for (HttpTask task : session.processing.values()) {
				executeInPool(task);
			}
			startScheduler();
		}
	}

	// 构造
	public HttpEngine() {
		DEBUG_MODE = JefConfiguration.getBoolean(JefConfiguration.Item.HTTP_DEBUG, false);
		// 载入Session和Cookies
		File sessionFile = new File(SESSION_FILENAME);
		try {
			if (sessionFile.exists()) {
				session = (HttpSession) IOUtils.loadObject(sessionFile);
				resumer = new ResumeSession();// 如果是载入Session，创建阻塞器。
			}
		} catch (Exception e) {
			System.err.print("无法载入上次的任务记录文件:" + sessionFile.getAbsolutePath());
			File badFile=new File(sessionFile.getAbsolutePath() + ".bad");
			IOUtils.escapeExistFile(badFile);
			IOUtils.copyFile(sessionFile, badFile);
		}
		if (session == null)
			session = new HttpSession();
		cookieManager = initCookieManager();
		startResume();
	}

	// 从文件中载入Cookies
	private static CookieManager initCookieManager() {
		File file = new File("Cookies.manager");
		CookieManager c = null;
		if (file.exists()) {
			c = (CookieManager) IOUtils.loadObject(file);
		}
		if (c == null) {
			c = new CookieManager();
		}
		return c;
	}

	/**
	 * 同步接口: 下载一个文件
	 */
	public File requestFile(int threadQut, String url, String filepath, ConnectOptions option, CommentEntry... comment) throws HttpException {
		HttpTask task = new HttpTask(threadQut, url, ReturnType.FILE, filepath, option);
		if (comment.length > 0)
			task.setComment(comment);
		session.waitings.put(Thread.currentThread(), task);
		executeInPool(task);
		while (isTaskRunning(task)) {
			ThreadUtils.doWait(task);
		}
		session.waitings.remove(Thread.currentThread());
		if (task.getReturnType().equals(ReturnType.ERROR_MESSAGE)) {
			throw new HttpException((String) task.getReturnObj());
		}
		return (File) task.getReturnObj();
	}

	/**
	 * 同步下载，返回数据流
	 */
	public InputStream requestStream(int threadQut, String url, ConnectOptions option) throws HttpException {
		HttpTask task = new HttpTask(threadQut, url, ReturnType.STREAM, null, option);
		session.waitings.put(Thread.currentThread(), task);
		executeInPool(task);
		while (isTaskRunning(task)) {
			ThreadUtils.doWait(task);
		}
		session.waitings.remove(Thread.currentThread());
		if (task.getReturnType().equals(ReturnType.ERROR_MESSAGE)) {
			throw new HttpException((String) task.getReturnObj());
		}
		return (InputStream) task.getReturnObj();
	}

	/**
	 * 同步下载，返回XML document
	 */
	public Document requestXML(String url, ConnectOptions option) throws HttpException {
		HttpTask task = new HttpTask(1, url, ReturnType.XML_DOC, null, option);
		session.waitings.put(Thread.currentThread(), task);
		executeInPool(task);
		while (isTaskRunning(task)) {
			ThreadUtils.doWait(task);
		}
		session.waitings.remove(Thread.currentThread());
		if (task.getReturnType().equals(ReturnType.ERROR_MESSAGE)) {
			throw new HttpException((String) task.getReturnObj());
		}
		return (Document) task.getReturnObj();
	}

	/**
	 * 同步下载，返回HTMLDoc
	 */
	public DocumentFragment requestHTML(String url, ConnectOptions option) throws HttpException {
		HttpTask task = new HttpTask(1, url, ReturnType.HTML_DOC, null, option);
		session.waitings.put(Thread.currentThread(), task);
		executeInPool(task);
		while (isTaskRunning(task)) {
			ThreadUtils.doWait(task);
		}
		session.waitings.remove(Thread.currentThread());
		if (task.getReturnType().equals(ReturnType.ERROR_MESSAGE)) {
			throw new HttpException((String) task.getReturnObj());
		}
		return (DocumentFragment) task.getReturnObj();
	}

	/**
	 * 同步下载，返回String对象
	 */
	public String requestString(String url, ConnectOptions option) throws HttpException {
		HttpTask task = new HttpTask(1, url, ReturnType.STRING, null, option);
		session.waitings.put(Thread.currentThread(), task);
		executeInPool(task);
		while (isTaskRunning(task)) {
			ThreadUtils.doWait(task);
		}
		session.waitings.remove(Thread.currentThread());
		if (task.getReturnType().equals(ReturnType.ERROR_MESSAGE)) {
			throw new HttpException((String) task.getReturnObj());
		}
		return (String) task.getReturnObj();
	}

	/**
	 * 同步下载，返回自动生成的对象 返回对象根据MIME-Type生成 text/html HTML_DOC text/xml XML_DOC text
	 * String Others STREAM
	 */
	public Entry<ReturnType, Object> request(String url, ConnectOptions option) throws HttpException {
		HttpTask task = new HttpTask(1, url, ReturnType.DEFAULT, null, option);
		session.waitings.put(Thread.currentThread(), task);
		executeInPool(task);
		while (isTaskRunning(task)) {
			ThreadUtils.doWait(task);
		}
		session.waitings.remove(Thread.currentThread());
		if (task.getReturnType().equals(ReturnType.ERROR_MESSAGE)) {
			throw new HttpException((String) task.getReturnObj()+"URL:"+url);
		}
		return new Entry<ReturnType, Object>(task.getReturnType(), task.getReturnObj());
	}

	/**
	 * 异步接口,添加一个任务
	 */
	public HttpTask submitTask(int threadQut, String url, ReturnType type, ConnectOptions option) {
		return submitTask(threadQut, url, type, option, null);
	}

	/**
	 * 异步接口，添加一个任务
	 */
	public HttpTask submitTask(int threadQut, String url, ReturnType type, ConnectOptions option, TaskHandler callback, CommentEntry... comments) {
		if (callback != null && !handlers.containsKey(callback.getName())) {
			this.registeCallback(callback);
		}
		HttpTask task = new HttpTask(threadQut, url, type, null, option);
		if (callback != null)
			task.setCallback(callback.getName());
		task.setComment(comments);
		session.tasks.add(task);
		if (DEBUG_MODE) {
			LogUtil.show("任务添加：" + ((comments == null) ? "" : comments + "  ") + url);
		}
		if (scheduler.getState().equals(Thread.State.TERMINATED)) {
			startScheduler();
		} else {
			ThreadUtils.doNotify(this);
		}
		return task;
	}

	/**
	 * 异步接口: 下载一个文件，可以指定下载路径；
	 * 
	 * @param filepath
	 *            :如果是以\/结尾，或者是一个已经存在的文件夹，文件下载到文件夹中，反之就认为是文件，直接存为该文件。
	 */
	public void downloadFile(int threadQut, String url, String filepath, ConnectOptions option, TaskHandler callback, CommentEntry... comments) {
		if (callback != null && !handlers.containsKey(callback.getName())) {
			this.registeCallback(callback);
		}
		HttpTask task = new HttpTask(threadQut, url, ReturnType.FILE, filepath, option);
		if (callback != null)
			task.setCallback(callback.getName());
		if (comments.length > 0)
			task.setComment(comments);
		session.tasks.add(task);
		if (DEBUG_MODE) {
			LogUtil.show("任务添加：" + url);
		}
		if (scheduler.getState().equals(Thread.State.TERMINATED)) {
			startScheduler();
		} else {
			ThreadUtils.doNotify(this);
		}
	}

	// 启动任务调度器
	private void startScheduler() {
		if (resumer != null) {// 由于状态没有回复，所以不能启动
			LogUtil.show("警告：由于以下回调对象没有注册，任务管理器不能启动" + resumer.toString());
			return;
		}
		if (scheduler == null || scheduler.getState() == Thread.State.TERMINATED) {
			scheduler = new TaskScheduler();
			scheduler.start();
		} else {
			System.err.print("thread state is " + scheduler.getState() + "can't restart.");
		}
	}

	void executeInPool(Runnable thread) {
		if (thread instanceof HttpTask) {
			((HttpTask) thread).setEngine(this);
		}
		pool.execute(thread);
	}

	public enum ClearType {
		ALL, QUEUED, ERROR
	}

	/**
	 * 清除全部任务
	 */
	public void clearTask(ClearType clearType) {
		if (clearType == ClearType.QUEUED) {
			session.tasks.clear();
		} else if (clearType == ClearType.ERROR) {
			session.errorTasks.clear();
		} else {
			session.tasks.clear();
			session.errorTasks.clear();
			session.finished = new AtomicInteger(0);
		}
	}

	/**
	 * 暂停任务运行
	 */
	public void pause() {
		scheduler.pause = true;
		LogUtil.show("HttpEngine任务队列已暂停。");
	}

	/**
	 * 恢复任务运行
	 */
	public void resume() {
		scheduler.pause = false;
		ThreadUtils.doNotify(this);
		LogUtil.show("HttpEngine任务队列已运行。");
	}

	/**
	 * 将任务列表导入到指定文件中
	 * 
	 * @param location
	 * @throws IOException
	 */
	public void exportFile(String fileName,boolean isCsv,String... options) throws IOException {
		boolean simple = !isCsv && !ArrayUtils.contains(options, "csv");// 是否使用简易模式
		if (!simple && !fileName.toLowerCase().endsWith(".csv")) {
			fileName = fileName.concat(".csv");
		}
		File file = new File(fileName);
		if (file.exists() && file.isDirectory()) {
			file = IOUtils.escapeExistFile(file);
		}
		int n;
		BufferedWriter w = IOUtils.getWriter(file,Charsets.UTF_8, false);
		if (!simple) {
			w.write(TaskDub.getCSVHeader());
			w.write(StringUtils.CRLF_STR);
		}
		if (ArrayUtils.contains(options, "error")) {// 导出错误
			n=exportErr(w, simple);
		} else if (ArrayUtils.contains(options, "queue")) {// 导出队列和运行中的
			n=exportQueue(w, simple);
		} else if (ArrayUtils.contains(options, "urllike")) {// 导出队列和运行中的
			n = ArrayUtils.indexOf(options, "urllike");
			String key = options[n + 1];
			n=exportCustom(w, key, simple);
		} else {// 导出全部All
			n=exportErr(w, simple);
			n+=exportQueue(w, simple);
		}
		w.close();
		LogUtil.show("导出了"+n+"个任务到"+file.getAbsolutePath());
	}

	private int exportCustom(BufferedWriter w, String key, boolean simple) throws IOException {
		int n=0;
		if (simple) {
			w.write("=== Queued ===");
			w.write(StringUtils.CRLF_STR);
			for (HttpTask t : session.tasks) {
				if (StringUtils.contains(t.getUrl().toString(), key, false)) {
					w.write(t.getUrl().toString());
					w.write(StringUtils.CRLF_STR);
					n++;
				}
			}
			w.write("=== Processing ===");
			w.write(StringUtils.CRLF_STR);
			for (HttpTask t : session.processing.values()) {
				if (StringUtils.contains(t.getUrl().toString(), key, false)) {
					w.write(t.getUrl().toString());
					w.write(StringUtils.CRLF_STR);
					n++;
				}
			}
			w.write("=== Errors ===");
			w.write(StringUtils.CRLF_STR);
			for (HttpTask t : session.errorTasks.values()) {
				if (StringUtils.contains(t.getUrl().toString(), key, false)) {
					w.write(t.getUrl().toString());
					w.write(StringUtils.CRLF_STR);
					n++;
				}
			}
		} else {
			w.write("=== Queued ===");
			w.write(StringUtils.CRLF_STR);
			for (HttpTask t : session.tasks) {
				if (StringUtils.contains(t.getUrl().toString(), key, false)) {
					exportTask(w, t);
					n++;
				}
			}
			w.write("=== Processing ===");
			w.write(StringUtils.CRLF_STR);
			for (HttpTask t : session.processing.values()) {
				if (StringUtils.contains(t.getUrl().toString(), key, false)) {
					exportTask(w, t);
					n++;
				}
			}
			w.write("=== Errors ===");
			w.write(StringUtils.CRLF_STR);
			for (HttpTask t : session.errorTasks.values()) {
				if (StringUtils.contains(t.getUrl().toString(), key, false)) {
					exportTask(w, t);
					n++;
				}
			}
		}
		return n;
	}

	private int exportQueue(BufferedWriter w, boolean simple) throws IOException {
		int n=0;
		if (simple) {
			w.write("=== Queued ===");
			w.write(StringUtils.CRLF_STR);
			for (HttpTask t : session.tasks) {
				w.write(t.getUrl().toString());
				w.write(StringUtils.CRLF_STR);
				n++;
			}
			w.write("=== Processing ===");
			w.write(StringUtils.CRLF_STR);
			for (HttpTask t : session.processing.values()) {
				w.write(t.getUrl().toString());
				w.write(StringUtils.CRLF_STR);
				n++;
			}
		} else {
			w.write("=== Queued ===");
			w.write(StringUtils.CRLF_STR);
			for (HttpTask t : session.tasks) {
				exportTask(w, t);
				n++;
			}
			w.write("=== Processing ===");
			w.write(StringUtils.CRLF_STR);
			for (HttpTask t : session.processing.values()) {
				exportTask(w, t);
				n++;
			}
		}
		return n;
	}

	private int exportErr(BufferedWriter w, boolean simple) throws IOException {
		w.write("=== Errors ===");
		w.write(StringUtils.CRLF_STR);
		int n=0;
		if (simple) {
			for (HttpTask t : session.errorTasks.values()) {
				w.write(t.getUrl().toString());
				w.write(StringUtils.CRLF_STR);
				n++;
			}
		} else {
			for (HttpTask t : session.errorTasks.values()) {
				exportTask(w, t);
				n++;
			}
		}
		return n;
	}

	private static void exportTask(BufferedWriter w, HttpTask t) throws IOException {
		TaskDub td = TaskDub.getFromTask(t);
		w.write(td.toCSVString());
		w.write(StringUtils.CRLF_STR);
	}

	/**
	 * 从指定的文件中载入下载列表
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void importFile(File file) throws IOException {
		BufferedReader r = IOUtils.getReader(file, "UTF-8");
		String str;
		int modCount = 0;
		int newCount = 0;
		int skipCount = 0;
		int delCount = 0;
		List<String> errs = new ArrayList<String>();
		List<String> locks = new ArrayList<String>();
		while ((str = r.readLine()) != null) {
			if (str.startsWith("#") || str.startsWith("=") || str.startsWith("-")) {
				continue;
			}
			TaskDub dub = TaskDub.load(str);
			if(dub==null)continue;
			if (StringUtils.isEmpty(dub.getId())) {
				ConnectOptions option=new ConnectOptions();
				option.setProxy(Proxy.create(dub.getProxy()));
				if(StringUtils.isNotEmpty(dub.getDupOption()))option.setDupFileOption(dub.getDupOption());
				if(StringUtils.isNotEmpty(dub.getReference()))option.setReference(dub.getReference());
				String path=dub.getFilePath()+"\\";
				downloadFile(StringUtils.toInt(dub.getThreadCount(),1), dub.url, path, option, null);
				newCount++;
			} else if ("D".equals(dub.state) || "R".equals(dub.state)||"REMOVE".equals(dub.state)) {
				HttpTask t = session.getTask(dub.getId());
				if (t == null) {
					errs.add(str);
				} else if (t.getState() == TaskState.POSTING || t.getState() == TaskState.PROCESSING) {
					locks.add(str);
				} else {
					session.removeTask(t);
					delCount++;
				}
			} else {
				HttpTask t = session.getTask(dub.getId());
				if (t == null) {
					errs.add(str);
				} else if (t.getState() == TaskState.POSTING || t.getState() == TaskState.PROCESSING) {
					locks.add(str);
				} else {
					if (dub.modifyTask(t)) {
						modCount++;
					} else {
						skipCount++;
					}
				}
			}
		}
		r.close();
		LogUtil.show(newCount + "个任务已加载。" + modCount + "任务修改成功，" + delCount + "个被删除，" + skipCount + "任务没有任何变化。");
		if (errs.size() > 0) {
			LogUtil.show(errs.size() + "个任务无法修改，因为任务已经完成或被删除。");
			LogUtil.show(locks);
		}
		if (errs.size() > 0) {
			LogUtil.show(locks.size() + "个任务无法修改/删除，因为任务正在运行中");
			LogUtil.show(locks);
		}
	}

	public enum Action {
		BREAK, CONTINUE, DELETE_QUEUED, RETRY, DELETE_ERROR, DELETE_ERR_BY_TYPE,RETRY_FORCE
	}

	// 针对任务批量处理
	private int taskProcess(String userExp, Action action,FileLogger fg) {
		boolean isAll = userExp.equalsIgnoreCase("ALL");
		int count = 0;
		List<String> strList=new ArrayList<String>();
		if (action == Action.BREAK) {
			for (HttpTask t : session.processing.values()) {
				if (t.getState() == TaskState.PROCESSING) {
					if (isAll||matchUrl(userExp,  t.getUrl().toString())) {
						strList.add(TaskDub.getFromTask(t).toCSVString());
						t.isBreak = -1;
						count++;
					}
				}
			}
		} else if (action == Action.CONTINUE) {
			for (HttpTask t : session.tasks) {
				if (t.getState() == TaskState.BREAK) {
					if (isAll||matchUrl(userExp,  t.getUrl().toString())) {
						strList.add(TaskDub.getFromTask(t).toCSVString());
						t.setState(TaskState.QUEUED);
						count++;
					}
				}
			}
		} else if (action == Action.DELETE_QUEUED) {
			for (Iterator<HttpTask> iter = session.tasks.iterator(); iter.hasNext();) {
				HttpTask t = iter.next();
				if (isAll||matchUrl(userExp, t.getUrl().toString())) {
					strList.add(TaskDub.getFromTask(t).toCSVString());
					iter.remove();
					count++;
				}
			}
		} else if (action == Action.DELETE_ERROR) {
			for (Iterator<String> iter = session.errorTasks.keySet().iterator(); iter.hasNext();) {
				String key=iter.next();
				HttpTask t = session.errorTasks.get(key);
				if (isAll||matchUrl(userExp, t.getUrl().toString())) {
					strList.add(TaskDub.getFromTask(t).toCSVString());
					session.errorTasks.remove(key);
					count++;
				}
			}
		} else if (action == Action.DELETE_ERR_BY_TYPE) {
			if(StringUtils.isNumeric(userExp))userExp="HTTP:"+userExp;
			for (Iterator<String> iter = session.errorTasks.keySet().iterator(); iter.hasNext();) {
				String key=iter.next();
				HttpTask t = session.errorTasks.get(key);
				if(isAll){
					strList.add(TaskDub.getFromTask(t).toCSVString());
					session.errorTasks.remove(key);
					count++;
				}else if (t.getReturnType()==ReturnType.ERROR_MESSAGE) {
					String errMsg=(String)t.getReturnObj();
					if(errMsg.contains(userExp)){
						strList.add(TaskDub.getFromTask(t).toCSVString());
						session.errorTasks.remove(key);
						count++;
					}
				}
			}
		} else if (action == Action.RETRY_FORCE) {
			for (Iterator<String> iter = session.errorTasks.keySet().iterator(); iter.hasNext();) {
				String key=iter.next();
				HttpTask t = session.errorTasks.get(key);
				if (isAll||matchUrl(userExp,  t.getUrl().toString())) {
					strList.add(TaskDub.getFromTask(t).toCSVString());
					session.errorTasks.remove(key);
			    	t.reset(true);
			    	session.tasks.add(t);
					count++;
				}
			}
		} else if (action == Action.RETRY) {
			for (Iterator<String> iter = session.errorTasks.keySet().iterator(); iter.hasNext();) {
				String key=iter.next();
				HttpTask t = session.errorTasks.get(key);
				if (isAll||matchUrl(userExp,  t.getUrl().toString())) {
					strList.add(TaskDub.getFromTask(t).toCSVString());
					session.errorTasks.remove(key);
			    	t.reset(false);
			    	session.tasks.add(t);
					count++;
				}
			}
		} else {
			throw new UnsupportedOperationException("Unknow operation");
		}
		LogUtil.show("已经执行"+action.name()+"操作，处理了"+count+"个任务。");
		if(fg!=null){
			fg.log(strList);
			LogUtil.show("处理记录位于:"+ fg.getFile().getAbsolutePath());
		}
		return count;
	}

	private static boolean matchUrl(String pattern,  String url) {
		return StringUtils.contains(url, pattern, true);
	}

	/**
	 * 执行指定的命令,包括 clear [queued/all/error]| export | import | cookie |cookiesave|cookieload| cc
	 * | pause | resume shutdown | start | set max | set perhost | debug |
	 * progress
	 */
	public void doVerb(String verb,ConsoleShell shell) {
		String[] text = verb.split(" ");
		if (text[0].equals("clear")) {// 用于清除类内容
			Assert.equals(text.length, 2, "参数不正确。");
			Assert.isEnumOf(text[1].toUpperCase(), ClearType.class, "参数无效。");
			this.clearTask(ClearType.valueOf(text[1].toUpperCase()));
		} else if (text[0].equals("export")) {// 用于导出某类内容
			Assert.isTrue(text.length > 1, "参数不正确。");
			try {
				exportFile(text[1], false,ArrayUtils.subArray(text, 2, text.length));
			} catch (IOException e) {
				Exceptions.log(e);
			}
		} else if (text[0].equals("csv")) {// 用于导出某类内容效果等同于 export csv
			Assert.isTrue(text.length > 1, "参数不正确。");
			try {
				exportFile(text[1],true, ArrayUtils.subArray(text, 2, text.length));
			} catch (IOException e) {
				Exceptions.log(e);
			}
		} else if (text[0].equals("import")) {// 用于导入某类内容
			Assert.isTrue(text.length > 1, "参数不正确。");
			try {
				File file=new File(text[1]);
				if(!file.exists())file=new File(text[1]+".csv");
				if(file.exists())importFile(file);
			} catch (IOException e) {
				Exceptions.log(e);
			}
		} else if (text[0].equals("break")) {// 强行停止任务，并排到队尾 (任务范围语法)
			Assert.isTrue(text.length > 1, "参数不正确。");
			if(text.length>2){
				FileLogger fg=new FileLogger(new File(text[2]));
				this.taskProcess(text[1], Action.BREAK,fg);
			}else{
				this.taskProcess(text[1], Action.BREAK,null);
			}
		} else if (text[0].equals("continue")) {// 将处于break状态的任务恢复(任务范围语法)
			Assert.isTrue(text.length > 1, "参数不正确。");
			if(text.length>2){
				FileLogger fg=new FileLogger(new File(text[2]));
				this.taskProcess(text[1], Action.CONTINUE,fg);
			}else{
				this.taskProcess(text[1], Action.CONTINUE,null);	
			}
		} else if (text[0].equals("dq")) { // 将任务删除(任务范围语法)
			Assert.isTrue(text.length > 1, "参数不正确。");
			if(text.length>2){
				FileLogger fg=new FileLogger(new File(text[2]));
				this.taskProcess(text[1], Action.CONTINUE,fg);
			}else{
				this.taskProcess(text[1], Action.DELETE_QUEUED,null);	
			}
		} else if (text[0].equals("de")) { // 将任务删除(任务范围语法)
			Assert.isTrue(text.length > 1, "参数不正确。");
			if(text.length>2){
				FileLogger fg=new FileLogger(new File(text[2]));
				this.taskProcess(text[1], Action.DELETE_ERROR,fg);
			}else{
				this.taskProcess(text[1], Action.DELETE_ERROR,null);	
			}
		} else if (text[0].equals("dr")) { // 将任务删除(任务范围语法)
			Assert.isTrue(text.length > 1, "参数不正确。");
			if(text.length>2){
				FileLogger fg=new FileLogger(new File(text[2]));
				this.taskProcess(text[1], Action.DELETE_ERR_BY_TYPE,fg);
			}else{
				this.taskProcess(text[1], Action.DELETE_ERR_BY_TYPE,null);	
			}
		} else if (text[0].equals("retry")) {//任务重试
			Assert.isTrue(text.length > 1, "参数不正确。");
			if(text.length>2){
				FileLogger fg=new FileLogger(new File(text[2]));
				if(this.taskProcess(text[1], Action.RETRY,fg)>0){
					ThreadUtils.doNotify(this);
				}
			}else{
				if(this.taskProcess(text[1], Action.RETRY,null)>0)
					ThreadUtils.doNotify(this);
			}
		} else if (text[0].equals("retryf")) {//任务重试
			Assert.isTrue(text.length > 1, "参数不正确。");
			if(text.length>2){
				FileLogger fg=new FileLogger(new File(text[2]));
				if(this.taskProcess(text[1], Action.RETRY_FORCE,fg)>0){
					ThreadUtils.doNotify(this);
				}
			}else{
				if(this.taskProcess(text[1], Action.RETRY_FORCE,null)>0)
					ThreadUtils.doNotify(this);
			}
		} else if (text[0].equals("cookie")) {//设置Cookie
			if (text.length > 2) {// 设置Cookie
				StringBuilder sb=new StringBuilder();
				sb.append(text[2]);
				for(int i=3;i<text.length;i++){
					sb.append(" ").append(text[i]);
				}
				String str=sb.toString();
				cookieManager.setCookie(text[1], str, true);
				LogUtil.show("已经设置" + text[1] + " Cookie:" + str);
			} else {
				LogUtil.show("=== Cookies(" + cookieManager.size() + ") ===");
				for (String domain : cookieManager.domains()) {
					LogUtil.show("HOST: "+cookieManager.getDomainCookie(domain).toString());
					LogUtil.show("");
				}
			}
		} else if (text[0].equals("cc")) {// 清除某个host的Cookie
			Assert.equals(text.length, 2, "参数不正确。");
			if ("all".equals(text[1])) {
				cookieManager.clearCookies();
			} else {
				if(!cookieManager.removeCookie(text[1])){
					LogUtil.show("没有找到站点的Cookie:" + text[1]);
				} 
			}
			if (cookieManager.isChanged) {
				IOUtils.saveObject(cookieManager, new File("Cookies.manager"));
				cookieManager.isChanged = false;
			}
		} else if (text[0].equals("cookiesave")) {// 保存成XML
			File file=cookieManager.saveXml();
			LogUtil.show("Cookies saved as "+file.getAbsolutePath());
		} else if (text[0].equals("cookieload")) {// 从XML中载入
			cookieManager.loadXml();
			cookieManager.isChanged = true;
		} else if (text[0].equals("info")) {
			text=(String[]) ArrayUtils.toFixLength(text,2);
			this.showProcessing(false);
			this.showQueInfo(true, text[1]);
			showSummary();
		} else if (text[0].equals("err")) {
			text=(String[]) ArrayUtils.toFixLength(text,3);
			showProcessing(false);
			showErrInfo(false,text[1],text[2]);
			showSummary();
		} else if (text[0].equals("error")) {
			text=(String[]) ArrayUtils.toFixLength(text,3);
			showProcessing(false);
			showErrInfo(true,text[1],text[2]);
			showSummary();
		} else if (text[0].equals("pause")) {
			this.pause();
		} else if (text[0].equals("resume")) {
			this.resume();
		} else if (text[0].equals("shutdown") || text[0].equals("stop")) {
			this.shutDown(SHUTDOWN_WAIT);
		} else if (text[0].equals("start")) {
			this.startScheduler();
		} else if (text[0].equals("set")) {
			Assert.equals(text.length, 3, "参数不正确。");
			if (text[1].equalsIgnoreCase("max")) {
				MAX_INSTANCE_QUT = StringUtils.toInt(text[2], MAX_INSTANCE_QUT);
			} else if (text[1].equalsIgnoreCase("perhost")) {
				MAX_INSTANCE_PER_HOST = StringUtils.toInt(text[2], MAX_INSTANCE_PER_HOST);
			} else {
				LogUtil.show("未知参数:" + text[1]);
			}
		} else if (text[0].equals("debug")) {// 开启/关闭Debug 选项
			if(text.length==1){
				DEBUG_MODE = !DEBUG_MODE;
				LogUtil.show(DEBUG_MODE ? "调试开启" : "调试关闭");	
			}else if(text[1].equals("clear")){
				DEBUG_HOSTS.clear();
				LogUtil.show("Debug Hosts Cleared.");
			}else{
				DEBUG_HOSTS.add(text[1]);
				LogUtil.show("Debug Hosts:");
				LogUtil.show(DEBUG_HOSTS);
			}
		} else if (text[0].equals("hostsave")) {// 保存站点信息
			try {
				HostManager.saveToFile(null);
			} catch (IOException e) {
				LogUtil.show(e);
			}
		} else if (text[0].equals("hostload")) {//载入站点信息
			try {
				HostManager.loadFromFile(null);
			} catch (IOException e) {
				LogUtil.show(e);
			}
		} else if (text[0].equals("host")) {// 显示站点信息
			LogUtil.show("==== Host Profile ====");
			HostProfile[] c = HostManager.getAll().values().toArray(new HostProfile[] {});
			Sorts.sort(c, 1);
			for (HostProfile host : c) {
				LogUtil.show(host.toString());
			}
			LogUtil.show("共计" + c.length + "个站点配置。");
		} else if (text[0].equals("modify")) {//批量设置会话
			TaskBatchModifyConversation c=new TaskBatchModifyConversation(shell,this);
			c.start();
		} else if (text[0].endsWith("edit")) {//单个设置会话
			startConversation(text[0], shell);
		} else {
			throw new IllegalArgumentException("Unknown command " + verb);
		}
	}

	public void startConversation(String type, ConsoleShell shell){
		if(type.equals("hostedit")){
			HostEditConversation c=new HostEditConversation(shell);
			c.start();
		}else if (type.equals("cookieedit")){
			CookieEditConversation c=new CookieEditConversation(shell,cookieManager);
			c.start();
		}else if (type.equals("taskedit")){
			TaskEditConversation c=new TaskEditConversation(shell,false,this);
			c.start();
		}else if (type.equals("erroredit")){
			TaskEditConversation c=new TaskEditConversation(shell,true,this);
			c.start();
		}else{
			throw new IllegalArgumentException("Unknown Conversation Name: "+ type);
		}
		
	}
	
	@SuppressWarnings("unused")
	private Object threadRapper=new Object(){
		
		protected void finalize() throws Throwable {
			if(scheduler.shutdown == WORKING)shutDown(SHUTDOWN_IMMEDIATELY);
		}
		
	};
	/**
	 * 要求关闭任务， 调度器将会等待目前运行中的任务全部完成，并且将任务队列保存到磁盘后才关闭 下次启动后将能够继续进行，因此这个是比较安全的做法。
	 */
	public void shutDown(int mode) {
		scheduler.shutdown = mode;
		ThreadUtils.doNotify(this);
		if (mode == SHUTDOWN_IMMEDIATELY) {
			pool.shutdownNow();
		}
		// 提示需要等待
		if (session.processing.size() > 0) {
			LogUtil.show("目前还有" + session.processing.size() + "任务在下载。请稍侯…");
		}
		//scheduler.saveSession();// 保存队列

		// 方法要求等待返回的场合
		if (mode == SHUTDOWN_WAIT) {
			int lastProcessing = session.processing.size();
			while (session.processing.size() > 0) {
				try {
					Thread.sleep(1000);// 每等1秒检查一次运行队列，如果不为始终等待
				} catch (InterruptedException e) {
					Exceptions.log(e);
				}
				if (lastProcessing > session.processing.size()) {
					LogUtil.show("还有" + session.processing.size() + "任务在下载…");
					lastProcessing = session.processing.size();
				}
			}
		}
		pool.shutdown();// shutdown立刻返回，但是线程池会等待到当前线程结束再结束,
						// 因此需要通过回调方法来监控线程池中的执行中线程数量
	}

	public State getState() {
		if (scheduler == null) {
			return State.WAITING;
		}
		return scheduler.getState();
	}

	protected static final int WORKING = 0;// 立刻关闭
	public static final int SHUTDOWN_SAFE = 1;// 方法立刻返回，等待现有任务完成关闭
	public static final int SHUTDOWN_WAIT = 2;// 方案不返回，等待现有任务完成后再返回
	public static final int SHUTDOWN_IMMEDIATELY = 3;// 立刻关闭

	/**
	 * 守护线程，负责调度任务
	 */
	class TaskScheduler extends Thread {
		public int shutdown = WORKING;
		public boolean pause = false;
		long lastSaveTime=0;

		
		public void run() {
			while (true) {
				boolean flag = checkFinishedTask();
				while (pause && shutdown == WORKING) {// 如果尚未关闭并且暂停
					ThreadUtils.doWait(HttpEngine.this);
					flag = checkFinishedTask();
				}
				if (session.tasks.size() > 0 && shutdown == WORKING) { // 如果未要求关闭,且有队列任务，财添加新任务
					if (session.processing.size() < MAX_INSTANCE_QUT) {// 限制最大并发任务数
						// 得到所有达到上限的站点；
						HttpTask task = null;
						String[] limitedSites = getLimitedSites();
						for (Iterator<HttpTask> iter = session.tasks.iterator(); iter.hasNext();) {
							task = iter.next();
							if (task.getState() == TaskState.BREAK)
								continue;
							if (!ArrayUtils.contains(limitedSites, task.getUrl().getHost())) {
								iter.remove();
								flag = true;
								session.processing.put(task.getId(), task);
								executeInPool(task);
								if (DEBUG_MODE) {
									LogUtil.show("任务执行：" + ((task.getComment() == null) ? "" : StringUtils.join(task.getComment(), ",")+ "  ") + task.getUrl().toString());
								}
								break;
							}
						}
					}
				}
				if (shutdown == SHUTDOWN_IMMEDIATELY) {// 立刻关闭
					saveSession();
					break;
				} else if (shutdown != WORKING && session.processing.size() == 0) {// 常规关闭，直到下载中的任务全部完成才会真正关闭
					saveSession();
					break;
				}
				if (!flag) {
					if(System.currentTimeMillis()-lastSaveTime>60000){
						saveSession();	
					}
					ThreadUtils.doWait(HttpEngine.this);
				}
			}
		}
		
		private synchronized void saveSession() {
			lastSaveTime=System.currentTimeMillis();
			File sessionFile = new File(SESSION_FILENAME);
			session.saveLog();
			boolean flag=renameSessionFiles(sessionFile,0);
			if(flag==false){
				LogUtil.show("任务文件备份失败。");
			}
			IOUtils.saveObject(session, sessionFile);
			if (cookieManager != null && cookieManager.isChanged) {
				cookieManager.save();
			}
		}

		private boolean renameSessionFiles(File file, int i) {
			StringBuilder sb=new StringBuilder();
			if(i==0){
				sb.append(file.getAbsolutePath()).append(".bk").append(i);	
			}else{
				sb.append(IOUtils.removeExt(file.getAbsolutePath())).append(".bk").append(i);
			}
			File toFile=new File(sb.toString());
			if(file.exists()){
				if(toFile.exists()){
					if(i>2){
						boolean flag=toFile.delete();
						//LogUtil.show("deleteing " + toFile.getAbsolutePath()+" "+flag);
						if(!flag)return false;
					}else{
						renameSessionFiles(toFile,i+1);
					}
				}
				boolean flag=file.renameTo(toFile);
				//LogUtil.show("rename " +file.getAbsolutePath()+" to "+ toFile.getAbsolutePath()+"  "+flag);
				if(!flag)return false;
			}
			return true;
		}

		// 计算哪些站点已经达到了下载任务的上限
		private String[] getLimitedSites() {
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			for (String id : session.processing.keySet()) {
				URL url = session.processing.get(id).getUrl();
				Integer n = map.get(url.getHost());
				if (n == null) {
					map.put(url.getHost(), new Integer(1));
				} else {
					map.put(url.getHost(), n + 1);
				}
			}
			for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
				String url = it.next();
				Integer n = map.get(url);
				HostProfile profile = HostManager.getHost(url);
				if (profile != null) {
					if (n < profile.getMaxCount()) {
						it.remove();
					}
				} else {
					if (n < MAX_INSTANCE_PER_HOST) {
						it.remove();
					}
				}
			}
			return map.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
		}

		// 检查并移除完成的任务
		private boolean checkFinishedTask() {
			boolean findEndTask = false;
			for (HttpTask task : session.processing.values()) {
				if (!isTaskRunning(task)) {
					session.processing.remove(task.getId());
					findEndTask = true;
					if (task.getState() == TaskState.BREAK) {
						session.tasks.add(task);
						continue;
					}
					TaskHandler callback = getCallback(task.getCallback());
					if (task.getState().equals(TaskState.ERROR)) {
						session.errorTasks.put(task.getId(), task);
						if (callback != null)
							callback.doFailure(task);
					} else {
						session.finished.incrementAndGet();// 计数器+1
						if (DEBUG_MODE) {
							LogUtil.show("任务完成：" + ((task.getComment() == null) ? "" : task.getComment() + "  ") + task.getUrl().toString());
						}
						if (callback != null)
							callback.doFinished(task);
					}

					if (session.processing.size() == 0 && session.tasks.isEmpty()) {
						LogUtil.show("下载队列中的任务均已完成。");
						saveSession();
					}
				}
			}
			return findEndTask;
		}

		private TaskHandler getCallback(String callname) {
			TaskHandler callback = null;
			if (callname != null) {
				callback = handlers.get(callname).get();
				if (callback == null)
					LogUtil.show("ID为:" + callname + "的回调任务没有注册！");
			}
			return callback;
		}
	}

	private static boolean isTaskRunning(HttpTask task) {
		return ArrayUtils.contains(HttpTask.RUNNING, task.getState());
	}

	HttpTask[] EMPTY=new HttpTask[0];
	
	public void showSummary(){
		LogUtil.show("=================================================");
		LogUtil.show("请求中:" + session.waitings.size() + "  下载中:" + getRunningTaskCount() + "  队列中:" + getQueuedTaskCount() + "  已完成:" + getFinishedTaskCount() + "  下载失败:" + getErrorTaskCount() + (isPause() ? "  任务暂停" : "  任务运行"));
	}
	/**
	 * 在控制台上显示任务摘要
	 */
	public void showProcessing(boolean complete) {
		if (session.processing.size() > 0) {
			LogUtil.show("=== 下载中 ===");
			for (HttpTask task : session.processing.values()) {
				String urlShow = (complete)?task.getUrl().toString():StringParser.omitCentret(task.getUrl().toString(), 63);
				LogUtil.show(task.getProgress() + " " + task.getLength() + " " + task.getThreadQut() + " " + urlShow);
			}
		}
		if (session.waitings.size() > 0) {
			LogUtil.show("=== 请求执行中 ===");
			for (Thread thread : session.waitings.keySet()) {
				HttpTask task = session.waitings.get(thread);
				LogUtil.show(thread.getId() + ":" + thread.getName());
				LogUtil.show(task.getUrl() + " " + task.getProgress());
			}
		}
		//ThreadUtils.doNotify(this);
	}
	
	/**
	 * 在控制台上显示任务详情
	 */
	private void showQueInfo(boolean complete,String pattern) {
		HttpTask[] v=session.tasks.toArray(EMPTY);
		if (session.tasks.size() > 0) {
			int n=0;
			LogUtil.show("=== 队列中 ===");
			for (HttpTask task : v) {
				if(pattern==null || StringUtils.contains(task.getUrl().toString(),pattern)){
					LogUtil.show(task.getState().name().substring(0,1)+" "+task.getUrl());
					n++;
				}
			}
			if(pattern!=null)LogUtil.show("=======总计："+n+"========");
		}
	}

	private void showErrInfo(boolean detail,String pattern,final String orderBy) {
		final String order=(orderBy==null)?"host":orderBy;
		if (session.errorTasks.size() > 0) {
			HttpTask[] v=session.errorTasks.values().toArray(EMPTY);
			Arrays.sort(v, new Comparator<HttpTask>(){
				
				public int compare(HttpTask o1, HttpTask o2) {
					return o1.getGroupValue(order).compareTo(o2.getGroupValue(order));
				}
			});
			
			int n=0;
			LogUtil.show("=== 下载失败 ===");
			for (HttpTask task : v) {
				if(pattern==null || StringUtils.contains(task.getUrl().toString(), pattern)){
					n++;
					if(detail){
						LogUtil.show("-- " + task.getUrl());
						LogUtil.show("   " + task.getFilepath() + "\n   " + task.getReturnObj());	
					}else{
						LogUtil.show(task.getUrl());
					}
				}
			}
			if(pattern!=null)LogUtil.show("=======总计："+n+"========");
		}
	}

	public boolean isPause() {
		return scheduler.pause;
	}

	public String getDownloadPath() {
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	public int getRunningTaskCount() {
		return session.processing.size();
	}

	public int getQueuedTaskCount() {
		return session.tasks.size();
	}

	public int getFinishedTaskCount() {
		return session.finished.intValue();
	}

	public int getErrorTaskCount() {
		return session.errorTasks.size();
	}

	public void addLog(String msg) {
		session.addLog(msg);
	}

	public boolean removeError(HttpTask toDel) {
		if(toDel.getState()==TaskState.ERROR){
			session.removeTask(toDel);
			return true;
		}
		return false;
	}

	public boolean removeQueued(HttpTask toDel) {
		if(toDel.getState()==TaskState.QUEUED){
			session.removeTask(toDel);
			return true;
		}
		return false;
	}
}
