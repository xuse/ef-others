package com.github.geequery.scheduler;


/**
 * <p>
 * 任务执行者
 * </p>
 * 
 * @version V1.0
 */
final class TaskExecutor implements Runnable {
//	private Logger log = LoggerFactory.getLogger("hv-scheduler.TaskExecutor");

	/**
	 * 任务
	 */
	private JobContextImpl task;
	private TriggerEvent taskEvent;

	public void setTaskEvent(TriggerEvent taskEvent) {
		this.taskEvent = taskEvent;
	}

	TaskExecutor(JobContextImpl task, TriggerEvent event) {
		this.task = task;
		this.taskEvent = event;
	}

	public void run() {
		task.execute(taskEvent);
	}

	/**
	 * 开启一个新线程来执行
	 */
	public void start() {
		Thread t = new Thread(this);
		t.setName("[Task-" + task.getId() + "]");
		t.start();
	}
}
