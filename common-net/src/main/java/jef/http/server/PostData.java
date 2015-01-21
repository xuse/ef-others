package jef.http.server;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface PostData {
	static final String SIMPLE_FORM_DATA = "application/x-www-form-urlencoded";
	static final String MULTIPART_FORM_DATA = "multipart/form-data";
	
	List<UploadFile> getFormFiles();

	String getParameter(String string);

	Map<String, String[]> getParameterMap();

	public static class UploadFile {
		File tmpFile;
		String fileName;
		String localPath;

		public UploadFile(File file, String filePath, String fieldName) {
			filePath = filePath.replace('\\', '/');
			int n = filePath.lastIndexOf('/');
			this.fileName = (n == -1) ? filePath : filePath.substring(n + 1);
			this.localPath = filePath;
			this.tmpFile = file;
		}

		/**
		 * 文件名
		 * 
		 * @return
		 */
		public String getFileName() {
			return fileName;
		}

		public File getTmpFile() {
			return tmpFile;
		}

		/**
		 * 本地的文件路径
		 * 
		 * @return
		 */
		public String getLocalPath() {
			return localPath;
		}
	}

}
