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
package jef.http.client.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLHandshakeException;

import jef.common.log.LogUtil;
import jef.http.client.BlockSession;
import jef.http.client.ConnectOptions;
import jef.http.client.ConnectSession;
import jef.http.client.DLHelper;
import jef.http.client.HostManager;
import jef.http.client.HostProfile;
import jef.http.client.HttpEngine;
import jef.tools.Assert;
import jef.tools.JefConfiguration;
import jef.tools.StringUtils;
import jef.tools.ThreadUtils;
import jef.tools.io.CountInputStream;
import jef.tools.support.JefBase64;

public class HttpConnectionImpl extends HttpConnection {
	private URLConnection con;
	private boolean connected;
	private ConnectOptions option;
	private int retryTimes = 0;
	private HostProfile hostProfile;
	private boolean breakPoint=true;
	private boolean debugMode;

	public final HostProfile getHostProfile() {
		return hostProfile;
	}

	public final boolean isConnected() {
		return connected;
	}

	public final ConnectOptions getOption() {
		return option;
	}




	HttpConnectionImpl(URLConnection con, boolean connected, ConnectOptions option,HostProfile profile) {
		this.con = con;
		this.option = option;
		this.connected = connected;
		this.hostProfile=profile;
		debugMode=false;
		if(HttpEngine.DEBUG_MODE){
			if(HttpEngine.DEBUG_HOSTS==null || HttpEngine.DEBUG_HOSTS.isEmpty()){
				this.debugMode=true;	
			}else{
				if(HttpEngine.DEBUG_HOSTS.contains(con.getURL().getHost())){
					this.debugMode=true;	
				}
			}
		}
	}

	public HttpStatus getHttpStatus() {
		if (!connected)
			throw new RuntimeException("还没有连接，不能获得数据");
		String statusLine = con.getHeaderField(null);
		if (statusLine != null) {
			int httpstatus = Integer.valueOf(statusLine.split(" ")[1]);
			return HttpStatus.get(httpstatus);
		} else {
			return HttpStatus.STATUS_408;
		}
	}

	/**
	 * 返回HTTP状态
	 * 
	 * @return
	 * @throws IOException
	 */
	public HttpStatus connect() throws IOException {
		if (!connected) {
			try {
				doMethod(con, option,this.debugMode);
				connected = true;
			}catch(SSLHandshakeException e){
				installCert();
				LogUtil.show("new cert was installed. please restart the JVM...");//目前新证书安装后要重新启动
			} catch (ConnectException e) {
				throw new IOException(e.getMessage() + ":" + con.getURL().getHost());
			}
			HttpStatus httpstatus = getHttpStatus();
			// 404猜测
			if (httpstatus.isNotFound()) {
				connected = false;
				String url = option.nextShift();
				if (url != null) {
					// logMsg("404:" + con.getURL().toString() + " shift to:" +
					// url);
					this.con = generateUrlCon(new URL(url), option,hostProfile);
					return this.connect();
				} else {
					throw new IOException("HTTP:404 not found.");
				}
			} else if (httpstatus.isRedirect()) {// 服务器重定向
				connected = false;
				String url = option.nextShift();
				if (url != null) {
					LogUtil.show(httpstatus + ":" + con.getURL().toString() + " shift to:" + url);
					this.con = generateUrlCon(new URL(url), option,hostProfile);
					return this.connect();
				} else {
					String moveto = con.getHeaderField("Location");
					String oldUrl = con.getURL().toString();
					if (sameExtName(oldUrl, moveto)) {
						this.con = generateUrlCon(new URL(DLHelper.mergeUrl(oldUrl, moveto)), option,hostProfile);
						return this.connect();
					} else {
						throw new IOException("HTTP:" + httpstatus + " moveto:" + moveto);
					}
				}
			} else if (httpstatus.isReady()) {
				if (debugMode){
					LogUtil.show("Response From:"+con.getURL().toString());
					LogUtil.show(con.getHeaderFields());
				}
				if(con.getHeaderField("Accept-Ranges")==null){
					this.breakPoint=false;
				}
				return httpstatus;
			} else {
				connected = false;
				LogUtil.show("Error Connecting:"+con.getURL().toString());
				LogUtil.show(con.getHeaderFields());
				throw new IOException(httpstatus.toString());
			}
		} else {
			return null;
		}
	}

