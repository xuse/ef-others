package org.easyframe.enterprise.rmi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jef.tools.reflect.BeanUtils;

import org.springframework.util.Assert;

public class ClientInvokeHandler implements InvocationHandler{
	private Object remoteService;
	private Class<?> newIntef;
	private Map<Method,Method> methodChange=new ConcurrentHashMap<Method,Method>();
	
	
	public ClientInvokeHandler(Object innerService,Class<?> newIntf) {
		this.remoteService=innerService;
		this.newIntef=newIntf;
	}
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Assert.notNull(remoteService);
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
			return m.invoke(remoteService, args);
		}
		
		Object[] v=(Object[]) m.invoke(remoteService,args);
		Object v0=v[0];//result;
		for(int i=0;i<outParams.size();i++){
			Object changedObject=v[i+1];
			copyValue(changedObject,outParams.get(i));
		}
		return v0;
	}
	private Method getTransMethod(Method method) {
		Method m=methodChange.get(method);
		if(m!=null)return m;
		try {
			m=newIntef.getMethod(method.getName(), method.getParameterTypes());
			methodChange.put(method, m);
			return m;	
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		
	}
	private void copyValue(Object source, Object target) {
		if(source==null || target==null)return;
		ParameterBackWriter w=null;
		if(source instanceof Map){
			w=RMIUtils.getParamBackWriter(Map.class);
		}else if(source instanceof Collection){
			w=RMIUtils.getParamBackWriter(Collection.class);
		}else if(source.getClass().isArray()){
			int l1=Array.getLength(source);
			int l2=Array.getLength(target);
			System.arraycopy(source, 0, target, 0, Math.min(l1, l2));
			return;
		}
		if(w!=null){
			w.copy(source, target);
		}else{
			BeanUtils.copyProperties(source, target);
		}
	}
}
