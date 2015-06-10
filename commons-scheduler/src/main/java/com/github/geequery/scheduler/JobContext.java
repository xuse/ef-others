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

//	/**
//	 * 得到本次执行的情况 获得任务的触发时间。(毫秒数)
//	 * <P>
//	 * 任务触发时间和任务的实际运行时间是不等同的。
//	 * <p>
//	 * 按框架实现，框架计时器以1秒的间隔去检查有无需要执行的任务。以开始检查的时间戳作为<tt>触发时间</tt>。<br>
//	 * 任务实际在判断是否需要执行时，已经在触发时间之后了。<br>
//	 * 之后、当任务被判断需要执行，通过调度线程获取CPU时间片并开始执行，此时往往已经明显滞后于触发时间。
//	 * <p>
//	 * 在一些精确控制时间的任务中，通过此方法可以获得任务的触发时间。此时再取系统时间，往往会大于触发时间。
//	 * 
//	 * @return 任务的触发时间，如果任务未被触发过，返回0
//	 */
//	public ExecutionState getRunningState();

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
	public Job getTaskEntity();

	/**
	 * 更新任务调度计划
	 * 
	 * @param schedulingPattern
	 *            任务执行计划表达式
	 */
	public void setSchedulingPattern(String schedulingPattern);
}
