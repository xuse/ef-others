package jef.http.server.jdk;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jef.http.server.actions.HttpAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

@SuppressWarnings("restriction")
public class HtttpServerJDK6Impl implements jef.http.server.HttpServer {
	private final Map<String,HttpHandler> context=new HashMap<String,HttpHandler>();
	private int port= 80;
	private int maxRequest = 100;
	private Logger log=LoggerFactory.getLogger(this.getClass());
	private ExecutorService executor;
	private HttpServer server;
	
	public int getMaxRequest() {
		return maxRequest;
	}

	public void setMaxRequest(int maxRequest) {
		this.maxRequest = maxRequest;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void doStart() throws IOException {
		HttpServerProvider provider = HttpServerProvider.provider();
		server = provider.createHttpServer(new InetSocketAddress(port), maxRequest);// 监听端口6666,能同时接
											
		for(Entry<String,HttpHandler> entry:context.entrySet()){
			server.createContext(entry.getKey(), entry.getValue());
		}
		if(executor==null){
			executor=Executors.newCachedThreadPool();
		}
		server.setExecutor(executor);
		server.start();
		log.info("Http Server started at port {}",port);
	}

	public void stop() {
		executor.shutdown();
		server.stop(1000);
	}

	public int getPort() {
		return port;
	}

	public void start() {
		try {
			doStart();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public void addAction(String context,HttpAction action){
		this.context.put(context,new ActionHandler(context,action));
	}
	
	public void addResourceContext(String context,File file){
		if(file.isDirectory()){
			ResourceHandler handler=new ResourceHandler(context,file);
			this.context.put(context, handler);	
		}else{
			throw new IllegalArgumentException(file.getAbsolutePath()+" is not a directory.");
		}
		
	}
}
