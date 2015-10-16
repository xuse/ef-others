package com.github.geequery.scheduler;

/**
 * 描述由API调用所执行的事件
 * 
 * @author jiyi
 *
 */
public class ApiCallEvent<T> implements TriggerEvent {
	/**
	 * 调用时间
	 */
	private long triggerTime;
	
	/**
	 * 任务对象
	 */
	private T data;

	public ApiCallEvent(T data) {
		this.data=data;
		this.triggerTime=System.currentTimeMillis();
	}

	@Override
	public RejectPolicy getRejectPolicy() {
		return RejectPolicy.QueuedOrDisacrd;
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
		return String.valueOf(data);
	}

	public T getData() {
		return data;
	}
	
}


