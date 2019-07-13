package jef.http.server.jdk;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import jef.http.server.WebExchange;
import jef.http.server.actions.HttpAction;
import jef.tools.Exceptions;

@SuppressWarnings("restriction")
public class ActionHandler implements HttpHandler {

	private HttpAction action;
	private String context;
	private int startLen;
	
	public ActionHandler(String context, HttpAction action) {
		this.action = action;
		this.context = context;
		this.startLen=context.length();
		if(context.endsWith("/")){
			startLen--;
		}
	}

	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		WebExchange we = new WebExchangeImpl(exchange,context,startLen);
		try {
			if (requestMethod.equalsIgnoreCase("GET")) {
				action.doGet(we);
			} else {
				action.doPost(we);
			}
			if(we.getStatus()<=0){
				we.setStatus(200);
			}
		} catch (Exception e) {
			we.setStatus(500);
			we.printStackTrace(e);
			Exceptions.log(e);
		} finally {
			we.close();
		}

	}

}
