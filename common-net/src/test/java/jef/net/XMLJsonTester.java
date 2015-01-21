package jef.net;

import java.io.IOException;

import jef.json.JsonUtil;
import jef.tools.JXB;
import jef.tools.X;
import jef.tools.XMLFastJsonParser;
import jef.tools.XMLUtils;
import jef.tools.string.RandomData;

import org.easyframe.fastjson.JSONObject;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLJsonTester extends org.junit.Assert{

	/**
	 * 飒飒
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Test
	public void ttt() throws SAXException, IOException, InstantiationException, IllegalAccessException {
		BeanForTest p = RandomData.newInstance(BeanForTest.class);
		JSONObject s =(JSONObject) JsonUtil.toJsonTree(p);

		{
			//对象到xml的直接转换，再转回来
			Document doc = XMLUtils.newDocument();
			JXB.objectToXML(p, doc);
			System.out.println("==================Document Style jxb===================");
			X.print(doc);
			
			BeanForTest o=(BeanForTest)JXB.elementToObject(doc.getDocumentElement());
			assertEquals(p.getName(), o.getName());
		}
		
		{
			//对象到xml的第一种转换，再转回来
			Document doc = XMLFastJsonParser.DEFAULT.toDocument(s);
			System.out.println("==================Document Style 1===================");
			X.print(doc);
			
			
			JSONObject r1 = XMLFastJsonParser.DEFAULT.toJsonObject(doc);
			BeanForTest p1 = JsonUtil.convert(r1,BeanForTest.class);
			assertEquals(p.getName(), p1.getName());
		}
		

		{
			Document doc = XMLFastJsonParser.SIMPLE.toDocument(s);
			System.out.println("==================Document Style 2===================");
			X.print(doc);
			
			JSONObject r2 = XMLFastJsonParser.SIMPLE.toJsonObject(doc);	
			BeanForTest p2 = JsonUtil.convert(r2,BeanForTest.class);
			assertEquals(p.getName(), p2.getName());
			
		}
	}
}

