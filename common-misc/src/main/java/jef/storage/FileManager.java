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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import jef.common.log.LogUtil;
import jef.storage.common.Storage;
import jef.storage.common.StorageConfigurations;
import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.StringUtils;

public class FileManager implements IFileManager {
	private FileDAO fileDAO;
	private String applicationName;

	/**
	 * 获得当前的应用配置名
	 * 
	 * @return
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * 设置应用配置名
	 * 
	 * @param applicationName
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * setter for Spring
	 * 
	 * @param fileDAO
	 */
	public void setFileDAO(FileDAO fileDAO) {
		this.fileDAO = fileDAO;
	}

	/**
	 * 获得指定的文件对象
	 * 
	 * @param fileID
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public JFile get(String fileID) throws IOException {
		JFile fileDO = fileDAO.get(fileID);
		if (fileDO != null) {
			FileHelper.fillContent(fileDO);
			fileDO.isSaved = true;
		}
		return fileDO;
	}

	/**
	 * 删除指定的文件对象
	 * 
	 * @param fileID
	 * @return
	 * @throws SQLException
	 */
	public boolean remove(String fileID) {
		if (StringUtils.isEmpty(fileID))
			return false;
		JFile fileDO = null;
		fileDO = fileDAO.get(fileID);
		if (fileDO == null)
			return false;
		fileDAO.remove(fileID);
		FileHelper.removeFile(fileDO);
		return true;
	}

	/**
	 * 储存指定的文件
	 * 
	 * @param file
	 * @param fileDesc
	 *            文件注释
	 */
	public JFile insert(File file, String fileDesc) throws IOException {
		return insert(file, fileDesc, file.getName());
	}

	/**
	 * 储存指定的文件
	 */
	public JFile insert(JFile fileDO) throws IOException {
		if (!fileDO.isSaved) {
			fileDAO.create(fileDO);
			FileHelper.saveFile(fileDO);
			fileDO.isSaved = true;
		}
		return fileDO;
	}

	/**
	 * 储存指定的文件
	 * 
	 * @param file
	 * @param fileDesc
	 *            文件注释
	 * @param fileName
	 *            实际文件名
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public JFile insert(File file, String fileDesc, String fileName) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		JFile fileDO = insert(in, fileName, fileDesc);
		in.close();
		return fileDO;
	}


	public JFile insert(byte[] file, String fileDesc) throws IOException {
		return insert(new ByteArrayInputStream(file),fileDesc);
	}
	
	/**
	 * 储存指定的流
	 * 
	 * @param in
	 *            输入流
	 * @param fileName
	 *            文件名
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public JFile insert(InputStream in, String fileName) throws IOException {
		return insert(in, fileName, null);
	}

	/**
	 * 储存指定的流
	 * 
	 * @param in
	 *            输入流
	 * @param fileName
	 *            文件名
	 * @param fileDesc
	 *            文件备注
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public JFile insert(InputStream in, String fileName, String fileDesc) throws IOException {
		JFile fileDO = new JFile(in, fileName, (applicationName == null) ? StorageConfigurations.DEFAULT_APPLICATION_NAME : applicationName);
		fileDO.setDescription(fileDesc);
		return insert(fileDO);
	}

	/**
	 * 检查本地数据和数据库中的一致性<br>
	 * 1. 如果文件存在，那么会使用XML log和数据库中的记录互相恢复缺失的记录。<br>
	 * 2. 如果文件存在，但XML log和数据库中都没有相应记录，文件会被移至backup目录下。<br>
	 * 3. 如果文件不存在，那么XML和数据库中对应的记录都会被删除。<br>
	 * <br>
	 * 提示: <br>
	 * 当数据库为空时，可以使用此功能从文件系统恢复数据。<br>
	 * 
	 * @param appCode
	 *            应用配置名
	 */
	public void keepConsistency(String appCode) {
		Storage app = StorageConfigurations.getApplicationContext(appCode);
		try {
			importFromDevice(app);
			clearByDevice(app);
		} catch (SQLException e) {
			Exceptions.log(e);
		} catch (IOException e) {
			Exceptions.log(e);
		}
	}

