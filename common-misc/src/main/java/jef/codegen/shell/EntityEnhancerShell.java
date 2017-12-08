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
import java.net.MalformedURLException;

import javax.swing.JFileChooser;

import jef.codegen.EntityEnhancer;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.ui.swing.PanelWrapper;

public class EntityEnhancerShell extends BaseModelGenerator {
	public EntityEnhancerShell() {
		super("JEF/JPA\u5B9E\u4F53\u589E\u5F3A\u5668");
	}

	private static final long serialVersionUID = 1L;
	private PanelWrapper pp;

	@Override
	protected void generate() {
		File target = new File(pp.getTextValue("target"));
		EntityEnhancer en=new EntityEnhancer();
		try {
			en.addRoot(target.toURI().toURL());
		} catch (MalformedURLException e) {
			throw Exceptions.asIllegalArgument(e);
		}
		en.enhance(pp.getTextValue("pkgNames").split(","));
	}

	public static void main(String... args) {
		new EntityEnhancerShell();
	}

	@Override
	protected void createInputs(PanelWrapper pp) {
		//"com.ailk.acct.payfee.acct.entity"
		this.pp = pp;
		pp.buttonFontSize = 13;
		pp.labelWidth = 70;
		pp.fileChooseMode = JFileChooser.DIRECTORIES_ONLY;
		
		pp.createFileOpenField("target", "class fileds:", 445, "...", null);// 保存文件的地址
		pp.createTextField("pkgNames", "package name", 460);
		File f = new File("bin");
		if(!f.exists()){
			f=f.getParentFile();
			f=IOUtils.findFile(f, "WEB-INF",true);
			if(f!=null){
				f= new File(f,"classes");
			}
		}
		if(f!=null)pp.setTextValue("target", f.getAbsolutePath());
		pp.setTextValue("pkgNames", "com,org");
	}

	@Override
	protected int getInputRows() {
		return 2;
	}

	@Override
	protected int getInputColumns() {
		return 2;
	}

	@Override
	protected String getAboutText() {
		return "Entity Enhancement aboutn JEF\n\n"+
"1.What is Enhancement Anyway?\n"+
"Entity bean is always a POJO, (current in JEF is not, but it is a goal of JEF roadmap.)\n"+
"But the JPA spec requires some type of monitoring of Entity objects, and the spec does not define how to implement this monitoring.\n"+ 
"Some JPA providers auto-generate new subclasses or proxy objects that front the user's Entity objects at runtime, while others use byte-code weaving technologies to enhance the actual Entity class objects at build time. \n\n" +
"2. How can I enhance entity in JEF?\n" +
"JEF supports both methods, but strongly suggests only using the build time enhancement.\n"+ 
"this tool is about to help you enhance you entity classes.\n\n" +
"3.Usega\n" +
"Choose the directory where you entity 'class' is. (Note, '.class' file, not '.java' file)\n" +
"and assign one or more package names.\n" +
"then click button to enhance these class files.";
	}
}