	private void installCert() throws IOException {
		URL url=this.getURL();
		InstallCert i=new InstallCert(url.getHost(),url.getPort());
		ThreadUtils.doSleep(500);
		try {
			i.install();//安装证书\
		} catch (GeneralSecurityException e1) {
			throw new IOException(e1.getMessage());
		}
	}

	private boolean sameExtName(String oldUrl, String moveto) {
		String a = StringUtils.substringAfterLast(oldUrl, ".");
		String b = StringUtils.substringAfterLast(moveto, ".");
		return a.equals(b) && a.length()>0;
	}

	private InputStream in;
	private CountInputStream countStream;
	public long getTransferPos(){
		String range = con.getHeaderField("Content-Range");
		long start=0;
		if (range != null) {
			range = StringUtils.stringRight(range, " ", true).toString();
			String args[] = range.replace('/', '-').split("-");
			 start= StringUtils.toLong(args[0], -1L);
		}
		if(countStream==null)return start;
		return start+countStream.getSize();
	}
	
	public InputStream getDownloadStream() throws IOException {
		if (!connected)
			throw new RuntimeException("还没有连接，不能获得数据");
		if (in != null)
			return in;
		countStream=new CountInputStream(con.getInputStream());
		if (con.getContentEncoding() != null && con.getContentEncoding().equalsIgnoreCase("gzip")) {
			in = new GZIPInputStream(countStream);
		} else {
			in = countStream;
		}
		return in;
	}

	static void doMethod(URLConnection con, ConnectOptions options,boolean debugMode) throws IOException {
		if (debugMode) {
			LogUtil.show("连接到" + con.getURL().toString() + ",内容为" + options.getMethod().toString());
		}
		HttpMethod method = options.getMethod();
		if (debugMode) {
			LogUtil.show(con.getRequestProperties());// 此日志必须在连接之前
		}
		method.doMethod(con);
		// Cookies处理
		List<String> list = con.getHeaderFields().get("Set-Cookie");
		if (list != null) {
			HostProfile pf = HostManager.getHost(con.getURL().getHost());
			if (pf!=null && pf.isAutoCookie()) {
				for (String str : list) {
					HttpEngine.setCookie(con.getURL().getHost(), str, false);
				}
			}
		}
	}

	public String getFilenameFromServer() throws IOException {
		if (!connected)
			connect();// 必须先连接
		String contentDisposition = con.getHeaderField("Content-Disposition");
		if (contentDisposition == null)
			return null;
		String fileName = StringUtils.trimToNull(StringUtils.substringAfter(contentDisposition, "filename="));
		fileName = new String(fileName.getBytes("ISO-8859-1"));
		return fileName;
	}
	
	public void addRetry(){
		this.retryTimes++;
	}

	public boolean supportBreakPoint(){
		if(hostProfile==null)return breakPoint;
		return hostProfile.isSupportBreakPoint();
	}
	
	/**
	 * 重新连接
	 * 
	 * @return true重新连接成功 false失败
	 */
	public boolean reconnect(long curPos, long endPos, BlockSession session) {
		close();
		connected = false;
		if (!canRetry())
			return connected;
		try {
			retryTimes++;
			this.con = generateUrlCon(con.getURL(), option,hostProfile);
			this.setRequestRangeAndConnect(curPos, endPos, session);
		} catch (IOException e) {
			connected = false;
			LogUtil.exception(e);
		}
		return connected;
	}

