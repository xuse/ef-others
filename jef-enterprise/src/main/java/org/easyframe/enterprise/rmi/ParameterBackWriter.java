package org.easyframe.enterprise.rmi;

import java.util.Collection;
import java.util.Map;

public interface ParameterBackWriter {
	void copy(Object source,Object target);
	
	
	static class _M implements ParameterBackWriter{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void copy(Object source, Object target) {
			Map m1=(Map)source;
			Map m2=(Map)target;
			m2.clear();
			m2.putAll(m1);
		}
	}
	static class _C implements ParameterBackWriter{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void copy(Object source, Object target) {
			Collection c1=(Collection)source;
			Collection c2=(Collection)target;
			c2.clear();
			c2.addAll(c1);
		}
	}
}
