package org.easyframe.enterprise.rmi;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class RmiServiceExporter implements InitializingBean{
	private String serviceName;
	private Object service;
	private Class<?> serviceInterface;
	private int registryPort;
	private Object[] interceptors;
	private boolean registerTraceInterceptor;
	
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public void setService(Object service) {
		this.service = service;
	}
	public void setServiceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}
	public void setRegistryPort(int registryPort) {
		this.registryPort = registryPort;
	}
	public void setInterceptors(Object[] interceptors) {
		this.interceptors = interceptors;
	}
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(serviceInterface);
		org.springframework.remoting.rmi.RmiServiceExporter rmi=new org.springframework.remoting.rmi.RmiServiceExporter();
		rmi.setServiceName(serviceName);
		
		rmi.setRegisterTraceInterceptor(registerTraceInterceptor);
		Class<?> clz=RMIUtils.getProxyInterface(serviceInterface);

		rmi.setServiceInterface(clz);
		rmi.setInterceptors(interceptors);
		rmi.setRegistryPort(registryPort);
		if(clz!=serviceInterface){
			ServiceInvokeHandler h=new ServiceInvokeHandler(service,serviceInterface);
			rmi.setService(Proxy.newProxyInstance(serviceInterface.getClassLoader(),new Class[]{clz},h));
		}else{
			rmi.setService(service);
		}
		rmi.afterPropertiesSet();
	}
	public void setRegisterTraceInterceptor(boolean registerTraceInterceptor) {
		this.registerTraceInterceptor = registerTraceInterceptor;
	}
	

}