	private int importFromDevice(Storage app) throws SQLException, IOException {
		File dir = new File(app.getRoot() + app.getDirectory());
		if (!dir.exists() || !dir.isDirectory())
			return 0;
		return scanFilesInXml(dir, app);
	}

	private void clearByDevice(Storage app) throws SQLException {
		List<JFile> list = fileDAO.getFdoList(app.getName());
		for (Iterator<JFile> iter = list.iterator(); iter.hasNext();) {
			JFile fdo = (JFile) iter.next();
			File tmpFile = new File(fdo.getStroagePath());
			if (tmpFile.exists() && tmpFile.isFile()) {
			} else {
				LogUtil.show("File ID:" + fdo.getUuid() + " 's file content was deleted. clearing database.");
				fileDAO.remove(fdo.getUuid());
			}
		}
	}

	private int scanFilesInXml(File dir, Storage app) throws SQLException, IOException {
		int n = 0;
		File logXml = new File(dir.getCanonicalPath() + "/fileLog.xml");
		if (logXml.exists()) {
			List<JFile> fdos = XMLHelper.restoreFilesFromXml(logXml, app.getName());
			for (JFile store : fdos) {
				JFile inDb = fileDAO.get(store.getUuid());
				if (inDb == null) {// 将从XML中获得的记录与数据库比对，如果没有则补充数据库记录
					n++;
					fileDAO.create(store);
					System.out.println("File ID:" + store.getUuid() + " Imported to database.");
				}
			}
			// 检查该目录下是否还有其他文件没有记录到XML中去。
			List<String> fnames = XMLHelper.getFilesNotExistInXml(fdos);
			for (String fname : fnames) {
				JFile fdo = fileDAO.get(IOUtils.removeExt(fname));
				// 如果文件没有记录到XML也没有记录到数据库，那么就删除
				if (fdo == null) {
					File tmpFile = new File(dir.getCanonicalPath() + "/" + fname);
					if (tmpFile.exists()) {
						IOUtils.copyToFolder(tmpFile, app.getRoot() + "/backup");
						tmpFile.delete();
						System.out.println("File ID:" + tmpFile.getName() + " Cannot found index info,was moved to backup folder.");
					}
				} else {
					// 如果数据库中存在，那么就在XML中补充节点
					XMLHelper.addFileLogElement(fdo);
					System.out.println("File ID:" + fdo.getUuid() + " record in XML was lost. recovered.");
				}
			}
		}
		for (File subDir : dir.listFiles()) {
			if (subDir.isDirectory()) {
				n += scanFilesInXml(subDir, app);
			}
		}
		return n;
	}

	/**
	 * 将制定ID的存储文件更新为指定的文件
	 */
	public JFile update(String oldId, JFile newFile) throws IOException {
		this.remove(oldId);
		if (newFile != null) {
			insert(newFile);
		}
		return newFile;
	}

	/**
	 * 开启一个事务
	 */
	public FSTransaction startTransaction() {
		return new FSTransaction(this);
	}

	/**
	 * 带回滚功能的FileManager
	 * 
	 * @author Administrator
	 */
	public class FSTransaction implements IFileManager {
		FileManager manager;
		List<JFile> insertedJFile = new ArrayList<JFile>();
		List<JFileInRecycleBin> removedJFile = new ArrayList<JFileInRecycleBin>();

		public FSTransaction(FileManager manager) {
			this.manager = manager;
		}

		public JFile insert(JFile fileDO) throws IOException {
			fileDO = manager.insert(fileDO);
			insertedJFile.add(fileDO);
			return fileDO;
		}

		public JFile update(String oldId, JFile newFile) throws IOException {
			if (oldId != null) {
				this.remove(oldId);
			}
			if (newFile != null) {
				return this.insert(newFile);
			}
			return null;
		}

