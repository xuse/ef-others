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
package jef.image.example;

import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class ImageTransferTest {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		JFrame frame1 = new ImageTransferFrame();
		JFrame frame2 = new ImageTransferFrame();
		frame1.setTitle("Frame 1");
		frame2.setTitle("Frame 2");
		frame1.show();
		frame2.show();
	}
}

class ImageTransferFrame extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageTransferFrame() {
		setSize(300, 300);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		Container contentPane = getContentPane();
		label = new JLabel();
		contentPane.add(label, "Center");

		JMenu fileMenu = new JMenu("File");
		openItem = new JMenuItem("Open");
		openItem.addActionListener(this);
		fileMenu.add(openItem);

		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(this);
		fileMenu.add(exitItem);

		JMenu editMenu = new JMenu("Edit");
		copyItem = new JMenuItem("Copy");
		copyItem.addActionListener(this);
		editMenu.add(copyItem);

		pasteItem = new JMenuItem("Paste");
		pasteItem.addActionListener(this);
		editMenu.add(pasteItem);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		setJMenuBar(menuBar);
	}

	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		if (source == openItem) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));

			chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
				public boolean accept(File f) {
					String name = f.getName().toLowerCase();
					return name.endsWith(".gif") || name.endsWith(".jpg") || name.endsWith(".jpeg") || f.isDirectory();
				}

				public String getDescription() {
					return "Image files";
				}
			});

			int r = chooser.showOpenDialog(this);
			if (r == JFileChooser.APPROVE_OPTION) {
				String name = chooser.getSelectedFile().getAbsolutePath();
				setImage(Toolkit.getDefaultToolkit().getImage(name));
			}
		} else if (source == exitItem)
			System.exit(0);
		else if (source == copyItem)
			copy();
		else if (source == pasteItem)
			paste();
	}

	private void copy() {
		ImageSelection selection = new ImageSelection(theImage);
		localClipboard.setContents(selection, null);
	}

	private void paste() {
		Transferable contents = localClipboard.getContents(this);
		if (contents == null)
			return;
		try {
			Image image = (Image) contents.getTransferData(ImageSelection.imageFlavor);
			setImage(image);
		} catch (Exception e) {
		}
	}

	public void setImage(Image image) {
		theImage = image;
		label.setIcon(new ImageIcon(image));
	}

	private static Clipboard localClipboard = new Clipboard("local");

	private Image theImage;

	private JLabel label;

	private JMenuItem openItem;

	private JMenuItem exitItem;

	private JMenuItem copyItem;

	private JMenuItem pasteItem;
}

class ImageSelection implements Transferable {
	public ImageSelection(Image image) {
		theImage = image;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(imageFlavor);
	}

	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(imageFlavor)) {
			return theImage;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	public static final DataFlavor imageFlavor = new DataFlavor(java.awt.Image.class, "AWT Image");

	private static DataFlavor[] flavors = { imageFlavor };

	private Image theImage;
}
