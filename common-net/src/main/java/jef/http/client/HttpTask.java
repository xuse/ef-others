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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.SAXException;

import jef.common.Entry;
import jef.common.log.LogUtil;
import jef.common.wrapper.Holder;
import jef.http.client.support.CommentEntry;
import jef.http.client.support.GetMethod;
import jef.http.client.support.HttpConnection;
import jef.http.client.support.PostMethod;
import jef.tools.ArrayUtils;
import jef.tools.Assert;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.ThreadUtils;
import jef.tools.XMLUtils;

public class HttpTask implements Runnable, Serializable{
	private static final long serialVersionUID = 126148287461276024L;
	public static final String FILE_POSTFIX = ".tmp";
	public static final int FORCE_SIGNLE_THREAD_SIZE = 1024*1024 ;// 小于1M强制使用单线程
	public static final int MAX_DLTHREAD_QUT = 10; // 最大下载线程数量


	/** 任务基本属性 */
	private URL url;
	private int threadQut; // 下载线程数量
	private ConnectOptions options; //HTTP连接和下载选项
	protected boolean isNewTask; // 是否新建下载任务，可能是断点续传任务
	protected String filepath;
	private CommentEntry[] comment;
	private String callback;//任务回调
	
	
	public String toString() {
		return url.toString();
	}

	//临时变量
	transient private int targetLength=-1;
	//任务被请求中
	transient int isBreak=0;
	
	private void setUrl(URL url) {
		this.url = url;
	}

	protected void setThreadQut(int threadQut) {
		this.threadQut = threadQut;
	}

	protected void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	transient private HttpEngine engine;	//Engine的句柄，用于notify和获取线程池*/
	
	/** 任务返回值相关 */
	private ReturnType returnType; // 返回类型
	private Object returnObj; // 返回对象
	private String id;			//17位长度数字，确保不重复的唯一ID
	
	public String getId() {
		return id;
	}

	/** 任务下载过程 */
	private Mode mode; // 工作模式
	private TaskState state = TaskState.QUEUED;
	private ConnectSession session;
	
	public HttpTask(String url) {
		this(1, url, ReturnType.DEFAULT, null,new ConnectOptions());
	}

	public HttpTask(int threadQut, String url) {
		this(threadQut, url, ReturnType.DEFAULT, null,new ConnectOptions());
	}
	
	public HttpTask(int threadQut, String url, ConnectOptions options) {
		this(threadQut, url, ReturnType.DEFAULT, null,new ConnectOptions());
	}
	public HttpTask(int threadQut, String url, ReturnType type, String filename){
		this(threadQut, url, type, null,new ConnectOptions());
	}
	
	public HttpTask(int threadQut, String url, ReturnType type, String filename, ConnectOptions options) {
		isNewTask = true;
		if (threadQut > MAX_DLTHREAD_QUT)threadQut = MAX_DLTHREAD_QUT;
		this.threadQut = threadQut;
		this.filepath = filename;
		this.options = options==null?ConnectOptions.DEFAULT:options;
		this.returnType = type;
		id=String.valueOf(System.currentTimeMillis()/1000).substring(2)+"-"+StringUtils.randomString();
		try {
			this.url = new URL(url);
		} catch (MalformedURLException ex) {
			System.out.print("非法的URL:"+url);
			throw new RuntimeException(ex);
		}
	}

	// 执行入口
	public void run() {
		Assert.notNull(engine);
		setState(TaskState.PROCESSING);
		try {
			if (isNewTask){
				this.targetLength=-1;
				newTask();
			} else {
				resumeTask();
			}
		} catch (IOException e) {
			returnType = ReturnType.ERROR_MESSAGE;
			returnObj = LogUtil.exceptionStack(e, "jef");
			if(HttpEngine.DEBUG_MODE)Exceptions.log(e);
			setState(TaskState.ERROR);
		} catch (Throwable e) {
			Exceptions.log(e);
			returnType = ReturnType.ERROR_MESSAGE;
			returnObj = LogUtil.exceptionStack(e, "jef");
			if(HttpEngine.DEBUG_MODE)Exceptions.log(e);
			setState(TaskState.ERROR);
		}
	}

