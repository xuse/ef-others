package com.github.geequery.scheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * <p>
 * 调度模式
 * </p>
 * <p>
 * 实例
 * </p>
 * <p>
 * <strong>5 * * * * *</strong><br />
 * 每5秒 (00:01:05, 00:01:05, 00:01:05 ...)
 * </p>
 * <p>
 * <strong>* * * * * *</strong><br />
 * 任意秒
 * </p>
 * <p>
 * <strong>* 12 * * * Mon</strong><br />
 * 每周1，每小时的第12分钟
 * </p>
 * <p>
 * <strong>* 12 16 * * Mon</strong><br />
 * 每周1,16点12分
 * </p>
 * <p>
 * <strong>59 11 * * * 1,2,3,4,5</strong><br />
 * 周1到周5 每小时11分59秒
 * </p>
 * <p>
 * <strong>59 11 * * * 1-5</strong><br />
 * 周1到周5 每小时11分59秒
 * </p>
 * <p>
 * <strong>*&#47;5 * * * * *</strong><br />
 * 每5秒 (00:01:05, 00:01:05, 00:01:05 ...)
 * </p>
 * <p>
 * <strong>3-18/5 * * * * *</strong><br />
 * 每分钟内 (00:01:03, 00:01:08, 00:01:13, 00:01:18, 00:02:03, 00:02:08 ...).
 * </p>
 * <p>
 * <strong>*<t>/</t>15 9-17 * * * *</strong><br />
 * 每小时9分-17分之间，能被15整除的秒数
 * </p>
 * <p>
 * <strong>* 12 10-16/2 * * *</strong><br />
 * 每天10-16点，间隔2小时， 12分
 * </p>
 * <p>
 * <strong>* * 12 1-15,17,20-25 * *</strong><br />
 * 每月1-15号，17号，20-25号的12点
 * </p>
 * <p>
 * <strong>* 0 5 * * *|* 8 10 * * *|* 22 17 * * *</strong><br />
 * 每天5点，10点8分，17点22分
 * </p>
 * @author shanguoming 2012-9-24 下午4:56:19
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2012-9-24
 * @modify by reason:{方法名}:{原因}
 */
class SchedulingPattern {
	
	/**
	 * 秒值分析器
	 */
	private static final ValueParser SECOND_VALUE_PARSER = new SecondValueParser();
	/**
	 * 分值分析器
	 */
	private static final ValueParser MINUTE_VALUE_PARSER = new MinuteValueParser();
	/**
	 * 小时分析器
	 */
	private static final ValueParser HOUR_VALUE_PARSER = new HourValueParser();
	/**
	 * 日期分析器
	 */
	private static final ValueParser DAY_OF_MONTH_VALUE_PARSER = new DayOfMonthValueParser();
	/**
	 * 月份分析器
	 */
	private static final ValueParser MONTH_VALUE_PARSER = new MonthValueParser();
	/**
	 * 星期分析器
	 */
	private static final ValueParser DAY_OF_WEEK_VALUE_PARSER = new DayOfWeekValueParser();
	
	/**
	 * 验证字符串是否合法
	 * @author shanguoming 2012-9-24 下午4:58:02
	 * @param schedulingPattern 调度模式字符串
	 * @return
	 */
	public static boolean validate(String schedulingPattern) {
		try {
			new SchedulingPattern(schedulingPattern);
		} catch (InvalidPatternException e) {
			return false;
		}
		return true;
	}
	
	private String asString;
	protected ArrayList<ValueMatcher> secondMatchers = new ArrayList<ValueMatcher>();
	protected ArrayList<ValueMatcher> minuteMatchers = new ArrayList<ValueMatcher>();
	protected ArrayList<ValueMatcher> hourMatchers = new ArrayList<ValueMatcher>();
	protected ArrayList<ValueMatcher> dayOfMonthMatchers = new ArrayList<ValueMatcher>();
	protected ArrayList<ValueMatcher> monthMatchers = new ArrayList<ValueMatcher>();
	protected ArrayList<ValueMatcher> dayOfWeekMatchers = new ArrayList<ValueMatcher>();
	protected int matcherSize = 0;
	
