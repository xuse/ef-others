package jef.http.server;

import java.io.File;

public class UploadFile {
	File tmpFile;
	String fileName;
	String partName;

	/**
	 * 
	 * @param file
	 * @param partName
	 * @param fileName
	 */
	public UploadFile(File file, String fileName, String partName) {
		this.tmpFile = file;
		this.partName = partName;
		this.fileName = fileName;
	}

	/**
	 * 上传的文件名
	 * 
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * 上传文件内容（临时文件）
	 * @return
	 */
	public File getTmpFile() {
		return tmpFile;
	}

	/**
	 * 上传时的字段名
	 * @return
	 */
	public String getPartName() {
		return partName;
	}
}