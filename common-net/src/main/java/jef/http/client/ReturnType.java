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

public enum ReturnType {
	HTML_DOC, // 只支持单线程
	XML_DOC, // 只支持单线程
	STRING, // 只支持单线程
	STREAM, // 用临时文件存放数据，返回流
	FILE, // 存放到指定的文件
	DEFAULT, // 根据ContentType自动判断1～4
	ERROR_MESSAGE
}
