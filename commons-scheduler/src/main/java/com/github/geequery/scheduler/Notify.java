package com.github.geequery.scheduler;

/**
 * 提醒任务执行
 * 
 * @author jiyi
 *
 */
public class Notify implements TriggerEvent {

	public static final Notify INSTANCE = new Notify();

	protected Notify() {
	}

	@Override
	public RejectPolicy getRejectPolicy() {
		return RejectPolicy.Discard;
	}
}
