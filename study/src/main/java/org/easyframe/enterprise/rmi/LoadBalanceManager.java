package org.easyframe.enterprise.rmi;

import java.rmi.Remote;

public interface LoadBalanceManager {

	/**
	 * 设置所有可用的远程连接地址
	 * @param serviceUrls
	 */
	void setUrls(String[] serviceUrls);

	/**
	 * 当某个连接不能使用时通知
	 * @param stub
	 * @return
	 */
	String notifyInvalidStub(Remote stub);

	/**
	 * 得到要使用的远程对象
	 * @return
	 */
	Remote getFreeStub();
}
