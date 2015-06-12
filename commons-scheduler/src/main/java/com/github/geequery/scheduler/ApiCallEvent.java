package com.github.geequery.scheduler;

/**
 * 描述由API调用所执行的事件
 * 
 * @author jiyi
 *
 */
public class ApiCallEvent implements TriggerEvent {
	/**
	 * 当发生调用时的线程堆栈
	 */
	private StackTraceElement[] trace;
	/**
	 * 调用时间
	 */
	private long triggerTime;

	public ApiCallEvent(StackTraceElement[] trace) {
		this.trace = trace;
		this.triggerTime=System.currentTimeMillis();
	}

	@Override
	public RejectPolicy getRejectPolicy() {
		return RejectPolicy.Discard;
	}

	/**
	 * 当发生调用时的线程堆栈
	 * @return
	 */
	public StackTraceElement[] getTrace() {
		return trace;
	}

	/**
	 * 调用时间
	 * @return
	 */
	public long getTriggerTime() {
		return triggerTime;
	}

	@Override
	public String toString() {
		return getClass().getName()+" from "+	trace[1];
	}
	
	
}


