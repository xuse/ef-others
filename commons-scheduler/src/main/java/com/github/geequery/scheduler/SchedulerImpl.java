package com.github.geequery.scheduler;

import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 任务管理器
 * </p>
 * 
 * @version V1.0
 */
final class SchedulerImpl {

	private static final Logger log = LoggerFactory.getLogger("hv-scheduler.Scheduler");
	/**
	 * 世界时区
	 */
	private TimeZone timezone = TimeZone.getDefault();
	/**
	 * 状态标志，如果true表示任务调度运行
	 */
	private transient volatile boolean started = false;
	/**
	 * 内存任务集
	 */
	private JobCollection jobCollection = new JobCollection();
	/**
	 * 时间线程
	 */
	private TimerDaemon timer = null;

	/**
	 * 线程池,默认使用缓存线程池{Executors.newCachedThreadPool()}
	 */
	private ExecutorService executor = Executors.newCachedThreadPool();
	/**
	 * 内部锁，用于同步status-aware操作
	 */
	private Object lock = new Object();

	/**
	 * 创建任务调度和初始化
	 */
	public SchedulerImpl() {
	}

	/**
	 * 构造
	 * 
	 * @param executor
	 *            线程池
	 * @see Executor
	 */
	public SchedulerImpl(ExecutorService executor) {
		this.executor = executor;
	}

	/**
	 * 设置任务执行所使用的线程池
	 * 
	 * @param executor
	 */
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	/**
	 * 设置世界时区
	 * 
	 * @author shanguoming 2012-9-21 上午11:32:44
	 * @param timezone
	 *            世界时区
	 */
	public void setTimeZone(TimeZone timezone) {
		if(timezone==null)return;
		this.timezone = timezone;
	}

	/**
	 * 获取设置的世界时区
	 * 
	 * @return 世界时区
	 */
	public TimeZone getTimeZone() {
		return timezone; 
	}

	/**
	 * 判断任务调度是否启动
	 * 
	 * @return true or false.
	 */
	public boolean isStarted() {
		synchronized (lock) {
			return started;
		}
	}

	/**
	 * 增设调度任务
	 * 
	 * @author shanguoming 2012-9-24 下午3:59:48
	 * @param schedulingPattern
	 *            '秒 分 时 日 月 周'
	 * @param job
	 *            任务对象
	 * @return
	 * @throws InvalidPatternException
	 */
	public String addSchedule(String id, Job job, String pattern) throws InvalidPatternException {
		if (null != job) {
			return addSchedule(new JobContextImpl(job, pattern, id));
		}
		return null;
	}

	/**
	 * 添加调度任务
	 * 
	 * @author shanguoming 2012-12-6 下午3:38:37
	 * @param schedulingPattern
	 * @param jobContext
	 * @return
	 */
	private String addSchedule(JobContextImpl jobContext) {
		return jobCollection.add(jobContext);
	}

	/**
	 * 根据任务ID改变任务调度模式
	 * 
	 * @author shanguoming 2012-9-24 下午3:13:36
	 * @param id
	 * @param schedulingPattern
	 *            调度模式
	 * @throws InvalidPatternException
	 */
	public void reschedule(String id, String schedulingPattern) throws InvalidPatternException {
		jobCollection.updatePattern(id, schedulingPattern);
	}

	/**
	 * 根据任务ID删除任务
	 * 
	 * @author shanguoming 2012-9-24 下午3:15:26
	 * @param id
	 */
	public void deschedule(String id) {
		jobCollection.remove(id);
	}
	
	/**
	 * 得到一个Job的上下文
	 * @param id
	 * @return
	 */
	final JobContextImpl getJob(String id) {
		return jobCollection.getJobContext(id);
	}

	/**
	 * 启动任务调度
	 * 
	 * @throws IllegalStateException
	 */
	public void start() throws IllegalStateException {
		synchronized (lock) {
			if (started) {
				throw new IllegalStateException("Scheduler already started");
			}
			timer = new TimerDaemon(this);
			timer.start();
			started = true;
		}
	}

	/**
	 * 停止任务调度
	 * 
	 * @author shanguoming 2012-9-24 下午3:21:12
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 */
	public void stop() throws IllegalStateException, InterruptedException {
		synchronized (lock) {
			if (!started) {
				throw new IllegalStateException("Scheduler not started");
			}
			if (timer != null) {
				timer.interrupt();
			}
			timer = null;
			started = false;
			executor.shutdown();
		}
	}

	/**
	 * 任务轮询检测
	 * 
	 * @author shanguoming 2012-9-24 下午3:21:38
	 * @param referenceTimeInMillis
	 * @return
	 */
	void spawnLauncher(long referenceTimeInMillis) {
		if (jobCollection.isEmpty() || !started) {
			return;
		}
		for (JobContextImpl job : jobCollection.getAllJobContext()) {
			try {
				SchedulingPattern pattern = job.getSchedulingPattern();
				if (null != pattern && pattern.match(getTimeZone(), referenceTimeInMillis)) {
					if (job.isRuning()) {
						log.info("任务{}:正在运行......", job.getId());
					} else {
						log.info("任务{}:满足[{}]表达式要求，任务开始执行。。。。。。", job.getId(), pattern.toString());
						spawnExecutor(job, referenceTimeInMillis);
					}
				}
			} catch (Exception e) {
				log.error("任务{}执行异常:", job.getId(), e);
			}
		}
	}

	/**
	 * 执行给定的任务
	 * 
	 * @author shanguoming 2012-9-24 下午3:22:20
	 * @param jobContext
	 *            任务对象
	 * @param 触发任务的时间点
	 */
	private void spawnExecutor(JobContextImpl jobContext, long referenceTimeInMillis) {
		if (jobContext == null)
			return;
		TimerEvent event = new TimerEvent(referenceTimeInMillis, timezone);
		execute(jobContext, event);
	}

	final void execute(JobContext jobContext, TriggerEvent event) {
		JobExecutor e = new JobExecutor(jobContext, event);
		if (this.executor != null) {
			executor.execute(e);
		} else {
			// 直接在新线程中运行
			e.start();
		}
	}


	@Override
	public String toString() {
		return "hv-scheduler-" + hashCode();
	}

	/**
	 * 得到所有的任务上下文
	 * 
	 * @return
	 */
	public List<JobContext> getAllJobs() {
		return jobCollection.safeList();
	}
}
