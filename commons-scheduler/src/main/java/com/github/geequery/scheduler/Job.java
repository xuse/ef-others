package com.github.geequery.scheduler;


/**
 * Job接口，实现此接口的即为任务 
 */
public interface Job {

	/**
	 * 获取任务调度模式
	 * @author shanguoming 2014年12月23日 上午11:41:28
	 * @return
	 * @modify: {原因} by shanguoming 2014年12月23日 上午11:41:28
	 */
	String getDefaultSchedulingPattern();

	/**
	 * 获取任务ID，要求不能重复
	 * 
	 * @author shanguoming 2014年12月23日 上午11:41:42
	 * @return
	 * @modify: {原因} by shanguoming 2014年12月23日 上午11:41:42
	 */
	String getId();

	/**
	 * 执行任务
	 * @param 任务事件对象
	 */
	abstract Object run(TriggerEvent taskEvent) throws Exception;

	/**
	 * 是否允许指定的事件并发执行。
	 * 
	 * @return
	 */
	boolean allowConcurent(TriggerEvent event);
}
