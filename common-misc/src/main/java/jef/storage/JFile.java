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
package jef.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import jef.common.log.LogUtil;
import jef.database.DataObject;
import jef.database.annotation.EasyEntity;
import jef.storage.common.Storage;
import jef.storage.common.StorageConfigurations;
import jef.tools.IOUtils;
import jef.tools.StringUtils;

@EasyEntity(checkEnhanced=false)
@Entity
public class JFile extends DataObject{
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(columnDefinition="varchar(42)",nullable=false)
	private String uuid;// 文件存储实体名称（不含扩展名）

	@Column(columnDefinition="varchar(255)",nullable=false)
	private String storageFolder;// 文件存取目录
	
	/**
	 * 保存方式
	 */
	@Column(columnDefinition="varchar(20)",nullable=false)
	private String storageType;// 文档保存采用的方式，见系统编码
	
	/**
	 *文件名称 
	 */
	@Column(columnDefinition="varchar(255)",nullable=false)
	private String realName;// 文件的名称 （不含扩展名）
	
	/**
	 * 备注
	 */
	@Column(columnDefinition="varchar(255)",nullable=false)
	private String description;// 文件描述
	
	/**
	 * 应用代码
	 */
	@Column(columnDefinition="varchar(60)",nullable=false)
	private String applicationCode;
	
	/**
	 * 文档插入时间
	 */
	@Column(columnDefinition="timestamp",nullable=false)
	private Date createTime;

	
	protected boolean isSaved=false;
	//内容
	private InputStream stream;//文件数据流
	private File file;
	
	/**
	 * 空构造
	 */
	public JFile(){};
	/**
	 * 用本地文件构造
	 * @param file
	 * @param desc
	 */
	public JFile(File file,String desc){
		try{
			init(new FileInputStream(file),file.getName(),null,desc);	
		}catch(IOException e){
			LogUtil.exception(e);
		}
	}
	/**
	 * 用输入流构造
	 * @param in
	 * @param fileName
	 * @param applicationCode
	 */
	public JFile(InputStream in, String fileName, String applicationCode) {
		init(in,fileName,applicationCode,null);
	}
	
	private void init(InputStream in, String fileName, String appCode,String fileDesc) {
		Storage app = StorageConfigurations.getApplicationContext(appCode);
		if(app.getStorageNaming()==Storage.StorageNaming.GUID){
			this.uuid=StringUtils.generateGuid();	
		}else{
			this.uuid=String.valueOf(new Date().getTime());
		}
		this.stream=in;
		this.createTime=new Date();
		if (fileName != null && app != null) {
			this.realName=fileName;
			String ext="";
			if(realName.indexOf(".")>-1){
				ext= realName.substring(realName.lastIndexOf(".") + 1, realName.length());
			}
			if(fileDesc!=null)this.description=fileDesc;
			this.applicationCode=app.getName();
			this.storageType=app.getStorageType();

			if (app.isEnableNativeStorage()) {// 如果有保持原有文档存储的格式文档，则
				if (app.getNoZipFileTypes().containsKey(ext.toLowerCase())) {// 如果在其范围内，则为压缩格式
					this.storageType=Storage.STORAGE_TYPE_ORIGINAL;
				}
			}
			this.storageFolder = FileHelper.calcStroagePath(app, fileName);// 存放目录
		}else{
			throw new NullPointerException();
		}
	}

	/**
	 * 获取 文件路径 的信息
	 * @return Returns the fileRemark String.
	 */
	public String getStroagePath() {
		if (storageType.equals(Storage.STORAGE_TYPE_ORIGINAL))
			return this.storageFolder + File.separator + this.uuid;

		if (storageType.equals(Storage.STORAGE_TYPE_ZIP))
			return this.storageFolder + File.separator + this.uuid + ".zip";
		return null;
	}

	public String getCreateTimeString() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(this.getCreateTime());
	}

	/**
	 * 获取 createTime 的信息
	 * 
	 * @return Returns the createTime Date.
	 */
	public Date getCreateTime() {
		if (createTime == null)
			createTime = new Date();
		return createTime;
	}

	/**
	 * 获取主键
	 * @return
	 */
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * 设置 createTime 的值
	 * 
	 * @param createTime
	 *            The createTime to set.
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * 获取 fileStorageDirectory 的信息 
	 * 
	 * @return Returns the fileStorageDirectory String.
	 */
	public String getStorageFolder() {
		return storageFolder;
	}

	/**
	 * 设置 fileStorageDirectory 的值
	 * @param fileStorageDirectory
	 *            The fileStorageDirectory to set.
	 */
	public void setStorageFolder(String fileStorageDirectory) {
		this.storageFolder = fileStorageDirectory;
	}

	/**
	 * 获取 fileRealName 的信息
	 * 
	 * @return Returns the fileRealName String.
	 */
	public String getRealName() {
		return realName;
	}

	/**
	 * 设置 fileRealName 的值
	 * 
	 * @param fileRealName
	 *            The fileRealName to set.
	 */
	public void setRealName(String fileRealName) {
		this.realName = fileRealName;
	}

	/**
	 * 获取 ApplicationCode 的信息
	 * 
	 * @return Returns the applicationCode String.
	 */
	public String getApplicationCode() {
		return applicationCode;
	}

	/**
	 * 设置 ApplicationCode 的值
	 */
	public void setApplicationCode(String applicationCode) {
		this.applicationCode = applicationCode;
	}

	/**
	 * 获取 description 的信息
	 * 
	 * @return Returns the description String.
	 */
	public String getDescription() {
		if (this.description == null)return " ";
		return description;
	}

	/**
	 * 设置 description 的值
	 * 
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 获取 storageType 的信息
	 * 
	 * @return Returns the storageType String.
	 */
	public String getStorageType() {
		return storageType;
	}

	/**
	 * 设置 storageType 的值 类型 为 String
	 * 
	 * @param storageType
	 *            The storageType to set.
	 */
	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}

	public InputStream getStream() {
		return stream;
	}
	
	/**
	 * 将输入流的内容保存为一个文件，获得这个文件。
	 * @param isCloseInputStream:保存完成后关闭输入流. 
	 */
	public File getFile() throws IOException{
		if(file !=null) return file;
		Storage app = StorageConfigurations.getApplicationContext(this.applicationCode);
		File dir=new File(app.getRoot()+"/view");
		if(!dir.exists())dir.mkdirs();
		file=new File(app.getRoot()+"/view/"+this.realName);
		IOUtils.copy(this.stream, new BufferedOutputStream(new FileOutputStream(file)), true);
		this.stream=new FileInputStream(file);
		return file;
	}

	void setStream(InputStream stream) {
		this.stream = stream;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		if(stream!=null)stream.close();
	}

	//JEF-ORM 定义
	public enum Field implements jef.database.Field{
		uuid,storageFolder,storageType,realName,description,applicationCode,
		createTime
	}
}
