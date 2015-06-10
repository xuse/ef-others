package com.github.geequery.scheduler.base;

import com.github.geequery.scheduler.JobContext;
import com.github.geequery.scheduler.ListenableJob;

/**
 * 抽象类，实现spring的InitializingBean, BeanNameAware接口
 * <p>
 * 对继承该类的子类注册到spring后，自动注册到SchedulerFactory schedulerFactory中的处理。
 * <h4>schedulingPattern的规则如下</h4>
 * <strong>5 * * * * *</strong> 每5秒 (00:01:05, 00:01:05, 00:01:05 ...) <br />
 * <strong>* * * * * *</strong> 任意秒<br />
 * <strong>* 12 * * * Mon</strong> 每周1，每小时的第12分钟 <br />
 * <strong>* 12 16 * * Mon</strong> 每周1,16点12分 <br />
 * <strong>59 11 * * * 1,2,3,4,5</strong> 周1到周5 每小时11分59秒 <br />
 * <strong>59 11 * * * 1-5</strong> 周1到周5 每小时11分59秒<br />
 * <strong>*<t>/</t>5 * * * * *</strong> 每5秒 (00:01:05, 00:01:05, 00:01:05 ...)
 * <br />
 * <strong>3-18<t>/</t>5 * * * * *</strong> 每分钟内 (00:01:03, 00:01:08, 00:01:13,
 * 00:01:18, 00:02:03, 00:02:08 ...). <br />
 * <strong>*<t>/</t>15 9-17 * * * *</strong> 每小时9分-17分之间，能被15整除的秒数<br />
 * <strong>* 12 10-16<t>/</t>2 * * *</strong> 每天10-16点，间隔2小时， 12分<br />
 * <strong>* * 12 1-15,17,20-25 * *</strong> 每月1-15号，17号，20-25号的12点 <br />
 * <strong>* 0 5 * * *|* 8 10 * * *|* 22 17 * * *</strong> 每天5点，10点8分，17点22分
 * @author shanguoming 2014年8月28日 上午9:36:54
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年8月28日 上午9:36:54
 */
public abstract class AbstractJob implements ListenableJob {
	
	private String schedulingPattern;
	private String taskId;
	/**
	 * 任务信息上下文
	 */
	protected JobContext context;
	
	@Override
	public String getDefaultSchedulingPattern() {
		return schedulingPattern;
	}
	
	public String getTaskId() {
		return taskId;
	}
	
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	@Override
	public void setJobContext(JobContext context) {
		this.context = context;
	}
	
	/**
	 * 返回TaskContext对象，
	 * 用户可以从 TaskContext对象中获得任务执行的次数、时间等统计信息
	 * @return 任务上下文
	 * @see JobContext
	 */
	public JobContext getTaskContext() {
		return context;
	}
}
