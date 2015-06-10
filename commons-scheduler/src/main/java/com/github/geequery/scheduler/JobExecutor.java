package com.github.geequery.scheduler;

/**
 * <p>
 * 任务执行者
 * </p>
 * 
 * @version V1.0
 */
final class JobExecutor implements Runnable {
	/**
	 * 任务
	 */
	private JobContext context;
	private TriggerEvent event;

	JobExecutor(JobContext job, TriggerEvent event) {
		this.context = job;
		this.event = event;
	}

	public void run() {
		context.execute(event);
	}

	/**
	 * 开启一个新线程来执行
	 */
	public void start() {
		Thread t = new Thread(this);
		t.setName("[Job-" + context.getId() + "]");
		t.start();
	}
}
