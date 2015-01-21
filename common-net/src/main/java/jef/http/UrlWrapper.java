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
package jef.http;

import jef.jre5support.Headers;

public interface UrlWrapper {
	/**
	 * 协议部分
	 * @return
	 */
	String getProtocol();
	
	/**
	 * 端口部分
	 * @return
	 */
	int getPort();
	
	/**
	 * 站点部分
	 * @return
	 */
	String getHost();
	
	/**
	 * 服务器端专用，Context路径
	 * @return
	 */
	String getContextPath();
	
	/**
	 * J2EE服务端专用，相对Context的路径
	 * @return
	 */
	String getRelativePath();
	
	/**
	 * 路径部分，排除了Host、参数、书签等的内容
	 * @return
	 */
	String getPath();
	
	/**
	 *  路径部分，不对原路径作URL解码
	 * @return
	 */
	String getRawPath();
	
	/**
	 * 路径部分，纯目录
	 * @return
	 */
	String getDirectory();
	/**
	 * 路径部分，文件
	 * @return
	 */
	String getFilename();
	
	/**
	 * 请求参数部分，第一个?后面，#前面的部分
	 * @return
	 */
	String getQuery();
	
	/**
	 * 书签部分，第一个#后面的部分
	 * @return
	 */
	String getRef();
	
	/**
	 * 返回URL文本
	 * @return
	 */
	String toString();
	
	/**
	 * 得到URL中的参数
	 * @return
	 */
	Headers getParameters();
}
