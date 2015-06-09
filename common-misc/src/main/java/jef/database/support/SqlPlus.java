/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jef.database.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jef.common.log.LogUtil;
import jef.database.DbCfg;
import jef.database.DbClient;
import jef.database.DbClientBuilder;
import jef.database.DbMetaData;
import jef.database.IQueryableEntity;
import jef.database.meta.Column;
import jef.database.meta.Index;
import jef.database.meta.TableInfo;
import jef.jre5support.script.JavaScriptUtil;
import jef.tools.IOUtils;
import jef.tools.JefConfiguration;
import jef.tools.StringUtils;
import jef.ui.ConsoleConversation;
import jef.ui.ConsoleShell;
import jef.ui.WinExecutor;
import jef.ui.console.AbstractConsoleShell;
import jef.ui.console.DefaultBatchConsoleShell;
import jef.ui.console.ShellResult;

import org.apache.commons.lang.ArrayUtils;

public class SqlPlus extends DefaultBatchConsoleShell implements ConsoleShell {
	// private boolean debugMode =
	// JefConfiguration.getBoolean(JefConfiguration.Item.DB_DEBUG, false);
	private static final String BUFFER_FILE = "sqlplus.buf";
	private static final String prompt = "SQL>";
	private boolean timming = false;
	private long start;
	private Writer spool;

	DbClient db = null;

