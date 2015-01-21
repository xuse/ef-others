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
package jef.ui;

import java.awt.FileDialog;
import java.awt.Frame;

public class AWTUtils  extends Frame {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3688005324354747825L;

	/**
	 * 显示文件打开对话框
	 */
	public static String fileOpenDialog(String msg) {
		FileDialog fd = new FileDialog(new AWTUtils(), msg);
		fd.setVisible(true);
		String filePath = null;
		if (fd.getFile() != null) {
			filePath = fd.getDirectory() + fd.getFile();
		}
		fd.dispose();
		return filePath;
	}

	/**
	 * 显示文件打开对话框
	 */
	public static String folderOpenDialog(String msg) {
		FileDialog fd = new FileDialog(new AWTUtils(), msg);
		fd.setVisible(true);
		String filePath = null;
		if (fd.getFile() != null) {
			filePath = fd.getDirectory() + fd.getFile();
		}
		fd.dispose();
		return filePath;
	}
	
	/**
	 * 显示文件保存对话框
	 */
	public static String fileSaveDialog(String msg) {
		FileDialog fd = new FileDialog(new AWTUtils(), msg, FileDialog.SAVE);
		fd.setVisible(true);
		String filePath = null;
		if (fd.getFile() != null) {
			filePath = fd.getDirectory() + fd.getFile();
		}
		fd.dispose();
		return filePath;
	}
}
