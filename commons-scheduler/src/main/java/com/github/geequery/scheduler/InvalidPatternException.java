package com.github.geequery.scheduler;

/**
 * <p>
 * 无效的模式异常
 * </p>
 * @author shanguoming 2012-9-24 下午2:22:18
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2012-9-24
 * @modify by reason:{方法名}:{原因}
 */
class InvalidPatternException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	InvalidPatternException() {
	}
	
	InvalidPatternException(String message) {
		super(message);
	}
}
