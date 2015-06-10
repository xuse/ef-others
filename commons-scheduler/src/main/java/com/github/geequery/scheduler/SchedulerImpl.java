package com.github.geequery.scheduler;

import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
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
	 * 任务的唯一编码
	 */
	private String guid = UUID.randomUUID().toString();
	/**
	 * 世界时区
	 */
	private TimeZone timezone = null;
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
	 * 获取当前任务的guid编码
	 * 
	 * @author shanguoming 2012-9-21 上午11:31:37
	 * @return guid编码
	 */
	public Object getGuid() {
		return guid;
	}

	/**
	 * 设置世界时区
	 * 
	 * @author shanguoming 2012-9-21 上午11:32:44
	 * @param timezone
	 *            世界时区
	 */
	public void setTimeZone(TimeZone timezone) {
		this.timezone = timezone;
	}

	/**
	 * 获取设置的世界时区
	 * 
	 * @return 世界时区
	 */
	public TimeZone getTimeZone() {
		return timezone != null ? timezone : TimeZone.getDefault();
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
	 * @param task
	 *            任务对象
	 * @return
	 * @throws InvalidPatternException
	 */
	public String addSchedule(Job task) throws InvalidPatternException {
		if (null != task) {
			return addSchedule(new JobContextImpl(task));
		}
		return null;
	}

	/**
	 * 添加调度任务
	 * 
	 * @author shanguoming 2012-12-6 下午3:38:37
	 * @param schedulingPattern
	 * @param task
	 * @return
	 */
	private String addSchedule(JobContextImpl task) {
		return jobCollection.add(task);
	}

	/**
	 * 更新调度任务
	 * 
	 * @author shanguoming 2014年12月22日 下午2:39:20
	 * @param task
	 * @modify: {原因} by shanguoming 2014年12月22日 下午2:39:20
	 */
	public void updateSchedule(Job task) throws InvalidPatternException {
		if (null != task) {
			jobCollection.update(new JobContextImpl(task));
		}
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
	 * 根据GUID，获取任务对象
	 * 
	 * @param id
	 * @return 任务对象
	 * @revised jiyi 2014-4-10
	 *          com.github.geequery.scheduler.Task这个类受保护外界不可见，所以还是改为直接返回TaskEntity.
	 */
	public Job getTask(String id) {
		JobContextImpl task = jobCollection.getTask(id);
		if (task == null)
			return null;
		return task.getTaskEntity();
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
		for (JobContextImpl task : jobCollection.getTasks()) {
			try {
				SchedulingPattern pattern = task.getSchedulingPattern();
				if (null != pattern && pattern.match(getTimeZone(), referenceTimeInMillis)) {
					if (task.isRuning()) {
						log.info("任务{}:正在运行......", task.getId());
					} else {
						log.info("任务{}:满足[{}]表达式要求，任务开始执行。。。。。。", task.getId(), pattern.toString());
						spawnExecutor(task, referenceTimeInMillis);
					}
				}
			} catch (Exception e) {
				log.error("任务{}执行异常:", task.getId(), e);
			}
		}
	}

	/**
	 * 执行给定的任务
	 * 
	 * @author shanguoming 2012-9-24 下午3:22:20
	 * @param task
	 *            任务对象
	 * @param 触发任务的时间点
	 */
	void spawnExecutor(JobContextImpl task, long referenceTimeInMillis) {
		if (task == null)
			return;
		TimerEvent event = new TimerEvent(referenceTimeInMillis);
		execute(task, event);
	}

	private void execute(JobContextImpl task, TriggerEvent event) {
		TaskExecutor e = new TaskExecutor(task, event);
		if (this.executor != null) {
			executor.execute(e);
		} else {
			// 直接在新线程中运行
			e.start();
		}
	}

	/**
	 * 手动触发执行
	 * @param taskId
	 * @param event
	 */
	public void executor(String taskId, TriggerEvent event) {
		JobContextImpl task = jobCollection.getTask(taskId);
		if (null != task) {
			execute(task,event);
		}
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
