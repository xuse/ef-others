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
import java.awt.image.RGBImageFilter;

/**
 * 
 * 颜色平衡调节器
 * ColorComponentScaler -- filters an image by multiplier its red, green and
 * blue color components by their given scale factors
 */
public class ColorComponentScaler extends RGBImageFilter {
	private double redMultiplier, greenMultiplier, blueMultiplier;

	private int newRed, newGreen, newBlue;

	private Color color, newColor;

	/**
	 * rm = red multiplier gm = green multiplier bm = blue multiplier
	 */
	public ColorComponentScaler(double rm, double gm, double bm) {
		canFilterIndexColorModel = true;
		redMultiplier = rm;
		greenMultiplier = gm;
		blueMultiplier = bm;
	}

	private int multColor(int colorComponent, double multiplier) {
		colorComponent = (int) (colorComponent * multiplier);
		if (colorComponent < 0)
			colorComponent = 0;
		else if (colorComponent > 255)
			colorComponent = 255;

		return colorComponent;
	}

	/**
	 * split the argb value into its color components, multiply each color
	 * component by its corresponding scaler factor and pack the components back
	 * into a single pixel
	 */
	public int filterRGB(int x, int y, int argb) {
		color = new Color(argb);
		newBlue = multColor(color.getBlue(), blueMultiplier);
		newGreen = multColor(color.getGreen(), greenMultiplier);
		newRed = multColor(color.getRed(), redMultiplier);
		newColor = new Color(newRed, newGreen, newBlue);
		return (newColor.getRGB());
	}
}
