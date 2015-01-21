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
package jef.http.server;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

import jef.http.UrlWrapper;

/**
 * 对HttpServletRequest 和HttpServletResponse对象的封装
 * 
 * @author Administrator
 * 
 */
public interface WebExchange extends Closeable{
	/**
	 * 返回一个输出流，可以通过向这个输出流提供数据来向客户端发送消息
	 * 
	 * @return
	 */
	Writer getOutput();

	/**
	 * 得到原始的输出流
	 * 
	 * @return
	 */
	OutputStream getRawOutput();

	/**
	 * 获取Post请求的数据。 会自动解析请求数据，拆离其中的文件附件。
	 * 
	 * @param charset
	 *            指定URLDecode时的编码，默认为UTF-8。charset超过一个以上的参数无效。
	 * @return
	 */
	PostData getPostdata();

	/**
	 * 输出文字
	 * 
	 * @param string
	 */
	void print(String string);


	/**
	 * 输出文字
	 * 
	 * @param string
	 */
	void println(String string);

	/**
	 * 输出文字
	 * 
	 * @param string
	 * @param charset
	 */
	void println(String string, String charset);

	/**
	 * 设置返回的消息头
	 * 
	 * @param name http头名称
	 * @param value http头内容
	 */
	void setResponseHeader(String name, String value);

	/**
	 * 设置HTTP状态
	 * @param status
	 *            HttpStatus
	 */
	void setStatus(int status);

	/**
	 * 将异常堆栈推向客户端
	 * @param t 异常
	 */
	void printStackTrace(Throwable t);

	/**
	 * 得到请求的URL
	 * @return 包装后的URL
	 * @see UrlWrapper
	 */
	UrlWrapper getRequestURL();

	/**
	 * 得到HTTP方法
	 * @return 方法
	 */
	public String getMethod();

	/**
	 * 得到协议
	 * @return http or https
	 */
	public String getProtocol();

	/**
	 * 得到实际路径
	 * @param removeEnd 要删除的文字
	 * @return
	 */
	public String getRealPath(String removeEnd);

	/**
	 * 获取URL中的参数
	 * 
	 * @param string
	 * @return
	 */
	String getParameter(String string);

	/**
	 * 得到所有参数构成的Map。对于POST请求，是包含了URL和POST内容中的所有参数。
	 * @return
	 */
	Map<String, String[]> getParameterMap();

	/**
	 * 输出JSON
	 * @param json
	 */
	public void returnJson(Object json);

	/**
	 * 输出文件 返回的文件名和数据流
	 * @param fileName
	 * @param stream
	 */
	public void returnFile(String fileName, InputStream stream);

	/**
	 * 重定向
	 * 
	 * @param url 重定向的地址
	 * @param message 消息
	 */
	void redirectWithMessage(String url, String message);

	/**
	 * 重定向
	 * 
	 * @param url 重定向的地址
	 */
	void redirect(String url);

	/**
	 * 设置用于参数的编码
	 * @return 当前编码
	 */
	public String getCharset();

	/**
	 * 设置用于写入返回文本的编码
	 * @param charset 编码
	 */
	public void setCharset(String charset);

	/**
	 * 关闭并释放资源
	 */
	void close();
	
	/**
	 * 获得响应码
	 * @return
	 */
	public int getStatus();
}
