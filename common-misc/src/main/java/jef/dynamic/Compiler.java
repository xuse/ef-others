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
package jef.dynamic;

import java.io.File;
import java.io.IOException;

import javax.management.ReflectionException;

import jef.tools.reflect.ClassEx;

public interface Compiler {
	/**
	 * 读取编译输出文件夹
	 * @return
	 */
	File getOutputFolder() ;
	/**
	 * 设置编译输出文件夹
	 * @param outputFolder
	 */
	void setOutputFolder(File outputFolder) ;
	
	/**
	 * 将指定的java文件编译。
	 * @param source 要处理的文件
	 * @param className 类名
	 * @return
	 * @throws ReflectionException
	 */
	ClassSource complie(File source, String className) throws ReflectionException;
	
	/**
	 * 编译多个java对象
	 * @param units
	 * @return
	 * @throws ReflectionException
	 * @throws IOException
	 */
	ClassSource[] complie(JavaSource... units) throws ReflectionException, IOException ;
	
	/**
	 * 编译一个java文件，并且立刻作为类动态载入
	 * @param sourceFolder
	 * @param className
	 * @return
	 */
	ClassEx complieAndLoad(File src, String className)throws ReflectionException;
	/**
	 * 编译多个java对象，并且作为类动态载入
	 * @param java3
	 * @return
	 * @throws ReflectionException
	 * @throws IOException
	 */
	ClassEx[] complieAndLoad(JavaSource... java3) throws ReflectionException;
	
	/**
	 * 得到编译器持有的ClassLoader。ClassLoader的行为根据具体实现会有所不同：
	 * 目前已知的实现中，DynamicClassLoader能够支持单个Class的reload,unload操作。
	 * 其他ClassLoader不支持，想要实现Reload功能必须丢弃整个ClassLoader，重新初始化
	 * @return
	 */
	ClassLoader getClassLoader();
	
	/**
	 * 重新设置ClassLoader，丢弃所有也已经载入的Class
	 */
	void resetClassLoader();
	public String getEncoding();
	public void setEncoding(String encoding);
}
