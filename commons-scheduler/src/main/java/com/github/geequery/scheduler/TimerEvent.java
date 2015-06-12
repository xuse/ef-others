package com.github.geequery.scheduler;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * 描述任务因为定时器而触发的事件，其中记录了满足定时器条件的时间点
 * @author jiyi
 *
 */
public final class TimerEvent implements TriggerEvent{
	private long fireTime;
	private TimeZone timeZone;
	
	TimerEvent(long fireTime, TimeZone timeZone) {
		this.fireTime=fireTime;
		this.timeZone=timeZone;
	}

	/**
	 * 得到定时器满足时的事件点。
	 * 
	 * 例如某任务在 1月1日23点59分59秒触发。由于种种原因，任务执行时已经到了6月2日。此时如用代码判断new Date()，就会得到6月2日。而不是触发任务任务时的时间点。
	 * @return
	 */
	public Calendar getFireTime() {
		Calendar c=Calendar.getInstance(timeZone);
		c.setTimeInMillis(fireTime);
		return c;
	}

	@Override
	public RejectPolicy getRejectPolicy() {
		return RejectPolicy.Discard;
	}

	@Override
	public String toString() {
		
		return super.toString();
	}
}
