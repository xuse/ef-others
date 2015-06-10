package com.github.geequery.scheduler;

/**
 * 实现这个接口的任务可以获得任务执行基本信息。 这个接口继承了Job
 * @author jiyi
 * @see Job
 */
public interface ListenableJob extends Job {
	
	/**
	 * scheduler会将任务执行上下文(JobContext)注入到用户自行实现的Job当中。 用户可以从 {@link JobContext}对象中获得任务执行的次数、时间等统计信息
	 * @param context 任务上下文
	 * @see JobContext
	 */
	void setJobContext(JobContext context);
}
