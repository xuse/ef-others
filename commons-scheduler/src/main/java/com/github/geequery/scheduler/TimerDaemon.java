package com.github.geequery.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 任务时间守护
 * </p>
 * @author shanguoming 2012-9-24 下午4:12:07
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2012-9-24
 * @modify by reason:{方法名}:{原因}
 */
class TimerDaemon extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger("hv-scheduler.TimerDaemon");
	/**
	 * 主任务调度
	 */
	private SchedulerImpl scheduler;
	
	public TimerDaemon(SchedulerImpl scheduler) {
		this.scheduler = scheduler;
		String name = scheduler.toString();
		setName(name);
	}
	
	/**
	 * 定时器算法 Jiyi 2014-3-27修改
	 * <p>
	 * 修改前，使用CountDownLatch#awit(long,TimeUnit)方法，每次睡眠1秒。<br>
	 * 该算法有跳秒问题。参见单元测试类com.github.geequery.scheduler.TestIgnoredSeconds。<br>
	 * 其原因虽然定时器每次都是固定间隔1秒去触发判断，但是由于各种计算开销，实际触发间隔会大于1秒。<br>
	 * 本来期望是每隔一秒去判断一次，但实际情况是每隔1秒多一点去触发判断一次。 这样下去积少成多，量变引发质变，就会出现“被跳过的一秒”。<br>
	 * 而如果某个任务被安排在特定的那一秒中执行，那么就会出现任务被漏掉。
	 * <p>
	 * 使用sleep和CountDownLatch其实效果一样，都是使用相对时间进行休眠（CountDownLatch在linux上的开销要大很多。），
	 * 精度上，Thread.sleep要稍低于CountDownLatch.await。 为减少误差，将休眠时间改为每次动态计算，并使用sleep方法。
	 * <p>
	 * 增加特殊的意外处理
	 * <ul>
	 * <li>sleep方法未达到下一秒准点前提前返回<br>
	 * 这种情况下，有可能计算得到的下一秒准点数和上一秒准点数相同，此时通过+1000强制校正到下一秒</li>
	 * <li>sleep方法因为无法再次获得CPU时间，延迟很久才返回<br>
	 * 此时上一秒准点数和下一秒准点数中间可能间隔若干秒。如果间隔在10秒以内，那么自动补充中间的准点秒数进行触发。<br>
	 * 之所以不处理10秒或以上的漏秒，是因为一般CPU时间片分配造成的漏秒不会超过10秒。超过的一般是因为用户修改系统时间引起的，故不处理。</li>
	 * </ul>
	 */
	public void run() {
		long lastSecond = System.currentTimeMillis();
		for (;;) {
			long nextSecond = ((System.currentTimeMillis() / 1000) + 1) * 1000;// 下一秒的时间
			if (nextSecond == lastSecond) {
				// 某些情况下，sleep睡眠不足，提前返回，在某些处理速度极快的机器上，这将造成计算得到的下一秒和上一秒完全一样，相当于该秒被触发了两次。
				nextSecond += 1000;
			} else {
				long interval = nextSecond - lastSecond;
				// 某些情况下，CPU被某些线程牢牢占据，定时器无法及时获得CPU时间片，会造成中间的几秒都被漏掉。
				if (interval > 1000 && interval < 10000) {
					// 对于间隔10秒以上的时间变化，一般不是因为CPU线程调度引起，而是因为用户修改了系统时间引起，因此不再补漏秒
					for (long lostSecond = lastSecond + 1000; lostSecond < nextSecond; lostSecond += 1000) {// 使用循环补上因为无法获得CPU而被漏掉的秒数
						scheduler.spawnLauncher(lostSecond);
						lastSecond = lostSecond;
					}
				}
			}
			// 计算需要睡眠等待的时间
			long sleepTime = (nextSecond - System.currentTimeMillis());
			if (sleepTime > 1000) {
				continue;// 出现这种情况是因为用户将系统时间向前调节了，故重新循环计算休眠时间
			}
			if (sleepTime > 0) {
				try {
					sleep(sleepTime);
					// 由于线程调度机制问题，从sleep中恢复的时间不一定等于指定的毫秒数（从理论上讲会大于该毫秒，但实际测试发现，也有“睡眠不足”的情况。）
					// 因此，此时再获取系统时间，有可能早于或晚于nextSecond的时间。
				} catch (InterruptedException e) {
					log.error("latch.await InterruptedException", e);
					break;
				}
			}
			scheduler.spawnLauncher(nextSecond);
			lastSecond = nextSecond;
		}
	}
}
