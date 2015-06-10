package com.github.geequery.scheduler;

/**
 * 描述任务因为定时器而触发的事件，其中记录了满足定时器条件的时间点
 * @author jiyi
 *
 */
public final class TimerEvent implements TriggerEvent{
	private long fireTime;
	
	public TimerEvent(long fireTime) {
		this.fireTime=fireTime;
	}

	public long getFireTime() {
		return fireTime;
	}

	@Override
	public RejectPolicy getRejectPolicy() {
		return RejectPolicy.Discard;
	}
}
