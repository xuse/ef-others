package com.github.geequery.scheduler;

/**
 * <p>
 * 用于对*进行规则验证的实现类
 * </p>
 * @author shanguoming 2012-9-24 下午1:50:36
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2012-9-24
 * @modify by reason:{方法名}:{原因}
 */
final class AlwaysTrueValueMatcher implements ValueMatcher {
	
	/**
	 * 永远为true
	 */
	public boolean match(int value) {
		return true;
	}
}
