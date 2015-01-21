package org.easyframe.rmi;

import jef.tools.ThreadUtils;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration("TestServer.xml")
public class TestServer extends AbstractJUnit4SpringContextTests{
	@Test
	public void testStartRMIService(){
		while(true){
			ThreadUtils.doSleep(2000);
		}
	}
}
