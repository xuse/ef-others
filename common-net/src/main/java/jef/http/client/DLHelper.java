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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import jef.common.MimeTypes;
import jef.common.log.LogUtil;
import jef.http.UrlWrapper;
import jef.http.client.support.HttpConnection;
import jef.inner.sun.Headers;
import jef.tools.ArrayUtils;
import jef.tools.Assert;
import jef.tools.ByteUtils;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.XMLUtils;
import jef.tools.string.CharsetName;

public class DLHelper {
	public static URL toURL(String s) {
		try {
			return new URL(s);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getCharsetFromStream(PushbackInputStream in,String contentType) throws IOException {
		byte[] buf=new byte[200];
		int len=in.read(buf);
		in.unread(buf, 0, len);
		if(contentType.equals("text/xml") || ByteUtils.matchStart(buf,"<?xml".getBytes())){
			return XMLUtils.getCharsetInXml(buf,len);
		}else{
			return DLHelper.getCharsetInHtml(buf,len);	
		}
	}
	
	
	public static Headers getHeaders(HttpServletRequest req){
		Headers hs=new Headers();
		Enumeration<String> e=req.getHeaderNames();
		while(e.hasMoreElements()){
			String s=e.nextElement();
			List<String> result=Collections.list(req.getHeaders(s));
			if(result.isEmpty()){
				hs.put(s, null);
			}else{
				hs.put(s, result.toArray(new String[result.size()]));
			}
		}
		return hs;
	}

	public static UrlWrapper getUrl(final HttpServletRequest request){
		final String url = request.getRequestURL().toString(); 
		final String uri = request.getRequestURI();
		return new UrlWrapper(){
			
			
			@Override
			public String toString() {
				StringBuffer sb=new StringBuffer(url);
				String q=getQuery();
				if(q!=null)sb.append('?').append(q);
				return sb.toString();
			}

			public String getHost() {
				
				return DLHelper.getHost(url);
			}

			public String getPath() {
				return StringUtils.urlDecode(uri);
			}
			public String getRawPath() {
				return uri;
			}
			public int getPort() {
				return request.getLocalPort();
			}

			public String getProtocol() {
				return StringUtils.substringBefore(request.getProtocol(), "/").toLowerCase();
			}

			public String getQuery() {
				return request.getQueryString();
			}

			public String getRef() {
				return null;
			}

			public String getContextPath() {
				return request.getContextPath();
			}

			public String getRelativePath() {
				return StringUtils.removeStart(getPath(), getContextPath());
			}

			public Headers getParameters() {
				return DLHelper.getParamsInUrl(getQuery(),false);
			}

			public String getDirectory() {
				String path=getPath();
				if(path.endsWith("/"))return path;
				return StringUtils.substringBeforeLast(path, "/")+"/";
			}

			public String getFilename() {
				String path=getPath();
				if(path.endsWith("/"))return null;
				return StringUtils.substringAfterLast(path, "/");
			}
		};
	}
	
	public static UrlWrapper getUrl(final URL url){
		return new UrlWrapper(){
			@Override
			public String toString() {
				return url.toString();
			}

			public String getHost() {
				return url.getHost();
			}

			public String getPath() {
				return StringUtils.urlDecode(url.getPath());
			}

			public String getRawPath() {
				return url.getPath();
			}
			
			public int getPort() {
				if(url.getPort()==-1)return url.getDefaultPort();
				return url.getPort();
			}

			public String getProtocol() {
				return url.getProtocol();
			}

			public String getQuery() {
				return url.getQuery();
			}

			public String getRef() {
				return url.getRef();
			}

			public String getContextPath() {
				return "/";
			}

			public String getRelativePath() {
				return url.getPath();
			}

			public Headers getParameters() {
				return getParamsInUrl(getQuery(),false);
			}

			public String getDirectory() {
				String path=url.getPath();
				if(path.endsWith("/"))return path;
				return StringUtils.substringBeforeLast(path, "/")+"/";
			}

			public String getFilename() {
				String path=url.getPath();
				if(path.endsWith("/"))return null;
				return StringUtils.substringAfterLast(path, "/");
			}
		};
	}

	/**
	 * 得到自动判断的返回类型
	 * 
	 * @param mimeType
	 * @return
	 */
	public static ReturnType getReturnTypeByMime(String mimeType) {
		if ("text/html".equals(mimeType)) {// HTML文档
			return ReturnType.HTML_DOC;
		} else if ("text/xml".equals(mimeType)) {// XML文档
			return ReturnType.XML_DOC;
		} else if (mimeType.startsWith("text")) {// String
			return ReturnType.STRING;
		} else { // 存为临时文件后输出
			return ReturnType.FILE;
		}
	}

	/**
	 * 得到URL当中的host
	 * 
	 * @param url
	 * @return
	 */
	public static String getHost(String url) {
		String str = StringUtils.substringAfter(url, "://");
		Assert.isNotEmpty(str);
		return StringUtils.stringLeft(str, "/", true).toString().toLowerCase();// 网址不区分大小写，统统转小写
	}

	/**
	 * 判断给定的字串是否合法的IP地址
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean isValidIPAddr(String ip) {
		String[] args = org.apache.commons.lang.StringUtils.split(ip, ".");
		if (args.length != 4)
			return false;
		for (int i = 0; i < 4; i++) {
			int value = StringUtils.toInt(args[i], -1);
			if (value < 0 || value > 255) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 解析域名为IP
	 * 
	 * @throws UnknownHostException
	 */
	public static String getIP(String name) throws UnknownHostException {
		InetAddress addr = InetAddress.getByName(name);
		return addr.getHostAddress();
	}

	/**
	 * 得到URL当中路径和query的全部内容。
	 * 
	 * @param url
	 * @return
	 */
	public static String getPathQuery(String url) {
		int n = url.indexOf("://");
		String str = (n == -1) ? url : url.substring(n + 3);
		return "/" + StringUtils.substringAfter(str, "/"); // path区分大小写，并且包含第一个/
	}
	
	public static String getQueryString(String url) {
		try {
			URL u = new URL(url);
			return getUrl(u).getQuery();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * 原本处理时计划使每个任务具有单独的session处理，保存在本地以便进行续传，后来将整个Http队列连同正在下载的对象进行了
	 * 持久化。因此此功能实际上可以不再使用
	 * 
	 * @param dlTask
	 */
	public static void recordTask(HttpTask dlTask) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(dlTask.getFilepath() + ".tsk"));
			out.writeObject(dlTask);
			out.close();
		} catch (IOException ex) {
			LogUtil.exception(ex);
			throw new RuntimeException(ex);
		} finally {
			try {
				out.close();
			} catch (IOException ex) {
				LogUtil.exception(ex);
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * 用于计算当网页中出现url相对路径时，新的url的绝对路径
	 * 
	 * @param parentUrl
	 * @param url
	 * @return
	 */
	public static String mergeUrl(String parentUrl, String url) {
		if (url.indexOf(':') > -1)
			return url;// 已经是一个完整的url，不需要merge
		url = url.replace('\\', '/');
		if (url.startsWith("/")) {
			String host = StringUtils.substringBefore(StringUtils.substringAfter(parentUrl, "://"), "/");
			return StringUtils.substringBefore(parentUrl, "://") + "://" + host + url;
		}
		if (url.startsWith("http://") || url.startsWith("https://")) {
			return url;
		}
		String pUrl = StringUtils.substringBeforeLast(parentUrl, "/");
		while (url.startsWith(".")) {
			if (url.startsWith("../")) {
				url = StringUtils.substringAfter(url, "../");
				pUrl = StringUtils.substringBeforeLast(pUrl, "/");
			} else if (url.startsWith("./")) {
				url = StringUtils.substringAfter(url, "./");
			} else {
				throw new IllegalArgumentException("more than two . in the url is invalid.");
			}
		}
		return pUrl + "/" + url;
	}

	/**
	 * 从相对路径得到完整路径
	 * @param path
	 * @param dir
	 * @return
	 */
	public static String mergePath(String path, String dir) {
		path = path.replace('\\', '/');
		if (path.indexOf(':') > -1 || path.charAt(0) == '/') {
			return path;
		} else {
			return dir + "/" + path;
		}
	}

	public static String getFilenameFromUrl(String url) {
		try {
			return getFilenameFromUrl(new URL(url));
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL invalid:" + url);
		}
	}

	public static String getFilenameFromUrl(URL url) {
		String fn = StringUtils.substringAfterLastIfExist(url.getFile(), "/");
		fn = StringUtils.urlDecode(fn);
		return StringUtils.toFilename(fn, "");
	}

	// 从连接中获取文件名
	public static String getOrGenerateFilename(HttpConnection con) throws IOException {
		String fileName = null;
		URL url = con.getURL();
		if (!con.isConnected()) {// 对于尚未连接的con，尽可能不连接以节省时间
			if (url.getQuery() == null) {
				fileName = getFilenameFromUrl(url);
				String ext = IOUtils.getExtName(fileName);
				if (!MimeTypes.contains(ext)) {
					fileName = null;// 不是常见可识别的扩展名，认为从URL中获得得名称不可靠。取消之
				}
			}else{
				HostProfile p=con.getHostProfile();
				if(p!=null && p.getFilenameParam()!=null){
					Headers params=getParamsInUrl(url.getQuery(),true);
					if(params.containsKey(p.getFilenameParam())){
						fileName=StringUtils.trimToNull(params.getFirst(p.getFilenameParam()));	
					}
				}
			}
			
		}
		if (fileName != null) {
			if (fileName.indexOf('%') > -1)
				fileName = StringUtils.urlDecode(fileName);
			return fileName;// 在不连接服务器的情况下返回文件名
		}

		// 到这里必须连接服务器了
		fileName = con.getFilenameFromServer();// 从服务器返回的信息中获得文件名
		if (StringUtils.isEmpty(fileName)) {// 服务器也无法获得，再次从url中获取
			fileName = getFilenameFromUrl(url);
		} else {
			fileName = StringUtils.toFilename(fileName, "");
		}
		if (StringUtils.isBlank(fileName)) {// url中也没有文件名，给出默认文件名
			fileName = "index.html";
		}
		if (fileName.indexOf('%') > -1)
			fileName = StringUtils.urlDecode(fileName);
		return fileName;
	}

	// 从HTML页面取得页面的编码
	private static String getCharsetInHtml(byte[] buf, int len) {
		buf = ArrayUtils.subarray(buf, 0, len);
		String s = new String(buf).toLowerCase();
		int n = s.indexOf("charset=");
		if (n > -1) {
			s = s.substring(n + 8);
			if (s.charAt(0) == '\"' || s.charAt(0) == '\'') {
				s = s.substring(1);
			}
			n = StringUtils.indexOfAny(s, "\"' ><");
			if (n > -1) {
				s = s.substring(0, n);
			}
			if(s.length()==0)return null;
			s = CharsetName.getStdName(s);
			return s;
		}else{
			return null;
		}
	}

	public static Headers getParamsInUrl(String query,boolean ignorCase){
		return getParamsInUrl(query,"UTF-8",ignorCase);
	}
	
	public static Headers getParamsInUrl(String query,String charset,boolean ignorCase){
		Headers paramsInUrl=new Headers(4, false);
		if(StringUtils.isEmpty(query))return paramsInUrl;
		//对query做decode
//		query=StringUtils.urlDecode(query, charset);
		//String query=uri.getQuery();
		if(query!=null){
			for(String x: query.split("&")){
				int n=x.indexOf("=");
				if(n>-1){
					String key=x.substring(0,n);
					paramsInUrl.add(key, StringUtils.urlDecode(x.substring(n+1), charset));
				}else{
					String key=x;
					paramsInUrl.put(key, null);	
				}
			}
		}
		return paramsInUrl;
	}
}
