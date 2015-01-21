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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.management.ReflectionException;

import jef.common.log.LogUtil;
import jef.dynamic.ClassSource.FileClassSource;
import jef.tools.ArrayUtils;
import jef.tools.Assert;
import jef.tools.IOUtils;
import jef.tools.reflect.ClassLoaderUtil;
import jef.tools.reflect.ClassEx;

import org.apache.commons.lang.StringUtils;

import sun.misc.Launcher;

/**
 * 编译工具类 用于将Java文件编译为Class文件。
 * 此实现基于com.sun.tools.javac.Main类。
 * 如果要在其他版本JDK上使用，稍后可以编写一个基于Ant的动态编译实现。
 * 
 * 备注：此处采用的方式tools.javac.Main是位于tools.jar包中的，自Java1.2之后的版本就支持了。
 * 目前比较流行的做法是用ToolProvider.getSystemJavaCompiler()来编译,但这个类到javaSe6 版本后才有
 * @author Jiyi
 */
public class DefaultCompiler implements jef.dynamic.Compiler{
	private DynamicClassLoader memLoader;
	private Class<?> complier;
	private Method method;
	private String classPath;
	private File outputFolder;
	private static final String JAVAC="com.sun.tools.javac.Main";
	private String encoding="UTF-8";
	
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public DefaultCompiler(ClassLoader p) throws ReflectionException {
		try {
			Class<?> complier = getComplieClass();
			init(complier);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		memLoader=new DynamicClassLoader(p);
	}
	public DefaultCompiler() throws ReflectionException {
		this(null);
	}
	
	public File getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(File outputFolder) {
		if (!outputFolder.exists())
			outputFolder.mkdirs();
		this.outputFolder = outputFolder;
		Assert.isTrue(outputFolder.isDirectory());
	}

	/**
	 * 编译指定的Java代码
	 * @param units
	 * @return
	 * @throws ReflectionException
	 * @throws IOException
	 */
	public ClassSource[] complie(JavaSource... units) throws ReflectionException {
		if(units.length==0)return new ClassSource[0];
		List<File> toBeDelete=new ArrayList<File>();

		File listFile=new File("classlist.txt");
		try{
			BufferedWriter bw=IOUtils.getWriter(listFile, null, false);
			List<String> clsNames=new ArrayList<String>(units.length);
			for(JavaSource u:units){
				File java=u.asSourceFile();//存为文件
				if(u.asTempFile()){
					toBeDelete.add(java);	
				}
				clsNames.add(u.getClassName());
				bw.write(java.getAbsolutePath());
				bw.newLine();
			}
			bw.close();
			toBeDelete.add(listFile);
			ClassSource[] cs=complie(listFile,clsNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY),true);
			for(File f:toBeDelete){
				f.delete();
			}
			return cs;	
		}catch(IOException io){
			throw new ReflectionException(io,io.getMessage());
		}
	}
	
	/**
	 * 编译并载入指定的java代码
	 * @param units
	 * @return
	 * @throws ReflectionException
	 * @throws IOException
	 */
	public ClassEx[] complieAndLoad(JavaSource... units) throws ReflectionException {
		ClassSource[] cs=complie(units);
		ClassEx[] result=new ClassEx[cs.length];
		int n=0;
		for(ClassSource c:cs){
			result[n++]=new ClassEx(memLoader.loadClass(c));
		}
		return result;
	}
	
