package jef.orm.mysql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import jef.database.DbCfg;
import jef.database.DbClient;
import jef.database.DbClientFactory;
import jef.database.DebugUtil;
import jef.database.OperateTarget;
import jef.database.innerpool.IConnection;
import jef.tools.IOUtils;
import jef.tools.JefConfiguration;
import jef.tools.ResourceUtils;
import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * MySQL测试基类
 * 
 * @Company Asiainfo-Linkage Technologies (China), Inc.
 * @author luolp@asiainfo-linkage.com
 * @Date 2012-8-20
 */
public class MySQLTestBase {

	protected static DbClient db;
	protected static String queryTable;

	@BeforeClass
	public static void init() throws SQLException {
		db = DbClientFactory
				.getDbClient(
						"jdbc:mysql://localhost:3306/openfire?autoReconnect=true&useUnicode=yes&characterEncoding=UTF8",
						"root", "");

		queryTable = JefConfiguration.get(DbCfg.DB_QUERY_TABLE_NAME);
	}

	protected static OperateTarget getDefaultTarget(){
		return new OperateTarget(db,null);
	}
	@AfterClass
	public static void close() throws SQLException {
		System.out.println("Closing database connections...");
		if (StringUtils.isNotBlank(queryTable)) {
			dropTable(queryTable);
		}
		db.close();
	}

	private static void dropTable(String table) {
		IConnection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = DebugUtil.getConnection(getDefaultTarget());
			pstmt = conn.prepareStatement("DROP TABLE " + table);
			pstmt.execute();
		} catch (SQLException e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		} finally {
			try {
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				Assert.fail(e.getMessage());
			} catch (Exception e) {
				Assert.fail(e.getMessage());
			}
		}
	}

	protected void prepareDbByNativeSqls(String sqlFilename) throws Exception {
		String sqls = IOUtils.asString(ResourceUtils.getResource(sqlFilename),"UTF-8");
		IConnection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = DebugUtil.getConnection(getDefaultTarget());
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sqls);
			pstmt.execute();
			conn.commit();
		} finally {
			try {
				pstmt.close();
				conn.closePhysical();
			} catch (SQLException e) {
				Assert.fail(e.getMessage());
			} catch (Exception e) {
				Assert.fail(e.getMessage());
			}
		}
	}

}
