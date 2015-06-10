package com.github.geequery.scheduler;

/**
 * 任务的触发事件
 * 
 * @version V1.0
 */
public interface TriggerEvent {
	/**
	 * 如果当前Job正在执行，传入的事件该如何处理，决定此策略。
	 * 
	 * @see RejectPolicy
	 * 
	 * @return
	 */
	RejectPolicy getRejectPolicy();
}