	public ClassEx complieAndLoad(File source, String className) {
		try {
			ClassSource[] cs = complie(source,new String[]{className},false);
			if(cs.length==0)return null;
			return new ClassEx(memLoader.loadClass(cs[0]));
		} catch (Exception e) {
			LogUtil.exception(e);
			throw new RuntimeException(e.getMessage());
		}
	}
	/**
	 * 编译Java文件,返回编译后的class文件
	 * @param sourceFolder
	 * @param className 不含.java扩展名
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public ClassSource complie(File source, String className) throws ReflectionException {
		ClassSource[] result=complie(source,new String[]{className},false);
		return result[0];
	}
	

	private ClassSource[] complie(File source, String[] className,boolean isListFile) throws ReflectionException {
		try {
			return innerComplie(source,className,isListFile);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException(e);
		} catch (IllegalAccessException e) {
			throw new ReflectionException(e);
		} catch (InvocationTargetException e) {
			throw new ReflectionException(e);
		}
	}

	private ClassSource[] innerComplie(File source,String[] className,boolean isListFile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		if(outputFolder==null){
			setOutputFolder(new File("tempclass"));
		}
		String p=outputFolder.getPath();
		String[] args=new String[] {"-encoding",encoding, "-cp", "\""+classPath+"\"","-d", p, ((isListFile)?"@":"")+source.getAbsolutePath()};
		//LogUtil.show(args);
		Object errorCode = method.invoke(null, new Object[] {args });
		if (!errorCode.equals(0)) {
			LogUtil.warn(errorCode);
			throw new IllegalArgumentException("Unknow exception occours, complie return code:" + errorCode);
		}
		List<ClassSource> result=new ArrayList<ClassSource>(className.length);
		for(String cName: className){
			File clsFile=new File(outputFolder.getPath() + "/" + cName.replace('.', '/') + ".class");
			if(!clsFile.exists()){
				LogUtil.warn("The climplied Class "+clsFile.getPath()+" not found!");
			}else{
				ClassSource s=new FileClassSource(clsFile,cName);
				result.add(s);
				addInnerClass(result,clsFile,cName);
			}
		}
		return result.toArray(new ClassSource[0]);
	}
	private void addInnerClass(List<ClassSource> result, File s,String cName) {
//		System.out.println("looking for "+cName+"|"+s.getParentFile().getPath());
		String pattern=IOUtils.removeExt(s.getName())+"$*";
//		System.out.println(pattern);
		for(File f:IOUtils.listFilesLike(s.getParentFile(), pattern)){
			String innerName=StringUtils.substringAfter(IOUtils.removeExt(f.getName()), "$");
			innerName=cName+"$"+innerName;
//			System.out.println("找到内部类:"+innerName);
			result.add(new FileClassSource(f,innerName));
		}
	}
	private void init(Class<?> complier) {
		Assert.equals(complier.getName(), "com.sun.tools.javac.Main");
		this.complier = complier;
		try {
			method = this.complier.getMethod("compile", new Class[] { String[].class });
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		initClassPath();
	}
	
	private void initClassPath() {
		// 计算Classpath,将当前应用程序的classpath全部读，以确保附加的CLass编译过程顺利
		ClassLoader cl=ClassLoaderUtil.class.getClassLoader();
		Iterator<URL> iter = Arrays.asList(((cl instanceof URLClassLoader)?(URLClassLoader)cl:ClassLoaderUtil.getAppClassLoader()).getURLs()).iterator();
		StringBuilder sb = new StringBuilder();
		if (iter.hasNext()) {
			URL url=iter.next();
			String path = jef.tools.StringUtils.urlDecode(url.getPath());
			sb.append(path);
			while (iter.hasNext()) {
				url=iter.next();
				sb.append(File.pathSeparator);
				path = jef.tools.StringUtils.urlDecode(url.getPath());
				sb.append(path);
			}
		}
		classPath = sb.toString();
	}

	private static Class<?> getComplieClass() throws URISyntaxException, ReflectionException{
		try{
			Class<?> c=Class.forName(JAVAC);
			return c;
		}catch(ClassNotFoundException e){
		}
		
		URL url = Launcher.getBootstrapClassPath().getURLs()[0];
		File file = new File(url.toURI().getPath());
		File jar = new File(file.getParent() + "/tools.jar");
		if (!jar.exists()) {
			jar = new File(file.getParent() + "/../../lib/tools.jar");
		}
		if (!jar.exists()) {
			System.out.println(jar.getAbsolutePath());
			throw new ReflectionException(new IOException("Your are using a JRE, please move to a JDK."));
		}
		ClassLoaderUtil.addClassPath(jar);
		Class<?> complier;
		try {
			complier = ClassLoaderUtil.getAppClassLoader().loadClass("com.sun.tools.javac.Main");
			return complier;
		} catch (ClassNotFoundException e) {
			throw new ReflectionException(e,JAVAC+" not found.");
		}
	}

	public ClassLoader getClassLoader() {
		return this.memLoader;
	}

	public void resetClassLoader() {
		this.memLoader=new DynamicClassLoader();
	}
}

