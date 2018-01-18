package org.easyframe.enterprise.rmi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.RemoteLookupFailureException;

public class DefaultLoadBalanceManager implements LoadBalanceManager{
	private String[] serviceUrls;
	private Remote[] cachedStubs;
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	
	
	private ReentrantLock lock=new ReentrantLock();
	
	//the valid stub count
	private volatile int available;
	
	//Marks the remote stub is invalid;
	private Remote INVALID = new Remote() {
	};

	public void setUrls(String[] serviceUrls) {
		this.serviceUrls=serviceUrls;
		cachedStubs = new Remote[serviceUrls.length];
		available=serviceUrls.length;
	}

	public String notifyInvalidStub(Remote stub) {
		lock.lock();
		String url=null;
		try{
			for(int i=0;i<cachedStubs.length;i++){
				if(cachedStubs[i]==stub){
					cachedStubs[i]=INVALID;
					available--;
					url=serviceUrls[i];
					break;
				}
			}	
		}finally{
			lock.unlock();
		}
		return url;
	}

	public Remote getFreeStub() {
		int index = getNextRemoteIndex();
		if(index<0)return null;
		if (cachedStubs[index] == null) {
			lock.lock();
			try{
				if(cachedStubs[index]==null){
					cachedStubs[index]=createStub(serviceUrls[index]);
				}
			}finally{
				lock.unlock();
			}
		}
		return cachedStubs[index];
	}
	
	
	private Remote createStub(String urlStr) {
		try {
			URL url = new URL(null, urlStr, new DummyURLStreamHandler());
			String protocol = url.getProtocol();
			if (protocol != null && !"rmi".equals(protocol)) {
				throw new MalformedURLException("Invalid URL scheme '" + protocol + "'");
			}
			String host = url.getHost();
			int port = url.getPort();
			String name = url.getPath();
			if (name != null && name.startsWith("/")) {
				name = name.substring(1);
			}
			Registry registry = LocateRegistry.getRegistry(host, port);
			Remote stub = registry.lookup(name);
				logger.debug("Located RMI stub with URL [{}]",(Object)this.serviceUrls);
			return stub;
		} catch (MalformedURLException ex) {
			throw new RemoteLookupFailureException("Service URL [" + getServiceUrl() + "] is invalid", ex);
		} catch (NotBoundException ex) {
			throw new RemoteLookupFailureException("Could not find RMI service [" + getServiceUrl() + "] in RMI registry", ex);
		} catch (RemoteException ex) {
			throw new RemoteLookupFailureException("Lookup of RMI stub failed", ex);
		}
	}
	
	private String getServiceUrl() {
		return StringUtils.join(this.serviceUrls);
	}

	private Random random = new Random();
	private int getNextRemoteIndex() {
		if(available>1){  //正常情况下，返回随机远程对象
			int index=random.nextInt(cachedStubs.length);
			while (cachedStubs[index] == INVALID) {
				index = random.nextInt(cachedStubs.length);
			}
			return index;
		}else if(available==0){//所有远程对象都已失效，返回-1
			return -1;
		}else{
			for(int i=0;i<cachedStubs.length;i++){//找寻唯一未失效的远程对象
				if(cachedStubs[i]!=INVALID){
					return i;
				}
			}
			return -1;
		}
	}
	
	private static class DummyURLStreamHandler extends URLStreamHandler {
		@Override
		protected URLConnection openConnection(URL url) throws IOException {
			throw new UnsupportedOperationException();
		}
	}
}
