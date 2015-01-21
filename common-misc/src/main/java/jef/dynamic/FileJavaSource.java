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
package jef.dynamic;

import java.io.File;

import jef.tools.Assert;
import jef.tools.IOUtils;

/**
 * 基本的Java源代码描述，由一个文件和一个类名构成
 * @author Administrator
 *
 */
public class FileJavaSource implements JavaSource{
	private File file;
	private String className;
	
	public File asSourceFile() {
		return file;
	}

	public String getClassName() {
		return className;
	}
	
	public 	FileJavaSource(File file,String clsName){
		this.file=file;
		this.className=clsName;
		Assert.isTrue(file.exists());
		Assert.equals(IOUtils.getExtName(file.getName()),"java");
	}

	public File asSourceFileInFolder(File folder) {
		Assert.isTrue(folder.isDirectory());
		String tmppath=className.replace('.', '/');
		File f=new File(folder,tmppath+".java");
		IOUtils.copyFile(file, f);
		return f;
	}

	public boolean asTempFile() {
		return false;
	}

//	public ICompilationUnit toEcjUnit() {
//		File file=asSourceFile();
//		try {
//			byte[] data = IOUtils.asByteArray(file);
//			if(this.asTempFile()){
//				file.delete();
//			}
//			return new DCompilationUnit(className,new ByteArrayInputStream(data));
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
}
