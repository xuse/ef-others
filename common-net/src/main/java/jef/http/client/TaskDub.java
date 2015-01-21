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

import jef.common.log.LogUtil;
import jef.http.client.support.Proxy;
import jef.tools.StringUtils;


public class TaskDub {

	private static final char COMMA = ',';
	String url;
	String proxy;
	String dupOption;
	String filePath;
	String length;
	String state;
	String returnType;
	String errMessage;
	String threadCount;
	String reference;
	String id;

	public static String getCSVHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("== URL, ");
		sb.append("Proxy, ");
		sb.append("DupOption, ");
		sb.append("LocalPath, ");
		sb.append("Length, ");
		sb.append("State, ");
		sb.append("ReturnType, ");
		sb.append("ErrorMessage, ");
		sb.append("ThreadCount, ");
		sb.append("Reference, ");
		sb.append("Host, ");
		sb.append("LocalFile, ");
		sb.append("Id ==");
		return sb.toString();
	}

	public String toCSVString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.url).append(COMMA);
		sb.append(this.proxy).append(COMMA);
		sb.append(this.dupOption).append(COMMA);
		sb.append(StringUtils.substringBeforeLast(this.filePath, File.separator)).append(COMMA);
		sb.append(this.length).append(COMMA);
		sb.append(this.state).append(COMMA);
		sb.append(this.returnType).append(COMMA);
		sb.append(this.errMessage).append(COMMA);
		sb.append(this.threadCount).append(COMMA);
		sb.append(this.reference).append(COMMA);
		sb.append(StringUtils.substringBefore(StringUtils.substringAfter(this.url, "://"),"/")).append(COMMA);
		sb.append(this.filePath).append(COMMA);
		sb.append(this.id);
		return sb.toString();
	}

	public final String getId() {
		return id;
	}

	public final void setId(String id) {
		this.id = id;
	}

	/**
	 * 从对象获得
	 * @param t
	 * @return
	 */
	public static TaskDub getFromTask(HttpTask t) {
		TaskDub td = new TaskDub();
		td.url = t.getUrl().toString();
		td.proxy = t.getOptions().getProxyString();
		td.dupOption = t.getOptions().getDupFileOption();
		td.filePath = t.getFilepath();
		td.length = t.getLength();
		td.state = t.getState().name();
		td.returnType = t.getReturnType().name();
		td.errMessage = (t.getReturnType()==ReturnType.ERROR_MESSAGE)?StringUtils.toString(t.getReturnObj()):"";
		td.errMessage = td.errMessage.replace(',', '\uFF0C').replace('\n', ' ');
		td.threadCount = String.valueOf(t.getThreadQut());
		td.reference = t.getOptions().getReference(null);
		td.id = t.getId();
		return td;
	}
	
	
	public static TaskDub[] getFromTask(HttpTask[] ts) {
		TaskDub[] rst=new TaskDub[ts.length];
		for(int i=0;i<ts.length;i++){
			rst[i]=getFromTask(ts[i]);
		}
		return rst;
	}

	/**
	 * 从行获得
	 * 
	 * @param str
	 * @return
	 */
	public static TaskDub load(String str) {
		int i = 0;
		String args[] = StringUtils.splitByWholeSeparatorPreserveAllTokens(str,",");
		if (args.length != 13 && args.length != 12) {
			LogUtil.show(args[0]+" invalid import");
			return null;
		}
		TaskDub td = new TaskDub();
		td.url = args[i++];
		td.proxy = args[i++];
		td.dupOption = args[i++];
		String localDir=args[i++];
		td.length = args[i++];
		td.state = args[i++];
		td.returnType = args[i++];
		td.errMessage = args[i++];
		td.threadCount = args[i++];
		td.reference = args[i++];
		i++;
		td.filePath = args[i++];
		if(args.length>12)td.id = args[i++];
		if(StringUtils.isBlank(td.id))td.filePath=localDir;
		return td;
	}

	public boolean modifyTask(HttpTask t) {
		boolean flag=false;
		TaskDub old = TaskDub.getFromTask(t);
		if (!StringUtils.equals(this.url, old.url)) {
			t.setUrlIfAvalible(this.url);
			flag=true;
		}
		if (!StringUtils.equals(this.proxy, old.proxy)) {
			t.getOptions().setProxy(Proxy.create(this.proxy));
			flag=true;
		}
		if (!StringUtils.equals(this.dupOption, old.dupOption)) {
			t.getOptions().setDupFileOption(this.dupOption);
			flag=true;
		}
		if (!StringUtils.equals(this.filePath, old.filePath)) {
			t.setFilepath(this.filePath);
			t.isNewTask=true;//当下载文件变化，强制任务状态为New
			flag=true;
		}
		if (!StringUtils.equals(this.returnType, old.returnType)) {
			t.setReturnType(ReturnType.valueOf(this.returnType));
			flag=true;
		}
		if (!StringUtils.equals(this.threadCount, old.threadCount)) {
			t.setThreadQut(Integer.valueOf(this.threadCount));
			flag=true;
		}
		if (!StringUtils.equals(this.reference, old.reference)) {
			t.getOptions().setReference(reference);
			flag=true;
		}
		return flag;
	}

	public final String getDupOption() {
		return dupOption;
	}

	public final String getErrMessage() {
		return errMessage;
	}

	public final String getFilePath() {
		return filePath;
	}

	public final String getLength() {
		return length;
	}

	public final String getProxy() {
		return proxy;
	}

	public final String getReference() {
		return reference;
	}

	public final String getReturnType() {
		return returnType;
	}

	public final String getState() {
		return state;
	}

	public final String getThreadCount() {
		return threadCount;
	}

	public final String getUrl() {
		return url;
	}
	
}
