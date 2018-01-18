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
package jef.http.server.jetty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import jef.common.Entry;
import jef.common.log.LogUtil;
import jef.http.server.WebServer;
import jef.http.server.actions.HttpAction;
import jef.tools.Assert;
import jef.tools.IOUtils;
import jef.tools.XMLUtils;

public class JettyServer implements WebServer {
	Server server;
	private int port = 80;
	private File webRoot = new File("WebRoot");
	private String context;
	private boolean useNIO = true;
	private ContextHandlerCollection handlers;
	private Map<String, WebAppContext> contexts = new HashMap<String, WebAppContext>();
	private boolean mavenStart;
	private boolean parentLoaderFirst = true;

	public boolean isParentLoaderFirst() {
		return parentLoaderFirst;
	}

	public void setParentLoaderFirst(boolean parentLoaderFirst) {
		this.parentLoaderFirst = parentLoaderFirst;
	}

	public boolean isMavenStart() {
		return mavenStart;
	}

	public void setMavenStart(boolean mavenStart) {
		this.mavenStart = mavenStart;
	}

	public JettyServer() {}


	public JettyServer(int port, File rootFolder) {
		this(port, rootFolder, null);
	}

	/**
	 * 构造
	 * @param port
	 * @param rootFolder
	 * @param contextPath
	 */
	public JettyServer(int port, File rootFolder, String contextPath) {
		if (port > 0) {
			this.port = port;
		}
		if (rootFolder != null && rootFolder.exists()) {
			this.webRoot = rootFolder;
		}
		if(contextPath!=null && !contextPath.startsWith("/"))contextPath="/".concat(contextPath);
		this.context = contextPath;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WebServer at port:" + port);
		sb.append("\n");
		int n = 0;
		for (String path : contexts.keySet()) {
			if (n > 0)
				sb.append("\n");
			WebAppContext context = contexts.get(path);
			sb.append(" Context:");
			sb.append(path).append("  ").append(context.getWar());
			n++;
		}
		return sb.toString();
	}

	public void startAndJoin() throws InterruptedException {
		start();
		this.server.join();
	}
	
	public void start() {
		try {
			init();
			server.start();
			LogUtil.show("HTTP Server is listening on port " + port);
		} catch (Throwable e) {
			LogUtil.exception(e);
			throw new RuntimeException(e.getMessage());
		}
		// server.setStopAtShutdown(true);
		// server.setSendServerVersion(false);
	}

	protected void init() throws SAXException, IOException {
		if(server==null){
			//判断端口是否被占用
			try {
				ServerSocket serverSocket =  new ServerSocket(port);
				serverSocket.close();
	        } catch (IOException e) {
	        	LogUtil.show("port:[" + port + "] has been used.");
	        	LogUtil.exception(e);
	        	throw new RuntimeException(e.getMessage());
	        }
	          
			if (useNIO) {
				server = new Server();
				server.setConnectors(new Connector[] { new NetworkTrafficServerConnector(server) {
					{
						setPort(port);
					}
				} });
			} else {
				server = new Server(port);
			}
			handlers=new ContextHandlerCollection();
			server.setHandler(handlers);
			initByContextFile();	
		}
	}

	private void initByContextFile() throws SAXException, IOException {
		if (!webRoot.exists())
			throw new FileNotFoundException();
		if (webRoot.isDirectory()) {
			if (this.context == null) {
				this.context = "/";
			}
			String webroot = IOUtils.getPath(webRoot);
			appendAppContext(context, webroot, null);
		} else {
			//加载JEF自定义的服务配置文件
			Document doc = XMLUtils.loadDocument(webRoot);
			Element root = doc.getDocumentElement();
			Assert.equals(root.getNodeName(), "web-apps", "The xml root is:" + root.getNodeName());
			for (Element app : XMLUtils.childElements(root, "web-app")) {
				String context = XMLUtils.attrib(app, "context");
				String webroot = XMLUtils.attrib(app, "webRoot");
				this.setParentLoaderFirst("true".equals(app.getAttribute("parentClassLoaderPriority")));
				appendAppContext(context, webroot, app);
			}
		}
	}
	
	public void addResourceContext(String context,File dir){
		WebAppContext main = new WebAppContext(handlers,dir.getAbsolutePath(), context);
		contexts.put(context, main);
		return;
	}
	
	public void addAction(String string, HttpAction action) {
		
		
		

		
	}

