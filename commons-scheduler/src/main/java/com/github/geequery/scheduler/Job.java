package com.github.geequery.scheduler;


/**
 * Job接口，实现此接口的即为任务 
 */
public interface Job {
	/**
	 * 获取任务Name，仅用于显示，不是任务ID
	 * 
	 * @author shanguoming 2014年12月23日 上午11:41:42
	 * @return
	 * @modify: {原因} by shanguoming 2014年12月23日 上午11:41:42
	 */
	String getName();

	/**
	 * 执行任务
	 * @param 任务事件对象
	 */
	abstract Object run(TriggerEvent event) throws Exception;

	/**
	 * 是否允许指定的事件并发执行。
	 * 
	 * @return
	 */
	boolean allowConcurent(TriggerEvent event);
}
