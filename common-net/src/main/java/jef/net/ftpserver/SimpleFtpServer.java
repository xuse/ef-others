package jef.net.ftpserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jef.common.log.LogUtil;
import jef.net.ftpserver.ftplet.FtpException;
import jef.net.ftpserver.ftplet.User;
import jef.net.ftpserver.listener.Listener;
import jef.net.ftpserver.listener.ListenerFactory;
import jef.net.ftpserver.usermanager.UserFactory;
import jef.tools.Assert;

/**
 * 简易的Ftp服务器接口，可以快速用API创建FTP服务
 * @author Administrator
 *
 */
public class SimpleFtpServer implements FtpServer {
	private int port=21;
	private String home;
	private Map<String,String> users;
	private boolean allowAnonymous=true;
	private FtpServer server;
	
	/**
	 * 空构造
	 */
	public SimpleFtpServer(){
	}
	
	/**
	 * 构造，指定端口
	 * @param port
	 */
	public SimpleFtpServer(int port){
		this(port,null);
	}
	/**
	 * 构造，指定端口和主目录
	 * @param port
	 * @param home
	 */
	public SimpleFtpServer(int port,String home){
		if(port>0){
			this.port=port;
		}
		this.home=home;
	}
	/**
	 * 返回端口
	 * @return
	 */
	public int getPort() {
		return port;
	}
	/**
	 * 设置端口
	 * @param port
	 */
	public void setPort(int port) {
		Assert.isNull(server,"Can not change a active server!");
		this.port = port;
	}
	/**
	 * 返回根目录
	 * @return
	 */
	public String getHome() {
		return home;
	}
	/**
	 * 设置根目录
	 * @param home
	 */
	public void setHome(String home) {
		Assert.isNull(server,"Can not change a active server!");
		this.home = home;
	}
	/**
	 * 获得所有用户
	 * @return
	 */
	public Map<String, String> getUsers() {
		if(users==null)return Collections.emptyMap();
		return users;
	}
	/**
	 * 设置用户
	 * @param users
	 */
	public void setUsers(Map<String, String> users) {
		Assert.isNull(server,"Can not change a active server!");
		this.users = users;
	}
	/**
	 * 添加一个用户
	 * @param name
	 * @param password
	 */
	public void addUser(String name,String password) {
		Assert.isNull(server,"Can not change a active server!");
		if(users==null){
			users=new HashMap<String,String>();
		}
		users.put(name, password);
	}
	/**
	 * 是否允许匿名用户
	 * @return
	 */
	public boolean isAllowAnonymous() {
		return allowAnonymous;
	}
	/**
	 * 设置允许匿名用户与否
	 * @param allowAnonymous
	 */
	public void setAllowAnonymous(boolean allowAnonymous) {
		Assert.isNull(server,"Can not change a active server!");
		this.allowAnonymous = allowAnonymous;
	}
	
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftpserver.FtpServer#start()
	 */
	public void start(){
		FtpServerFactory f=new FtpServerFactory();
		UserFactory uf=new UserFactory();
		if(home==null){
			home=System.getProperty("user.dir");	
		}
		uf.setHomeDirectory(home);
		//处理匿名登录问题
		if(allowAnonymous){
			uf.setName("anonymous");
			addUser(f,uf.createUser());
		}else{
			ConnectionConfigFactory cf=new ConnectionConfigFactory();
			cf.setAnonymousLoginEnabled(false);
			f.setConnectionConfig(cf.createConnectionConfig());
		}
		if(users!=null){
			for(Entry<String,String> user:users.entrySet()){
				uf.setName(user.getKey());
				uf.setPassword(user.getValue());
				addUser(f, uf.createUser());
			}	
		}
		if(port>0){
			ListenerFactory lf=new ListenerFactory();
			lf.setPort(port);
			Listener ls=lf.createListener();
			f.getListeners().put("default", ls);	
		}
		server=f.createServer();
		try {
			LogUtil.info("SimpleFtp server at port "+ port +" starting...");
			server.start();
		} catch (FtpException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void addUser(FtpServerFactory f,User user) {
		LogUtil.show("user: "+user+" created.");
		try {
			f.getUserManager().save(user);
		} catch (FtpException e) {
			throw new IllegalArgumentException(e);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftpserver.FtpServer#suspend()
	 */
	public void suspend(){
		server.suspend();
	}
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftpserver.FtpServer#resume()
	 */
	public void resume(){
		server.resume();
	}
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftpserver.FtpServer#stop()
	 */
	public void stop() {
		server.stop();
	}
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftpserver.FtpServer#isStopped()
	 */
	public boolean isStopped() {
		return server.isStopped();
	}
	/*
	 * (non-Javadoc)
	 * @see jef.net.ftpserver.FtpServer#isSuspended()
	 */
	public boolean isSuspended() {
		return server.isSuspended();
	}
}
