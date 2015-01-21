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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jef.tools.IOUtils;

/**
 * 动态的ClassLoader加载器
 * @author Administrator
 *
 */
public class DynamicClassLoader extends ClassLoader {
	public DynamicClassLoader(ClassLoader p) {
		super(p);
	}
	public DynamicClassLoader() {
		super(IOUtils.class.getClassLoader());
	}
	public Class<?> findClass(String name) throws ClassNotFoundException {
		 if(map.containsKey(name)){
			return map.get(name).loadClass(name);
		 }
		 throw new ClassNotFoundException(name);
	 }
	 
	 public boolean unloadClass(String clsname){
		 if(!map.containsKey(clsname))return false;
		 map.remove(clsname);
		 return true;
	 }
	 
	Map<String,SimpleClassLoader> map=new ConcurrentHashMap<String,SimpleClassLoader>();
	
	/**
	 * 载入Class，会自动根据ClassSource中的修改时间重新装载
	 * @param c
	 * @return
	 */
	public synchronized Class<?> loadClass(ClassSource c) {
		SimpleClassLoader loaded=map.get(c.getName());
		if(loaded!=null && loaded.getLastModified()!=c.getLastModified()){
			loaded=null;
		}
		if(loaded==null){
			loaded=new SimpleClassLoader(this,c);
			map.put(c.getName(), loaded);
			//System.out.println("add "+c.getName());
		}
		try {
			return loaded.loadClass(c.getName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 重新载入Class
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	public synchronized Class<?> reloadClass(ClassSource c) throws ClassNotFoundException {
		SimpleClassLoader loaded=new SimpleClassLoader(this,c);
		map.put(c.getName(), loaded);			
		try {
			return loaded.loadClass(c.getName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	//一个loader只负责一个Class,这样可以卸载
	public static class SimpleClassLoader extends ClassLoader {
		ClassSource data;
		public SimpleClassLoader(ClassLoader parent) {
			super(parent);
		}

		public SimpleClassLoader(ClassLoader parent, ClassSource c) {
			super(parent);
			this.data=c;
		}

		public long getLastModified() {
			return data.getLastModified();
		}

		protected synchronized Class<?> loadClass(String name, boolean resolve)throws ClassNotFoundException   {
			Class<?> c = findLoadedClass(name);//如果已经加载过，直接返回
			if(c!=null){
				if (resolve) {
					resolveClass(c);
				}
				return c;
			}
			if(data.getName().equals(name)){//没有加载过并且是已经注入的类
				byte[] bytes = data.getData();
				Class<?> theClass = defineClass(name, bytes, 0, bytes.length);
				if (theClass == null)throw new ClassFormatError();
				return theClass;
			}else{							//其他情况，使用默认的行为（从父类加载）
				return super.loadClass(name,resolve);	
			}
		}			
	}
}
