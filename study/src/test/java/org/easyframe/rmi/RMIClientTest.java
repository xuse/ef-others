package org.easyframe.rmi;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jef.tools.ThreadUtils;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration("RMIClientTest.xml")
public class RMIClientTest extends AbstractJUnit4SpringContextTests{
	@Autowired
	private TestService testService;
	
	@Test
	public void test(){
		final Map<String,AtomicInteger> map=new ConcurrentHashMap<String,AtomicInteger>();
		map.put("service instance 1", new AtomicInteger());
		map.put("service instance 2", new AtomicInteger());
		for(int i=0;i<10;i++){
			final int id=i;
			Thread t=new Thread(){

				@Override
				public void run() {
					for(int i=0;i<20;i++){
						Foo foo=testService.getFoo(new Foo());
//						System.out.println(foo);
						
						String name=testService.getName();
						map.get(name).incrementAndGet();
						
						Map<String,Object> input=new HashMap<String,Object>();
						Map<String,Integer> map=testService.getValue(input);
//						System.out.println(map);
//						System.out.println(input);	
					}
					System.out.println("线程:"+id+"完成.");
				}
				
			};
			t.start();
		}
		ThreadUtils.doSleep(2000);
		System.out.println(map);
	}
}
