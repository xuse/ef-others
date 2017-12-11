import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import jef.tools.IOUtils;

public class SnakeYamlTest {

	@Test
	public void testSnake() throws IOException {
		Yaml yaml = new Yaml();
		Map me = yaml.load(this.getClass().getResource("/test_snake.yaml").openStream());
		System.out.println(JSON.toJSONString(me));

		// X.print(X.putBean(X.$new(), me));
	}

	@Test
	public void yamlBeanTest() throws IOException {
		YamlReader reader = new YamlReader(IOUtils.getReader(this.getClass(),"/test_snake.yaml","UTF-8"));
		Map demo = reader.read(Map.class);
		reader.close();
		System.out.println(JSON.toJSONString(demo));
	}

}
