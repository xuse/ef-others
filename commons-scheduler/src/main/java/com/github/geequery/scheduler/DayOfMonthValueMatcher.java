package com.github.geequery.scheduler;

import java.util.ArrayList;

/**
 * <p>
 * 用于对每月日期进行规则验证的实现类
 * </p>
 * @author shanguoming 2012-9-24 下午1:55:51
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2012-9-24
 * @modify by reason:{方法名}:{原因}
 */
final class DayOfMonthValueMatcher extends IntArrayValueMatcher {
	
	private static final int[] lastDays = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	
	/**
	 * 创建一个新的实例DayOfMonthValueMatcher.
	 * @param integers 日期的数字集合
	 */
	public DayOfMonthValueMatcher(ArrayList<Integer> integers) {
		super(integers);
	}
	
	/**
	 * 验证给定值是否符合规则
	 * @author shanguoming 2012-9-24 下午2:03:11
	 * @param value 日
	 * @param month 月
	 * @param isLeapYear 是否闰年
	 * @return true or false
	 */
	public boolean match(int value, int month, boolean isLeapYear) {
		return (super.match(value) || (value > 27 && match(32) && isLastDayOfMonth(value, month, isLeapYear)));
	}
	
	/**
	 * 验证是否当月最后一天
	 * @author shanguoming 2012-9-24 下午2:05:01
	 * @param value 日
	 * @param month 月
	 * @param isLeapYear 是否闰年
	 * @return true or false
	 */
	public boolean isLastDayOfMonth(int value, int month, boolean isLeapYear) {
		if (isLeapYear && month == 2) {
			return value == 29;
		} else {
			return value == lastDays[month - 1];
		}
	}
}
