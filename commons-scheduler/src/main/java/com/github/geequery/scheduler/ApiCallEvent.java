package com.github.geequery.scheduler;

/**
 * 描述由API调用所执行的事件
 * 
 * @author jiyi
 *
 */
public class ApiCallEvent implements TriggerEvent {
	/**
	 * 调用时间
	 */
	private long triggerTime;
	
	/**
	 * 任务对象
	 */
	private Object data;

	public ApiCallEvent(Object data) {
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

	/**
	 * 返回指定类型的结果，如果类型不匹配返回null.
	 * @param type 需要的类型
	 * @return null if type not match
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAs(Class<T> type) {
		if(type==null)return null;
		if(data!=null && type.isAssignableFrom(data.getClass())) {
			return (T)data;
		}else {
			return null;
		}
	}
	
	
	public Object getData() {
		return data;
	}
	
}