	/**
	 * 恢复任务时被调用，用于断点续传时恢复各个线程。
	 * @throws IOException 
	 */
	private void resumeTask() throws IOException {
		if(session instanceof BlockSession){
			BlockSession bs=(BlockSession)session;
			bs.setDLTask(this);
			bs.resumeDownload(engine);
		}else{
			throw new IOException("任务类型不支持继续下载。");
		}
	}

	/**
	 * 新建任务时被调用，通过连接资源获取资源相关信息，并根据具体长度创建线程块，线程创建完毕后，即刻通过线程池进行调度
	 * @throws IOException 
	 */
	private void newTask() throws IOException {
		initTask();//创建session
		Assert.notNull(session);
		isNewTask = false;
		
		if (mode==Mode.TEXT) {
			doTextDownload();
		} else if (mode==Mode.BLOCK){
			doBlockDownload();
		}else{//下载文件已经存在，不需要下载
			((BlockSession)session).doFinished();
		}
	}

	private static final int SIZE_1M = 1048576;
	//计算下载策略和返回方式
	private void initTask() throws IOException {
		//请求方式计算
		HttpConnection con = calcConnectMode();
		//初始化Session
		if(Mode.BLOCK==mode){
			//凡是需要多线程Block模式下载的数据：
			if(!(options.getMethod() instanceof GetMethod)){//确认非GET模式不进行多线程下载,并且复用之前已经创建的链接
				threadQut = 1;
			}else if (this.threadQut > 1 ||this.threadQut <0) {
				if(targetLength<=0){//尝试获取目标文件长度
					if (con == null){
						Holder<HttpConnection> reuse=new Holder<HttpConnection>();
						targetLength=HttpConnection.getLengthOnly(url, options,reuse);
						if(reuse.get()!=null)con=reuse.get();
					}else if (!con.isConnected()){
						Holder<HttpConnection> reuse=new Holder<HttpConnection>();
						targetLength=HttpConnection.getLengthOnly(url, options,reuse);
						if(reuse.get()!=null)con=reuse.get();
					}else{
						targetLength =con.getContentLength();	
					}	
				}
				if (targetLength < FORCE_SIGNLE_THREAD_SIZE)threadQut = 1;
				if(this.threadQut<0){
					if(HttpEngine.DEBUG_MODE){
						LogUtil.show("尝试获取文件长度来决定下载的线程数。");
					}
					if(targetLength > SIZE_1M*210){
						LogUtil.show(url+"自动采用5线程下载。");
						threadQut = 5;	
					}else if(targetLength>SIZE_1M*70){
						LogUtil.show(url+"自动采用4线程下载。");
						threadQut = 4;
					}else if(targetLength>SIZE_1M*20){
						LogUtil.show(url+"自动采用3线程下载。");
						threadQut = 3;
					}else if(targetLength>SIZE_1M*6){
						LogUtil.show(url+"自动采用双线程下载。");
						threadQut = 2;
					}else{
						threadQut = 1;
					}
					if(HttpEngine.DEBUG_MODE){
						LogUtil.show("确定使用" + threadQut +"个线程下载。");
					}
				}
			}
			String fileTmppath=filepath + FILE_POSTFIX;
			File tmpFile = IOUtils.escapeExistFile(new File(fileTmppath));
			IOUtils.createFolder(tmpFile.getParent());
			fileTmppath=IOUtils.getPath(tmpFile);
			session = new BlockSession(targetLength, threadQut,fileTmppath,this,con);
		}else if(Mode.TEXT==mode){
			session =  new TextSession(this,con);
		}else{//不需要下载
			String fileTmppath=filepath;
			session = new BlockSession(-1, 0,fileTmppath,this,con);
		}
	}

	
	
