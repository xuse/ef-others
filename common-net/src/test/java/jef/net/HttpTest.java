package jef.net;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import jef.common.log.LogUtil;
import jef.http.client.ConnectOptions;
import jef.http.client.HttpEngine;
import jef.http.client.support.HttpException;
import jef.http.client.support.Proxy;
import jef.tools.XMLUtils;
import jef.tools.rss.HtmlPage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

public class HttpTest {
	private static HttpEngine engine;

	@BeforeClass
	public static void setup() {
		try{
			engine = new HttpEngine();
		} catch (Exception e) {
			LogUtil.exception(e);
		}
	}

	/**
	 * 测试多线程下载文件
	 * 
	 * @throws HttpException
	 */
	@Test
	@Ignore
	public void testDownload1() throws HttpException {
		// 在公司里测试需要设置公司的代理
		ConnectOptions options = new ConnectOptions(Proxy.create("ailk\\jiyi:aaa_333@proxy.asiainfo-linkage.com:8080"));
//单线程下载
		File file = engine.requestFile(1, "http://bar.baidu.com/sobar/install/BaiduSearchBar_Firefox1.0.xpi", null, options);
		System.out.println("download from baidu:" + file);
		assertTrue(file.length() > 0);
		file.delete();
	}

	@Test
	@Ignore
	public void testDownload2() throws HttpException {
		// 在公司里测试需要设置公司的代理
		ConnectOptions options = new ConnectOptions(Proxy.create("ailk\\jiyi:aaa_333@proxy.asiainfo-linkage.com:8080"));
//多线程下载
		File file = engine.requestFile(3, "http://bar.baidu.com/sobar/install/BaiduSearchBar_Firefox1.0.xpi", null, options);
		System.out.println("download from baidu:" + file);
		assertTrue(file.length() > 0);
		file.delete();
	}
	
	@Test
	@Ignore
	public void testHTML() throws HttpException, IOException {
		// 在公司里测试需要设置公司的代理
		ConnectOptions options = new ConnectOptions(Proxy.create("ailk\\jiyi:aaa_333@proxy.asiainfo-linkage.com:8080"));
		HtmlPage doc = new HtmlPage(engine.requestHTML("http://www.baidu.com", options));
		XMLUtils.output(doc.getBodyNode(), System.out);
	}
	
	@Test
	@Ignore
	public void testXML() throws HttpException, IOException {
		Document doc=engine.requestXML("http://10.10.10.141/easyframe/schema/entity.xsd", null);
		XMLUtils.output(doc,System.out);
	}
	
	@AfterClass
	public static void close(){
		engine.shutDown(0);
	}
	
}
