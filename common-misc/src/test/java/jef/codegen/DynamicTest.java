package jef.codegen;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;

import javax.management.ReflectionException;

import jef.codegen.ast.JavaMethod;
import jef.codegen.ast.JavaUnit;
import jef.codegen.support.OverWrittenMode;
import jef.common.log.LogUtil;
import jef.dynamic.DefaultCompiler;
import jef.tools.ThreadUtils;
import jef.tools.reflect.ClassLoaderUtil;
import jef.tools.reflect.ClassEx;

public class DynamicTest {

	public static void main(String... ps) throws Exception {

		Thread t2 = new Thread() {
			@Override
			public void run() {
				try {
					example();
				} catch (Exception e) {
					LogUtil.exception(e);
				}
			}

		};
		t2.start();
		// System.out.println("Out test:");
		// Class c=Class.forName("pkg.TestJavaMain");
		// System.out.println(c);
	}

	/**
	 * 使用举例：现场生成一个Java文件，编译后立刻载入执行
	 * 
	 * @throws ReflectionException
	 * @throws IOException
	 */
	public static void example() throws ReflectionException, IOException {
		File src = new File("c:/123");
		JavaUnit java = new JavaUnit("pkg", "TestJava");

		java.addImport("java.util.HashMap");
		java.addField(Modifier.PRIVATE, String.class, "name=\"张三\"");
		JavaMethod method = new JavaMethod("test");
		java.addMethod(method);
		method.addparam(String.class, "var");
		method.addparam(Integer.class, "num");
		method.addContent("System.out.println(name+\",\"+var);");
		method.addContent("System.out.print(\"十年后是\"+(num+10)+\"岁。\");");

		 File saved = java.saveToSrcFolder(src,"UTF-8",OverWrittenMode.AUTO);
		 String className = java.getClassName();
		 jef.dynamic.Compiler tool = prepareCompileTools(ClassLoaderUtil.getAppClassLoader());
		 ClassEx r = tool.complieAndLoad(saved, className);
		 r.invokeWithNewInstance("test","你好",12);

	}
	
	/**
	 * 正常情况下只有JDK才有Tools.jar的编译工具包，默认是不加载到项目中的，这里通过动态加载功能，让程序具备动态编译Java文件的能力
	 * 
	 * @return Complier对象，编译工具对象
	 * @throws IOException
	 */
	public static jef.dynamic.Compiler prepareCompileTools(ClassLoader sysloader, String... name) throws ReflectionException {
		return new DefaultCompiler(sysloader);
	}


	// @SuppressWarnings("unchecked")
	public static void example2() throws Exception {
		JavaUnit java = new JavaUnit("pkg", "TestJava");

		java.addImport("java.util.HashMap");
		java.addImport("jef.tools.StringUtils");
		java.addField(Modifier.PRIVATE, String.class, "name=\"来了\"");
		java.addContent("public void test(String var, Integer num) {");
		java.addContent("  System.out.println(name+\",\"+var);");
		java.addContent("  System.out.println(StringUtils.toInt(\"12\",-1));");
		java.addContent("  System.out.print(\"而是年后是\"+(num+10)+\"岁。\");");
		java.addContent("}");

		JavaUnit java2 = new JavaUnit("pkg", "TestJavaMain");
		java2.addContent("public void main() {");
		java2.addContent("  TestJava t=new TestJava();");
		java2.addContent("  t.test(\"ssssss\",50);");
		java2.addContent("}");

		jef.dynamic.Compiler cc = prepareCompileTools(ClassLoaderUtil.getAppClassLoader(), "ant");

		ClassEx c= cc.complieAndLoad(java.asSourceFile(), java.getClassName());
//		for (ClassEx c : cs) {
//			if (c.getSimpleName().equals("TestJavaMain")) {
//				c.invokeWithNewInstance("main");
//			}
//		}
//		cs = null;

		ThreadUtils.doSleep(1000);
		JavaUnit java3 = new JavaUnit("pkg", "TestJavaMain");
		java3.addContent("public void main() {");
		java3.addContent("  TestJava t=new TestJava();");
		java3.addContent("  t.test(\"重新载入后\",10);");
		java3.addContent("}");
//		ClassEx[] c1 = cc.complieAndLoad(java3, java);
//		for (ClassEx c : c1) {
//			if (c.getSimpleName().equals("TestJavaMain")) {
//				c.invokeWithNewInstance("main");
//			}
//		}
	}
}