	public SqlPlus(AbstractConsoleShell parent) {
		super(parent);
		if (parent != null) {
			if (parent instanceof SqlPlusSupport) {
				this.db = ((SqlPlusSupport) parent).getDb();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		SqlPlus s = new SqlPlus(null);
		s.start();
	}

	public void start() throws IOException {
		LogUtil.commonDebugAdapter = false;
		if (db == null && StringUtils.isNotBlank(JefConfiguration.get(DbCfg.DB_NAME))) {
			db = new DbClientBuilder(false).build();
			LogUtil.show("database connected.");
		}

		if (parent == null) { // 没有上级Shell的情况下，自己启动Shell.
			withMyShell(db);
			if (parent == null && db != parent && db.isOpen()) {
				db.close();
			}
			LogUtil.show("exited");
			closeSpool();
		}
	}

	protected boolean isMultiBatch() {
		return true;
	}

	private void withMyShell(DbClient db) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		ShellResult ret = ShellResult.CONTINUE;
		System.out.print(getPrompt());
		do {
			String str = readline(br);
			// if (StringUtils.isBlank(str))
			// continue;
			ret = perform(str, true);
		} while (ret != ShellResult.TERMINATE);
	}

	private void closeSpool() {
		if (spool != null) {
			IOUtils.closeQuietly(spool);
			LogUtil.removeOutput(spool);
			spool = null;
		}
	}

	private void createSpool(String fileName) {
		try {
			OutputStream os = new FileOutputStream(new File(fileName), false);
			spool = new OutputStreamWriter(os, "UTF-8");
			LogUtil.addOutput(spool);
		} catch (IOException e) {
			LogUtil.exception(e);
		}

	}

	protected void executeEnd(String[] cmds, String last) {
		if (last.endsWith("/")) {// 去掉结束符
			last = last.substring(0, last.length() - 1);
		} else if (last.indexOf(';') > -1) {
			last = StringUtils.substringBefore(last, ";");
		}
		String s = StringUtils.join(cmds, "\n");
		s += last;
		try {
			executeSql(s, db);
		} catch (SQLException e) {
			LogUtil.show(e.getMessage());
		}
	}

	class ConnectConversation extends ConsoleConversation<String> {
		public ConnectConversation(ConsoleShell app) {
			super(app);
		}

		protected void execute() {
			Preferences p = Preferences.userRoot().node("SqlPlus");
			String connected = p.get("lastConnected", null);
			String url = null;
			String user = null;
			String password = null;
			if (connected != null && connected.length() > 0) {
				String[] params = connected == null ? null : StringUtils.split(connected, "*");
				url = params[0];
				user = params[1];
				password = params[2];
			}
			url = getInputWithDefaultValue("JDBC Url:", url);
			if (StringUtils.isEmpty(url)) {
				return;
			}
			user = getInputWithDefaultValue("username:", user);
			password = getInputWithDefaultValue("password:", password);
			try {
				DbClient client = new DbClientBuilder(url, user, password).build();
				if (parent != null && SqlPlus.this.db != ((SqlPlusSupport) parent).getDb()) {
					SqlPlus.this.db.close();
				}
				SqlPlus.this.db = client;
				p.put("lastConnected", url + "*" + user + "*" + password);
				caseConnected(db, url, user, password);
			} catch (Exception e) {
				LogUtil.exception(e);
			}
		}
	}

	private ShellResult executeOtherCommand(String str) throws SQLException {
		if (str.startsWith("connect")) {
			ConnectConversation c = new ConnectConversation(this);
			c.start();
		} else if (str.equalsIgnoreCase(".schemas")) {
			if (!assertDb(db))
				return ShellResult.CONTINUE;
			LogUtil.show(StringUtils.join(db.getNoTransactionSession().getMetaData(null).getSchemas(), '\n'));
			timming();
		} else if (str.equalsIgnoreCase(".catlogs")) {
			if (!assertDb(db))
				return ShellResult.CONTINUE;
			LogUtil.show(StringUtils.join(db.getNoTransactionSession().getMetaData(null).getCatalogs(), '\n'));
			timming();
		} else if (str.equalsIgnoreCase(".tables")) {
			if (!assertDb(db))
				return ShellResult.CONTINUE;
			LogUtil.show(StringUtils.join(db.getNoTransactionSession().getMetaData(null).getTables(true), '\n'));
			timming();
		} else if (str.equalsIgnoreCase(".views")) {
			if (!assertDb(db))
				return ShellResult.CONTINUE;
			LogUtil.show(StringUtils.join(db.getNoTransactionSession().getMetaData(null).getViews(true), '\n'));
			timming();
		} else if (str.equalsIgnoreCase(".procedures")) {
			if (!assertDb(db))
				return ShellResult.CONTINUE;
			DbMetaData m = db.getNoTransactionSession().getMetaData(null);
			LogUtil.show("==== Procedures of User: " + m.getUserName() + " ====");
			LogUtil.show(m.getProcedures(""));
			timming();
		} else if (str.startsWith("set timming ")) {
			String swith = StringUtils.substringAfter(str, "timming ").trim().toUpperCase();
			timming = swith.equalsIgnoreCase("ON");
		} else if (str.equalsIgnoreCase(".dbinfo")) {
			if (!assertDb(db))
				return ShellResult.CONTINUE;
			DbMetaData meta = db.getNoTransactionSession().getMetaData(null);
			LogUtil.show("==== General Infomation ====");
			LogUtil.show(meta.getDbVersion());
			LogUtil.show("User              \t" + meta.getUserName());

			LogUtil.show("==== SQL keyword ====");
			LogUtil.show(meta.getSQLKeywords());
			LogUtil.show("==== Supported functions ====");
			LogUtil.show(meta.getAllBuildInFunctions());
			LogUtil.show("==== Supported DataType ====");
			LogUtil.show(StringUtils.join(meta.getSupportDataType(), ','));
		} else if (str.equalsIgnoreCase("help")) {
			LogUtil.show(helpInfo);
		} else if (str.startsWith(".create ")) {
			if (!assertDb(db))
				return ShellResult.CONTINUE;
			String[] args = StringUtils.substringAfter(str, ".create ").trim().split(" ");
			if (args[0].length() == 0)
				return ShellResult.CONTINUE;
			String fileName = args[0];
			try {
				Class<?> c = Class.forName(fileName);
				String tableName = (args.length > 1) ? args[1] : c.getSimpleName();
				db.createTable(c.asSubclass(IQueryableEntity.class), tableName, null);
			} catch (ClassNotFoundException e) {
				LogUtil.show("类" + fileName + "不存在。");
			} catch (ClassCastException e) {
				LogUtil.show("类" + fileName + "不是合法的DataObject类");
			} catch (Exception e) {
				LogUtil.show(e);
			}
			timming();
		} else if (str.equalsIgnoreCase(".tablesize")) {
			if (!assertDb(db))
				return ShellResult.CONTINUE;
			DbMetaData m = db.getNoTransactionSession().getMetaData(null);
			Map<String, String> result = new HashMap<String, String>();
			for (TableInfo table : m.getTables(true)) {
				String sql = "select count(*) as count from " + table.getName();
				long n = db.loadBySql(sql, Long.class);
				result.put(table.getName(), String.valueOf(n));
			}
			LogUtil.show(result);
			timming();
		} else if (str.startsWith("spool ")) {
			String fileName = StringUtils.substringAfter(str, "spool ").trim();
			if ("OFF".equalsIgnoreCase(fileName)) {
				if (spool == null) {
					LogUtil.show("没有打开的Spool。");
				} else {
					closeSpool();
				}
			} else {
				if (spool != null) {
					LogUtil.show("之前的Spool尚未关闭。");
				} else {
					createSpool(fileName);
				}
			}
		} else if (str.startsWith("desc ")) {
			if (!assertDb(db))
				return ShellResult.CONTINUE;
			String tableName = db.getProfile().getObjectNameToUse(StringUtils.substringAfter(str, "desc ").trim());
			DbMetaData meta = db.getNoTransactionSession().getMetaData(null);
			if (meta.existTable(tableName)) {
				List<Column> cs = meta.getColumns(tableName, true);
				LogUtil.show("======= Table " + tableName + " has " + cs.size() + " columns. ========");
				for (Column c : cs) {
					LogUtil.show(StringUtils.rightPad(c.getColumnName(), 10) + "\t" + StringUtils.rightPad(c.getDataType(), 9) + "\t" + c.getColumnSize() + "\t" + (c.isNullAble() ? "null" : "not null") + "\t" + c.getRemarks());
				}
				LogUtil.show("======= Table " + tableName + " Primary key ========");
				LogUtil.show(meta.getPrimaryKey(tableName));

				Collection<Index> is = meta.getIndexes(tableName);
				LogUtil.show("======= Table " + tableName + " has " + is.size() + " indexes. ========");
				for (Index i : is) {
					LogUtil.show(i);
				}
				timming();
			} else {
				LogUtil.show("Table " + tableName + " does not exist.");
			}
		} else if (str.startsWith("@")) {
			if (!assertDb(db))
				return ShellResult.CONTINUE;
			String filename = StringUtils.substringAfter(str, "@").trim();
			File file = new File(filename);
			if (!file.exists() && filename.indexOf(".") == -1) {
				file = new File(filename + ".sql");
			}
			if (!file.exists() || file.isDirectory()) {
				LogUtil.show("file is not exist.");
			} else {
				executeFile(file);
			}
		} else {
			return new ShellResult(str);
		}
		return ShellResult.CONTINUE;
	}

	public void caseConnected(DbClient db2, String url, String user, String password) {
	}

	private ShellResult executeFile(File file) {
		BufferedReader reader = null;
		ShellResult result = ShellResult.CONTINUE;
		try {
			reader = IOUtils.getReader(file, null);
			if (file.getName().endsWith(".js")) {
				ScriptEngine e = JavaScriptUtil.newEngine();
				e.put("db", db);
				try {
					e.eval(reader);
				} catch (ScriptException e1) {
					LogUtil.exception(e1);
				}
			} else {
				String str;
				while ((str = reader.readLine()) != null) {
					if (str.startsWith("#") || str.startsWith("--") || str.startsWith("@")) {
						LogUtil.show(str.trim());
					} else {
						try {
							LogUtil.show(str.trim());
							result = perform(str, false);
						} catch (Exception e) {
							LogUtil.show(e.getMessage());
						}
					}
				}
			}
		} catch (IOException e) {
			LogUtil.exception(e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return result;
	}

	private String readline(BufferedReader br) {
		try {
			String str = br.readLine();
			if (str == null)
				str = "exit";
			return StringUtils.trimToNull(str);
		} catch (IOException e) {
			LogUtil.exception(e);
			return null;
		}
	}

	private void executeSql(String str, DbClient db) throws SQLException {
		if (!assertDb(db))
			return;
		if (str.toUpperCase().startsWith("SELECT") || str.toUpperCase().startsWith("VALUES") || str.toUpperCase().startsWith("CALL") || str.toUpperCase().startsWith("(SELECT")) {
			if (spool == null) {
				LogUtil.show(db.getResultSet(str, JefConfiguration.getInt(JefConfiguration.Item.CONSOLE_SHOW_RESULT_LIMIT, 30)));
			} else {
				LogUtil.show(db.getResultSet(str, 0));
			}
		} else {
			int n = db.executeSql(str);
			if (n > -1) {
				LogUtil.show("operation is done on " + n + " records.");
			} else {
				LogUtil.show("sql executed.");
			}
		}
		timming();

	}

	private void timming() {
		if (timming) {
			long time = System.currentTimeMillis() - start;
			LogUtil.show("Time cost: " + time + "ms.");
		}
	}

	private boolean assertDb(DbClient db) throws SQLException {
		if (timming)
			start = System.currentTimeMillis();
		if (db == null || db.isOpen() == false) {
			LogUtil.show("Database was not connected yet.");
			return false;
		}
		return true;
	}

	public String getPrompt() {
		if (sub.get() != null)
			return sub.get().getPrompt();
		if (poolSize() > 0) {
			return "--";
		} else {
			return prompt;
		}
	}

	private static final String helpInfo = "Commands of SQL Mode\n\n" + "exit             		Exit SQl mode.\n" + "help             		Display this screen.\n" + "desc tablename			Display the detail of table\n" + "connect database 		Connect to another database.\n"
			+ ".tables         		List all tables in database.\n" + ".views         		List all views in database.\n" + ".procedures     		List all procedures in database.\n" + ".dbinfo        		List basic infomations of database.\n" + "set timming ON|OFF		set the timming mode.\n"
			+ "@filename              execute sql scripts.\n" + "And any other SQL statments...";

	private static String[] exitCommands = new String[] { "q", "qsql", "exit" };

	public void exit() {
		LogUtil.show(this.getClass().getSimpleName() + " exit.");
	}

	protected int appendCommand(String str) {
		if (ArrayUtils.contains(exitCommands, str)) {
			return RETURN_TERMINATE;
		} else if (str.startsWith("edit") && super.poolSize() == 0) {
			File file = new File(BUFFER_FILE);
			try {
				if (!file.exists())
					file.createNewFile();
			} catch (IOException e) {
				LogUtil.exception(e);
			}
			long last = file.lastModified();
			if (WinExecutor.execute("notepad.exe " + file.getAbsolutePath()) == 0) {
				if (file.exists() && file.lastModified() != last) {
					ShellResult result = executeFile(file);
					if (result == ShellResult.TERMINATE)
						return RETURN_TERMINATE;
				}
			}
			return RETURN_CANCEL;
		} else if (str.startsWith("--")) {
			return RETURN_CONTINUE;
		} else {
			try {
				ShellResult s = executeOtherCommand(str);
				if (s.needProcess()) {
					str = s.getCmd();
					if (str.endsWith("/") || str.indexOf(';') > -1) {
						return RETURN_READY;
					} else {
						commandPool.add(str + "\n");
					}
				}
			} catch (SQLException e) {
				LogUtil.exception(e);
			}
		}
		return RETURN_CONTINUE;
	}
}
