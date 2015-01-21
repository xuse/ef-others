package jef.net;

import java.io.File;
import java.io.IOException;

import jef.common.log.LogUtil;
import jef.net.ftp.client.Ftp;
import jef.net.ftp.client.SFtpImpl;
import jef.tools.IOUtils;

import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO 依赖特定环境，故先ignore
 *
 */
@Ignore
public class SFtpTest {

	@Test
	public void testSftp() throws IOException{
		LogUtil.show("========== test Sftp ===========");
		Ftp ftp=new SFtpImpl("localhost","jiyi","123456");
		ftp.setDebug(true);
		ftp.setDefaultEncoding("GBK");
		if(!ftp.connect()){
			throw new IllegalStateException();
		}
		LogUtil.show(ftp.dir("thread_dump.3?"));
		ftp.cd("aaa123/aa");
		ftp.cdUp();
		ftp.pwd();
		ftp.delete("aa");
		ftp.upload(new File("D:/我的文档111/Downloads/ConcurrentReferenceMap源码"),new File("D:/我的文档111/Downloads/[Doc]Java程序单元测试规范.docx"));
		IOUtils.closeQuietly(ftp);
	}
}
