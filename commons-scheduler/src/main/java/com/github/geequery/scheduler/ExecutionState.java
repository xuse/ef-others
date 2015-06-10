package com.github.geequery.scheduler;

import org.slf4j.Logger;

/**
 * 任务的执行状态记录
 * 
 * @author jiyi
 *
 */
public final class ExecutionState {
	/**
	 * 任务次数，从1开始
	 */
	private int serial;
	/**
	 * 启动时间
	 */
	private long fireTime;
	/**
	 * 结束时间
	 */
	private volatile long endTime;
	/**
	 * 成功标志
	 */
	private boolean success;
	/**
	 * 错误异常
	 */
	private Throwable exception;

	/**
	 * 执行结果
	 */
	private Object result;

	/**
	 * 执行完成后的回调
	 */
	FeedbackEvent feedback;

	void invokeCallback(Logger log) {
		if (feedback != null) {
			try {
				if (success) {
					feedback.success(result, this);
				} else {
					feedback.executeError(this);
				}
			} catch (Exception e1) {
				log.error("执行回调任务[{}]的回调方法异常", feedback);
			}
			// 回调执行完后即释放掉
			feedback = null;
		}
	}

	/**
	 * 构造
	 * 
	 * @param serial
	 */
	ExecutionState(int serial) {
		this.serial = serial;
		this.fireTime = System.currentTimeMillis();
	}

	/**
	 * 标记运行失败
	 * 
	 * @param ex
	 */
	void endWithException(Throwable ex) {
		this.exception = ex;
		this.endTime = System.currentTimeMillis();
	}

	/**
	 * 标记运行成功
	 */
	void endSuccess(Object result) {
		this.success = true;
		this.result = result;
		this.endTime = System.currentTimeMillis();
	}

	/**
	 * 得到执行次数的序号，第一次执行时为1
	 * 
	 * @return
	 */
	public int getSerial() {
		return serial;
	}

	/**
	 * 执行开始时间
	 * 
	 * @return
	 */
	public long getFireTime() {
		return fireTime;
	}

	/**
	 * 执行结束时间
	 * 
	 * @return
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * 成功标志
	 * 
	 * @return 只有运行结束并成功后才返回true，如果还在运行中返回false
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * 执行时的异常
	 * 
	 * @return
	 */
	public Throwable getException() {
		return exception;
	}

	/**
	 * 是否结束
	 * 
	 * @return true执行结束，false执行未结束。
	 */
	public boolean isFinished() {
		return endTime != 0;
	}

	/**
	 * 获得执行结果
	 * 
	 * @return
	 */
	public Object getResult() {
		return result;
	}

}
