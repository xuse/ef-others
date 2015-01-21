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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import jef.ui.swing.tree.IconNode;
import jef.ui.swing.tree.IconNodeRenderer;

public class AnimatedIconTreeExample extends JFrame {
	private static final long serialVersionUID = 1L;

	public AnimatedIconTreeExample() {
		super("AnimatedIconTreeExample");
		String[] strs = { "CARNIVORA", // 0
				"Felidae", // 1
				"Acinonyx jutatus  (cheetah)", // 2
				"Panthera leo  (lion)", // 3
				"Canidae", // 4
				"Canis lupus  (wolf)", // 5
				"Lycaon pictus  (lycaon)", // 6
				"Vulpes Vulpes  (fox)" }; // 7

		IconNode[] nodes = new IconNode[strs.length];
		for (int i = 0; i < strs.length; i++) {
			nodes[i] = new IconNode(strs[i]);
		}
		nodes[0].add(nodes[1]);
		nodes[0].add(nodes[4]);
		nodes[1].add(nodes[2]);
		nodes[1].add(nodes[3]);
		nodes[4].add(nodes[5]);
		nodes[4].add(nodes[6]);
		nodes[4].add(nodes[7]);

		nodes[2].setIcon(new ImageIcon("title4.gif"));
		nodes[3].setIcon(new ImageIcon("title4.gif"));
		nodes[5].setIcon(new ImageIcon("title4.gif"));
		nodes[6].setIcon(new ImageIcon("title4.gif"));
		nodes[7].setIcon(new ImageIcon("title4.gif"));
		
		JTree tree = new JTree(nodes[0]);
		tree.setCellRenderer(new IconNodeRenderer());
		setImageObserver(tree, nodes);

		JScrollPane sp = new JScrollPane(tree);
		getContentPane().add(sp, BorderLayout.CENTER);
	}

	@SuppressWarnings("unused")
	private Object[] getRoot() {
		String[] strs = { "CARNIVORA", // 0
				"Felidae", // 1
				"Acinonyx jutatus  (cheetah)", // 2
				"Panthera leo  (lion)", // 3
				"Canidae", // 4
				"Canis lupus  (wolf)", // 5
				"Lycaon pictus  (lycaon)", // 6
				"Vulpes Vulpes  (fox)" }; // 7

		IconNode[] nodes = new IconNode[strs.length];
		for (int i = 0; i < strs.length; i++) {
			nodes[i] = new IconNode(strs[i]);
		}
		nodes[0].add(nodes[1]);
		nodes[0].add(nodes[4]);
		nodes[1].add(nodes[2]);
		nodes[1].add(nodes[3]);
		nodes[4].add(nodes[5]);
		nodes[4].add(nodes[6]);
		nodes[4].add(nodes[7]);

		nodes[2].setIcon(new ImageIcon("title4.gif"));
		nodes[3].setIcon(new ImageIcon("title4.gif"));
		nodes[5].setIcon(new ImageIcon("title4.gif"));
		nodes[6].setIcon(new ImageIcon("title4.gif"));
		nodes[7].setIcon(new ImageIcon("title4.gif"));
		return nodes;  
	}

	private void setImageObserver(JTree tree, IconNode[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			ImageIcon icon = (ImageIcon) nodes[i].getIcon();
			if (icon != null) {
				icon.setImageObserver(new NodeImageObserver(tree, nodes[i]));
			}
		}
	}

	class NodeImageObserver implements ImageObserver {
		JTree tree;

		DefaultTreeModel model;

		TreeNode node;

		NodeImageObserver(JTree tree, TreeNode node) {
			this.tree = tree;
			this.model = (DefaultTreeModel) tree.getModel();
			this.node = node;
		}

		public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
			if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
				TreePath path = new TreePath(model.getPathToRoot(node));
				Rectangle rect = tree.getPathBounds(path);
				if (rect != null) {
					tree.repaint(rect);
				}
			}
			return (flags & (ALLBITS | ABORT)) == 0;
		}
	}

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception evt) {}

		AnimatedIconTreeExample frame = new AnimatedIconTreeExample();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setSize(280, 200);
		frame.setVisible(true);
	}
}



