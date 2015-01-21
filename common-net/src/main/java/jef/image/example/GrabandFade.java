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

import java.applet.Applet;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;

public class GrabandFade extends Applet {
	/**
	 * 渐变为黑色
	 */
	private static final long serialVersionUID = 4067019629401886764L;

	private Image originalImage;

	private Image newImage;

	private MemoryImageSource mis;

	private int width;

	private int height;

	private int index = 10;

	private int[] originalPixelArray;

	private boolean imageLoaded = false;

	private String imageURLString = "file:c:/222.png";

	public void init() {
		URL url;
		try {
			// set imageURLString here
			url = new URL(imageURLString);
			originalImage = getImage(url);
		} catch (MalformedURLException me) {
			showStatus("Malformed URL: " + me.getMessage());
		}

		/*
		 * Create PixelGrabber and use it to fill originalPixelArray with image
		 * pixel data. This array will then by used by the MemoryImageSource.
		 */
		try {
			PixelGrabber grabber = new PixelGrabber(originalImage, 0, 0, -1, -1, true);
			if (grabber.grabPixels()) {
				width = grabber.getWidth();
				height = grabber.getHeight();
				originalPixelArray = (int[]) grabber.getPixels();

				mis = new MemoryImageSource(width, height, originalPixelArray, 0, width);
				mis.setAnimated(true);
				newImage = createImage(mis);
			} else {
				System.err.println("Grabbing Failed");
			}
		} catch (InterruptedException ie) {
			System.err.println("Pixel Grabbing Interrupted");
		}
	}

	/**
	 * overwrite update method to avoid clearing of drawing area
	 */
	public void update(Graphics g) {
		paint(g);
	}

	/**
	 * continually draw image, then decrease color components of all pixels
	 * contained in the originalPixelArray array until color components are all
	 * 0
	 */
	public void paint(Graphics g) {
		int value;
		int alpha, sourceRed, sourceGreen, sourceBlue;
		if (newImage != null) {
			g.drawImage(newImage, 0, 0, this); // redraw image

			// if image isn't faded to black, continue
			if (imageLoaded == false) {
				imageLoaded = true;
				for (int x = 0; x < width; x += 1)
					for (int y = 0; y < height; y += 1) {

						// find the color components
						value = originalPixelArray[x * height + y];
						alpha = (value >> 24) & 0x000000ff;
						sourceRed = (value >> 16) & 0x000000ff;
						sourceGreen = (value >> 8) & 0x000000ff;
						sourceBlue = value & 0x000000ff;

						// subtract index from each red component
						if (sourceRed > index) {
							sourceRed -= index;
							imageLoaded = false;
						} else
							sourceRed = 0;

						// subtract index from each green component
						if (sourceGreen > index) {
							sourceGreen -= index;
							imageLoaded = false;
						} else
							sourceGreen = 0;

						// subtract index from each blue component
						if (sourceBlue > index) {
							sourceBlue -= index;
							imageLoaded = false;
						} else
							sourceBlue = 0;

						/*
						 * when we pack new color components into integer we
						 * make sure the alpha (transparency) value represents
						 * opaque
						 */
						value = (alpha << 24);
						value += (sourceRed << 16);
						value += (sourceGreen << 8);
						value += sourceBlue;

						// fill pixel array
						originalPixelArray[x * height + y] = value;
					}
				mis.newPixels(); // send pixels to ImageConsumer
			}
		}
	}

	public static void main(String[] argv) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GrabandFade a = new GrabandFade();

		frame.getContentPane().add(a);
		frame.setSize(300, 300);
		a.init();
		a.start();
		frame.setVisible(true);

	}
}
