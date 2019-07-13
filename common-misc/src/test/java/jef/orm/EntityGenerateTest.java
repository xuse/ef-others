package jef.orm;

import java.io.File;
import java.sql.SQLException;

import org.junit.Test;

import com.github.geequery.codegen.EntityGenerator;
import com.github.geequery.codegen.MetaProvider.DbClientProvider;

import jef.database.DbClientBuilder;
import jef.database.DbUtils;
import jef.tools.Exceptions;

public class EntityGenerateTest {

	/**
	 * 类生成测试
	 * 
	 * @throws SQLException
	 */
	@Test
	public void findChinese() throws SQLException {
		try {
			EntityGenerator g = new EntityGenerator();
			System.out.println(ClassLoader.getSystemClassLoader().getClass().getName());
			System.out.println(ClassLoader.getSystemClassLoader());
			System.out.println(Thread.currentThread().getContextClassLoader());
			System.out.println("DBUTILS");
			System.out.println(DbUtils.class.getClassLoader());
			g.setProvider(new DbClientProvider(new DbClientBuilder().build()));
			g.addExcludePatter(".*_\\d+$");
			g.addExcludePatter("AAA");
			g.setMaxTables(4);
			g.setSrcFolder(new File("c:/"));
			g.setBasePackage("com.ailk.acct.payfee.acct.entity");
			g.generateSchema();
		} catch (Exception e) {
			Exceptions.log(e);
		}
	}
}
