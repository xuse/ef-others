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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;

/**
 * GrabandFadewithRasters.java -- displays provided image and then slowly fades
 * to black
 */
public class GrabandFadewithRasters extends Applet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3783922692809342343L;

	private Image originalImage;

	private Image newImage;

	private MemoryImageSource mis;

	private int width;

	private int height;

	private int index = 10;

	private int[] originalPixelArray;

	private boolean imageLoaded = false;

	private WritableRaster raster;

	private String imageURLString = "file:c:/222.png";

	public void init() {
		URL url;
		try {
			url = new URL(imageURLString);
			originalImage = getImage(url);
		} catch (MalformedURLException me) {
			showStatus("Malformed URL: " + me.getMessage());
		}

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

		DataBufferInt dbi = new DataBufferInt(originalPixelArray, width * height);

		int bandmasks[] = { 0xff000000, 0x00ff0000, 0x0000ff00, 0x000000ff };
		SampleModel sm;
		sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width, height, bandmasks);

		raster = Raster.createWritableRaster(sm, dbi, null);
	}

	public void update(Graphics g) {
		paint(g);
	}
	
	@SuppressWarnings("unused")
	public void paint(Graphics g) {
		int value;
		int sourceRed, sourceGreen, sourceBlue;
		if (newImage != null) {
			g.drawImage(newImage, 0, 0, this);
			if (imageLoaded == false) {
				imageLoaded = true;
				for (int x = 0; x < width; x += 1)
					for (int y = 0; y < height; y += 1) {
						value = originalPixelArray[x * height + y];
						sourceRed = raster.getSample(x, y, 1);
						sourceGreen = raster.getSample(x, y, 2);
						sourceBlue = raster.getSample(x, y, 3);

						if (sourceRed > index) {
							sourceRed -= index;
							imageLoaded = false;
						} else
							sourceRed = 0;

						if (sourceGreen > index) {
							sourceGreen -= index;
							imageLoaded = false;
						} else
							sourceGreen = 0;

						if (sourceBlue > index) {
							sourceBlue -= index;
							imageLoaded = false;
						} else
							sourceBlue = 0;

						raster.setSample(x, y, 1, sourceRed);
						raster.setSample(x, y, 2, sourceGreen);
						raster.setSample(x, y, 3, sourceBlue);
					}
				mis.newPixels();
			}
		}
	}

	public static void main(String[] argv) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GrabandFadewithRasters a = new GrabandFadewithRasters();

		frame.getContentPane().add(a);
		frame.setSize(800, 600);
		a.init();
		a.start();
		frame.setVisible(true);

	}
}
