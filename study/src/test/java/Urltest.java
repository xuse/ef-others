import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.ho.yaml.Yaml;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

import jef.tools.IOUtils;

public class Urltest {
	@Test
	public void testUrl() throws MalformedURLException {
		URL u = new URL("http://10.19.132.101:8081/apidocs#1?aaa");
		String path = u.getPath();
		System.out.println(path);
	}

	@Test
	public void testYaml1() throws FileNotFoundException {
		File dumpFile = IOUtils.urlToFile(this.getClass().getResource("/test_jyml.yaml"));
		System.out.println(dumpFile);
		Map map = Yaml.loadType(dumpFile, HashMap.class);
		System.out.println(JSON.toJSONString(map));

		// X.print(X.putBean(X.$new(), me));
	}

}
