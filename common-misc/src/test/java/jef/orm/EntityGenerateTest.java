package jef.orm;

import java.io.File;
import java.sql.SQLException;

import jef.codegen.EntityGenerator;
import jef.codegen.MetaProvider.DbClientProvider;
import jef.common.log.LogUtil;
import jef.database.DbClient;
import jef.database.DbUtils;

import org.junit.Test;

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
			g.setProvider(new DbClientProvider(new DbClient()));
			g.addExcludePatter(".*_\\d+$");
			g.addExcludePatter("AAA");
			g.setMaxTables(4);
			g.setSrcFolder(new File("c:/"));
			g.setBasePackage("com.ailk.acct.payfee.acct.entity");
			g.generateSchema();
		} catch (Exception e) {
			LogUtil.exception(e);
		}
	}
}
