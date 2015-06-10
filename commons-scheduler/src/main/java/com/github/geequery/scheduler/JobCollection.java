package com.github.geequery.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务集
 * @version V1.0
 */
final class JobCollection {
	
	/**
	 * 任务列表
	 */
	private Map<String, JobContextImpl> taskmap = new ConcurrentHashMap<String, JobContextImpl>();
	
	/**
	 * 任务数
	 * @author shanguoming 2012-9-24 下午2:37:11
	 * @return
	 */
	public int size() {
		return taskmap.size();
	}
	
	/**
	 * 添加任务
	 * @author shanguoming 2012-9-24 下午2:39:31
	 * @param task 任务对象
	 * @return
	 */
	public String add(JobContextImpl task) {
		if (null != task) {
			if (null == getTask(task.getId())) {
				taskmap.put(task.getId(), task);
				return task.getId();
			} else {
				throw new TaskExistException("任务id为" + task.getId() + "的任务已经存在");
			}
		}
		return null;
	}
	
	/**
	 * 更新调度模式
	 * @author shanguoming 2012-9-24 下午2:41:03
	 * @param id guid
	 * @param pattern 调度模式
	 */
	public void updatePattern(String id, String pattern) throws InvalidPatternException {
		JobContextImpl t = taskmap.get(id);
		if (t != null) {
			t.setSchedulingPattern(pattern);
		}
	}
	
	public void update(JobContextImpl task) {
		if (null != task) {
			taskmap.put(task.getId(), task);
		}
	}
	
	/**
	 * 删除指定GUID的任务和调度模式
	 * @author shanguoming 2012-9-24 下午2:41:24
	 * @param id GUID
	 * @throws IndexOutOfBoundsException
	 */
	public void remove(String id) throws IndexOutOfBoundsException {
		taskmap.remove(id);
	}
	
	/**
	 * 根据GUID获取任务对象
	 * @author shanguoming 2012-9-24 下午2:41:54
	 * @param id GUID
	 * @return 任务对象
	 */
	public JobContextImpl getTask(String id) {
		return taskmap.get(id);
	}
	
	/**
	 * 获取任务列表
	 * @author shanguoming 2012-12-6 下午4:58:54
	 * @return 返回任务列表
	 */
	public Collection<JobContextImpl> getTasks() {
		return taskmap.values();
	}
	
	/**
	 * 任务列表是否为空
	 * @return 任务列表为空时返回true
	 */
	public boolean isEmpty() {
		return taskmap.isEmpty();
	}

	public List<JobContext> safeList() {
		return new ArrayList<JobContext>(this.taskmap.values());
	}
}