		public boolean remove(String fileID) throws IOException {
			JFileInRecycleBin old = manager.removeToRecycleBin(fileID);
			if (old != null)
				removedJFile.add(old);
			return old != null;
		}

		public void rollback() throws SQLException {
			for (JFile file : insertedJFile) {
				manager.remove(file.getUuid());
			}
			for (JFileInRecycleBin file : removedJFile) {
				manager.restore(file);
			}
		}

		public void commit() {
			insertedJFile.clear();
			removedJFile.clear();
		}

		public JFile get(String fileID) throws IOException {
			return manager.get(fileID);
		}

		public JFile insert(File file, String fileDesc) throws IOException {
			JFile fileDO = manager.insert(file,fileDesc);
			insertedJFile.add(fileDO);
			return fileDO;
		}

		public JFile insert(byte[] file, String fileDesc) throws IOException {
			JFile fileDO = manager.insert(file,fileDesc);
			insertedJFile.add(fileDO);
			return fileDO;
		}
	}

	/**
	 * 移除文件，区别是移动到回收站中
	 * 
	 * @param oldId
	 * @return
	 * @throws SQLException
	 */
	protected JFileInRecycleBin removeToRecycleBin(String fileID) throws IOException {
		if (StringUtils.isEmpty(fileID))
			return null;
		JFile fileDO = fileDAO.get(fileID);
		if (fileDO == null)
			return null;
		fileDAO.remove(fileID);
		JFileInRecycleBin f = new JFileInRecycleBin(fileDO);
		try {
			if (FileHelper.removeFileToRecycleBin(f)) {
				return f;
			} else {
				return null;
			}
		} catch (SAXException e) {
			Exceptions.log(e);
			return null;
		} catch (IOException e) {
			Exceptions.log(e);
			return null;
		}
	}

	/**
	 * 从回收站中恢复文件
	 * 
	 * @param file
	 */
	protected boolean restore(JFileInRecycleBin file) {
		try {
			return FileHelper.resore(file);
		} catch (SAXException e) {
			Exceptions.log(e);
		} catch (IOException e) {
			Exceptions.log(e);
		}
		return false;
	}

	/**
	 * 描述一个位于回收站里的JFile
	 * 
	 * @author Administrator
	 */
	public class JFileInRecycleBin extends JFile {
		private static final long serialVersionUID = 1L;

		public JFileInRecycleBin(JFile file) {
			this.setApplicationCode(file.getApplicationCode());
			this.setCreateTime(file.getCreateTime());
			this.setDescription(file.getDescription());
			this.setRealName(file.getRealName());
			this.setStorageFolder(file.getStorageFolder());
			this.setStorageType(file.getStorageType());
			this.setUuid(file.getUuid());
			this.deleteTime = new Date();
		}

		private Date deleteTime;

		protected Date getDeleteTime() {
			return deleteTime;
		}

		protected void setDeleteTime(Date deleteTime) {
			this.deleteTime = deleteTime;
		}

		public String getRecycleBinFolder() {
			Storage app = StorageConfigurations.getApplicationContext(getApplicationCode());
			return app.getRoot() + "/RecycleBin";
		}

		public String getRecycleBinPath() {
			Storage app = StorageConfigurations.getApplicationContext(getApplicationCode());
			String path = app.getRoot() + "/RecycleBin/";
			if (getStorageType().equals(Storage.STORAGE_TYPE_ORIGINAL)) {
				return path + getUuid();
			} else if (getStorageType().equals(Storage.STORAGE_TYPE_ZIP)) {
				return path + getUuid() + ".zip";
			} else {
				return null;
			}
		}
	}

	/**
	 * 所有
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void clearAll() throws SQLException, IOException {
		List<JFile> result = fileDAO.getFdoList(this.applicationName);
		for (JFile file : result) {
			this.remove(file.getUuid());
		}
	}
}
