package org.easyframe.rmi;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class TestServiceImpl implements TestService {
	
	private String id;

	public Map<String, Integer> getValue(Map<String, Object> input) {
		input.put("AAA", "BBBB");
		return Collections.singletonMap("avbn", 1);
	}

	public String getName() {
		return id;
	}

	public Foo getFoo(Foo in) {
		Foo foo=new Foo();
		foo.setId(1);
		foo.setName("Name1");
		foo.setModified(new Date());
		return foo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}

