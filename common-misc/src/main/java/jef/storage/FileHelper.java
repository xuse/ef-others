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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;

import jef.common.log.LogUtil;
import jef.storage.FileManager.JFileInRecycleBin;
import jef.storage.common.Storage;
import jef.storage.common.StorageConfigurations;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.zip.ZipInputStream;
import jef.tools.zip.ZipOutputStream;

import org.xml.sax.SAXException;


/**
 * 此类为文件处理工具，为文档存储系统的核心类
 */
final class FileHelper {
	public static void saveFile(JFile fileDO) throws IOException{
		createDirectory(fileDO);// 创建目录
		innerFileSave(fileDO);// 文件创建
	}

	private static void createDirectory(JFile fileDO) {
		File fileLogFile = new File(fileDO.getStorageFolder() + "/" + StorageConfigurations.fileLogName);
		if (!fileLogFile.exists()) {
			try {// 产生文件存储日志文件
				XMLHelper.generateXML(fileDO.getStorageFolder(),fileDO.getCreateTimeString());
			} catch (IOException e) {
				LogUtil.exception(e);
			}
		}
	}

	/**
	 * 创建一个文件
	 * @throws IOException 
	 * @throws IOException 
	 */
	private static void innerFileSave(JFile fileDO) throws IOException {
		String storageName = fileDO.getStroagePath();
		if (Storage.STORAGE_TYPE_ZIP.equals(fileDO.getStorageType())) {
			//压缩存放模式
			zip(storageName, fileDO.getStream(), fileDO.getRealName());
		} else if (Storage.STORAGE_TYPE_ORIGINAL.equals(fileDO.getStorageType())) {
			//普通存放模式
			OutputStream os = new FileOutputStream(new File(storageName));
			IOUtils.copy(fileDO.getStream(), os, true);
		}
		//if(fileDO.isAutoCloseStream())fileDO.closeStream();
		XMLHelper.addFileLogElement(fileDO);
	}

	//计算文件存储路径
	static String calcStroagePath(Storage app, String fileName){
		String fileAccessDirectory = app.getRoot() + app.getDirectory();
		String realStoragePath = null;
		String nowDate = new SimpleDateFormat("yyyy/MM/dd/HH").format(new Date());
		if (Storage.DirStructure.Y1.equals(app.getDirStructure())) {
			realStoragePath = fileAccessDirectory + "/" + nowDate.substring(0, 4);
		} else if (Storage.DirStructure.YM2.equals(app.getDirStructure())){
			realStoragePath = fileAccessDirectory + "/" + nowDate.substring(0, 7);
		} else if (Storage.DirStructure.YMD3.equals(app.getDirStructure())){
			realStoragePath = fileAccessDirectory + "/" + nowDate.substring(0, 10);
		} else if (Storage.DirStructure.YMDH4.equals(app.getDirStructure())){
			realStoragePath = fileAccessDirectory + "/" + nowDate;
		} else if (Storage.DirStructure.FILENAME_HASH.equals(app.getDirStructure())){
			realStoragePath = fileAccessDirectory + "/" + StringUtils.getCRC(fileName);
		}
		File default_DD_Dir = new File(realStoragePath);
		default_DD_Dir.mkdirs();
		return realStoragePath;
	}
	
	/**
	 * 填充文件内容
	 * 
	 * @param fileID
	 * @param fileDirectory
	 * @throws IOException 
	 */
	static void fillContent(JFile fileDO) throws IOException {
		String storageType = fileDO.getStorageType();
		File fileStorage = new File(fileDO.getStroagePath());
		if (Storage.STORAGE_TYPE_ZIP.equals(storageType)) {
			fileDO.setStream(getUnzipStream(fileStorage));
		} else if (Storage.STORAGE_TYPE_ORIGINAL.equals(storageType)) {
			fileDO.setStream(new BufferedInputStream(new FileInputStream(fileStorage)));
		}
	}

	/**
	 * 删除一个文件
	 */
	static void removeFile(JFile fileDO) {
		String filePath = null;
		filePath = fileDO.getStroagePath();
		File file = new File(filePath);
		if (file.exists() && file.isFile()) {
			file.delete();
			XMLHelper.removeFileLogElement(fileDO.getStorageFolder(),fileDO.getStorageType(),fileDO.getUuid());
		}
	}

	private static void zip(String zipFileName, InputStream in, String fileName) throws IOException  {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		out.putNextEntry(new ZipEntry(fileName));
		IOUtils.copy(in, out, true);
	}

	private static InputStream getUnzipStream(File zipFile) throws IOException{
		FileInputStream fis = new FileInputStream(zipFile);
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fis));
		if (zin.getNextEntry() != null) {
			return zin;
		}
		return null;
	}

	public static boolean removeFileToRecycleBin(JFileInRecycleBin fileDO) throws SAXException, IOException {
		File file = new File(fileDO.getStroagePath());
		File newFile=new File(fileDO.getRecycleBinPath());
		if (file.exists() && file.isFile()) {
			IOUtils.move(file, newFile);
			XMLHelper.removeFileLogElement(fileDO.getStorageFolder(),fileDO.getStorageType(),fileDO.getUuid());
			XMLHelper.addRecycleBinLog(fileDO);
			return true;
		}
		return false;
	}

	public static boolean resore(JFileInRecycleBin fileDO) throws SAXException, IOException {
		File file = new File(fileDO.getStroagePath());
		File newFile=new File(fileDO.getRecycleBinPath());
		if (newFile.exists() && newFile.isFile()) {
			IOUtils.move(newFile, file);
			XMLHelper.addFileLogElement(fileDO);
			XMLHelper.removeRecycleBinLog(fileDO);
			return true;
		}
		return false;
	}
}
