package jef.stroage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import jef.common.JefException;
import jef.storage.FileManager;
import jef.storage.FileServiceFactory;
import jef.storage.JFile;
import jef.tools.ResourceUtils;

import org.junit.Ignore;
import org.junit.Test;

public class FileStorgeTest {
	@Test
	@Ignore
	public void testIRC() throws IOException, SQLException, JefException {
		FileManager fm = FileServiceFactory.getFileManage();
		fm.clearAll();
		System.out.println("========App name" + fm.getApplicationName() + "========");
		//
		JFile f1 = fm.insert(ResourceUtils.getResourceFile("build.number"), "测我文件1.com");
		JFile f2 = fm.insert(ResourceUtils.getResourceFile("DrFeeDetail.bo.xml"), "测试.ini");
		fm.insert(ResourceUtils.getResourceFile("testfile/NTDETECT.123"), "系统");
		System.out.println("========Step2========");
		JFile f3 = fm.get(f1.getUuid());
		System.out.println(f3.getRealName());
		File f = f3.getFile();
		System.out.println(f.getCanonicalPath());
		System.out.println(f.length());
		System.out.println("========其他========");
		JFile f4 = fm.get(f2.getUuid());
		f = f4.getFile();
		System.out.println(f4.getRealName());
		System.out.println(f.length());
		System.out.println(f.getCanonicalPath());
		System.out.println("======再其他啊=======");
		fm.remove(f1.getUuid());
		f3 = fm.get(f1.getUuid());
		System.out.println(f3);
	}

	@Test
	@Ignore
	public void testConsistency() throws JefException {
		FileManager fm = FileServiceFactory.getFileManage();
		fm.keepConsistency("PIMS");
	}

	@Test
	public void tt2() throws IOException {
		Properties pro = new Properties();
		pro.setProperty("username", "kkk");
		pro.setProperty("password", "nopass");
		pro.setProperty("username\\myName", "jiyi");
		FileOutputStream fos = new FileOutputStream("c:\\config.xml");
		pro.storeToXML(fos, "test");
		fos.close();
	}
}