	public SchedulingPattern(String pattern) throws InvalidPatternException {
		if (null == pattern || "".equals(pattern.trim())) {
			return;
		}
		this.asString = pattern;
		StringTokenizer st1 = new StringTokenizer(pattern, "|");
		if (st1.countTokens() < 1) {
			return;
		}
		while (st1.hasMoreTokens()) {
			String localPattern = st1.nextToken();
			StringTokenizer st2 = new StringTokenizer(localPattern, " \t");
			if (st2.countTokens() != 6) {
				throw new InvalidPatternException("invalid pattern: \"" + localPattern + "\"");
			}
			try {
				secondMatchers.add(buildValueMatcher(st2.nextToken(), SECOND_VALUE_PARSER));
			} catch (Exception e) {
				throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing second field: " + e.getMessage() + ".");
			}
			try {
				minuteMatchers.add(buildValueMatcher(st2.nextToken(), MINUTE_VALUE_PARSER));
			} catch (Exception e) {
				throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing minutes field: " + e.getMessage() + ".");
			}
			try {
				hourMatchers.add(buildValueMatcher(st2.nextToken(), HOUR_VALUE_PARSER));
			} catch (Exception e) {
				throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing hours field: " + e.getMessage() + ".");
			}
			try {
				dayOfMonthMatchers.add(buildValueMatcher(st2.nextToken(), DAY_OF_MONTH_VALUE_PARSER));
			} catch (Exception e) {
				throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing days of month field: " + e.getMessage() + ".");
			}
			try {
				monthMatchers.add(buildValueMatcher(st2.nextToken(), MONTH_VALUE_PARSER));
			} catch (Exception e) {
				throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing months field: " + e.getMessage() + ".");
			}
			try {
				dayOfWeekMatchers.add(buildValueMatcher(st2.nextToken(), DAY_OF_WEEK_VALUE_PARSER));
			} catch (Exception e) {
				throw new InvalidPatternException("invalid pattern \"" + localPattern + "\". Error parsing days of week field: " + e.getMessage() + ".");
			}
			matcherSize++;
		}
	}
	
	/**
	 * 构建日期值匹配
	 * @author shanguoming 2012-9-24 下午5:00:57
	 * @param str 调度模式字符串
	 * @param parser 分析器类型
	 * @return
	 * @throws Exception
	 */
	private ValueMatcher buildValueMatcher(String str, ValueParser parser) throws Exception {
		if (str.length() == 1 && str.equals("*")) {
			return new AlwaysTrueValueMatcher();
		}
		ArrayList<Integer> values = new ArrayList<Integer>();
		StringTokenizer st = new StringTokenizer(str, ",");
		while (st.hasMoreTokens()) {
			String element = st.nextToken();
			ArrayList<Integer> local;
			try {
				local = parseListElement(element, parser);
			} catch (Exception e) {
				throw new Exception("invalid field \"" + str + "\", invalid element \"" + element + "\", " + e.getMessage());
			}
			for (Iterator<Integer> i = local.iterator(); i.hasNext();) {
				Integer value = i.next();
				if (!values.contains(value)) {
					values.add(value);
				}
			}
		}
		if (values.size() == 0) {
			throw new Exception("invalid field \"" + str + "\"");
		}
		if (parser == DAY_OF_MONTH_VALUE_PARSER) {
			return new DayOfMonthValueMatcher(values);
		} else {
			return new IntArrayValueMatcher(values);
		}
	}
	
