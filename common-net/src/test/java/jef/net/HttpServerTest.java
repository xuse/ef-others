package jef.net;

import java.io.File;

import jef.http.server.HttpServer;
import jef.http.server.actions.AboutAction;
import jef.http.server.actions.DirManagerAction;
import jef.http.server.actions.PasswordPicAction;
import jef.http.server.jdk.HtttpServerJDK6Impl;

/**
 * 启动HTTP服务，调试用
 * 
 * @author jiyi
 * 
 */
public class HttpServerTest {
	public static void main(String[] args) {
		{
			HttpServer server =new HtttpServerJDK6Impl();
			server.addResourceContext("/c", new File("c:/"));
			server.addAction("/about", new AboutAction());
			server.addAction("/d", new DirManagerAction(new File("d:/")));
			server.addAction("/pass", new PasswordPicAction());
			server.start();	
		}
//		{
//			JettyServer server=new JettyServer();
//			server.addResourceContext("/c", new File("c:/"));
//			server.addAction("/about", new AboutAction());
//			server.addAction("/d", new DirManagerAction());
//			server.addAction("/pass", new PasswordPicAction());
//			server.start();	
//			
//		}
		
		
	}
}
