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

import javax.swing.JFileChooser;

import jef.codegen.Junit4Generator;
import jef.ui.swing.PanelWrapper;

import org.apache.commons.lang.StringUtils;


public class Junit4GeneratorShell extends BaseModelGenerator{
	public Junit4GeneratorShell() {
		super("Junit测试案例批量生成器");
	}

	private static final long serialVersionUID = 1L;
	private PanelWrapper pp;
	private String pkgNames;
	private File target;
	
	@Override
	protected void generate() {
		pkgNames=pp.getTextValue("package");
		target=new File(pp.getTextValue("target"));
		if(!target.exists()){
			System.out.println("目标文件夹"+target.getPath()+"不存在。");
			return;
		}else if(target.isFile()){
			System.out.println("目标不是文件夹。");
			return;
		}
		Junit4Generator gen=new Junit4Generator();
		gen.setSourceFolder(target);
		gen.generateForPackage(StringUtils.split(pkgNames,","));
	}
	
	public static void main(String... args){
		new Junit4GeneratorShell();
	}

	@Override
	protected void createInputs(PanelWrapper pp) {
		this.pp=pp;
		pp.buttonFontSize=13;
		pp.labelWidth=45;
		pp.fileChooseMode=JFileChooser.DIRECTORIES_ONLY;
		
		pp.createTextField("package", "包路径", 460);
		pp.createFileSaveField("target","存到文件夹", 460, "...",null);// 保存文件的地址
		File f=new File("test");
		pp.setTextValue("target",f.getAbsolutePath());
		pp.setTextValue("package","com,org");
	}

	@Override
	protected int getInputRows() {
		return 2;
	}

	@Override
	protected String getAboutText() {
		return "用于批量生成Junit4的单元测试代码框架。";
	}
}