	/**
	 * 解析列表元素
	 * @author shanguoming 2012-9-24 下午5:03:03
	 * @param str 调度模式字符串
	 * @param parser 分析器类型
	 * @return
	 * @throws Exception
	 */
	private ArrayList<Integer> parseListElement(String str, ValueParser parser) throws Exception {
		StringTokenizer st = new StringTokenizer(str, "/");
		int size = st.countTokens();
		if (size < 1 || size > 2) {
			throw new Exception("syntax error");
		}
		ArrayList<Integer> values;
		try {
			values = parseRange(st.nextToken(), parser);
		} catch (Exception e) {
			throw new Exception("invalid range, " + e.getMessage());
		}
		if (size == 2) {
			String dStr = st.nextToken();
			int div;
			try {
				div = Integer.parseInt(dStr);
			} catch (NumberFormatException e) {
				throw new Exception("invalid divisor \"" + dStr + "\"");
			}
			if (div < 1) {
				throw new Exception("non positive divisor \"" + div + "\"");
			}
			ArrayList<Integer> values2 = new ArrayList<Integer>();
			for (int i = 0; i < values.size(); i += div) {
				values2.add(values.get(i));
			}
			return values2;
		} else {
			return values;
		}
	}
	
	/**
	 * 分析范围
	 * @author shanguoming 2012-9-24 下午5:03:41
	 * @param str 调度模式字符串
	 * @param parser 分析器类型
	 * @return
	 * @throws Exception
	 */
	private ArrayList<Integer> parseRange(String str, ValueParser parser) throws Exception {
		if (str.equals("*")) {
			int min = parser.getMinValue();
			int max = parser.getMaxValue();
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int i = min; i <= max; i++) {
				values.add(Integer.valueOf(i));
			}
			return values;
		}
		StringTokenizer st = new StringTokenizer(str, "-");
		int size = st.countTokens();
		if (size < 1 || size > 2) {
			throw new Exception("syntax error");
		}
		String v1Str = st.nextToken();
		int v1;
		try {
			v1 = parser.parse(v1Str);
		} catch (Exception e) {
			throw new Exception("invalid value \"" + v1Str + "\", " + e.getMessage());
		}
		if (size == 1) {
			ArrayList<Integer> values = new ArrayList<Integer>();
			values.add(Integer.valueOf(v1));
			return values;
		} else {
			String v2Str = st.nextToken();
			int v2;
			try {
				v2 = parser.parse(v2Str);
			} catch (Exception e) {
				throw new Exception("invalid value \"" + v2Str + "\", " + e.getMessage());
			}
			ArrayList<Integer> values = new ArrayList<Integer>();
			if (v1 < v2) {
				for (int i = v1; i <= v2; i++) {
					values.add(Integer.valueOf(i));
				}
			} else if (v1 > v2) {
				int min = parser.getMinValue();
				int max = parser.getMaxValue();
				for (int i = v1; i <= max; i++) {
					values.add(Integer.valueOf(i));
				}
				for (int i = min; i <= v2; i++) {
					values.add(Integer.valueOf(i));
				}
			} else {
				// v1 == v2
				values.add(Integer.valueOf(v1));
			}
			return values;
		}
	}
	
	/**
	 * 根据时间戳计算调度模式和时间是否匹配
	 * @author shanguoming 2012-9-24 下午5:11:40
	 * @param timezone
	 * @param millis
	 * @return
	 */
	public boolean match(TimeZone timezone, long millis) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(millis);
		gc.setTimeZone(timezone);
		int second = gc.get(Calendar.SECOND);
		int minute = gc.get(Calendar.MINUTE);
		int hour = gc.get(Calendar.HOUR_OF_DAY);
		int dayOfMonth = gc.get(Calendar.DAY_OF_MONTH);
		int month = gc.get(Calendar.MONTH) + 1;
		int dayOfWeek = gc.get(Calendar.DAY_OF_WEEK) - 1;
		int year = gc.get(Calendar.YEAR);
		for (int i = 0; i < matcherSize; i++) {
			ValueMatcher secondMatcher = secondMatchers.get(i);
			ValueMatcher minuteMatcher = minuteMatchers.get(i);
			ValueMatcher hourMatcher = hourMatchers.get(i);
			ValueMatcher dayOfMonthMatcher = dayOfMonthMatchers.get(i);
			ValueMatcher monthMatcher = monthMatchers.get(i);
			ValueMatcher dayOfWeekMatcher = dayOfWeekMatchers.get(i);
			boolean eval = secondMatcher.match(second) && minuteMatcher.match(minute) && hourMatcher.match(hour)
			        && ((dayOfMonthMatcher instanceof DayOfMonthValueMatcher)?((DayOfMonthValueMatcher)dayOfMonthMatcher).match(dayOfMonth, month, gc.isLeapYear(year)):dayOfMonthMatcher.match(dayOfMonth))
			        && monthMatcher.match(month) && dayOfWeekMatcher.match(dayOfWeek);
			if (eval) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 根据系统默认时间戳计算任务调度和时间是否匹配
	 * @author shanguoming 2012-9-24 下午5:12:19
	 * @param millis
	 * @return
	 */
	public boolean match(long millis) {
		return match(TimeZone.getDefault(), millis);
	}
	
	public String toString() {
		return asString;
	}
	
	/**
	 * 解析别名
	 * @author shanguoming 2012-9-24 下午5:13:57
	 * @param value
	 * @param aliases
	 * @param offset
	 * @return
	 * @throws Exception
	 */
	private static int parseAlias(String value, String[] aliases, int offset) throws Exception {
		for (int i = 0; i < aliases.length; i++) {
			if (aliases[i].equalsIgnoreCase(value)) {
				return offset + i;
			}
		}
		throw new Exception("invalid alias \"" + value + "\"");
	}
	
	/**
	 * 默认值分析器
	 */
	private static interface ValueParser {
		
		public int parse(String value) throws Exception;
		
		public int getMinValue();
		
		public int getMaxValue();
	}
	
	/**
	 * 简单值分析器
	 */
	private static class SimpleValueParser implements ValueParser {
		
		/**
		 * 最小值
		 */
		protected int minValue;
		/**
		 * 最大值
		 */
		protected int maxValue;
		
		public SimpleValueParser(int minValue, int maxValue) {
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		
		public int parse(String value) throws Exception {
			int i;
			try {
				i = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new Exception("invalid integer value");
			}
			if (i < minValue || i > maxValue) {
				throw new Exception("value out of range");
			}
			return i;
		}
		
		public int getMinValue() {
			return minValue;
		}
		
		public int getMaxValue() {
			return maxValue;
		}
	}
	
	/**
	 * 秒值分析器
	 */
	private static class SecondValueParser extends SimpleValueParser {
		
		/**
		 * Builds the value parser.
		 */
		public SecondValueParser() {
			super(0, 59);
		}
	}
	
	/**
	 * 分值分析器
	 */
	private static class MinuteValueParser extends SimpleValueParser {
		
		/**
		 * Builds the value parser.
		 */
		public MinuteValueParser() {
			super(0, 59);
		}
	}
	
	/**
	 * 小时分析器
	 */
	private static class HourValueParser extends SimpleValueParser {
		
		/**
		 * Builds the value parser.
		 */
		public HourValueParser() {
			super(0, 23);
		}
	}
	
	/**
	 * 日期分析器
	 */
	private static class DayOfMonthValueParser extends SimpleValueParser {
		
		/**
		 * Builds the value parser.
		 */
		public DayOfMonthValueParser() {
			super(1, 31);
		}
		
		public int parse(String value) throws Exception {
			if (value.equalsIgnoreCase("L")) {
				return 32;
			} else {
				return super.parse(value);
			}
		}
	}
	
	/**
	 * 月份分析器
	 */
	private static class MonthValueParser extends SimpleValueParser {
		
		/**
		 * 月份别名
		 */
		private static String[] ALIASES = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
		
		public MonthValueParser() {
			super(1, 12);
		}
		
		public int parse(String value) throws Exception {
			try {
				return super.parse(value);
			} catch (Exception e) {
				return parseAlias(value, ALIASES, 1);
			}
		}
	}
	
	/**
	 * 星期分析器
	 */
	private static class DayOfWeekValueParser extends SimpleValueParser {
		
		/**
		 * 星期别名
		 */
		private static String[] ALIASES = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};
		
		public DayOfWeekValueParser() {
			super(0, 7);
		}
		
		public int parse(String value) throws Exception {
			try {
				return super.parse(value) % 7;
			} catch (Exception e) {
				return parseAlias(value, ALIASES, 0);
			}
		}
	}
}
