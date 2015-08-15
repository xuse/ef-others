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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jef.common.MimeTypes;
import jef.common.log.LogUtil;
import jef.http.UrlWrapper;
import jef.http.server.PostData;
import jef.http.server.UploadFile;
import jef.http.server.WebExchange;
import jef.tools.Assert;
import jef.tools.DateUtils;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.ThreadUtils;

public class DirManagerAction implements HttpAction {
	private static final String suffix = "";// ".mgr";
	private File baseDir;

	private static String script1 = "function rename(name){var to=prompt('rename to:',name);if(to==null)return;var " + "url='" + suffix + "?rename='+name+'&to='+to;window.location=url;}\n";

	private static String script2 = "function del(name){if(confirm('Delete?'))location='" + suffix + "?del='+name;}";
	private List<FileUploadListener> listeners = new ArrayList<FileUploadListener>();

	public void addListener(FileUploadListener listener) {
		if (listeners == null)
			listeners = new ArrayList<FileUploadListener>();
		listeners.add(listener);
	}

	public DirManagerAction(File baseDir) {
		this.baseDir = baseDir;
	}

	public interface FileUploadListener {
		public void onFileChanged(File file);
	}

	public void doGet(WebExchange exchange) {
		UrlWrapper url = exchange.getRequestURL();
		String path = StringUtils.removeEnd(url.getPath(), suffix);

		if (path.endsWith("!res")) {
			String resource = StringUtils.substringAfterLast(path, "/");
			InputStream in = this.getClass().getResourceAsStream(resource);
			if (in != null) {
				exchange.setStatus(200);
				try {
					IOUtils.copy(in, exchange.getRawOutput(), true);
					return;
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					IOUtils.closeQuietly(in);
				}
			}
			exchange.setStatus(404);
			LogUtil.warn(resource + " not found!");
			return;
		}

		String filePath = exchange.getRealPath(suffix);
		Assert.notNull(filePath);
		File dir = new File(baseDir, filePath);
		if (!dir.exists()) {
			exchange.setStatus(404);
			LogUtil.warn("The file " + path + " is not exist.");
			return;
		}
		try {
			if (dir.isFile()) {
				if (url.getQuery() != null) {
					processFileQuery(dir, exchange);
					return;
				}
				doFileManager(dir, exchange);
			} else {
				if (!path.endsWith("/")) {
					exchange.redirect(path + "/" + suffix + ((url.getQuery() == null) ? "" : "?" + url.getQuery()));
					return;
				}
				if (url.getQuery() != null) {
					String message = processFolderQuery(dir, exchange);
					if (message == null) {
						exchange.redirect(url.getRawPath());
					} else {
						exchange.redirectWithMessage(url.getRawPath(), message);
					}
					return;
				}
				doFolderManager(dir, exchange, path);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void processFileQuery(File dir, WebExchange exchange) {
		if (exchange.getParameter("edit") != null) {
			returnjavaFile(dir, exchange, true);
		}
	}

	private String processFolderQuery(File dir, WebExchange exchange) {
		if (exchange.getParameter("del") != null) {
			File del = new File(dir, exchange.getParameter("del"));
			if (del.exists()) {
				if (del.isFile()) {
					del.delete();
				} else {
					IOUtils.deleteTree(del, true);
				}
			}
		} else if (exchange.getParameter("rename") != null) {
			String source = exchange.getParameter("rename");
			String target = exchange.getParameter("to");
			// System.out.println(source+"->"+target);
			File s = new File(dir, source);
			File t = new File(dir, target);
			if (s.exists()) {
				if (!t.exists()) {
					ThreadUtils.doSleep(1000);
					if (!s.renameTo(t)) {
						return "rename failure.";
					}
				} else {
					return "Target file was exist.";
				}

			}
		}
		return null;
	}

	private void doFolderManager(File file, WebExchange exchange, String path) throws IOException {
		exchange.setResponseHeader("Content-Type", "text/html");
		exchange.setStatus(200);
		exchange.print("<html><head><meta charset='UTF-8' /><title>\n");
		exchange.print("Directory:" + file.getName());
		exchange.print("</title><script>" + script1 + script2 + "</script></head><body>\n");
		exchange.print("<strong>Directory:" + file.getName() + "</strong><hr>");
		exchange.print("<a href=../" + suffix + ">Parent Folder</a><br><br>");
		exchange.print("\n<table>");
		exchange.print("<tr><td width=180><b>");
		exchange.print("File Name");
		exchange.print("&nbsp;</b></td><td width=80><b>");
		exchange.print("Type");
		exchange.print("</b></td><td width=120><b>");
		exchange.print("Last Modified");
		exchange.print("</b></td><td><b>");
		exchange.print("Operation");
		exchange.print("</b></td></tr>");

		for (File sub : IOUtils.listFolders(file)) {
			writeFileEntry(exchange, sub);
		}
		for (File sub : IOUtils.listFiles(file)) {
			writeFileEntry(exchange, sub);
		}
		exchange.print("</table>");
		exchange.print("<form action='' enctype='multipart/form-data' method=post>Add:" + "<input type='file' name='file1'/><input type=submit value=Upload>"
				+ "</form><form action='' method=post>Folder:<input type=text name='directory'><input type=submit value='Create Folder'></form>");
		exchange.print("</body></html>");

	}

	private void writeFileEntry(WebExchange exchange, File sub) {
		exchange.print("<tr><td>");
		exchange.print("<a href='./" + StringUtils.urlEncode(sub.getName()) + (sub.isDirectory() ? "/" + suffix + "'>" : suffix + "'>"));
		exchange.print(sub.getName());
		exchange.print("&nbsp;</a></td><td>");
		exchange.print(sub.isDirectory() ? "&lt;DIR&gt;&nbsp;" : StringUtils.formatSize(sub.length()));
		exchange.print("</td><td>");
		exchange.print(DateUtils.format(sub.lastModified()));
		exchange.print("</td><td align=right>");
		exchange.print("<a href='javascript:rename(\"" + sub.getName() + "\")'>Rename</a>&nbsp;");
		exchange.print("<a href='javascript:del(\"" + StringUtils.urlEncode(sub.getName()) + "\");'>Delete</a>");
		exchange.print("</td></tr>");
	}

	private void doFileManager(File file, WebExchange exchange) throws IOException {
		String fileName = file.getName();

		if (IOUtils.getExtName(fileName).equals("java")) {
			returnjavaFile(file, exchange, false);
			return;
		}
		String mimeType = MimeTypes.getByFileName(fileName);
		if ("text/html".equals(mimeType)) {
			exchange.setResponseHeader("Content-Type", mimeType);
		} else if (mimeType.startsWith("text")) {
			exchange.setResponseHeader("Content-Type", mimeType);
		} else if (mimeType.startsWith("image")) {
			exchange.setResponseHeader("Content-Type", mimeType);
		} else {
			exchange.setResponseHeader("Content-Disposition", "attachment; filename=\"" + StringUtils.urlEncode(fileName) + "\"");
		}
		exchange.setResponseHeader("Content-Length", String.valueOf(file.length()));
		exchange.setStatus(200);
		InputStream in = new FileInputStream(file);
		try {
			IOUtils.copy(in, exchange.getRawOutput(), true);
		} finally {
			in.close();
		}
	}

	private void returnjavaFile(File dir, WebExchange exchange, boolean editMode) {
		String fileName = dir.getName();
		if (!IOUtils.getExtName(fileName).equals("java"))
			return;

		exchange.setResponseHeader("Content-Type", "text/html;charset=utf-8");
		exchange.setStatus(200);
		if (editMode) {
			exchange.print("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><link href='__java.css!res' rel=stylesheet type=text/css /></head><body>" + "<form action='?save=1' method=post acceptcharset='UTF-8'>Edit: " + dir.getPath()
					+ "<br><textarea name=java id=java style='width:80%; height:500px'>");
			try {
				exchange.print(IOUtils.asString(IOUtils.getReader(dir, null)));
			} catch (IOException e) {
				LogUtil.exception(e);
			}
			String url = "\"" + StringUtils.urlEncode(dir.getName()) + suffix + "\"";
			exchange.print("</textarea><br><input type=submit value=save><input type=button value=return onclick='window.location=" + url + ";'></form>");
			exchange.println("</body></html>");
		} else {
			exchange.print("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><link href='__java.css!res' rel=stylesheet type=text/css /></head><body><a href='?edit=1'>Edit</a>" + "&nbsp;<a href='./" + suffix
					+ "'>Return</a><textarea id=java style='height:1px;visibility:hidden'>");
			try {
				exchange.print(IOUtils.asString(IOUtils.getReader(dir, null)));
			} catch (IOException e) {
				LogUtil.exception(e);
			}
			exchange.print("</textarea>" + "<script type=text/javascript src='__java.js!res'></script>" + "</body></html>");
		}
	}

	public void doPost(WebExchange exchange) {
		String path = exchange.getRequestURL().getRawPath();
		File dir = new File(baseDir, StringUtils.urlDecode(exchange.getRealPath(suffix)));
		if (!dir.exists()) {
			LogUtil.warn("The file " + dir + "is not exist.");
			exchange.setStatus(404);
			return;
		}
		if (dir.isDirectory()) {
			List<UploadFile> fs = exchange.getPostdata().getFormFiles();
			for (UploadFile f : fs) {
				File newLocal = new File(dir, f.getFileName());
				newLocal = IOUtils.escapeExistFile(newLocal);
				IOUtils.copyFile(f.getTmpFile(), newLocal);
				if (this.listeners != null) {
					for (FileUploadListener l : this.listeners) {
						l.onFileChanged(newLocal);
					}
				}
			}
			String create = exchange.getPostdata().getParameter("directory");
			if (StringUtils.isNotEmpty(create)) {
				File f = new File(dir, StringUtils.toFilename(create, "_"));
				if (!f.exists()) {
					f.mkdirs();
				}
			}
		} else {
			PostData data = exchange.getPostdata();
			if (data.getParameter("save") != null) {
				try {
					String content = data.getParameter("java");
					IOUtils.saveAsFile(dir, content);
					for (FileUploadListener l : this.listeners) {
						l.onFileChanged(dir);
					}
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
				// File bak=new File(dir.getAbsolutePath()+".bak");
				// if(bak.exists())bak.delete();
				// if(!IOUtils.move(dir,bak)){
				// dir=IOUtils.escapeExistFile(dir);
			}
		}
		// 不是直接返回内容，而是重定向
		// doGet(exchange);
		exchange.redirect(path);

	}

}
