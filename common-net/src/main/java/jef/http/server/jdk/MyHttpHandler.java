//package jef.http.server.jdk;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URI;
//import java.util.HashMap;
//import java.util.Map;
//
//import jef.http.server.HttpPostContent;
//import jef.tools.IOUtils;
//import jef.tools.StringUtils;
//
//import com.sun.net.httpserver.Headers;
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//
//
//public class MyHttpHandler implements HttpHandler {
//	
//	public static String[] indexPages=new String[]{
//		"index.html","index.htm","default.htm","default.html"
//	};
//	
//	public MyHttpHandler(HttpActions actions){
//		super();
//		this.actions=actions;
//		File root=new File(WEB_ROOT);
//		if(!root.exists())root.mkdirs();
//	}
//	
//	public void handle(HttpExchange exchange) {
//		WebAction actionBean=actions.get(exchange.getRequestURI().getPath());
//		if(actionBean==null){
//			goStaticPages(exchange);
//		}else{
//			goDynamicPages(exchange, actionBean);
//		}
//		exchange.close();
//	}
//	
//	private void goDynamicPages(HttpExchange exchange, WebAction action) {
//		URI uri=exchange.getRequestURI();
//		String requestMethod = exchange.getRequestMethod();
//		Map<String,String> params=new HashMap<String,String>();
//		String query=uri.getQuery();
//		if(query!=null){
//			for(String x: uri.getQuery().split("&")){
//				int n=x.indexOf("=");
//				if(n>-1){
//					params.put(x.substring(0,n).toLowerCase(), x.substring(n+1));
//				}else{
//					params.put(x.toLowerCase(), null);	
//				}
//			}
//		}
//		try{
//			if (requestMethod.equalsIgnoreCase("GET")) {
//				action.doGet(params,exchange);
//			}else{
//				HttpPostContent content=new HttpPostContent(exchange.getRequestHeaders(),exchange.getRequestBody());
//				for(String key: content.getParameters().keySet()){
//					if(!params.containsKey(key)){
//						params.put(key,  content.getParameters().get(key));
//					}
//				}
//				action.doPost(params,content,exchange);
//				content.destroy();
//			}
//		}catch(Exception e){
//			error500(exchange,e.getMessage());
//		}
//		
//	}
//
//	private void goStaticPages(HttpExchange exchange) {
//		URI uri=exchange.getRequestURI();
//		File file=null;
//		if(uri.getPath().endsWith("/")){
//			for(String p: indexPages){
//				new File(WEB_ROOT+uri.getPath()+p);
//				if(file.exists()){
//					break;
//				}
//			}
//		}else{
//			file=new File(WEB_ROOT+uri.getPath());	
//		}
//		if(file!=null && file.exists()){
//			responseFile(file,exchange);	
//		}else{
//			error404(exchange);
//		}
//		return;
//	}
//
//	private void responseFile(File file, HttpExchange exchange) {
//		try {
//			String fileName=file.getName();
//			String mimeType=MIME_TYPES.get(StringUtils.substringAfterLast(fileName,".").toLowerCase());
//			if(mimeType==null)mimeType=MIME_TYPES.get("*");
//			Headers responseHeaders = exchange.getResponseHeaders();
//			responseHeaders.set("Content-Type", mimeType);
//			if(!mimeType.startsWith("text")){
//				responseHeaders.set("Content-Disposition", "attachment; filename=\"" + fileName+"\"");	
//			}
//			exchange.sendResponseHeaders(200, 0);
//			InputStream in=new FileInputStream(file);
//			OutputStream out=exchange.getResponseBody();
//			IOUtils.copy(in ,out , true);
//			in.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	//display Http error 404
//	private void error404(HttpExchange exchange){
//		Headers responseHeaders = exchange.getResponseHeaders();
//		OutputStream responseBody = exchange.getResponseBody();
//		try {
//			exchange.sendResponseHeaders(404, 0);
//			responseHeaders.set("Content-Type", "text/plain");
//			responseBody.write("The page you requested was not found.".getBytes());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}finally{
//			try {
//				responseBody.close();
//			} catch (IOException e) {}
//		}
//	}
//	
//	//display Http error 500	
//	private void error500(HttpExchange exchange, String errorInfo){
//		try {
//			Headers responseHeaders = exchange.getResponseHeaders();
//			OutputStream responseBody = exchange.getResponseBody();
//			exchange.sendResponseHeaders(200, 0);
//			responseHeaders.set("Content-Type", "text/plain");
//			responseBody.write("Sorry, the server encounter an error.\n\n".getBytes());
//			responseBody.write("Detail:\n".getBytes());
//			responseBody.write("\n\n".getBytes());
//			responseBody.write(errorInfo.getBytes());
//			responseBody.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	
//}
//
//