	/**
	 * 计算请求的方式
	 */
	private HttpConnection calcConnectMode() throws IOException {
		HttpConnection con = null;
		//需要先创建连接，根据Content-Type再作判断
		if (returnType==ReturnType.DEFAULT) {
			con = HttpConnection.createConnection(url,options,true);
			String contType=StringUtils.substringBefore(con.getContentType(), ";").toLowerCase();
			this.returnType = DLHelper.getReturnTypeByMime(contType);
		}

		//凡是以文本形式直接返回的请求
		if (returnType.equals(ReturnType.HTML_DOC) || returnType.equals(ReturnType.XML_DOC) || returnType.equals(ReturnType.STRING)) {
			mode = Mode.TEXT;// 按TEXT方式单线程下载
			threadQut = 1;
		} else if (returnType.equals(ReturnType.FILE)) {//保存成文件
			mode = Mode.BLOCK;
			if (filepath == null) {	//未指定保存的文件名，将连接服务器来得到文件名
				if(con==null)con=HttpConnection.createConnection(url,options,false);
				if(options.getMethod() instanceof PostMethod){
					this.state=TaskState.POSTING;//对于Post请求，必须先发送请求才能获得文件名
					con.connect();
				}
				String fileName=DLHelper.getOrGenerateFilename(con);
				filepath = engine.getDownloadPath() + "/" + fileName;
			}else if(filepath.endsWith("/")||filepath.endsWith("\\")){	//指定了保存文件的目录，但是文件名还是要从服务器获取
				if(con==null)con=HttpConnection.createConnection(url,options,false);
				if(options.getMethod() instanceof PostMethod){
					this.state=TaskState.POSTING;//对于Post请求，必须先发送请求才能获得文件名
					con.connect();
				}
				String fileName=DLHelper.getOrGenerateFilename(con);
				filepath = filepath+ fileName;
			}else if(StringUtils.isNotBlank(filepath)){	//已经指定了保存到文件
				File file=new File(filepath);
				if(file.exists() && file.isDirectory()){//指定目标文件是个文件夹，还是得到服务器上去取文件名保存到该文件夹下
					if(con==null)con=HttpConnection.createConnection(url,options,false);
					if(options.getMethod() instanceof PostMethod){
						this.state=TaskState.POSTING;
						con.connect();
					}
					String fileName=DLHelper.getOrGenerateFilename(con);
					filepath = filepath+"/"+ fileName;
				}
			}
			File file=new File(filepath);//初始化保存的目标文件
			if(file.exists()){   //如果目标文件已经存在
				if(options.isDupFileOption()==ConnectOptions.DUP_SKIP){//下载策略为目标文件存在就不下载的场合
					ThreadUtils.doSleep(400);
					mode=Mode.ALREADY_DOWNLOAD;
					if(con!=null && con.isConnected()){
						con.close();
						LogUtil.show("任务:"+url+"已经存在("+file.getAbsolutePath()+")，不再下载。(连接关闭)");
					}else{
						LogUtil.show("任务:"+url+"已经存在("+file.getAbsolutePath()+")，不再下载。");	
					}
					
				}else if(options.isDupFileOption()==ConnectOptions.DUP_CHECKL_LENGTH){//下载策略为目标文件存在，检查文件长度一致性。
						targetLength=-1;
					if(con==null){
						Holder<HttpConnection> reuse=new Holder<HttpConnection>();
						targetLength=HttpConnection.getLengthOnly(url, options,reuse);
						if(reuse.get()!=null)con=reuse.get();
						
					}else if(!con.isConnected()){//使用仅请求一个字节的方式来获取文件长度
						Holder<HttpConnection> reuse=new Holder<HttpConnection>();
						targetLength=HttpConnection.getLengthOnly(url, options,reuse);
						if(reuse.get()!=null)con=reuse.get();
					}else{
						targetLength=con.getContentLength();
					}
					
					if(targetLength>-1){
						if(file.length()==targetLength){//文件相同不下载
							mode=Mode.ALREADY_DOWNLOAD;
							LogUtil.show("任务:"+url+"已经下载过，不再下载。");
							if(con!=null)con.close();
						}else{//文件名相同但长度不同，还是下载
							file = IOUtils.escapeExistFile(file);	
						}
					}else{//文件长度无法获得,认为需下载
						file = IOUtils.escapeExistFile(file);
					}
				}else{//强制下载， 更名并下载
					file = IOUtils.escapeExistFile(file);	
				}
			}
			filepath = IOUtils.getPath(file);
			//System.out.println(this.url+" MODE:"+ this.mode);
		} else if (returnType.equals(ReturnType.STREAM)) {
			filepath = File.createTempFile("~down", null).getCanonicalPath();
			mode = Mode.BLOCK;
		} else{
			throw new RuntimeException("无法识别的返回类型。");
		}
		return con;
	}

