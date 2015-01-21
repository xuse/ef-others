package jef.http.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import jef.http.server.actions.JsonResponse;
import jef.tools.JefConfiguration;
import jef.tools.JefConfiguration.Item;
import jef.tools.StringUtils;

import org.easyframe.fastjson.JSON;

public abstract class AbstractExchange implements WebExchange {
	private static boolean withJsonHeader = JefConfiguration.getBoolean(Item.HTTP_SEND_JSON_HEADER, true);
	protected String charset = "UTF-8";
	
	public void print(String string) {
		try {
			getOutput().write(string);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void println(String string, String charset) {
		try {
			OutputStream out = getRawOutput();
			out.write(string.getBytes(charset));
			out.write(StringUtils.CRLF);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	

	public void println(String string) {
		try {
			getOutput().write(string);
			getOutput().write("\r\n");
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public void redirect(String path) {
		setResponseHeader("Location", path);
		setStatus(301);
	}
	
	public void printStackTrace(Throwable t) {
		PrintStream stream=new PrintStream(getRawOutput());
		stream.print("<pre>");
		t.printStackTrace(stream);
		stream.print("</pre>");
		stream.flush();
	}
	
	public void redirectWithMessage(String string, String message) {
		this.print("<script>");
		message = message.replace('\"', '\'');
		this.print("var msg=\"" + message + "\";");
		this.print("alert(msg);");
		this.print("location='" + string + "'");
		this.print("</script>");
	}
	

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		if (StringUtils.isNotBlank(charset))
			this.charset = charset;
	}

	public void returnJson(Object json) {
		this.getOutput();
		if (withJsonHeader) {
			// RFC 4627: The MIME media type for JSON text is application/json.
			setResponseHeader("Content-Type","application/json;charset=" + this.charset);
		}
		if (json instanceof JsonResponse) {
			((JsonResponse) json).output(getOutput());
		} else {
			JSON.writeJSONStringTo(json, getOutput());
		}
	}
}
