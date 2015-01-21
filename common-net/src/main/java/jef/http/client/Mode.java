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

/**
 * 任务工作模式
 */
enum Mode {
	TEXT, //单线程连接。前三种返回方式使用的工作模式 (当非GET请求时，都使用此模式)
	BLOCK,//多线程方式。后两种返回方式使用的工作模式
	ALREADY_DOWNLOAD//告知不需要下载，文件已经存在
}
