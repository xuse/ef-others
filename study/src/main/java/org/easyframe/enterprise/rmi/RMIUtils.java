package org.easyframe.enterprise.rmi;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jef.accelerator.asm.ASMUtils;
import jef.accelerator.asm.ClassReader;
import jef.accelerator.asm.ClassVisitor;
import jef.accelerator.asm.ClassWriter;
import jef.accelerator.asm.MethodVisitor;
import jef.accelerator.asm.Opcodes;
import jef.common.annotation.InOut;
import jef.common.annotation.Out;
import jef.tools.reflect.UnsafeUtils;

public class RMIUtils {
	
	
	public static Class<?> getProxyInterface(Class<?> intf){
		if(!intf.isInterface()){
			return intf;
		}
		
		List<Method> outParamMethod=getOutParamMethods(intf);
		if(outParamMethod.isEmpty())return intf;
		return generateNewIntf(intf,outParamMethod);
	}

	private static Class<?> generateNewIntf(Class<?> intf, List<Method> outParamMethod) {
		final String newName=intf.getName()+"_RMI";
		try{
			Class<?> clz=intf.getClassLoader().loadClass(newName);
			if(clz!=null)return clz;
		}catch(ClassNotFoundException e){
		}
		final Map<String,String> methodNames=new HashMap<String,String>();
		for(Method m:outParamMethod){
			String key=m.getName()+ASMUtils.getMethodDesc(m.getReturnType(), m.getParameterTypes());
			String value=ASMUtils.getMethodDesc(Object[].class, m.getParameterTypes());
			methodNames.put(key,value);
		}
		
		try{
			ClassReader cl=new ClassReader(intf.getName());
			ClassWriter cw=new ClassWriter(0);
			cl.accept(new ClassVisitor(Opcodes.ASM5,cw) {
				@Override
				public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
					super.visit(version, access, newName.replace('.', '/'), signature, superName, interfaces);
				}

				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
					String newDesc=methodNames.get(name+desc);
					if(newDesc!=null){
						return super.visitMethod(access, name, newDesc, null, exceptions);
					}else{
						return super.visitMethod(access, name, desc, signature, exceptions);
					}
				}
				
			}, 0);
			byte[] data=cw.toByteArray();
			//For Debug.....
			//IOUtils.saveAsFile(new File("c:/",newName+".class"), data);
			//////////////////////////
			return UnsafeUtils.defineClass(newName, data, 0, data.length, intf.getClassLoader());			
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
	}

	private static List<Method> getOutParamMethods(Class<?> intf) {
		List<Method> result=new ArrayList<Method>();
		for(Method m:intf.getMethods()){
			if(m.isSynthetic() || m.isBridge() || Modifier.isStatic(m.getModifiers())){
				continue;
			}
			boolean isOut=false;
			for(Annotation[] annos: m.getParameterAnnotations()){
				if(hasOutParam(annos)){
					isOut=true;
					break;
				}
			}
			if(isOut)
				result.add(m);
		}
		return result;
	}
	
	static boolean hasOutParam(Annotation[] annos) {
		for(Annotation ann:annos){
			Class<?> clz=ann.annotationType();
			if(clz==Out.class || clz==InOut.class){
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<Class,ParameterBackWriter> backwriters=new HashMap<Class,ParameterBackWriter>();
	static{
		backwriters.put(Map.class, new ParameterBackWriter._M());
		backwriters.put(Collections.class, new ParameterBackWriter._C());
	}
	static ParameterBackWriter getParamBackWriter(Class clz){
		return backwriters.get(clz);
	}
	
	public static void registerOutParamBackWriter(Class clz,ParameterBackWriter writer){
		synchronized (backwriters) {
			if(writer==null){
				backwriters.remove(clz);
			}else{
				backwriters.put(clz, writer);
			}
		}
	}
}
