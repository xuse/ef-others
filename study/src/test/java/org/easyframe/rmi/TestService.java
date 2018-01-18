package org.easyframe.rmi;

import java.util.Map;

import jef.common.annotation.InOut;

public interface TestService {
	public Map<String,Integer> getValue(@InOut Map<String,Object> input);
	
	public String getName();
	
	public Foo getFoo(Foo in);

}
