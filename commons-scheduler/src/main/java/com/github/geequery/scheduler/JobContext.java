package com.github.geequery.scheduler;

import java.util.Queue;

/**
 * 任务执行调度上下文信息
 * 
 * @author jiyi
 * @see #getReferenceTimeInMillis()
 * @see #getLastFireTime()
 * @see #getLastPeriod()
 * @see #getExecuteTimes()
 */
public interface JobContext {

	/**
	 * 获得任务的运行次数。
	 * 
	 * @return 运行次数，首次运行时返回1，未运行时返回0。
	 */
	int getExecuteTimes();

	/**
	 * 得到最近n次的运行状态
	 * 
	 * @return
	 */
	public Queue<ExecutionState> getHistory();

	/**
	 * 任务是否正在运行
	 * 
	 * @return
	 */
	public boolean isRuning();

	/**
	 * 获得任务ID
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * 获得任务对象
	 * 
	 * @return
	 */
	public Job getJob();

	/**
	 * 更新任务调度计划
	 * 
	 * @param schedulingPattern
	 *            任务执行计划表达式
	 */
	public void setSchedulingPattern(String schedulingPattern);
	
	/**
	 * 任务执行
	 * @param event
	 */
	void execute(TriggerEvent event);
}
