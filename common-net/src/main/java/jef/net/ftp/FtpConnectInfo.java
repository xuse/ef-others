package jef.net.ftp;

import java.io.Serializable;

public class FtpConnectInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String host;
	private int port=21;
	private String user;
	private String password;
	private String homeDir;
	private boolean isSsh;
	
	public FtpConnectInfo(){
	}
	public FtpConnectInfo(String host,String user,String password,String home){
		this.host=host;
		this.user=user;
		this.password=password;
		this.homeDir=home;
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getHomeDir() {
		return homeDir;
	}
	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
	}
	public boolean isSsh() {
		return isSsh;
	}
	public void setSsh(boolean isSsh) {
		this.isSsh = isSsh;
	}
}
