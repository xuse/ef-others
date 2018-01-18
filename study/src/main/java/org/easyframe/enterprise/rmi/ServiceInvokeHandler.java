package org.easyframe.enterprise.rmi;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;

public class ServiceInvokeHandler implements InvocationHandler{
	private Object service;
	private Class<?> rawIntdf;
	private Map<Method,Method> methodChange=new ConcurrentHashMap<Method,Method>();
	
	
	public ServiceInvokeHandler(Object service,Class<?> rawIntdf){
		this.service=service;
		this.rawIntdf=rawIntdf;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Assert.notNull(service);
		int n=0;
		List<Object> outParams=new ArrayList<Object>();
		for(Annotation[] annos: method.getParameterAnnotations()){
			if(RMIUtils.hasOutParam(annos)){
				outParams.add(args[n]);	
			}
			n++;
		}
		Method m=getTransMethod(method);
		if(outParams.isEmpty()){
			return m.invoke(service, args);
		}
		
		
		Object[] v=new Object[outParams.size()+1];
		v[0]=m.invoke(service, args);
		for(int i=0;i<outParams.size();i++){
			v[i+1]=outParams.get(i);
		}
		return v;
	}
	
	private Method getTransMethod(Method method) {
		Method m=methodChange.get(method);
		if(m!=null)return m;
		try {
			m=rawIntdf.getMethod(method.getName(), method.getParameterTypes());
			methodChange.put(method, m);
			return m;	
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		
	}
}
