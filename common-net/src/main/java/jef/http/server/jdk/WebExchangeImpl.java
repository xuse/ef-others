package jef.http.server.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import jef.common.MimeTypes;
import jef.common.log.LogUtil;
import jef.http.UrlWrapper;
import jef.http.client.DLHelper;
import jef.http.server.AbstractExchange;
import jef.http.server.PostData;
<<<<<<< HEAD
import jef.net.Headers;
=======
import jef.inner.sun.Headers;
>>>>>>> 83fdddc646e6b87855bb33b18a749b3eda1ada60
import jef.tools.IOUtils;
import jef.tools.StringUtils;

@SuppressWarnings("restriction")
final class WebExchangeImpl extends AbstractExchange {
	HttpExchange raw;

	private String charset = "UTF8";

	private PostDataImpl post;

	private Headers headers;

	private String context;

	private int startLen;

	WebExchangeImpl(HttpExchange raw, String context, int contextLen) {
		this.raw = raw;
		this.context = context;
		this.startLen = contextLen;
	}

	/**
	 * 
	 */
	private Writer out;

	public Writer getOutput() {
		if (out == null) {
			try {
				out = new OutputStreamWriter(raw.getResponseBody(), charset);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return out;
	}

	public OutputStream getRawOutput() {
		return raw.getResponseBody();
	}

	public PostData getPostdata() {
		if (post == null) {
			post = new PostDataImpl(this);
		}
		return post;
	}

	public void setResponseHeader(String name, String value) {
		raw.getResponseHeaders().add(name, value);
	}

	public void setStatus(int status) {
		try {
			raw.sendResponseHeaders(status, 0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int getStatus() {
		return raw.getResponseCode();
	}

	public UrlWrapper getRequestURL() {
		String s;
		if (raw instanceof com.sun.net.httpserver.HttpsExchange) {
			s = "https://";
		} else {
			s = "http://";
		}
		s += raw.getLocalAddress().getHostName();
		if (raw.getLocalAddress().getPort() != 80) {
			s = ":" + raw.getLocalAddress().getPort();
		}
		try {
			URL url = new URL(s + raw.getRequestURI());
			return DLHelper.getUrl(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public String getMethod() {
		return raw.getRequestMethod();
	}

	public String getProtocol() {
		return raw.getProtocol();
	}

	public String getRealPath(String removeEnd) {
		String path = raw.getRequestURI().getPath();
		if (startLen > 0) {
			path = path.substring(startLen);
		}
		return path;
	}

	public String getParameter(String string) {
		if ("GET".equalsIgnoreCase(raw.getRequestMethod())) {
			String[] result = getParameterMap().get(string);
			if (result != null)
				return result[0];
			return null;
		} else {
			return getPostdata().getParameter(string);
		}
	}

	public Map<String, String[]> getParameterMap() {
		if ("GET".equalsIgnoreCase(raw.getRequestMethod())) {
			if (this.headers == null) {
				headers = DLHelper.getParamsInUrl(raw.getRequestURI().getQuery(), true);
			}
			return headers;
		} else {
			return getPostdata().getParameterMap();
		}
	}

	public void returnFile(String fileName, InputStream stream) {
		String mimeType = MimeTypes.getByFileName(fileName);
		setResponseHeader("Content-Type", mimeType);
		setResponseHeader("Content-Disposition", "attachment; filename=\"" + StringUtils.urlEncode(fileName) + "\"");
		try {
			IOUtils.copy(stream, raw.getResponseBody(), true);
		} catch (IOException e) {
			LogUtil.exception(e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public void close() {
		if (this.post != null) {
			post.close();
			post = null;
		}
		if (this.out != null) {
			IOUtils.closeQuietly(out);
			out = null;
		}
		this.raw.close();
	}
}
