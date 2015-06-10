package com.github.geequery.scheduler;


/**
 * Job可以接收多个触发事件 TriggerEvent。
 * 如果这些事件并行发生，此处决定其策略。
 * 
 * 由于Job本身的设计默认不允许并发。所以此处决定新的触发事件是否丢弃还是延时执行。
 * 
 * @author jiyi
 *
 */
public enum RejectPolicy {
	/**
	 * 如果任务已经很在运行，新事件直接丢弃。
	 */
	Discard,
	
	/**
	 * 如果任务已经在运行，新事件排队等待，但如果队列太长，则放弃执行。（如果事件有回调，还会回调失败接口）
	 */
	QueuedOrDisacrd,
	
	/**
	 * 如果任务已经在运行，新事件排队等待，无论等多久都要被执行。
	 */
	ForceQueue,	

	/**
	 * 如果任务已经在运行，新事件排队等待，但如果队列太长，新事件不排队直接并行执行。
	 */
	CallersRun,
}
