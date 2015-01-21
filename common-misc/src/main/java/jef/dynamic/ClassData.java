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



/**
 * 用于动态编译，在内存中存放Class数据的对象
 * 
 * @author Administrator
 * 
 */
public class ClassData implements ClassSource{
	private final String name;
	private byte[] data;
	private long lastModified;

	public ClassData(String name,byte[] data) {
		this.name = name;
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	public String getName() {
		return name;
	}
	
	public void touch(){
		this.lastModified=System.currentTimeMillis();
	}
	
}
