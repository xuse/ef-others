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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

//Swing 关闭当前窗口,打开新窗口
public class CloseAndReopen extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	JButton bt;
	JPanel p;

	public CloseAndReopen() {
		bt = new JButton("Test");
		bt.addActionListener(this);
		p = new JPanel();
		this.setTitle("Test");
		this.setSize(800, 600);
		p.add(bt);
		this.add(p);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		new CloseAndReopen();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(bt)) {
			Password p = new Password(this);
			p.setVisible(true);
			this.setVisible(false);
		}
	}

	class Password extends JFrame {
		public Password(final JFrame main) {
			this.setTitle("Password");
			this.setSize(300, 200);
			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					main.setVisible(true);
				}
			});
		}

		private static final long serialVersionUID = 1L;
	}
}
