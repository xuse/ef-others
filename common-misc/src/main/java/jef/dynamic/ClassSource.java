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
import java.io.IOException;

import jef.tools.Assert;
import jef.tools.IOUtils;

public interface ClassSource {
	String getName();
	byte[] getData();
	long getLastModified();
	
	public static class FileClassSource implements ClassSource{
		private File file;
		private String name;
		private long lastModified;
		
		public FileClassSource(File file,String className){
			Assert.isTrue(file.exists());
			this.name=className;
			this.file=file;
			this.lastModified=file.lastModified();
		}
		
		public byte[] getData() {
			try {
				return IOUtils.toByteArray(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public long getLastModified() {
			return lastModified;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name+"\t"+file.getAbsolutePath();
		}
		
	}
}
