package org.easyframe.enterprise.rmi;

import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.remoting.rmi.RmiClientInterceptor;

public class RmiProxyFactoryBean extends RmiClientInterceptor implements FactoryBean<Object>, InitializingBean {
	private Object serviceProxy;
	private boolean isSingle;
	private LoadBalanceManager manager;
	
	

	public void afterPropertiesSet() {
		List<String> result = new ArrayList<String>();
		for (String s : StringUtils.split(this.getServiceUrl(), ";")) {
			s = s.trim();
			if (s.length() > 0) {
				result.add(s);
			}
		}
		String[] serviceUrls = result.toArray(new String[result.size()]);
		this.isSingle = serviceUrls.length <= 1;
		
		if (!isSingle) { // to support load balance
			this.setCacheStub(false);
			this.setLookupStubOnStartup(false);
			manager=new DefaultLoadBalanceManager();
			manager.setUrls(serviceUrls);
		}
		super.afterPropertiesSet();
		if (getServiceInterface() == null) {
			throw new IllegalArgumentException("Property 'serviceInterface' is required");
		}
		Class<?> rawClz = this.getServiceInterface();
		Class<?> newClz = RMIUtils.getProxyInterface(rawClz);
		if (newClz == rawClz) { // 没变化的情况下直接用原来的
			serviceProxy = new ProxyFactory(rawClz, this).getProxy(getBeanClassLoader());
		} else {
			ClientInvokeHandler handler = new ClientInvokeHandler(new ProxyFactory(newClz, this).getProxy(getBeanClassLoader()), newClz);
			serviceProxy = Proxy.newProxyInstance(getBeanClassLoader(), new Class[] { rawClz }, handler);
		}
	}

	public Object getObject() {
		return this.serviceProxy;
	}

	public Class<?> getObjectType() {
		return getServiceInterface();
	}

	public boolean isSingleton() {
		return true;
	}

	
	/**
	 * Fetches an RMI stub and delegates to {@code doInvoke}.
	 * If configured to refresh on connect failure, it will call
	 * {@link #refreshAndRetry} on corresponding RMI exceptions.
	 * @see #getStub
	 * @see #doInvoke(MethodInvocation, Remote)
	 * @see #refreshAndRetry
	 * @see java.rmi.ConnectException
	 * @see java.rmi.ConnectIOException
	 * @see java.rmi.NoSuchObjectException
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Remote stub = getStub();
		try {
			return doInvoke(invocation, stub);
		}
		catch (RemoteConnectFailureException ex) {
			return handleRemoteConnectFailure(invocation, ex, stub);
		}
		catch (RemoteException ex) {
			if (isConnectFailure(ex)) {
				return handleRemoteConnectFailure(invocation, ex, stub);
			}
			else {
				throw ex;
			}
		}
	}
	
	//deal with connection error...
	private Object handleRemoteConnectFailure(MethodInvocation invocation, Exception ex, Remote stub) throws Throwable {
		if(isSingle){
			throw ex;
		}
		String url=manager.notifyInvalidStub(stub);
		//Retry...
		Remote freshStub = getStub();
		if(freshStub!=null){
			String msg = "Could not connect to RMI service [" + url + "] - retrying";
			if (logger.isDebugEnabled()) {
				logger.warn(msg, ex);
			}else if (logger.isWarnEnabled()) {
				logger.warn(msg);
			}
			return doInvoke(invocation, freshStub);
		}
		throw ex;
	}
	
	
	protected Remote getStub() throws RemoteLookupFailureException {
		if (isSingle)
			return super.getStub();
		return manager.getFreeStub();
	}
}
