package jef.http.server.jetty;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import jef.tools.ArrayUtils;

import org.eclipse.jetty.webapp.WebAppClassLoader;

public class JettyWebAppClassLoader extends WebAppClassLoader{

	public JettyWebAppClassLoader(ClassLoader parent, Context context) throws IOException {
		super(parent, context);
	}

	public JettyWebAppClassLoader(Context context) throws IOException {
		super(context);
	}

	@Override
	public URL[] getURLs() {
		if(this.getParent() instanceof URLClassLoader){
			URLClassLoader parent=(URLClassLoader)this.getParent();
			return ArrayUtils.merge(super.getURLs(), parent.getURLs());
		}else{
			return super.getURLs();
		}
	}
}
