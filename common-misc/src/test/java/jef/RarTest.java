package jef;

import java.io.File;
import java.io.IOException;

import jef.tools.ZipUtilsEx;

import org.junit.Ignore;
import org.junit.Test;

/**
 * TODO 依赖环境变量，故先ingore
 *
 */
@Ignore
public class RarTest {
	
	@Test
	public void testRar() throws IOException{
		ZipUtilsEx.uncompress(new File("c:/xa.rar"), "c:\\aa123", null);
	}

}