	/**
	 * 实现分块下载
	 */
	private void doBlockDownload() throws IOException  {
		BlockSession bs=(BlockSession)session;
		bs.startDownload(engine);
	}

	/**
	 * TEXT模式下载
	 */
	private void doTextDownload() throws IOException {
		TextSession ts=(TextSession)session;
		ts.send();
		//listener.listen();//由于TextSession.send是阻塞的IO方法，所以本线程不需要监听
		setState(TaskState.PROCESSING);
		doFinishTextTask(ts.getCon());
	}

	/**
	 * 当TEXT下载完成时执行
	 */
	private void doFinishTextTask(HttpConnection con) throws IOException {
		if(HttpEngine.DEBUG_MODE){
			LogUtil.show(con.getHeaderFields());
		}
		InputStream in=null;
		String charSet=null;
		try{
			in=con.getDownloadStream();
			charSet= StringUtils.substringAfter(con.getContentType(),"charset=");
			if(StringUtils.isBlank(charSet)){
				String contType=StringUtils.substringBefore(con.getContentType(), ";").toLowerCase();
//				this.returnType = DLHelper.getReturnTypeByMime(contType);
				//当没有编码时，使用默认编码,对于html，从文档中获取，对于XML，默认使用XML
				charSet=null;
				in=new PushbackInputStream(in,200);
				charSet=DLHelper.getCharsetFromStream((PushbackInputStream)in,contType);
			}
			if(HttpEngine.DEBUG_MODE){//开启调试模式时，下载到的网页存到本地。
				File raw=new File("c:/http_"+System.currentTimeMillis());
				IOUtils.saveAsFile( raw,in);
				IOUtils.closeQuietly(con);
				in=new FileInputStream(raw);
			}
		}catch(ConnectException e){
			throw new IOException(e.getMessage());
		}
		if("gb2312".equalsIgnoreCase(charSet))charSet="GBK";
		//直接从服务器获取流，并进行解析。
		try {
			if(ReturnType.HTML_DOC==returnType){
				setReturnObj(XMLUtils.loadHtmlDocument(in, charSet));
			}else if(ReturnType.XML_DOC==returnType){
				setReturnObj(XMLUtils.loadDocument(in,charSet,true,false));
			}else if(ReturnType.STRING==returnType){
				setReturnObj(IOUtils.asString(in,charSet,false));
			}else{
				setState(TaskState.ERROR);
				throw new IOException("TEXT模式只能返回HTML DOC,XML DOC 和String三种对象,but task:"+url+" is "+returnType);
			}
			setState(TaskState.DONE);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}finally{
			in.close();
		}
	}
	
	/*
	 * 各种Getter / Setter
	 */
	public int getThreadQut() {
		return threadQut;
	}

	public URL getUrl() {
		return url;
	}
	
	public synchronized boolean setUrlIfAvalible(String newurl) {
		if(this.state == TaskState.DONE || this.state==TaskState.POSTING || this.state==TaskState.PROCESSING){
			return false;	
		}else{
			try {
				this.setUrl(new URL(newurl));
				return true;
			} catch (MalformedURLException e) {
				Exceptions.log(e);
				return false;
			}
		}
	}
	
	protected synchronized void setReturnObj(Object obj) {
		this.returnObj=obj;
	}
	
	public synchronized Object getReturnObj() {
		return returnObj;
	}

	public ConnectOptions getOptions() {
		return options;
	}

	protected synchronized void setReturnType(ReturnType returnType) {
		this.returnType = returnType;
	}

