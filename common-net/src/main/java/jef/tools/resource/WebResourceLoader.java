package jef.tools.resource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * 在Web应用中，根据Web目录定位资源的加载器。
 * 注意该加载器
 * <listener>
//	  <description>initConfig</description>//这个是描述
//	  <listener-class>jef.tools.resource.WebResourceLoader</listener-class>
// </listener>
 * @author jiyi
 *
 */
public class WebResourceLoader extends AResourceLoader implements ServletContextListener {
	private static File webroot;

    public void contextDestroyed(ServletContextEvent arg0) {
    }
    
    public File getWebroot() {
		return webroot;
	}


	public void contextInitialized(ServletContextEvent event) {
    	webroot=new File(event.getServletContext().getRealPath("/"));
    }



	public URL getResource(String name) {
		File file=new File(webroot,name);
		try {
			return file.exists()?file.toURI().toURL():null;
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException();
		}
	}



	@SuppressWarnings("unchecked")
	public List<URL> getResources(String name) {
		File file=new File(webroot,name);
		try {
			return file.exists()?Arrays.asList(file.toURI().toURL()):Collections.EMPTY_LIST;
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException();
		}
	}

}
