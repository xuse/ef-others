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

/**
 * 文件分割与合并器
 * 主要用于:移动存储设备空间有限时候的文件传输,
 *         把大的文件分割成小的文件,
 *         在需要时把小的合并成大的原来的文件.
 *         (还有一种文件分割合并器,是把多个小的文件合并成一个大的文件,
 *         并能够把合并成的大的文件分割成小的原有的文件,
 *         类似于文件打包器)
 * 基本功能:1.文件打开,保存,
 *                    2文件的分割,选择分割的大小(size),分割的单位(Kb,Mb)
 *         3.文件的合并,
 *         4.显示分割和合并时程序运行的详细信息; 
 *         5.提示文件分割或合并的成功与失败
 * 使用注意:
 *         文件分割或者合并时候,不要在目标文件夹内
 *         放置本软件其他文件的分割文件(文件名里有@字符的文件)   
 *         每次合并文件时,只需要打开其中一个分割过的文件(文件名有@字符的)!     
 */
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import jef.common.Callback;
import jef.common.log.LogUtil;
import jef.tools.IOUtils;
import jef.tools.StringUtils;

public class FileCutter extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextField text1;// 打开文件的地址

	private JTextField text2;// 保存文件的地址

	private JTextField text3;// 分割大小

	private JTextArea text4;// 提示信息的文本

	private JComboBox combo;// 获得切割文件大小的单位

	private About about;

	private ActionListener menuListener =new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			String temp = e.getActionCommand();
			if ("关于".equals(temp))
				about.setVisible(true);

		}
	};
	
	private ActionListener buttonListener =new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			String temp = e.getActionCommand();
			if ("Cut".equals(temp))
				cut();
			else if ("Combine".equals(temp))
				combine();
		}
	};
	
	public FileCutter() {
		super("Open Portal元数据XML生成器 (For Hibernate)");
		JMenuBar bar = new JMenuBar();
		this.setJMenuBar(bar);
		JMenu menu = new JMenu("帮助");
		JMenuItem item = new JMenuItem("关于");
		menu.add(item);
		item.addActionListener(menuListener);
		bar.add(menu);
		about = new About();
		this.initWidget();
		this.setLocation(200, 300);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(600, 220);
		this.pack();
		this.setVisible(true);
		this.addInfo("请选择hbm文件!\n");
	}
	

	
	// 构建界面,增加监听器
	public void initWidget() {
		this.setLayout(new GridLayout(1, 2));
		JPanel actionPanel = new JPanel();
		JPanel logPanel = new JPanel();
		this.add(actionPanel);
		this.add(logPanel);
		
		String[] names = { "Open", "Save To", "Cut", "Combine" };
		JButton[] buttons = this.makeButton(names, buttonListener);
		
		actionPanel.setLayout(new GridLayout(7, 1, 0, 0));
		PanelWrapper pp=new PanelWrapper(actionPanel);
		
		Callback<File,Throwable> openCall=new Callback<File,Throwable>(){
			public void call(File file) throws Throwable {
				addInfo("Open file successfully");
				addInfo(IOUtils.getPath(file));
				addInfo("thie size of the file is : " + file.length() / 1024 + "Kb");
				if (file.length() / 1024 > 0)
					addInfo(" or " + file.length() / (1024 * 1024) + " Mb");
			}
		};
		pp.labelWidth=39;
		text1 = pp.createFileOpenField("source","源文件", 208, "..",openCall);// 打开文件的地址
		pp.fileChooseMode=JFileChooser.DIRECTORIES_ONLY;
		
		text2 = pp.createFileSaveField("saveto","存到", 208, "...",null);// 保存文件的地址
		//pp.reset();
		text1.setEditable(false);
		text2.setEditable(false);// 设置两个文本行不可编辑,方便判断有效性
		text1.addActionListener(buttonListener);
		text2.addActionListener(buttonListener);
		
		text3 = pp.createTextField("unitSize","分割", 120);//分割大小
		combo = pp.createCombo("unit","单位", 100, new String[]{"KB","MB"});
		pp.align=FlowLayout.RIGHT;
		pp.addLine(buttons[2],buttons[3]);
		//------------
		text4 = new JTextArea("attention:", 10, 25);// 提示信息的文本
		JScrollPane pane = new JScrollPane(text4);// 加上滚动条
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		logPanel.add(pane);
		this.add(actionPanel);
		this.add(logPanel);
	}

	public JButton[] makeButton(String[] names, ActionListener listener) {
		JButton[] buttons = new JButton[names.length];
		for (int i = 0; i < names.length; i++) {
			buttons[i] = new JButton(names[i]);
			buttons[i].addActionListener(listener);
			buttons[i].setFont(new Font(null,0,11));
		}
		return buttons;
	}

	public void cut() {// 切割文件的方法
		if (!check())
			return;
		int size;// 要分割的尺寸
		try {// 文件尺寸是否有效
			size = Integer.parseInt(text3.getText());
			if (combo.getSelectedIndex() == 0) {
				size = size * 1024;
			} else
				size = size * 1024 * 1024;
		} catch (Exception e) {
			LogUtil.exception(e);
			this.addInfo("you inputed " + "an illegal number for cutting");
			return;
		}
		File openFile;
		try {// 打开的文件是否有效
			openFile = new File(text1.getText());
		} catch (Exception ee) {
			LogUtil.exception(ee);
			this.addInfo("creat file failed! ");
			return;
		}
		// 都没有问题,切割文件
		int n=IOUtils.cut(openFile, text2.getText(), size);
		if(n>0){
			this.addInfo("cuted successfully!! "+ n + "files created.");
		}else{
			this.addInfo("Icreat " + "iostream failed! \n cuting failed");
		}
	}

	public boolean check() {// 检查各个输入是否都有效!
		if (text1.getText().length() == 0) {
			this.addInfo("please choose a file first!");
			return false;
		}
		if (text2.getText().length() == 0) {
			this.addInfo("please set the saving path");
			return false;
		}
		if (text3.getText().length() == 0) {
			this.addInfo("please input a number as the cuting size");
			return false;
		}
		return true;
	}

	public void combine() {// 合并文件的方法
		if (!checkCombin())
			return;
		try {
			File path = new File(text1.getText());
			if (!path.exists())	return;
			
			File[] files = path.getParentFile().listFiles();// 获得这个目录下的所有文件
			String baseName=StringUtils.substringBeforeLast(path.getName(),"@");
			
			this.addInfo("files  " + files.length);
			// 用一个TreeMap存储所有的等待合并的文件
			TreeMap<Integer, String> mp = new TreeMap<Integer, String>();
			for (int i = 0; i < files.length; i++) {
				String name = files[i].getName();
				if (name.startsWith(baseName+"@")) {// 如果是可以识别的分割文件
				// this.addInfor("files "+files.length);
					String[] s = name.split("@");
					int no = Integer.parseInt(s[1]);
					mp.put(no, IOUtils.getPath(files[i]));
					this.addInfo("find file : " + IOUtils.getPath(files[i]));
				}
			}
			// 如果map的size是0,就说明没有找到文件
			if (mp.size() <= 0) {
				this.addInfo("没有找到文件供合并!");
				return;
			}
			// 获得文件的名字,不包含路径
			if(IOUtils.combine(mp.values(), text2.getText(), baseName)){
				this.addInfo("合并完成。");
			}else{
				this.addInfo("Exception!! combine failed");
			}
		} catch (Exception ee) {
			LogUtil.exception(ee);
			this.addInfo("Exception! combining failed!");
		}
	}

	public boolean checkCombin() {
		if (text1.getText().indexOf("@") == -1) {
			this.addInfo("not a cutted file" + text1.getText());
			return false;
		}
		if (text2.getText().length() == 0) {
			this.addInfo("please set the saving path");
			return false;
		}
		return true;
	}

	public void addInfo(String s) {// 增加提示信息
		text4.append("\n" + "@:");
		text4.append(s);
	}

	private class About extends JFrame {
		private static final long serialVersionUID = 1L;
		public About() {
			super("关于元数据生成器");
			this.add(new JLabel("作者:mr.jiyi@gmail.com"));
			this.setSize(200, 100);
			this.setLocation(250, 150);
		}
	}

	public static void main(String[] args) {
		new FileCutter();
	}
}
