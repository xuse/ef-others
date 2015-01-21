package jef.http.server;

import java.io.File;

import jef.http.server.actions.HttpAction;

/**
 * 最基本行为的HTTP服务器
 * @author jiyi
 *
 */
public interface HttpServer {
	/**
	 * 启用服务
	 */
	void start();
	/**
	 * 关闭服务
	 */
	void stop();
	/**
	 * 获得端口
	 * @return
	 */
	int getPort();
	
	
	void addResourceContext(String string, File file);
	
	void addAction(String string, HttpAction aboutAction);
}
