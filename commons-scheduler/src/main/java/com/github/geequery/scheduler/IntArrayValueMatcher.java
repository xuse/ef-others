package com.github.geequery.scheduler;

import java.util.ArrayList;

/**
 * <p>
 * 用于对整数进行规则验证的实现类
 * </p>
 * @author shanguoming 2012-9-24 下午2:18:06
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2012-9-24
 * @modify by reason:{方法名}:{原因}
 */
class IntArrayValueMatcher implements ValueMatcher {
	
	/**
	 * 整数数组
	 */
	private ArrayList<Integer> values;
	
	/**
	 * 创建一个新的实例IntArrayValueMatcher.
	 * @param integers
	 */
	public IntArrayValueMatcher(ArrayList<Integer> integers) {
		this.values = integers;
	}
	
	public boolean match(int value) {
		if (values != null && !values.isEmpty()) {
			for (Integer i : values) {
				if (i != null && i.intValue() == value) {
					return true;
				}
			}
		}
		return false;
	}
}
