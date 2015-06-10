package com.github.geequery.scheduler;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务调度器门面，建议全局唯一
 * @author jiyi
 *
 */
public class SchedulerFacade {
	private static final Logger log = LoggerFactory.getLogger("hv-scheduler.SchedulerFactory");
	
	/**
	 * 私有任务调度器
	 */
	private final SchedulerImpl scheduler = new SchedulerImpl();
	
	/**
	 * 设置任务执行的线程池，尽量在构造时传入
	 * @param executor ExecuteService 线程池
	 */
	public void setExecutor(ExecutorService executor) {
		scheduler.setExecutor(executor);
	}
	
	/**
	 * 为一个任务重设调度计划
	 * @param jobId 任务ID
	 * @param schedulingPattern 新的任务计划Pattern
	 * @throws NoSuchElementException 指定的任务不存在
	 */
	public void reschedule(String jobId, String schedulingPattern) throws NoSuchElementException{
		scheduler.reschedule(jobId, schedulingPattern);
		log.info("重设任务[{}]的调度模式为[{}]", jobId, schedulingPattern);
	}
	
	/**
	 * 立刻指定指定ID的任务。
	 * 如果任务正在运行，将被放入任务队列(要求event不为null)，等待当前执行完后被执行。任务队列上限5个。
	 * 
	 * @param jobId 任务ID
	 * @param event 触发任务的事件，定时触发是一种情况。一旦因为某些认为操作引发任务执行时，传入事件有助于在任务执行时判断当前场景。
	 */
	public void fireJob(String jobId, TriggerEvent event) {
		scheduler.executor(jobId, event);
		log.info("非定时执行任务[{}]，任务事件为[{}]", jobId, event.getClass().getName());
	}
	
	/**
	 * 删除指定ID的任务
	 * @param id
	 */
	public void deschedule(String id) {
		scheduler.deschedule(id);
		log.info("删除调度器内的任务[{}]", id);
	}
	
	/**
	 * 添加任务
	 * @param task 任务对象
	 */
	public void addTask(Job... tasks) throws InvalidPatternException {
		if (tasks != null && tasks.length > 0) {
			for (Job taskEntity : tasks) {
				scheduler.addSchedule(taskEntity);
			}
		}
	}
	
	/**
	 * 更新任务
	 * @param tasks 将相同ID的任务替换为新的Job实例。相应的，任务调度计划也被更新。
	 */
	public void updateTask(Job... tasks) {
		if (tasks != null && tasks.length > 0) {
			for (Job taskEntity : tasks) {
				scheduler.updateSchedule(taskEntity);
			}
		}
	}
	
	/**
	 * 启动任务调度
	 * @return true表示启动成功。如果已经启动了返回false
	 */
	public boolean start() {
		if (!scheduler.isStarted()) {
			log.info("任务调度器。。。。。。。开始启动");
			scheduler.start();
			log.info("任务调度器。。。。。。。就绪");
			return true;
		}
		return false;
	}
	
	/**
	 * 重启任务调度器
	 * @return 是否启动成功
	 */
	public void restart() {
		log.info("任务调度器。。。。。。。开始重启");
		stop();
		start();
	}
	
	/**
	 * 停止任务调度器
	 * @return 是否停止任务成功(成功：true or 失败：false)
	 */
	public boolean stop() {
		if (scheduler.isStarted()) {
			try {
				log.info("任务调度器。。。。。。。开始停止");
				scheduler.stop();
				log.info("任务调度器。。。。。。。停止");
			} catch (IllegalStateException e) {
				log.error("任务调度非法状态的异常", e);
				return false;
			} catch (InterruptedException e) {
				log.error("任务调度正在执行的线程被中断异常", e);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 获取调度器任务列表当前的所有上下文（其中包括Job）
	 * @return Job
	 * @see JobContext 
	 */
	public List<JobContext> getAllJobs() {
		return scheduler.getAllJobs();
	}
	
	/**
	 * 获取调度器任务的执行对象
	 * @param id 任务ID
	 * @return
	 */
	public Job getJob(String id) {
		return scheduler.getTask(id);
	}
}
