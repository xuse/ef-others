package com.github.geequery.scheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 任务上下文的默认实现
 * </p>
 * 
 * @version V1.0
 */
final class JobContextImpl implements JobContext {

	private static final Logger log = LoggerFactory.getLogger("hv-scheduler.Task");
	

	// 运行锁
	private final ReentrantLock lock = new ReentrantLock();
	

	/**
	 * 最多记录最近10次的历史记录
	 */
	private static final int MAX_HISTORY = 10;
	/**
	 * 任务对象
	 */
	private Job job;

	/**
	 * 运行次数计数器
	 */
	private final AtomicInteger count = new AtomicInteger();

	/**
	 * 上次的执行状态
	 */
	final Queue<ExecutionState> history = new LinkedList<ExecutionState>();

	/**
	 * 当任务正在执行时，如果有人工触发，根据传入的事件，允许加入到积压队列中，以便持续处理积压的事件<br>
	 * 等待执行的队列，当人为触发时的事件被压入队列中。
	 */
	private BlockingQueue<TriggerEvent> queues = new LinkedBlockingQueue<TriggerEvent>(5);

	/**
	 * 执行计划
	 */
	private SchedulingPattern pattern;

	/**
	 * 构造
	 * 
	 * @param job
	 *            任务
	 * @throws InvalidPatternException
	 */
	public JobContextImpl(Job job) throws InvalidPatternException {
		if (job == null) {
			throw new NullPointerException("Can not input a null task entity to hv-scheduler!");
		}
		this.job = job;
		this.pattern = new SchedulingPattern(job.getDefaultSchedulingPattern());
		if (job instanceof ListenableJob) {
			((ListenableJob) job).setJobContext(this);
		}
	}

	/**
	 * 获得任务调度计划
	 * 
	 * @return 解析后的计划
	 * @see SchedulingPattern
	 */
	public SchedulingPattern getSchedulingPattern() {
		return pattern;
	}

	/**
	 * 更新任务调度计划
	 * 
	 * @param schedulingPattern
	 *            任务执行计划表达式
	 */
	public void setSchedulingPattern(String schedulingPattern) throws InvalidPatternException {
		this.pattern = new SchedulingPattern(schedulingPattern);
	}

	/**
	 * 获取任务对象
	 * 
	 * @author shanguoming 2012-9-24 下午4:04:05
	 * @return
	 * @revised 2014-3-27
	 *          Jiyi，该方法有危害性，获得TaskEntity后可以变更ID,执行计划，但Task中的pattern不会更新
	 *          。因此要注意不可随意修改pattern
	 */
	public Job getTaskEntity() {
		return job;
	}

	/**
	 * 执行任务。
	 * 
	 * 外部逻辑是允许并发的，但大部分Job都不支持并发。因此要将并行任务转换为串行任务
	 */
	public void execute(TriggerEvent event) {
		if(job.allowConcurent(event)) {
			//并发执行
			run(event).invokeCallback(log);;
		}else {
			//串行先尝试加锁
			if (lock.tryLock()) {
				List<ExecutionState> states=new ArrayList<ExecutionState>();
				try {
					states.add(run(event));
					//执行积压队列里的任务
					while ((event = queues.poll()) != null) {
						log.info("执行[{}]中排队的任务，事件类型：{}", job.getId(), event.getClass());
						states.add(run(event));
					}
				} finally {
					//解锁
					lock.unlock();
				}
				// 回调的调用过程不被包含在任务执行过程中，因此可能会并发。
				for(ExecutionState state:states) {
					state.invokeCallback(log);
				}
			} else if (null != event) {
				switch(event.getRejectPolicy()) {
				case CallersRun:
					//先尝试串行，如果积压过多直接并行。
					if (queues.offer(event)) {
						log.warn("任务[{}]正在运行，把事件[{}]放入队列", job.getId(), event.getClass().getName());
					} else {
						run(event).invokeCallback(log);
					}
					break;
				case Discard:
					//丢弃，忽略
					log.warn("任务[{}]正在运行，事件[{}]被丢弃。", job.getId(), event.getClass().getName());
					break;
				case QueuedOrDisacrd:
					//尝试串行，失败后告警并回调
					if (queues.offer(event)) {
						log.warn("任务[{}]正在运行，把事件[{}]放入队列", job.getId(), event.getClass().getName());
					} else {
						callAbort(event);
					}
					break;
				case ForceQueue:
					//尝试串行，无论等多久都要串行。
					try {
						queues.put(event);
					} catch (InterruptedException e) {
						log.error("任务在等待过程中发生错误",e);
					}
					break;
				}
			} else {
				log.warn("任务[{}]正在运行，不支持空Event事件放入队列", job.getId());
			}	
		}
		
		
	}

	private void callAbort(TriggerEvent event) {
		if(event instanceof FeedbackEvent) {
			ExecutionState state = new ExecutionState(count.incrementAndGet());
			state.endWithException(new RejectedExecutionException());
			((FeedbackEvent) event).executeError(state);
		}
	}

	private ExecutionState run(TriggerEvent taskEvent) {
		// 更新当前状态
		ExecutionState state = new ExecutionState(count.incrementAndGet());
		if (taskEvent != null && (taskEvent instanceof FeedbackEvent)) {
			state.feedback = (FeedbackEvent) taskEvent;
		}

		try {
			state.endSuccess(job.run(taskEvent));
			// 标记为成功
		} catch (Throwable e) {
			log.error("task error,task id=" + getId(), e);
			state.endWithException(e);
			// 标记为失败
		} finally {
			addHistory(state);
			log.info("任务[{}]执行完成", job.getId());
		}
		return state;
	}

	private void addHistory(ExecutionState state) {
		history.add(state);
		if (history.size() > MAX_HISTORY) {
			history.poll();
		}
	}

	/**
	 * 获取任务ID
	 * 
	 * @return
	 */
	public String getId() {
		return job.getId();
	}

	/**
	 * 任务是否运行中
	 * 
	 * @return
	 */
	public boolean isRuning() {
		//如果锁住就说明有任务在运行。
		return lock.isLocked();
	}

	@Override
	public int getExecuteTimes() {
		return count.get();
	}
	
	/**
	 * 得到最近n次的执行结果
	 * @return
	 */
	public Queue<ExecutionState> getHistory() {
		return history;
	}

	public String toString() {
		return "Task[" + job + "]";
	}
}
