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
package jef.storage.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述一个应用程序的存储配制
 */
public class Storage {
	private String name;
	private String root;
	
	private String directory;// 设备存放路径
	private DirStructure dirStructure;// 内层目录格式
	private StorageNaming storageNaming;  //文件命名
	
	private String storageType;// 文件存放的格式
	private boolean enableNativeStorage;// 是否打开保持原有格式文档的存储模式
	private Map<String,String> noZipFileTypes = new HashMap<String,String>();// 保持原文档存储的格式类型
	

	public Map<String, String> getNoZipFileTypes() {
		return noZipFileTypes;
	}

	public void setNoZipFileTypes(Map<String, String> noZipFileTypes) {
		this.noZipFileTypes = noZipFileTypes;
	}

	public boolean isEnableNativeStorage() {
		return enableNativeStorage;
	}

	public void setEnableNativeStorage(boolean enableNativeStorage) {
		this.enableNativeStorage = enableNativeStorage;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public DirStructure getDirStructure() {
		return dirStructure;
	}
	
	public void setDirStructure(DirStructure dirStructure) {
		this.dirStructure = dirStructure;
	}

	public String getStorageType() {
		return storageType;
	}

	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}
	
	public enum DirStructure {
		FILENAME_HASH("采用Hash结构模式，进行重新分配"), Y1("采用年一级目录形式，路径为 /YYYY"), YM2("采用年月二级目录形式，路径为 /YYYY/MM"), YMD3("采用年月日三级目录形式，路径为 /YYYY/MM/DD"), YMDH4("采用年月日时 四级目录形式，路径为 YYYY/MM/DD/HH");
		private String description;

		public String getDescription() {
			return description;
		}

		private DirStructure(String des) {
			this.description = des;
		}
	}
	public enum StorageNaming {
		TIMESTAMP,GUID
	}

	/**
	 * 文件存放格式
	 */
	public static final String STORAGE_TYPE_ZIP = "zipFile";
	public static final String STORAGE_TYPE_ORIGINAL = "nativeFile";


	public StorageNaming getStorageNaming() {
		return storageNaming;
	}

	public void setStorageNaming(StorageNaming storageNaming) {
		this.storageNaming = storageNaming;
	}
}
