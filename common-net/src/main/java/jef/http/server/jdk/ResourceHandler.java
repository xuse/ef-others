package jef.http.server.jdk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;

import jef.common.MimeTypes;
import jef.common.log.LogUtil;
import jef.tools.DateFormats;
import jef.tools.DateUtils;
import jef.tools.IOUtils;
import jef.tools.StringUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
final class ResourceHandler implements HttpHandler {
	private File root;
	private static String[] indexPages = new String[] { "index.html", "index.htm", "default.htm", "default.html" };
	private String start;
	private int startLen;
	private boolean allowList = true;

	public ResourceHandler(String start, File root) {
		this.start = start;
		this.root = root;
		this.startLen = start.length();
		if (start.endsWith("/")) {
			startLen--;
		}
	}

	public void handle(HttpExchange exchange) throws IOException {
		loadResource(exchange);
	}

	private void loadResource(HttpExchange exchange) {
		URI uri = exchange.getRequestURI();
		File file = null;
		String path = uri.getPath();
		if (startLen > 0) {
			path = path.substring(startLen);
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		file = new File(root, path);
		if (!file.exists()) {
			LogUtil.warn("Resource not found: " + path);
			error404(exchange);
			return;
		}

		if (file.isFile()) {
			responseFile(file, exchange);
		} else {
			File index= getIndexPageFile(path);
			if(index!=null){
				responseFile(file, exchange);
			}else if (allowList) {
				try {
					listDir(file, exchange);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					exchange.close();
				}
			} else {
				error404(exchange);
			}
		}
	}

	private File getIndexPageFile(String path) {
		for (String p : indexPages) {
			File file = new File(root, path + p);
			if (file.exists()){
				return file;
			}
		}
		return null;
	}

	private void listDir(File file, HttpExchange exchange1) throws IOException {
		exchange1.getResponseHeaders().set("Content-Type", "text/html");
		exchange1.sendResponseHeaders(200, 0);
		PrintStream exchange = new PrintStream(exchange1.getResponseBody());
		exchange.print("<html><head><meta charset='UTF-8' /><title>\n");
		exchange.print("Directory:" + file.getName());
		exchange.print("</title></head><body>\n");
		exchange.print("<strong>Directory:" + file.getName() + "</strong><hr>");
		exchange.print("<a href=../>Parent Folder</a><br><br>");
		exchange.print("\n<table>");
		exchange.print("<tr><td width=180><b>");
		exchange.print("File Name");
		exchange.print("&nbsp;</b></td><td width=80><b>");
		exchange.print("Type");
		exchange.print("</b></td><td width=120><b>");
		exchange.print("Last Modified");
		exchange.print("</b></td></tr>");

		for (File sub : IOUtils.listFolders(file)) {
			writeFileEntry(exchange, sub);
		}
		for (File sub : IOUtils.listFiles(file)) {
			writeFileEntry(exchange, sub);
		}
		exchange.print("</table>");
		exchange.print("</body></html>");
	}

	private void writeFileEntry(PrintStream exchange, File sub) {
		exchange.print("<tr><td>\r\n");
		exchange.print("<a href='./" + StringUtils.urlEncode(sub.getName()) + (sub.isDirectory() ? "/'>" : "'>"));
		exchange.print(sub.getName());
		exchange.print("&nbsp;</a></td><td>");
		exchange.print(sub.isDirectory() ? "&lt;DIR&gt;&nbsp;" : StringUtils.formatSize(sub.length()));
		exchange.print("</td><td>\r\n");
		exchange.print(DateFormats.DATE_CS.format(sub.lastModified()));
		exchange.print("</td></tr>\r\n");
	}

	private void responseFile(File file, HttpExchange exchange) {
		String fileName = file.getName();
		String mimeType = MimeTypes.get(IOUtils.getExtName(fileName));
		com.sun.net.httpserver.Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", mimeType);
		responseHeaders.set("Content-Length", String.valueOf(file.length()));
		if (!mimeType.startsWith("text") && !mimeType.startsWith("image")) {
			responseHeaders.set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		}
		
		InputStream in = null;
		OutputStream out = exchange.getResponseBody();
		try {
			exchange.sendResponseHeaders(200, 0);
			in = IOUtils.getInputStream(file);
			IOUtils.copy(in, out, true);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
			exchange.close();
		}
	}

	public boolean isAllowList() {
		return allowList;
	}

	public void setAllowList(boolean allowList) {
		this.allowList = allowList;
	}

	private void error404(HttpExchange exchange) {
		try {
			exchange.sendResponseHeaders(404, 0);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			exchange.close();
		}
	}
}