	static URLConnection generateUrlCon(URL url, ConnectOptions options,HostProfile pf) throws IOException {
		Proxy p = null;
		if (!JefConfiguration.getBoolean(JefConfiguration.Item.HTTP_DISABLE_PROXY, false)) {
			if (options.getProxy() != null && options.getProxy().isValid()) {
				p = options.getProxy().toJavaProxy();
			} else if (pf != null && pf.getProxy() != null) {
				p = pf.getProxy().toJavaProxy();
			}
		}
		if (p != null && HttpEngine.DEBUG_MODE) {
			LogUtil.show("connect " + url.toString() + " with proxy " + p.toString());
		}
		URLConnection con = (p == null) ? url.openConnection() : url.openConnection(p);
		con.setConnectTimeout(0);
		con.setReadTimeout(JefConfiguration.getInt(JefConfiguration.Item.HTTP_TIMEOUT, 0) * 1000);
		if (con instanceof HttpURLConnection) {
			if (options.isAllowRedirect()==-1) {
				if (pf != null && !pf.isAllowRedirect()) {
					((HttpURLConnection) con).setInstanceFollowRedirects(false);
				}
			}else{
				((HttpURLConnection) con).setInstanceFollowRedirects(options.isAllowRedirect()==1);
			}
		}
		// 设置连接的各种属性
		con.setAllowUserInteraction(true);
		// 代理验证
		if (options.getProxyLogin() != null)
			con.setRequestProperty("Proxy-Authorization", "Basic " + JefBase64.encode(options.getProxyLogin().getBytes()).replace("\n", ""));
		// 网站验证
		if (options.getAuthorization() != null) {
			con.setRequestProperty("Authorization", "Basic " + JefBase64.encode(options.getAuthorization().getBytes()).replace("\n", ""));
		}
		// 接收语言
		con.setRequestProperty("Accept-Language", "en-us,zh-cn,zh-tw,en-gb,en;q=0.7,*;q=0.3");
		// 接收类型
		con.setRequestProperty("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, */*");
		// 接收编码
		con.setRequestProperty("Accept-Encoding", "gzip,deflate");// x-gzip,

		// 客户端
		con.setRequestProperty("User-Agent", options.getUserAgent());
		//con.setRequestProperty("Host", url.getHost());
		// Cookie
		if (StringUtils.isNotEmpty(options.getCookie())) {// 设置Cookie
			if (!options.getCookie().equals(ConnectOptions.NO_COOKIE)) {
				con.setRequestProperty("Cookie", options.getCookie());
			}
		} else if (pf == null || pf.isAutoCookie()) {
			String cookie = HttpEngine.getCookie(url);
			if (StringUtils.isNotEmpty(cookie)) {
				con.setRequestProperty("Cookie", cookie);
			}
		}
		// 引用页
		if (options.getReference(url) != null) {
			con.setRequestProperty("Referer", options.getReference(url));
		}
		// Session
		if (options.isKeepSessionOnServer()) {
			con.setRequestProperty("Connection", "Keep-Alive");
		} else {
			con.setRequestProperty("Connection", "Close");
		}
		// 自定义属性

		if (options.getCustomHeader() != null) {
			Map<String, String> customHeader = options.getCustomHeaderMap();
			for (String key : customHeader.keySet()) {
				con.setRequestProperty(key, customHeader.get(key));
			}
		} else {
			if (pf != null && pf.getCustomHeader() != null) {
				Map<String, String> customHeader = pf.getCustomHeaderMap();
				for (String key : customHeader.keySet()) {
					con.setRequestProperty(key, customHeader.get(key));
				}
			}
		}
		return con;
	}
	
	public boolean canRetry() {
		return retryTimes < option.getRetry();
	}

	public void close() {
		try {
			if (connected && in != null) {
				if (con instanceof HttpURLConnection) {
					((HttpURLConnection) con).disconnect();
				}
				in.close();
				in = null;
			}
		} catch (IOException e) {
			LogUtil.exception(e);
		}
	}

	public String getContentType() {
		if (!connected)
			throw new RuntimeException("还没有连接，不能获得数据");
		return con.getContentType();
	}
	


	public Map<String, List<String>> getHeaderFields() {
		if (!connected)
			throw new RuntimeException("还没有连接，不能获得数据");
		return con.getHeaderFields();
	}

	public void setRequestProperty(String key, String value) {
		con.setRequestProperty(key, value);
	}

	public String getHeaderField(String string) {
		if (!connected)
			throw new RuntimeException("还没有连接，不能获得数据");
		return con.getHeaderField(string);
	}

