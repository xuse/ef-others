package jef.http.server.jetty;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.reflect.BeanUtils;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

public class WebAppContextEx extends WebAppContext {
	FileResource[] otherDirs;

	public WebAppContextEx(ContextHandlerCollection handlers, String webroot, String context) {
		super(handlers, webroot, context);
		String s = System.getProperty("hik.ext.resource");
		if (StringUtils.isEmpty(s)) {
			File file = new File(webroot, "hik.ext.resource");
			if (file.exists() && file.isFile()) {
				try {
					s = IOUtils.asString(file, null);
				} catch (IOException e) {
				}
			}
		}
		if (StringUtils.isEmpty(s)) {
			return;
		}
		List<FileResource> files = new ArrayList<FileResource>();
		for (String dir : StringUtils.split(s, ";")) {
			dir = dir.trim();
			File file;
			if (dir.startsWith("/") || dir.indexOf(':') > -1) {
				file = new File(dir);
			} else {
				file = new File(webroot, dir);
			}
			if (file.exists()) {
				FileResource res;
				try {
					res = new FileResource(file.toURI().toURL());
					files.add(res);
					System.out.println("附加资源路径：" + file.getAbsolutePath());
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			} else {
				System.out.println("附加资源路径不存在：" + file.getAbsolutePath());
			}
		}
		this.otherDirs = files.toArray(new FileResource[files.size()]);
	}

	private ClassLoader cl;

	public ClassLoader getClassLoader() {
		if (cl == null) {
			try {
				cl=new JettyWebAppClassLoader(this);
				BeanUtils.setFieldValue(this, "_ownClassLoader", true);
				setClassLoader(cl);
				return cl;
			} catch (IOException e) {
				throw new RuntimeException();
			}
		}
		return cl;
	}

	@Override
	public void setClassLoader(ClassLoader classLoader) {
		super.setClassLoader(classLoader);
		this.cl = classLoader;
	}

	@Override
	public Resource getResource(String uriInContext) throws MalformedURLException {
		// System.out.println(uriInContext);
		Resource r = super.getResource(uriInContext);
		if (r instanceof FileResource) {
			if (!(((FileResource) r).getFile()).exists()) {
				if (otherDirs != null) {
					for (FileResource s : otherDirs) {
						try {
							FileResource ff = (FileResource) s.addPath(uriInContext);
							if (ff.getFile().exists()) {
								r = ff;
								break;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}
		}
		return r;
	}

}
