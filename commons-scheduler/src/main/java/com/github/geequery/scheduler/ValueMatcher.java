package com.github.geequery.scheduler;

/**
 * <p>
 * 日期值匹配接口
 * </p>
 * @author shanguoming 2012-9-24 下午1:52:18
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2012-9-24
 * @modify by reason:{方法名}:{原因}
 */
interface ValueMatcher {
	
	/**
	 * 验证给定的值是否符合规则
	 * @author shanguoming 2012-9-24 下午1:52:44
	 * @param value 日期值
	 * @return
	 */
	boolean match(int value);
}