	/**
	 * 添加一个Context
	 * @param context
	 * @param webroot
	 * @param root
	 * @throws IOException
	 */
	public void appendAppContext(String context, String webroot, Element root) throws IOException {
		WebAppContextEx main = new WebAppContextEx(handlers,webroot, context);
		main.setParentLoaderPriority(this.parentLoaderFirst);// 为了兼容Spring
		//目前采用了基于Eclipse的资源加载方式，似乎不需要这样了。
//		if(this.mavenStart){
//			main.setClassLoader(new MavenClassloader(new URL[]{getClass().getResource("/")},this.getClass().getClassLoader()));
//		}
		contexts.put(context, main);
		if (root == null) {
			File f = new File(webroot, "WEB-INF");
			if (f.exists() && f.isDirectory()) {
				f = new File(f, "web.xml");
				if (f.exists() && f.isFile()) {// 是某个Web项目
					return;
				}
			}
			// 开放某个目录，加上密码
			Map<String, String> users = new HashMap<String, String>();
			users.put("jef", "jefpassword");
			LogUtil.show("the directory " + webroot + " is will publish with authentication: jef/jefpassword");
			addAuthenticator(main, users);
			return;
		}
		Element userlist = XMLUtils.first(root, "users");
		if (userlist != null) {
			Map<String, String> users = new HashMap<String, String>();
			for (Element e : XMLUtils.childElements(userlist, "user")) {
				String user = e.getAttribute("name");
				users.put(user, e.getAttribute("password"));
			}
			addAuthenticator(main, users);
		}
		List<Element> actions = XMLUtils.childElements(root, "action");
		if (actions.size() > 0) {
			// 旧配置，配置了若干WebAction类，使用自动生成web.xml配置文件的方式，将Webaction包装成Servlet运行。
			final List<Entry<String, String>> map = new ArrayList<Entry<String, String>>();
			for (Element action : actions) {
				String path = XMLUtils.attrib(action, "path");
				String className = XMLUtils.attrib(action, "className");
				map.add(new Entry<String, String>(path, className));
			}
			WebXmlConfiguration config = new WebXmlConfiguration() {
				@Override
				protected Resource findWebXml(WebAppContext context) throws IOException, MalformedURLException {
					Document doc = XMLUtils.newDocument();
					doc.setDocumentURI("http://java.sun.com/dtd/web-app_2_3.dtd");
					Element root = XMLUtils.addElement(doc, "web-app");
					for (Entry<String, String> action : map) {
						Element servlet = XMLUtils.addElement(root, "servlet");
						XMLUtils.addElement(servlet, "servlet-name").setTextContent(action.getValue());
						XMLUtils.addElement(servlet, "servlet-class").setTextContent("jef.http.server.actions.DefaultServlet");
						Element param = XMLUtils.addElement(servlet, "init-param");
						XMLUtils.addElement(param, "param-name").setTextContent("class");
						XMLUtils.addElement(param, "param-value").setTextContent(action.getValue());
						Element servletMapping = XMLUtils.addElement(root, "servlet-mapping");
						XMLUtils.addElement(servletMapping, "servlet-name").setTextContent(action.getValue());
						XMLUtils.addElement(servletMapping, "url-pattern").setTextContent(action.getKey());
					}
					File f = File.createTempFile("~tempConfig", ".xml");
					XMLUtils.saveDocument(doc, f, "UTF-8");
					return  Resource.newResource(f.toURI().toURL());
				}
			};
//			main.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
//			 main.setInitParameter("dirAllowed", "true");
			main.setConfigurations(new Configuration[] { config });
			main.setResourceBase(webroot);
		}
	}

	private void addAuthenticator(WebAppContext main, final Map<String, String> userlist) {
		// 验证策略
		ConstraintSecurityHandler sh = new ConstraintSecurityHandler();
		Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, "admin");// 限制访问者角色
		constraint.setAuthenticate(true);
		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec("/*");
		sh.setConstraintMappings(new ConstraintMapping[] { cm });
//		MappedLoginService login = new MappedLoginService() {
//			protected UserIdentity loadUser(String s) {
//				return null;
//			}
//
//			protected void loadUsers() throws IOException {
//				for (String user : userlist.keySet()) {
//					putUser(user, Credential.getCredential(userlist.get(user)), new String[] { "admin" });
//				}
//			}
//		};
//		sh.setLoginService(login);
		main.setSecurityHandler(sh);
	}

	public void stop() {
		try {
			contexts.clear();
			server.stop();
			server.destroy();
			server = null;
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
	}

	public int getPort() {
		return port;
	}

	public String getDefaultContext() {
		return this.context;
	}

	public String getDefaultRoot() {
		return this.webRoot.getAbsolutePath();
	}

	public boolean isStarted() {
		return server.isStarted();
	}

	public boolean isStopped() {
		return server.isStopped();
	}

	public void doStart() {
		try {
			server.start();
		} catch (Exception e) {
			LogUtil.exception(e);
			throw new RuntimeException(e);
		}
	}

	public void doStop() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
