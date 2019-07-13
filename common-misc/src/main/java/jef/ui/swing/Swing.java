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
package jef.ui.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jef.tools.StringUtils;

public class Swing {
	public static File fileOpenDialog(String msg, int selectionMode,String defaultValue) {
		JFileChooser chooser = new JFileChooser();
		// 设置文件选择模式,只要文件
		if(StringUtils.isNotEmpty(defaultValue))
			chooser.setSelectedFile(new File(defaultValue));
		chooser.setFileSelectionMode(selectionMode);
		chooser.showOpenDialog(null);
		File file = chooser.getSelectedFile();
		return file;
	}

	public static File fileSaveDialog(String msg, int selectionMode,String defaultValue) {
		JFileChooser chooser = new JFileChooser();
		// 设置文件选择模式,只要目录
		if(StringUtils.isNotEmpty(defaultValue))
			chooser.setSelectedFile(new File(defaultValue));
		chooser.setFileSelectionMode(selectionMode);
		chooser.showSaveDialog(null);
		File file = chooser.getSelectedFile();
		return file;
	}

	public static boolean showQuestion(String message){
		int i=JOptionPane.showConfirmDialog(null, message, "Confirm", JOptionPane.YES_NO_OPTION);
		return i==JOptionPane.YES_OPTION;
	}
	
	public static final int OK = 0;
	public static final int YES_NO = 4;
	public static final int OK_CANCEL = 8;
	public static final int YES_NO_CANCEL = 16;
	public static final int INPUT = 32;

	public static Object msgbox(String msg, int... type) {
		Object result = null;
		int mType = 0;
		if (type.length == 0) {} else {
			mType = type[0];
		}

		switch (mType) {
		case OK:
			JOptionPane.showMessageDialog(null, msg);
			break;
		case YES_NO:
			result = new Integer(JOptionPane.showConfirmDialog(null, msg));
			break;
		case INPUT:
			result = JOptionPane.showInputDialog(null, msg);
			break;
		default:
			;
		}
		return result;
	}
}