	public synchronized ReturnType getReturnType() {
		return returnType;
	}
	
	/**
	 * 任务状态
	 */
	public static enum TaskState{
		QUEUED,
		POSTING,
		PROCESSING,
		DONE,
		ERROR,
		BREAK
	}
	public static final TaskState[] RUNNING={TaskState.QUEUED, TaskState.POSTING, TaskState.PROCESSING};
	
	public String getFilepath() {
		return filepath;
	}
	public CommentEntry[] getComment() {
		return comment;
	}
	public void setComment(CommentEntry[] comment) {
		this.comment = ArrayUtils.removeNull(comment);
	}

	public String getComment(String key){
		if(comment==null || key==null)return null;
		Entry<String,String> e=getEntry(key);
		if(e!=null)return e.getValue();
		return null;
	}
	
	private Entry<String,String> getEntry(String key){
		if(key==null)return null;
		for(Entry<String,String> e: comment){
			if(e==null)continue;
			if(key.equals(e.getKey())){
				return e;
			}
		}
		return null;
	}

	public void setComment(String key,String value) {
		if(key==null)return;
		if(comment==null || comment.length==0){
			comment=new CommentEntry[]{new CommentEntry(key,value)};
		}else{
			Entry<String,String> e=getEntry(key);
			if(e==null){
				comment=(CommentEntry[]) ArrayUtils.add(comment, new CommentEntry(key,value));	
			}else{
				e.setValue(value);
			}
		}
	}
	
	public synchronized TaskState getState() {
		return state;
	}

	public synchronized void setState(TaskState state) {
		if(this.state == state)return;
		this.state = state;
		if(state==TaskState.ERROR ||state==TaskState.DONE){
			ThreadUtils.doNotifyAll(this);//提醒发出下载任务的线程检查任务状态
			ThreadUtils.doNotify(engine);//提醒下载管理器检查任务状态。
			//注意这里不用NotifyAll的前提是确保只可能存在一个线程（即任务调度线程）等待engine的锁。
			//因此不允许在其他任何线程的代码中出现wait(engine)这样的用法。	
		}
	}
	void setEngine(HttpEngine engine) {this.engine = engine;}

	public synchronized void setOptions(ConnectOptions options) {
		this.options = options;
	}
	public String getProgress(){
		StringBuilder sb=new StringBuilder();
		if(session==null){
			sb.append(this.state.name().substring(0,1));			
		}else{
			String percent=StringUtils.leftPad(session.getCompletedPercent()+"%", 3);
			sb.append(this.state.name().substring(0,1)).append(" ").append(percent);	
		}
		 return sb.toString();
	}

	public final String getCallback() {
		return callback;
	}

	public final void setCallback(String callback) {
		this.callback = callback;
	}

	public void addLog(String msg) {
		engine.addLog(msg);
		LogUtil.show(msg);
	}

	public String getLength() {
		if(session==null){
			return "";
		}else{
			return session.getLength();
		}
	}

	/**
	 * 当失败后重置状态
	 * @param flag 
	 */
	public void reset(boolean flag) {
		returnType=ReturnType.FILE;
		returnObj=null;
		state = TaskState.QUEUED;
		options.shiftTo=0;
		if(session instanceof BlockSession){
			String path=((BlockSession)session).getFileTmppath();
			if(path==null || !new File(path).exists()){
				isNewTask=true;
				mode=null;
				session = null;	
			}else{
				if(flag){//强制重新开始任务
					File tmp=new File(path);
					if(tmp.exists())tmp.delete();
					isNewTask=true;
					mode=null;
					session = null;
				}
			}
		}else{
			isNewTask=true;
			mode=null;
			session = null;
		}
	}

	
	public String getGroupValue(String name) {
		if(name.equals("type")){
			return this.returnType.name();
		}else if(name.equals("host")){
			return this.url.getHost();
		}else if(name.equals("error")){
			if(this.state==TaskState.ERROR){
				return (String)this.returnObj;	
			}else{
				return "";
			}
		}
		throw new RuntimeException("Unknow group keyword:" + name);
	}


}
