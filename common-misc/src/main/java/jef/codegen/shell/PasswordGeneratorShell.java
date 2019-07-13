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

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JTextField;

import jef.database.DbUtils;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.ui.swing.PanelWrapper;
import jef.ui.swing.Swing;

public class PasswordGeneratorShell extends BaseModelGenerator {
	public PasswordGeneratorShell() {
		super("数据库加密口令生成器");
	}

	private static final long serialVersionUID = 1L;
	private PanelWrapper pp;

	@Override
	protected void generate() {
		Task t = new Task();
		t.start();
	}

	private class Task extends Thread {
		public void run() {
			String pass = pp.getTextValue("password");
			try {
				String str = DbUtils.ecrypt(pass);
				System.out.println("密文生成："+ pass +" ->" + str);
				pp.setTextValue("result", str);
				StringSelection ss = new StringSelection(str);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
				Swing.msgbox("the encrypted password is copyed to clip board.", 0);
			} catch (IOException e) {
				Exceptions.log(e);
			}
		}
	}

	public static void main(String... args) {
		new PasswordGeneratorShell();
		URL url=DbUtils.class.getClassLoader().getResource("encrypt.key");
		if(url!=null){
			System.out.println("找到自定义的密钥文件:"+IOUtils.urlToFile(url).getPath());
		}else{
			System.out.println("没有发现自定义密钥文件，将使用系统缺省密钥。");
		}
	}

	@Override
	protected void createInputs(PanelWrapper pp) {
		// "com.ailk.acct.payfee.acct.entity"
		this.pp = pp;
		pp.buttonFontSize = 13;
		pp.labelWidth = 60;
		pp.fileChooseMode = JFileChooser.DIRECTORIES_ONLY;
		pp.createTextField("password", "口令明文", 205);
		JTextField text = pp.createTextField("result", "口令密文", 205);
		text.setEditable(false);
		pp.setTextValue("password", "");
	}

	@Override
	protected int getInputRows() {
		return 1;
	}

	@Override
	protected int getInputColumns() {
		return 2;
	}

	@Override
	protected String getAboutText() {
		return "使用本工具可以生成密文。";
	}
}
