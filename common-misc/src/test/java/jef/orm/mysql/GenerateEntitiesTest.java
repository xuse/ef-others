package jef.orm.mysql;

import java.io.File;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.github.geequery.codegen.EntityGenerator;
import com.github.geequery.codegen.MetaProvider.DbClientProvider;

import jef.database.DbClientBuilder;
import jef.database.dialect.AbstractDialect;
import junit.framework.TestSuite;

/**
 * 从MySQL数据库生成entity测试类
 * TODO 依赖MySQL数据库，故先ignore
 * 
 * @see EntityGenerator
 * 
 * @Company Asiainfo-Linkage Technologies (China), Inc.
 * @author luolp@asiainfo-linkage.com
 * @Date 2012-8-2
 */ 
@Ignore
public class GenerateEntitiesTest {

	private EntityGenerator generator;

	@Before
	public void setUp() {
		generator = new EntityGenerator();
		generator.setProfile(AbstractDialect.getDialect("mysql"));
		generator.setProvider(new DbClientProvider(new DbClientBuilder().build()));
		generator.setSrcFolder(new File("src1"));
		generator.setBasePackage("jef.generated.dataobject.mysql");
	}

	/**
	 * 测试由{@code TestSuite}过程中产生的表生成entity（即 DB->ENTITY过程）是否成功。
	 * 
	 * @see TestSuite
	 */
	@Test
	public void testGenerateEntities() {
		try {
			// 列类型为数组类型的表，过滤掉；若实际数据库环境中存在其他含有列类型为数据、自定义类型的，也需过滤掉。
			generator.addExcludePatter("arr");
			generator.setMaxTables(999);
			generator.generateSchema();
		} catch (SQLException e) {
			Assert.fail(e.getMessage());
		}
	}

}
