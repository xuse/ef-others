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
package jef.codegen.shell;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import jef.ui.swing.PanelWrapper;
import jef.ui.swing.Swing;
import jef.ui.swing.VerticalFlowLayout;

public abstract class BaseModelGenerator extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	protected JTextArea jlog;// 提示信息的文本
	private About about;
	protected JButton submit;
	protected JButton exit;

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		Font vFont = new Font("Dialog", Font.PLAIN, 13);
		//JDK 6/5的默认字体是不一样的，这会造成排版混乱，因此无论是6还是5，都调整一下字体
		UIManager.put("ToolTip.font", vFont);
		UIManager.put("Table.font", vFont);
		UIManager.put("TableHeader.font", vFont);
		UIManager.put("TextField.font", vFont);
		UIManager.put("ComboBox.font", vFont);
		UIManager.put("TextField.font", vFont);
		UIManager.put("PasswordField.font", vFont);
		UIManager.put("TextArea.font", vFont);
		UIManager.put("TextPane.font", vFont);
		UIManager.put("EditorPane.font", vFont);
		UIManager.put("FormattedTextField.font", vFont);
		UIManager.put("Button.font", vFont);
		UIManager.put("CheckBox.font", vFont);
		UIManager.put("RadioButton.font", vFont);
		UIManager.put("ToggleButton.font", vFont);
		UIManager.put("ProgressBar.font", vFont);
		UIManager.put("DesktopIcon.font", vFont);
		UIManager.put("TitledBorder.font", vFont);
		UIManager.put("Label.font", vFont);
		UIManager.put("List.font", vFont);
		UIManager.put("TabbedPane.font", vFont);
		UIManager.put("MenuBar.font", vFont);
		UIManager.put("Menu.font", vFont);
		UIManager.put("MenuItem.font", vFont);
		UIManager.put("PopupMenu.font", vFont);
		UIManager.put("CheckBoxMenuItem.font", vFont);
		UIManager.put("RadioButtonMenuItem.font", vFont);
		UIManager.put("Spinner.font", vFont);
		UIManager.put("Tree.font", vFont);
		UIManager.put("ToolBar.font", vFont);
		UIManager.put("OptionPane.messageFont", vFont);
		UIManager.put("OptionPane.buttonFont", vFont);
	}

	private ActionListener menuListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String temp = e.getActionCommand();
			if ("\u5173\u4E8E".equals(temp))
				about.setVisible(true);

		}
	};

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("\u53D6\u6D88".equals(cmd)) {
			onExit();
			dispose();
		} else if ("\u751F\u6210".equals(cmd)) {
			new Thread(){
				@Override
				public void run() {
					lock();
					try{
						generate();	
					}finally{
						unlock();	
					}
				}
			}.start();
		} else {
			Swing.msgbox("Unknown command "+ cmd);
		}
	}

	private PrintStream output;

	protected void onExit(){
	}
	
	protected abstract void generate();

	protected void lock() {
		exit.setEnabled(false);
		submit.setEnabled(false);
	}

	protected void unlock() {
		exit.setEnabled(true);
		submit.setEnabled(true);
	}

	public BaseModelGenerator(String title) {
		super(title);
		JMenuBar bar = new JMenuBar();
		this.setJMenuBar(bar);
		JMenu menu = new JMenu("\u5E2E\u52A9");
		JMenuItem item = new JMenuItem("\u5173\u4E8E");
		menu.add(item);
		item.addActionListener(menuListener);
		bar.add(menu);
		about = new About(this.getTitle());
		this.initWidget();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		adjustBounds();
		this.pack();
		this.setVisible(true);
		// 给输出重定向
		OutputStream out = new OutputStream() {
			ByteBuffer bf = ByteBuffer.allocate(1024);
			private Charset charset = Charset.forName("UTF-8");

			@Override
			public void write(int c) throws IOException {
				if (c == 0x0A || c == 0x0D) {
					flush();
				} else {
					bf.put((byte) c);
				}
			}

			@Override
			public void flush() throws IOException {
				bf.flip();
				CharBuffer sb = charset.decode(bf);
				bf.clear();
				if (sb.length() > 0) {
					String data=sb.toString();
					if(data.indexOf("[DEBUG]")>-1)return;
					jlog.append(data);
					jlog.append("\n");
					jlog.setCaretPosition(jlog.getText().length());
				}
			}
		};
		output = new PrintStream(out);
		System.setOut(output);
		System.setErr(output);
	}

	private void adjustBounds() {
		int width = getMyWidth();
		int height = getMyHeight();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (width > screenSize.width)
			width = screenSize.width;
		if (height > screenSize.height)
			height = screenSize.height;
		this.setBounds((screenSize.width - width) / 2, (screenSize.height - 10 - height) / 2, width, height);
	}

	protected int getMyWidth() {
		return 600;
	}

	protected int getMyHeight() {
		return 220;
	}

	// 构建界面,增加监听器
	public void initWidget() {
		this.setLayout(new VerticalFlowLayout(FlowLayout.CENTER));
		JPanel actionPanel = new JPanel();
		JPanel logPanel = new JPanel();
		this.add(actionPanel);
		this.add(logPanel);
		// 行、列、
		actionPanel.setLayout(new GridLayout(getInputRows() + 1, getInputColumns(), 0, 4));
		PanelWrapper pp = new PanelWrapper(actionPanel);
		createInputs(pp);

		pp.align = FlowLayout.RIGHT;
		submit = pp.newButton("\u751F\u6210", 60, this);
		exit = pp.newButton("\u53D6\u6D88", 60, this);
		pp.addLine(submit, exit);

		// ------------
		jlog = new JTextArea("", 10, 48);// 提示信息的文本
		jlog.setLineWrap(true);
		JScrollPane pane = new JScrollPane(jlog);// 加上滚动条
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		logPanel.add(pane);
		this.add(actionPanel);
		this.add(logPanel);
	}

	protected int getInputColumns() {
		return 1;
	}

	protected abstract int getInputRows();

	protected abstract void createInputs(PanelWrapper pp);

	private class About extends JDialog {
		private static final long serialVersionUID = 1L;
		
		public About(String name) {
			super(BaseModelGenerator.this,true);
			setTitle("\u5173\u4E8E"+name);
			this.setLayout(new VerticalFlowLayout(FlowLayout.CENTER));
			this.setResizable(false);
			JPanel title = new JPanel();
			title.add(new JLabel("Author:mr.jiyi@gmail.com"));
			
			
			JTextArea text = new JTextArea("", 9, 32);// 提示信息的文本
			text.setText(getAboutText());
			text.setEditable(false);
			text.setLineWrap(true);
			JScrollPane pane = new JScrollPane(text);// 加上滚动条
			pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			
			this.add(pane);
			this.add(title);
			this.setSize(390, 250);
			this.setLocation(250, 150);
		}
	}

	protected abstract String getAboutText();
}
