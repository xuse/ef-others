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
package jef.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jef.common.MimeTypes;
import jef.http.UrlWrapper;
import jef.http.client.DLHelper;
import jef.http.server.actions.ServletExchange;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.io.Charsets;

public class ServletExchangeImpl extends AbstractExchange implements ServletExchange {
	private HttpServletRequest request;
	private HttpServletResponse response;
	private String method;
	private boolean paramIgnorCase;
	private boolean isPost;

	public ServletExchangeImpl(HttpServletRequest req, HttpServletResponse resp, boolean paramIgnorCase) {
		this.request = req;
		this.response = resp;
		this.method = req.getMethod().toLowerCase();
		this.paramIgnorCase = paramIgnorCase;
		this.isPost = "post".equals(method);

		String contentType = req.getHeader("Content-Type");
		String charset = getCharset(contentType);
		if (charset != null) {
			this.charset = charset;
		}
	}

	private String getCharset(String contentType) {
		if (StringUtils.isEmpty(contentType)) {
			return null;
		}
		int n = contentType.indexOf("charset=");
		if (n > -1) {
			contentType = contentType.substring(n + 8);
			if (contentType.charAt(0) == '\"' || contentType.charAt(0) == '\'') {
				contentType = contentType.substring(1);
			}

			n = StringUtils.indexOfAny(contentType, "\"' ><");
			if (n > -1) {
				contentType = contentType.substring(0, n);
			}
			if (contentType.length() == 0)
				return null;
			contentType = Charsets.getStdName(contentType);
			return contentType;
		} else {
			return null;
		}
	}

	public ServletExchangeImpl(HttpServletRequest req, HttpServletResponse resp) {
		this(req, resp, false);
	}

	private OutputStreamWriter output;
	private OutputStream rawout;
	private ServletPostData postdata;

	public Writer getOutput() {
		if (output == null) {
			initOut();
		}
		return output;
	}

	public OutputStream getRawOutput() {
		if (output == null) {
			initOut();
		} else {
			try {
				output.flush();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return rawout;
	}

	private void initOut() {
		try {
			rawout = response.getOutputStream();
			output = new OutputStreamWriter(rawout, charset);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public ServletPostData getPostdata() {
		if (postdata == null) {
			try {
				postdata = new ServletPostData(request, this, paramIgnorCase);
			} catch (Exception e) {
				Exceptions.log(e);
				throw new RuntimeException(e);
			}
		}
		return postdata;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponseContentType(String type) {
		response.setContentType(type);
	}

	public void setResponseHeader(String name, String value) {
		response.setHeader(name, value);
	}

	public void setStatus(int status) {
		response.setStatus(status);
	}

	public void forward(String path, Map<String, String> params) {
		try {
			if (params != null && params.size() > 0) {
				for (String key : params.keySet()) {
					this.getRequest().setAttribute(key, params.get(key));
				}
			}
			this.getRequest().getRequestDispatcher(path).forward(this.getRequest(), this.getResponse());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public String getParameter(String string) {
		if (isPost) {
			return getPostdata().getParameter(string);
		} else {
			return request.getParameter(string);
		}
	}

	public Map<String, String[]> getParameterMap() {
		if (isPost) {
			this.getPostdata();
			return postdata.getParameterMap();
		} else {
			Map<String, String[]> result = request.getParameterMap();
			// result为Collections$UnmodifiableMap，不允许被修改，复制处理。
			Map<String, String[]> _result = new HashMap<String, String[]>();
			for (String key : result.keySet()) {
				_result.put(key, result.get(key));
			}
			return _result;
		}
	}

	public String getMethod() {
		return method;
	}

	public String getProtocol() {
		return request.getProtocol();
	}

	@SuppressWarnings("deprecation")
	public String getRealPath(String removeEnd) {
		UrlWrapper uu = getRequestURL();
		String str = (removeEnd != null) ? StringUtils.removeEnd(uu.getRelativePath(), removeEnd) : uu.getRelativePath();
		return request.getRealPath(str);
	}

	private UrlWrapper u = null;

	public UrlWrapper getRequestURL() {
		if (u == null)
			u = DLHelper.getUrl(request);
		return u;
	}

	public void returnFile(String fileName, InputStream stream) {
		returnFile(fileName, stream, 0);
	}

	private void returnFile(String fileName, InputStream stream, int length) {
		if (length > 0)
			response.setContentLength(length);
		String mimeType = MimeTypes.getByFileName(fileName);
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + StringUtils.urlEncode(fileName) + "\"");
		try {
			IOUtils.copy(stream, response.getOutputStream(), true);
		} catch (IOException e) {
			Exceptions.log(e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public void close() {
		if (this.postdata != null) {
			postdata.destroy();
		}
		if (output != null) {
			IOUtils.closeQuietly(output);
		} else {
			try {
				IOUtils.closeQuietly(response.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public int getStatus() {
		return response.getStatus();
	}
}
