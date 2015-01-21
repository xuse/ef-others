package jef.net;


import jef.net.ftpserver.SimpleFtpServer;
import jef.net.ftpserver.ftplet.FtpException;

public class FtpServerTest {
	public static void main(String[] args) throws FtpException {
		SimpleFtpServer server=new SimpleFtpServer(23,"d:/");
		server.setAllowAnonymous(false);
		server.addUser("jiyi", "123456");
		server.addUser("jef", "123");
		server.start();
	}
}
