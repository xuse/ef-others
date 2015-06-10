package com.github.geequery.scheduler;

/**
 * @author shanguoming 2014年12月22日 下午2:23:00
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月22日 下午2:23:00
 */
public class JobExistException extends RuntimeException {
	
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 1L;
	
	JobExistException(String message) {
		super(message);
	}
}
