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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JFrame;

public class DoubleBufferedImage extends JApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3721789130318458500L;

	private Image dbImage;

	private Image originalImage;

	private int xLocation = 0;

	private int imageWidth, imageHeight;

	private Graphics dbImageGraphics;

	private String imageURLString = "file:///c:/222.jpg";

	public void init() {
		URL url = null;
		try {
			url = new URL(imageURLString);
		} catch (MalformedURLException me) {
			showStatus("Malformed URL: " + me.getMessage());
		}

		originalImage = getImage(url);

		MediaTracker mt = new MediaTracker(this);
		mt.addImage(originalImage, 0);
		try {
			mt.waitForID(0);
		} catch (InterruptedException ie) {
		}

		imageWidth = originalImage.getWidth(null);
		imageHeight = originalImage.getHeight(null);

		dbImage = this.createImage(imageWidth, imageHeight);
		dbImageGraphics = dbImage.getGraphics();
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		if (xLocation == imageWidth)
			xLocation = 0;

		dbImageGraphics.clearRect(0, 0, imageWidth, imageHeight);
		dbImageGraphics.drawImage(originalImage, 0, 0, this);
		dbImageGraphics.setColor(Color.red);
		dbImageGraphics.fillOval(xLocation, imageHeight / 2, 20, 20);

		// now dbImage's drawing area appears
		g.drawImage(dbImage, 0, 0, this);

		xLocation++;
		repaint(10);
	}

	public static void main(String[] argv) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DoubleBufferedImage a = new DoubleBufferedImage();

		frame.getContentPane().add(a);
		frame.setSize(300, 300);
		a.init();
		a.start();
		frame.setVisible(true);

	}
}
