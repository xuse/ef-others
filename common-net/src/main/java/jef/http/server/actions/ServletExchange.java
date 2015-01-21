package jef.http.server.actions;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jef.http.server.WebExchange;

public interface ServletExchange extends WebExchange {
	/**
	 * 内部跳转
	 * 
	 * @param path
	 * @param params
	 */
	void forward(String path, Map<String, String> params);
	
	/**
	 * 将封装的HttpRequest暴露出来，尽量不要用
	 * 
	 * @return
	 */
	HttpServletRequest getRequest();

	/**
	 * 暴露出封装的HttpServletResponse
	 * @return
	 */
	HttpServletResponse getResponse();
}
