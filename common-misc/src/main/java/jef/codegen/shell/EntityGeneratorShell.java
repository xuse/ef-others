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
package jef.codegen.shell;

import java.io.File;
import java.sql.SQLException;

import javax.swing.JFileChooser;

import jef.common.log.LogUtil;
import jef.database.DbClientBuilder;
import jef.ui.swing.PanelWrapper;

import com.github.geequery.codegen.EntityGenerator;
import com.github.geequery.codegen.MetaProvider.DbClientProvider;

public class EntityGeneratorShell extends BaseModelGenerator {
	public EntityGeneratorShell() {
		super("JEF/JPA\u5B9E\u4F53\u5B9A\u4E49\u751F\u6210\u5668");
	}

	private static final long serialVersionUID = 1L;
	private PanelWrapper pp;

	@Override
	protected void generate() {
		String dbType=pp.getTextValue("dbType");
		String host= pp.getTextValue("host");
		String sid=pp.getTextValue("sid");
		String user= pp.getTextValue("user");
		String password=pp.getTextValue("password");
		String pkgName=pp.getTextValue("pkgName");
		File target = new File(pp.getTextValue("target"));
		
		EntityGenerator g = new EntityGenerator();
		try {
			g.setProvider(new DbClientProvider(new DbClientBuilder(dbType, host, 0, sid, user, password).build()));
//			g.setProfile(DbmsProfile.Oracle);
			g.addExcludePatter(".*_\\d+$"); //防止出现分表
			g.addExcludePatter("AAA");      //排除表
			g.setMaxTables(999);
			g.setSrcFolder(target);
			g.setBasePackage(pkgName);
			g.generateSchema();
		} catch (SQLException e) {
			LogUtil.exception(e);
		}
	}

	public static void main(String... args) {
		new EntityGeneratorShell();
	}

	@Override
	protected void createInputs(PanelWrapper pp) {
		//"com.ailk.acct.payfee.acct.entity"
		this.pp = pp;
		pp.buttonFontSize = 13;
		pp.labelWidth = 60;
		pp.fileChooseMode = JFileChooser.DIRECTORIES_ONLY;
		pp.createCombo("dbType", "数据库类型", 205, new String[]{"oracle","derby","mysql","sqlServer","sqLite","db2"});
		pp.createTextField("host", "数据库地址", 205);
		pp.createTextField("sid", "数据库SID", 205);
		pp.createTextField("user", "用户名", 205);
		pp.createTextField("password", "密码", 205);
		pp.createTextField("pkgName", "实体类包名", 205);
		
		pp.createFileSaveField("target", "存到文件夹", 170, "...", null);// 保存文件的地址
		File f = new File("test");
		pp.setTextValue("target", f.getAbsolutePath());
		pp.setTextValue("pkgName", "com.ailk.demo.entity");
		pp.setTextValue("sid", "pocdb");
		pp.setTextValue("host", "10.10.12.31");
		pp.setTextValue("user", "ZG");
		pp.setTextValue("password", "ZG");
	}

	@Override
	protected int getInputRows() {
		return 3;
	}

	@Override
	protected int getInputColumns() {
		return 2;
	}

	@Override
	protected String getAboutText() {
		return "使用本工具可以从数据库的表结构中生成数据实体的java代码。";
	}
}
