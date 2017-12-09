import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.alibaba.fastjson.JSON;

import jef.tools.X;

public class SnakeYamlTest {

	@Test
	public void testSnake() throws IOException {
		Yaml yaml = new Yaml();
		Map me = yaml.load(this.getClass().getResource("/test_snake.yaml").openStream());
		System.out.println(JSON.toJSONString(me));
		
		//X.print(X.putBean(X.$new(), me));
	}

}
