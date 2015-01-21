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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ConvolveOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import jef.common.log.LogUtil;
import jef.tools.IOUtils;
import jef.tools.StringUtils;

import org.apache.commons.lang.ArrayUtils;

public class ImageProcessingTest {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		JFrame frame = new ImageProcessingFrame();
		frame.show();
	}
}

class ImageProcessingFrame extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImageProcessingFrame() {
		setTitle("ImageProcessingTest");
		setSize(300, 400);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		Container contentPane = getContentPane();
		panel = new ImageProcessingPanel();
		contentPane.add(panel, "Center");

		JMenu fileMenu = new JMenu("File");
		openItem = new JMenuItem("Open");
		openItem.addActionListener(this);
		fileMenu.add(openItem);

		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(this);
		fileMenu.add(exitItem);

		reset = new JMenuItem("reset");
		reset.addActionListener(this);
		fileMenu.add(reset);
		
		JMenu editMenu = new JMenu("Edit");
		blurItem = new JMenuItem("Blur");
		blurItem.addActionListener(this);
		editMenu.add(blurItem);

		sharpenItem = new JMenuItem("Sharpen");
		sharpenItem.addActionListener(this);
		editMenu.add(sharpenItem);

		brightenItem = new JMenuItem("Brighten");
		brightenItem.addActionListener(this);
		editMenu.add(brightenItem);

		edgeDetectItem = new JMenuItem("Edge detect");
		edgeDetectItem.addActionListener(this);
		editMenu.add(edgeDetectItem);

		negativeItem = new JMenuItem("Negative");
		negativeItem.addActionListener(this);
		editMenu.add(negativeItem);

		rotateItem = new JMenuItem("Rotate");
		rotateItem.addActionListener(this);
		editMenu.add(rotateItem);

		filter1Item = new JMenuItem("F1");
		filter1Item.addActionListener(this);
		editMenu.add(filter1Item);

		filter2Item = new JMenuItem("F2");
		filter2Item.addActionListener(this);
		editMenu.add(filter2Item);

		filter3Item = new JMenuItem("F3");
		filter3Item.addActionListener(this);
		editMenu.add(filter3Item);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		setJMenuBar(menuBar);
		
		
		String name=Preferences.userRoot().get("jef.image.test.lastload",null);
		if(name!=null && new File(name).exists()){
			panel.loadImage(name);
		}
	}

	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		if (source == openItem) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));

			chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
				public boolean accept(File f) {
					String ext=IOUtils.getExtName(f.getName());
					return ArrayUtils.contains(new String[]{"png","jpg","bmp","jpeg","gif","pic"}, ext)||f.isDirectory();
				}
				public String getDescription() {
					return "Image files";
				}
			});

			int r = chooser.showOpenDialog(this);
			if (r == JFileChooser.APPROVE_OPTION) {
				String name = chooser.getSelectedFile().getAbsolutePath();
				panel.loadImage(name);
			}
		} else if (source == reset){
			panel.reset();
		} else if (source == exitItem)
			System.exit(0);
		else if (source == blurItem)
			panel.blur();
		else if (source == sharpenItem)
			panel.sharpen();
		else if (source == brightenItem)
			panel.brighten();
		else if (source == edgeDetectItem)
			panel.edgeDetect();
		else if (source == negativeItem)
			panel.negative();
		else if (source == rotateItem)
			panel.rotate();
		else if (source == filter1Item)
			panel.f1();
		else if (source == filter2Item)
			panel.f2();
		else if (source == filter3Item)
			panel.f3();
	}

	private ImageProcessingPanel panel;

	private JMenuItem openItem;
	private JMenuItem exitItem;
	private JMenuItem reset;
	
	private JMenuItem blurItem;
	private JMenuItem sharpenItem;
	private JMenuItem brightenItem;
	private JMenuItem edgeDetectItem;
	private JMenuItem negativeItem;
	private JMenuItem rotateItem;
	private JMenuItem filter1Item;
	private JMenuItem filter2Item;
	private JMenuItem filter3Item;
}

class ImageProcessingPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null)
			g.drawImage(image, 0, 0, null);
	}


	//红色加强
	void redenhance() {
		ColorComponentScaler c = new ColorComponentScaler(1.2, 1.0, 1.0);
		Image img = createImage(new FilteredImageSource(image.getSource(), c));
		image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		image.getGraphics().drawImage(img, 0, 0, null);
		repaint();
	}
	//绿色加强
	void greenEnhance(){
		ColorComponentScaler c = new ColorComponentScaler(1.0, 1.2, 1.0);
		Image img = createImage(new FilteredImageSource(image.getSource(), c));
		image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		image.getGraphics().drawImage(img, 0, 0, null);
		repaint();
	}
	//蓝色加强
	void BlueEnhance(){
		ColorComponentScaler c = new ColorComponentScaler(1.0, 1.0, 1.2);
		Image img = createImage(new FilteredImageSource(image.getSource(), c));
		image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		image.getGraphics().drawImage(img, 0, 0, null);
		repaint();		
	}

	public void f1() {
		LogUtil.show(StringUtils.join(jef.image.Curve.f1, ','));
		BufferedImageOp op = new LookupOp(new ByteLookupTable(0, jef.image.Curve.f1), null);
		filter(op);
	}
	public void f2() {
		LogUtil.show(StringUtils.join(jef.image.Curve.f2, ','));
		BufferedImageOp op = new LookupOp(new ByteLookupTable(0, jef.image.Curve.f2), null);
		filter(op);
	}
	public void f3() {
		LogUtil.show(StringUtils.join(jef.image.Curve.f3, ','));
		BufferedImageOp op = new LookupOp(new ByteLookupTable(0, jef.image.Curve.f3), null);
		filter(op);
	}

	public void loadImage(String name) {
		Image loadedImage = Toolkit.getDefaultToolkit().getImage(name);
		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(loadedImage, 0);
		try {
			tracker.waitForID(0);
		} catch (InterruptedException e) {
		}
		source=loadedImage;
		Preferences.userRoot().put("jef.image.test.lastload", name);
		image = new BufferedImage(loadedImage.getWidth(null), loadedImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.drawImage(loadedImage, 0, 0, null);
		repaint();
	}
	
	public void reset() {
		image.getGraphics().drawImage(source, 0, 0, null);
		repaint();
	}

	private void filter(BufferedImageOp op) {
		BufferedImage filteredImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		op.filter(image, filteredImage);
		image = filteredImage;
		repaint();
	}

	private void convolve(float[] elements) {
		Kernel kernel = new Kernel(3, 3, elements);
		ConvolveOp op = new ConvolveOp(kernel);
		filter(op);
	}

	public void blur() {
		float weight = 1.0f / 9.0f;
		float[] elements = new float[9];
		for (int i = 0; i < 9; i++)
			elements[i] = weight;
		convolve(elements);
	}

	public void sharpen() {
		float[] elements = { 0.0f, -1.0f, 0.0f, -1.0f, 5.f, -1.0f, 0.0f, -1.0f, 0.0f };
		convolve(elements);
	}

	void edgeDetect() {
		float[] elements = { 0.0f, -1.0f, 0.0f, -1.0f, 4.f, -1.0f, 0.0f, -1.0f, 0.0f };
		convolve(elements);
	}

	public void brighten() {
		float a = 1.5f;
		float b = -20.0f;
		RescaleOp op = new RescaleOp(a, b, null);
		filter(op);
	}

	void negative() {
		byte negative[] = new byte[256];
		for (int i = 0; i < 256; i++)
			negative[i] = (byte) (255 - i);
		ByteLookupTable table = new ByteLookupTable(0, negative);
		LookupOp op = new LookupOp(table, null);
		filter(op);
	}

	void rotate() {
		AffineTransform transform = AffineTransform.getRotateInstance(Math.toRadians(5), image.getWidth() / 2, image.getHeight() / 2);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		filter(op);
	}
	private Image source;
	private BufferedImage image;
}
