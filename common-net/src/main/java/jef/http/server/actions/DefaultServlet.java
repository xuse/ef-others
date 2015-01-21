/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jef.http.server.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jef.common.log.LogUtil;
import jef.http.server.ServletExchangeImpl;
import jef.http.server.WebExchange;

import org.apache.commons.lang.StringUtils;

/**
 * Servlet转调用Action的适配器类
 * 
 * @author Administrator
 * 
 */
public class DefaultServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private HttpAction defaultHandler;

//	static class MessageAction implements HttpAction {
//		public void doGet(WebExchange exchange) {
//			exchange.print("Hello, your request method is not found.");
//			exchange.close();
//		}
//
//		public void doPost(WebExchange exchange) {
//			exchange.print("Hello, your request method is not found.");
//			exchange.close();
//		}
//	}

	public HttpAction getAction() {
		return this.defaultHandler;
	}

	public void init() throws ServletException {
		super.init();
		String className = super.getInitParameter("class");
		try {
			Class<?> c = Class.forName(className.trim());
			this.defaultHandler = (HttpAction) c.newInstance();
		} catch (Exception e) {
			LogUtil.exception(e);
		}
		String register = super.getInitParameter("register");
		if (StringUtils.isNotEmpty(register)) {
			super.getServletContext().setAttribute(register, this);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletExchange ex = new ServletExchangeImpl(req, resp, true);
		try {
			defaultHandler.doGet(ex);
		} finally {
			ex.close();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletExchange ex = new ServletExchangeImpl(req, resp, true);
		try {
			defaultHandler.doPost(ex);
		} finally {
			ex.close();
		}
	}
}
