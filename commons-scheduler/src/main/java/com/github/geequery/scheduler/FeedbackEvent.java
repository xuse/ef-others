package com.github.geequery.scheduler;

/**
 * 可以监听任务执行结果的Event对象
 * @version V1.0
 */
public interface FeedbackEvent extends TriggerEvent{
	
	/**
	 * 任务正常结束时触发
	 * @param result
	 * @param state
	 */
	void success(Object result,ExecutionState state);
	
	/**
	 * 任务异常退出时触发。
	 * @param state
	 */
	void executeError(ExecutionState state);
}