	/**
	 * 得到下载对象的总长度
	 * @return
	 */
	public int getContentLength() {
		if (!connected)
			throw new RuntimeException("还没有连接，不能获得数据");
		
		String range = con.getHeaderField("Content-Range");
		if (range != null){
			String length = StringUtils.substringAfterLast(range, "/");	
			return StringUtils.toInt(length, -1); 
		}
		if (con.getContentLength() == -1) {
			return StringUtils.toInt(con.getHeaderField("Accept-Length"), -1);
		} else {
			return con.getContentLength();
		}
	}

	public URL getURL() {
		return con.getURL();
	}

	public void setRequestRangeAndConnect(long curPos, long endPos, BlockSession session) throws IOException {
		Assert.isFalse(connected, "已经连接，不能设置请求范围。");
		String setting = null;
		long totalLength = (session == null) ? -1 : session.getTotalLength();
		if (totalLength > 0 && totalLength == endPos)
			endPos = -1;// 不需要显式指定结束位置
		if (endPos > 0) {
			setting = "bytes=" + curPos + "-" + (endPos - 1);
			if(!HostProfile.breakPoint(hostProfile))throw new IOException(HttpStatus.CUSTOM_BREAKPOINT_UNSUPPORT.message);
			con.setRequestProperty("Range", setting);
		} else if (curPos > 0) {
			setting = "bytes=" + curPos + "-";
			if(!HostProfile.breakPoint(hostProfile))throw new IOException(HttpStatus.CUSTOM_BREAKPOINT_UNSUPPORT.message);
			con.setRequestProperty("Range", setting);
		}
		this.connect();
		if (setting != null) {
			String range = con.getHeaderField("Content-Range");
			if (range != null) {
				range = StringUtils.stringRight(range, " ", true).toString();
				String args[] = range.replace('/', '-').split("-");
				long start = StringUtils.toLong(args[0], -1L);
				long end = StringUtils.toLong(args[1], -1L) + 1;// HttpRange是含头含尾的，对应Java表示尾部应该+1
				Assert.isFalse(start < 0, "从服务器得到的Range不是数字？！" + args[0]);
				Assert.isFalse(end < 1, "从服务器得到的Range不是数字？！" + args[0]);
				if (start != curPos || (endPos > -1 && end != endPos)) {
					logMsg("警告!:在请求:" + con.getURL() + "时：请求range为" + setting + "。返回Range为" + range, session);
				} else {
					if (HttpEngine.DEBUG_MODE) {
						logMsg("在请求:" + con.getURL() + "时：请求range为" + setting + "。返回Range为" + range, session);
					}
				}
			} else {
				HostManager.setNotSupportBreakPoint(con.getURL());
				logMsg("警告!:在请求:" + con.getURL() + "时：请求range为" + setting + "。返回数据中没有标明Range.", session);
				throw new IOException("异常 !:在请求:" + con.getURL() + "时：请求range为" + setting + "。返回数据中没有标明Range.");
			}
		}
	}

	private void logMsg(String msg, BlockSession session) {
		if (session == null) {
			LogUtil.show(msg);
		} else {
			session.addLog(msg);
		}

	}

	// 检查服务器返回的Range和请求指定时的Range是否一致
	public void checkRange(int id, long curPos, long endPos, ConnectSession session) {
		String range = con.getHeaderField("Content-Range");
		if (StringUtils.isNotEmpty(range)) {
			range = StringUtils.stringRight(range, " ", true).toString();
			String args[] = range.replace('/', '-').split("-");
			long start = StringUtils.toLong(args[0], -1L);
			long end = StringUtils.toLong(args[1], -1L) + 1;// HttpRange是含头含尾的，对应Java表示尾部应该+1
			Assert.isFalse(start < 0, "从服务器得到的Range不是数字？！" + args[0]);
			Assert.isFalse(end < 1, "从服务器得到的Range不是数字？！" + args[0]);
			if (start != curPos || end != endPos) {
				String requested = String.valueOf(curPos) + "-" + endPos;
				String returned = String.valueOf(start) + "-" + end;
				StringBuilder sb = new StringBuilder();
				sb.append("在分块").append(con.getURL()).append("时,Thread:");
				sb.append(id).append("请求Range(").append(requested).append("),实际返回Range(");
				sb.append(id).append(returned).append(")。");
				String msg = sb.toString();
				session.addLog(msg);
				LogUtil.show(msg);
			}
		}
	}

}
