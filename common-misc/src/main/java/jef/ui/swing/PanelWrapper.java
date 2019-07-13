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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.ObjectUtils;

import jef.common.Callback;
import jef.tools.Exceptions;
import jef.tools.IOUtils;

/**
 * Swing工具，用具包装一个Panel并在Panel上快速生成组件
 * @author 季怡
 */
public class PanelWrapper {
	public int lineHeight=22;
	public int buttonFontSize=11;
	
	public int align=FlowLayout.LEFT;
	public int fileChooseMode=JFileChooser.FILES_ONLY;
	public int labelWidth=0;
	public int fileSelectButtonWidth=32;
	
	Container c;
	private Map<String,JComponent> inputs=new HashMap<String,JComponent>();
	
	public String getTextValue(String name){
		JComponent c=inputs.get(name);
		if(c==null)return null;
		if(c instanceof JTextField){
			return ((JTextField)c).getText();
		}else if(c instanceof JComboBox){
			JComboBox combo=(JComboBox)c;
			return ObjectUtils.toString(combo.getSelectedItem());
		}else{
			throw new IllegalArgumentException(c.getName() + " is not supported componet.");
		}
	}
	
	public void setTextValue(String name, String absolutePath) {
		JComponent c=inputs.get(name);
		if(c==null)return;
		if(c instanceof JTextField){
			((JTextField)c).setText(absolutePath);
		}else if(c instanceof JComboBox){
			JComboBox combo=(JComboBox)c;
			for(int i=0;i<combo.getItemCount();i++){
				if(ObjectUtils.equals(combo.getItemAt(i), absolutePath)){
					combo.setSelectedIndex(i);
					return;
				}
			}
			combo.insertItemAt(absolutePath, 0);
			combo.setSelectedIndex(0);
		}else{
			throw new IllegalArgumentException(c.getName() + " is not supported componet.");
		}
		
	}
	
	
	public PanelWrapper(Container c){
		this.c=c;
	}
	
	public void reset(){
		align=FlowLayout.LEFT;
		fileChooseMode=JFileChooser.FILES_ONLY;
	}
	
	/**
	 * @Title: addLine
	 * @Description: 将多个组件添加为一行
	 * @param @param widgets
	 * @param @return    设定文件
	 * @return JPanel    返回类型
	 * @throws
	 */
	public JPanel addLine(JComponent... widgets){
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new FlowLayout(align,5, 0));
		for(JComponent w:widgets ){
			logPanel.add(w);	
		}
		c.add(logPanel);
		return logPanel;
	}
	
//	//创建一棵树
//	public JTree createTree(TreeNode node){
//		JPanel logPanel = new JPanel();
//		logPanel.setLayout(new FlowLayout(align,5, 0));
////		for(JComponent w:widgets ){
////			logPanel.add(w);	
////		}
////		c.add(logPanel);
////		return logPanel;
//		return null;
//	}
	
	/**
	 * 添加一个字段
	 * @Title: createTextField
	 * @param name 字段名（变量名）
	 * @param label 显示文字
	 * @param width 宽度
	 * @param widgets 输入框后添加的控件
	 * @throws
	 */
	public JTextField createTextField(String name,String label,int width,JComponent... widgets){
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new FlowLayout(align,5, 0));
		if(label!=null)logPanel.add(getLabel(label));
		JTextField text=new JTextField();
		text.setPreferredSize(new Dimension(width,lineHeight));
		logPanel.add(text);
		for(JComponent w:widgets ){
			logPanel.add(w);	
		}
		c.add(logPanel);
		this.inputs.put(name, text);
		return text;
	}

	private JLabel getLabel(String label) {
		JLabel r=new JLabel(label);
		if(labelWidth>0){
			r.setPreferredSize(new Dimension(labelWidth, lineHeight));
		}
		return r;
	}

	//创建一个打开文件的输入框
	public JTextField createFileOpenField(String name,String label,int width,String buttonText,final Callback<File,Throwable> callback){
		return innerCreateOpenSaveFileField(name,label,width,buttonText,fileSelectButtonWidth,callback,true,fileChooseMode);
	}

	//创建一个保存文件输入框
	public JTextField createFileSaveField(String name,String label,int width,String buttonText,final Callback<File,Throwable> callback){
		return innerCreateOpenSaveFileField(name,label,width,buttonText,fileSelectButtonWidth,callback,false,fileChooseMode);
	}
	
	private JTextField innerCreateOpenSaveFileField(String name,String label,int width,String buttonText,int buttonWidth,final Callback<File,Throwable> callback,final boolean isOpen,final int type){
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new FlowLayout(align,5, 0));
		if(label!=null)logPanel.add(getLabel(label));
		final JTextField text=new JTextField();
		text.setPreferredSize(new Dimension(width,lineHeight));
		logPanel.add(text);
		text.setEditable(false);
		JButton button = new JButton(buttonText);
		if(buttonWidth>0)button.setPreferredSize(new Dimension(buttonWidth,lineHeight));
		button.setFont(new Font(null,0,buttonFontSize));
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				File file = isOpen?Swing.fileOpenDialog("",type,text.getText()):Swing.fileSaveDialog("",type,text.getText());
				if (file == null) {// 如果文件是空,返回
					return;
				}
				text.setText(IOUtils.getPath(file));
				try {
					if(callback!=null)callback.call(file);
				} catch (Throwable e1) {
					Exceptions.log(e1);
				}
			}
		});
		this.inputs.put(name, text);
		logPanel.add(button);
		c.add(logPanel);
		return text;
	}
	
	public JComboBox createCombo(String name,String label,int width,String[] options,JComponent... widgets){
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new FlowLayout(align,5, 0));
		if(label!=null)logPanel.add(getLabel(label));
		
		JComboBox combo = new JComboBox();
		for(String str:options){
			combo.addItem(str);
		}
		combo.setPreferredSize(new Dimension(width, lineHeight));
		logPanel.add(combo);
		for(JComponent w:widgets ){
			logPanel.add(w);	
		}
		c.add(logPanel);
		this.inputs.put(name, combo);
		return combo;
	}
	
	public JButton newButton(String text,int width,ActionListener listener){
		JButton b=new JButton(text);
		b.addActionListener(listener);
		b.setFont(new Font(null,0,buttonFontSize));
		if(width>0)b.setPreferredSize(new Dimension(width,lineHeight));
		return b;
	}
}
