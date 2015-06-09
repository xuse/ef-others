package jef;

import java.io.File;
import java.io.IOException;

import jef.tools.XMLUtils;
import jef.tools.reflect.ClassLoaderUtil;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class GeneralTest {
	@Test
	public void ttt1() throws SAXException, IOException {
		ClassLoaderUtil.addClassPath(new File("E:/MyWork/jef/easyframe-core/common-ioc/target/common-ioc-1.7.0-SNAPSHOT.jar"));
		Document doc = XMLUtils.loadDocument("E:/MyWork/jef/chatserver/src/main/webapp/WEB-INF/web.xml");
//		XMLUtils.parseHTML(new URL("http://baidu.com"));

	}
}
