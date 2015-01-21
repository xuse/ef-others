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
package jef.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import jef.common.Entry;
import jef.common.RGB;
import jef.common.log.LogUtil;
import jef.tools.IOUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;

import com.sun.imageio.plugins.jpeg.JPEGImageWriter;

public class JefImage {
	public static final double ROTATE_45 = Math.PI / 4;
	public static final double ROTATE_90 = Math.PI / 2;
	public static final double ROTATE_135 = Math.PI / 4 * 3;
	public static final double ROTATE_180 = Math.PI;
	public static final double ROTATE_225 = Math.PI / 4 * 5;
	public static final double ROTATE_270 = Math.PI / 2 * 3;
	public static final double ROTATE_315 = Math.PI / 4 * 7;

	public static int DEFAULT_TRANSFORM_METHOD = AffineTransformOp.TYPE_BICUBIC;

	static RenderingHints DEFAULT_RENDER_HINT;
	static {
		Map<RenderingHints.Key, Object> map = new HashMap<RenderingHints.Key, Object>();
		map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		map.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		map.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		DEFAULT_RENDER_HINT = new RenderingHints(map);
	}

	// 红色调red band Matrix
	static final float RED_BAND_MATRIX[][] = { { 1.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 0.0f } };

	// 绿色调矩阵green band Matrix
	static final float GREEN_BAND_MATRIX[][] = { { 0.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, 0.0f, 0.0f } };

	// 蓝色调矩blue band Matrix)
	static final float BLUE_BAND_MATRIX[][] = { { 0.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 1.0f } };

	// 反色矩阵(Matrix that inverts all the bands)
	static final float INVERSE_BAND_MATRIX[][] = { { -1.0f, 0.0f, 0.0f }, { 0.0f, -1.0f, 0.0f }, { 0.0f, 0.0f, -1.0f } };

	// 平均色调矩阵(Matrix that reduces the intensities of all bands)
	static final float AVERAGE_BAND_MATRIX[][] = { { 0.5f, 0.0f, 0.0f }, { 0.0f, 0.5f, 0.0f }, { 0.0f, 0.0f, 0.5f } };

	// 锐化矩阵1
	static float[] SHARP_0 = { 0.0f, -0.75f, 0.0f, -0.75f, 4.0f, -0.75f, 0.0f, -0.75f, 0.0f };
	static float[] SHARPEN_LOW = { 0.0f, -1.0f, 0.0f, -1.0f, 5.f, -1.0f, 0.0f, -1.0f, 0.0f };
	static float[] SHARPEN_HIGH = { -1.0f, -1.0f, -1.0f, -1.0f, 9.0f, -1.0f, -1.0f, -1.0f, -1.0f };

	// 高斯柔化矩阵
	static float GAUSSIAN_BLUR[] = { 0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f, 0.0625f, 0.125f, 0.0625f };

	// 柔化矩阵
	static float BLUR_1[] = { 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f };
	static float BLUR_2[] = { 1.0f / 9.0f, 1.0f / 10.0f, 1.0f / 9.0f, 1.0f / 12.0f, 1.0f / 10.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 10.0f, 1.0f / 9.0f };

	// 边缘检测矩
	static float[] EDGE_DETECT_0 = { 0.0f, -0.75f, 0.0f, -0.75f, 3.0f, -0.75f, 0.0f, -0.75f, 0.0f };
	static float[] EDGE_DETECT_HIGH = { 0.0f, -1.0f, 0.0f, -1.0f, 4.f, -1.0f, 0.0f, -1.0f, 0.0f };
	static float[] EDGE_DETECT_LOW = { 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f };

	// Fields
	private BufferedImage img;
	private BufferedImage source;
	private File localFile;
	private boolean saved;
	private ImageType type;

	/**
	 * 从文件构
	 * 
	 * @param file
	 * @throws IOException
	 */
	public JefImage(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		initStream(in);
		localFile = file;
		saved = true;
		in.close();
	}

	/**
	 * 从数据流构
	 * 
	 * @param in
	 */
	public JefImage(InputStream in) throws IOException {
		initStream(in);
		saved = false;
	}

	/**
	 * 保持比例缩放图片，最大边不超过指定的大小。如果对该边没有要求，则传入0即可
	 * 
	 * @param width
	 * @param height
	 */
	public float resize(int width, int height) {
		float ratio1 = (float) width / img.getWidth();
		float ratio2 = (float) height / img.getHeight();
		float ratio;
		if (ratio1 <= 0 && ratio2 <= 0) {
			return 1;
		} else if (ratio1 <= 0) {
			ratio = ratio2;
		} else if (ratio2 <= 0) {
			ratio = ratio1;
		} else {
			ratio = Math.min(ratio1, ratio2);
		}
		int newh = (int) (img.getHeight() * ratio);
		int neww = (int) (img.getWidth() * ratio);
		this.doTransform(neww, newh, AffineTransform.getScaleInstance(ratio, ratio));
		return ratio;
	}

	/**
	 * 不保持比例缩放图片，即强制缩放到指定的大小
	 * 
	 * @param width
	 * @param height
	 */
	public void resizeWithoutScale(int width, int height) {
		float ratio1 = ((float) width) / img.getWidth();
		float ratio2 = ((float) height) / img.getHeight();
		this.doTransform(width, height, AffineTransform.getScaleInstance(ratio1, ratio2));
	}

	/**
	 * 保存图片
	 * 
	 * @return
	 */
	public File save() {
		if (saved) {
			System.out.print("not save needed.");
			return localFile;
		}
		try {
			if (localFile == null) {
				localFile = File.createTempFile("~result", "." + type.name());
			}
			saveAs(localFile, type);
			return localFile;
		} catch (IOException e) {
			LogUtil.exception(e);
			return null;
		}
	}

	/**
	 * 恢复
	 */
	public void reset() {
		img = source;
	}

	/**
	 * 标记恢复点
	 */
	public void mark() {
		source = img;
	}

	/**
	 * 保存
	 * 
	 * @param file
	 * @param saveType
	 * @return
	 */
	public boolean saveAs(File file, ImageType saveType) {
		try {
			FileImageOutputStream out = new FileImageOutputStream(file);
			String typeName = (saveType == null) ? IOUtils.getExtName(file.getName()) : saveType.name();
			return saveAs(out, typeName, 80);
		} catch (IOException e) {
			LogUtil.exception(e);
			return false;
		}
	}

	/**
	 * Saveas JPG
	 * 
	 * @param file
	 * @param qu
	 */
	public boolean saveAsJpg(File file, int quality) {
		try {
			FileImageOutputStream out = new FileImageOutputStream(file);
			return saveAs(out, ImageType.JPEG.name(), quality);
		} catch (IOException e) {
			LogUtil.exception(e);
			return false;
		}

		/**
		 * 使用JPEGEncoder的实现方法，需要比值 try{ FileOutputStream out = new
		 * FileOutputStream(file); // 输出到文件流 JPEGImageEncoder encoder =
		 * JPEGCodec.createJPEGEncoder(out); JPEGEncodeParam param =
		 * encoder.getDefaultJPEGEncodeParam(img); param.setQuality(quality,
		 * true); encoder.encode(img); // 近JPEG编码 out.close(); return true;
		 * }catch(IOException e){ LogUtil.exception(e); return false; }
		 */
	}

	/**
	 * 转换为灰度图片
	 */
	public void toGray() {
		ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), DEFAULT_RENDER_HINT);
		BufferedImage result = op.filter(img, null);
		img = result;
		saved = false;
	}

	private static Entry<AffineTransform, Point> calcTransform(double radian, int width, int height) {
		while (radian < 0) {
			radian += Math.PI * 2;
		}
		while (radian > Math.PI * 2) {
			radian -= Math.PI * 2;
		}
		AffineTransform transform = AffineTransform.getRotateInstance(radian);
		int newh = height;
		int neww = width;
		if (radian <= Math.PI / 2) {// 0~90度之间时
			int h = height;
			double sin = Math.sin(radian);
			double cos = Math.cos(radian);
			newh = (int) (height * cos + width * sin);
			neww = (int) (height * sin + width * cos);
			transform.translate(h * cos * sin, (h * cos * cos) - h);
		} else if (radian <= Math.PI) {// 90~180度之间时
			radian = radian - ROTATE_90;
			int h = width;
			double sin = Math.sin(radian);
			double cos = Math.cos(radian);
			newh = (int) (height * sin + width * cos);
			neww = (int) (height * cos + width * sin);
			transform.translate((h * cos * cos) - h, -h * cos * sin - height);
		} else if (radian <= ROTATE_270) {// 180~270度之间时
			radian = radian - Math.PI;
			int h = height;
			double sin = Math.sin(radian);
			double cos = Math.cos(radian);
			newh = (int) (height * cos + width * sin);
			neww = (int) (height * sin + width * cos);
			transform.translate(-width - (h * cos * sin), -h * cos * cos);
		} else {// 270~360度之间时
			radian = radian - ROTATE_270;
			int h = width;
			double sin = Math.sin(radian);
			double cos = Math.cos(radian);
			newh = (int) (height * sin + width * cos);
			neww = (int) (height * cos + width * sin);
			transform.translate(-h * cos * cos, h * sin * cos);
		}
		return new Entry<AffineTransform, Point>(transform, new Point(neww, newh));
	}

	/**
	 * 旋转，传入弧度值
	 * 
	 * @param radian
	 */
	public void rotate(double radian) {
		Entry<AffineTransform, Point> result = calcTransform(radian, img.getWidth(), img.getHeight());
		Point newSize = result.getValue();
		doTransform(newSize.x, newSize.y, result.getKey());
	}

	/**
	 * 剪切图片
	 * 
	 * @param startX
	 *            起始坐标x
	 * @param startY
	 *            起始坐标y
	 * @param width
	 *            剪切图宽
	 * @param height
	 *            剪切图高
	 */
	public void crop(int startX, int startY, int width, int height) {
		if (width == 0 || startX + width > img.getWidth()) {
			width = img.getWidth() - startX;
		}
		if (height == 0 || startY + height > img.getHeight()) {
			height = img.getHeight() - startY;
		}
		img = img.getSubimage(startX, startY, width, height);
		saved = false;
	}

	/**
	 * 反色
	 */
	public void colorInvert() {
		this.doRescaleFilter(-1.0f, 256f);
		/**
		 * 反色算法2,耗时为前者的1.5倍，暂时不用 BandCombineOp op = new
		 * BandCombineOp(INVERSE_BAND_MATRIX, null); BufferedImage result =
		 * getCompatible(img.getWidth(),img.getHeight(),img,false);
		 * op.filter(img.getRaster(), result.getRaster()); img = result; saved =
		 * false;
		 */
		/*
		 * 反色算法3 byte negative[] = new byte[256]; for (int i = 0; i < 256; i++)
		 * negative[i] = (byte) (255 - i); ByteLookupTable table = new
		 * ByteLookupTable(0, negative); LookupOp op = new LookupOp(table,
		 * null); filter(op);
		 */
	}

	/**
	 * 红色
	 */
	public void colorRedBand() {
		BandCombineOp op = new BandCombineOp(JefImage.RED_BAND_MATRIX, null);
		BufferedImage result = getCompatible(img.getWidth(), img.getHeight(), img, false);
		op.filter(img.getRaster(), result.getRaster());
		img = result;
		saved = false;
	}

	/**
	 * 绿色
	 */
	public void colorGreenBand() {
		BandCombineOp op = new BandCombineOp(JefImage.GREEN_BAND_MATRIX, null);
		BufferedImage result = getCompatible(img.getWidth(), img.getHeight(), img, false);
		op.filter(img.getRaster(), result.getRaster());
		img = result;
		saved = false;
	}

	/**
	 * 蓝色
	 */
	public void colorBlueBand() {
		BandCombineOp op = new BandCombineOp(JefImage.BLUE_BAND_MATRIX, null);
		BufferedImage result = getCompatible(img.getWidth(), img.getHeight(), img, false);
		op.filter(img.getRaster(), result.getRaster());
		img = result;
		saved = false;
	}

	/**
	 * 平均
	 */
	public void colorAverageBand() {
		BandCombineOp op = new BandCombineOp(JefImage.AVERAGE_BAND_MATRIX, null);
		BufferedImage result = getCompatible(img.getWidth(), img.getHeight(), img, false);
		op.filter(img.getRaster(), result.getRaster());
		img = result;
		saved = false;
	}

	/**
	 * 锐化
	 * 
	 * @param sharpenData
	 */
	public void sharpen() {
		this.doConvolveFilter(SHARPEN_LOW);
	}

	/**
	 * 高斯柔化
	 * 
	 * @return
	 */
	public void gaussianBlur() {
		this.doConvolveFilter(GAUSSIAN_BLUR);
	}

	/**
	 * 柔化
	 */
	public void blur() {
		this.doConvolveFilter(BLUR_1);
	}

	/**
	 * 边缘检测
	 */
	public void edgeDetect() {
		this.doConvolveFilter(EDGE_DETECT_HIGH);
	}

	/**
	 * 调节亮度,传入+-255之间的数值
	 * 
	 * @param level
	 */
	public void adjustBright(int level) {
		if (level > 255)
			level = 255;
		this.doRescaleFilter(1.0f, level);
	}

	/**
	 * 调节对比度，传入+-100之间的数值
	 * 
	 * @param level
	 */
	public void adjustContrast(int level) {
		float scaleFactor = 1.0F + level / 100f; // 对比
		this.doRescaleFilter(scaleFactor, 0);
	}

	/**
	 * 水平翻转
	 */
	public void flipHorizontal() {// 先沿X正半轴移动一个图像位，然后右边翻转，翻回原来的位
		AffineTransform mirrorTransform = AffineTransform.getTranslateInstance(img.getWidth(), 0);
		mirrorTransform.scale(-1.0, 1.0); // flip horizontally
		BufferedImageOp op = new AffineTransformOp(mirrorTransform, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage result = getCompatible(img.getWidth(), img.getHeight(), img, false);
		op.filter(img, result);
		img = result;
		saved = false;
	}

	/**
	 * 垂直翻转
	 */
	public void flipVertical() {
		AffineTransform mirrorTransform = AffineTransform.getTranslateInstance(0, img.getHeight());
		mirrorTransform.scale(1.0, -1.0); // flip horizontally
		BufferedImageOp op = new AffineTransformOp(mirrorTransform, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage result = getCompatible(img.getWidth(), img.getHeight(), img, false);
		op.filter(img, result);
		img = result;
		saved = false;
	}

	/**
	 * 在指定位置叠加图片，可以指定透明度
	 * 
	 * @param img
	 * @param x
	 * @param y
	 * @param trans
	 *            透明度，默认0表示不透明.100表示完全透明
	 */
	public void addImage(Image newimg, int x, int y, int transparency) {
		BufferedImage result = getCompatible(img.getWidth(), img.getHeight(), img, false);
		result.getGraphics().drawImage(img, 0, 0, null);
		Graphics2D g2d = result.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (100 - transparency) / 100f)); // 1.0f为透明，值从0-1.0，依次变得不透明
		g2d.drawImage(newimg, x, y, null);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		g2d.dispose();
		img = result;
		saved = false;
	}

	/**
	 * 在指定位置添加文字，可以指定透明度
	 * 
	 * @param text
	 *            文字
	 * @param format
	 *            文字格式
	 * @param x
	 *            位置
	 * @param y
	 *            位置
	 * @param transparency
	 *            透明0=不透明 100=完全透明
	 */
	public void addText(String text, TextFormat format, int x, int y, int transparency, double theta) {
		if (format == null)
			format = new TextFormat();
		BufferedImage result = getCompatible(img.getWidth(), img.getHeight(), img, false);
		result.getGraphics().drawImage(img, 0, 0, null);
		Graphics2D g2d = result.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (100 - transparency) / 100f)); // 1.0f为透明，值从0-1.0，依次变得不透明
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(format.getFontColor());
		if (format.getFontFile() != null && format.getFontFile().exists()) {
			try {
				Font font = Font.createFont(Font.TRUETYPE_FONT, format.getFontFile());
				g2d.setFont(font.deriveFont((format.isBold() ? Font.BOLD : 0) + (format.isItalic() ? Font.ITALIC : 0), format.getFontSize()));
			} catch (FontFormatException e) {
				LogUtil.exception(e);
			} catch (IOException e) {
				LogUtil.exception(e);
			}
		} else {
			g2d.setFont(new Font(format.getFontName(), (format.isBold() ? Font.BOLD : 0) + (format.isItalic() ? Font.ITALIC : 0), format.getFontSize()));
		}
		g2d.rotate(theta);
		g2d.drawString(text, x, y + format.getFontSize());
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		g2d.dispose();
		img = result;
		saved = false;
	}

	public static TextFormat getTextFormat(String fontName, int fontSize, Color color, boolean bold, boolean italic) {
		TextFormat format = TextFormat.getInstance(fontName, fontSize);
		format.setBold(bold);
		format.setItalic(italic);
		format.setFontColor(color);
		return format;
	}

	/**
	 * 在图片上添加矩形，可以指定透明度
	 * 
	 * @param x
	 * @param y
	 * @param transparency
	 */
	public void addRect(Color c, int x, int y, int width, int height, int transparency) {
		Rectangle rect = new Rectangle();
		rect.setBounds(x, y, width, height);
		addShape(c, rect, transparency);
	}

	/**
	 * 在图片上添加指定形状
	 * 
	 * @param c
	 * @param s
	 * @param transparency
	 */
	public void addShape(Color c, Shape s, int transparency) {
		// BufferedImage
		// result=getCompatible(img.getWidth(),img.getHeight(),img,false);
		BufferedImage result = img;
		result.getGraphics().drawImage(img, 0, 0, null);
		Graphics2D g2d = result.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (100 - transparency) / 100f)); // 1.0f为透明，值从0-1.0，依次变得不透明
		g2d.setColor(c);
		g2d.fill(s);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		g2d.dispose();
		img = result;
		saved = false;
	}

	// public abstract Image merge();
	// public abstract void addLayer();// public abstract addImage();
	// public abstract void addFrame();
	// public abstract int getFrameCount();
	// public abstract int getLayerCount();
	// public abstract AnimeFrame getFrame(int i);
	// public abstract Image getLayer(int i);
	// public abstract void clearAll();

	public static void main(String... str) throws FileNotFoundException, IOException {
		JefImage j = new JefImage(new File("c:/222.jpg"));
		j.rotate(Math.PI / 8 * 7);
		j.saveAsJpg(new File("c:/ddd.jpg"), 80);
	}

	/**
	 * 曲线调节
	 * 
	 * @param table
	 */
	public void curve(byte[] table) {
		BufferedImageOp op = new LookupOp(new ByteLookupTable(0, table), null);
		BufferedImage result = getCompatible(img.getWidth(), img.getHeight(), img, false);
		op.filter(img, result);
		img = result;
		saved = false;
	}

	/**
	 * 卷积调节
	 * 
	 * @param data
	 */
	public void doConvolveFilter(float[] data) {
		Kernel kernel = new Kernel(3, 3, data);
		BufferedImage src = newBufferedImageWithRGB(img);
		src.getGraphics().drawImage(img, 0, 0, null);
		ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, DEFAULT_RENDER_HINT);
		BufferedImage result = convolve.filter(src, null);
		img = result;
		saved = false;
	}

	/**
	 * 亮度对比度调节
	 * 
	 * @param scaleFactor
	 *            对比，1.0f为不变,2.0f最大，0最小
	 * @param offset
	 *            亮度 0为不变， +255最大， -255最小
	 */
	public void doRescaleFilter(float scaleFactor, float offset) {
		RescaleOp rescale = new RescaleOp(scaleFactor, offset, DEFAULT_RENDER_HINT);
		// 使用索引色板的图形不能直接操作,所以先放到另外一个图片中.
		BufferedImage src;
		if (img.getType() == BufferedImage.TYPE_BYTE_BINARY || img.getType() == BufferedImage.TYPE_BYTE_INDEXED || img.getType() == BufferedImage.TYPE_CUSTOM) {
			src = newBufferedImageWithRGB(img);
			Graphics2D g2d = src.createGraphics();
			g2d.drawImage(img, 0, 0, null);
			g2d.dispose();
		} else {
			src = img;
		}
		// 输出结果也必须是在RGB空间的图片上
		BufferedImage result = newBufferedImageWithRGB(img);
		rescale.filter(src, result);
		img = result;
		saved = false;
	}

	/*
	 * 各种变换处理中，部分操作将希望保持原图色板的情况下完成计算，比较典型的有几何变换，如AffineTransformOp的三种插值类型，
	 * 双三次>双线性>相邻插值，越前者质量越好，但是前两者会产生新的颜色，后者不产生新颜色。因此当使用前两种插值方法时，
	 * 一定要使新产生的图像保持和源图一致的色板，否则颜色会失真。
	 */
	private void doTransform(int neww, int newh, AffineTransform transform) {
		BufferedImage result = getCompatible(neww, newh, img, false);
		new AffineTransformOp(transform, DEFAULT_TRANSFORM_METHOD).filter(img, result);
		img = result;
		saved = false;
	}

	/**
	 * 构造新的JefImage对象
	 * 
	 * @param width
	 * @param height
	 * @param type
	 */
	public static JefImage createNew(int width, int height, Color background) {
		if (background == null)
			background = Color.white;
		JefImage jImg = new JefImage();
		jImg.img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		jImg.source = jImg.img;
		jImg.saved = false;
		jImg.type = ImageType.BMP;// 默认为BMP
		Graphics gg = jImg.img.getGraphics();
		gg.setColor(background);
		gg.fillRect(0, 0, width, height);
		return jImg;
	}

	static Color[] colors = new Color[] { Color.red, Color.green, Color.black, Color.blue, new Color(0, 127, 127), new Color(127, 0, 127), new Color(127, 127, 0), new Color(255, 60, 60), Color.darkGray
	// Color.gray
	};

	/**
	 * 创建一个密码图片
	 * 
	 * @return
	 */
	public static Entry<String, JefImage> createPasswordImage() {
		Random r = new Random();
		int height = 25;
		int width = 85;
		int offset = 10;
		int yoffset = 1;
		JefImage img = JefImage.createNew(width, height, Color.gray);
		String passwordStr = RandomStringUtils.randomAlphabetic(5).toUpperCase();

		img.addRect(Color.white, 2, 2, width - 4, height - 4, 0);
		for (char c : passwordStr.toCharArray()) {
			Color cc = colors[r.nextInt(colors.length)];
			// System.out.println(cc);
			TextFormat format = JefImage.getTextFormat("Arial", 14, cc, true, RandomUtils.nextBoolean());
			img.addText(String.valueOf(c), format, offset, yoffset + r.nextInt(8), 0, 0);
			offset += 13;
		}
		Graphics2D g = img.img.createGraphics();
		g.setColor(Color.gray);

		for (int i = 0; i < 3; i++) {
			Point a = new Point(r.nextInt(width), r.nextInt(height));
			Point b = new Point(r.nextInt(width), r.nextInt(height));
			while (distance(a, b) < height / 8) {
				a = b;
				b = new Point(r.nextInt(width), r.nextInt(height));
			}
			g.drawLine(a.x, a.y, b.x, b.y);
		}
		g.dispose();
		return new Entry<String, JefImage>(passwordStr, img);
	}

	// 计算两个点的距离
	public static double distance(Point a, Point b) {
		int x = Math.abs(a.x - b.x);
		int y = Math.abs(a.y - b.y);
		double distance = Math.sqrt((x ^ 2 + y ^ 2));
		// System.out.println(a+"  "+b+"  "+distance);
		return distance;
	}

	private JefImage() {};

	/**
	 * 应用指定的Filter
	 * 
	 * @param filter
	 */
	protected void applyFilter(ImageFilter filter) {
		Image result = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(img.getSource(), filter));
		img = toBufferedImage(result);
		saved = false;
	}

	/**
	 * 创建一个新的兼容格式的图像，可以指定背景是否透明
	 * 
	 * @param neww
	 * @param newh
	 * @param src
	 * @param transparent
	 * @return
	 */
	public static BufferedImage getCompatible(int neww, int newh, BufferedImage src, boolean transparent) {
		Graphics2D g2d = src.createGraphics();
		BufferedImage newimg;
		if (transparent) {
			newimg = g2d.getDeviceConfiguration().createCompatibleImage(neww, newh, java.awt.Transparency.TRANSLUCENT);
		} else {
			newimg = g2d.getDeviceConfiguration().createCompatibleImage(neww, newh);
		}
		g2d.dispose();
		return newimg;
	}

	/**
	 * 将Image对象转换为BufferedImage
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage)
			return (BufferedImage) image;
		BufferedImage buf = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
		buf.getGraphics().drawImage(image, 0, 0, null);
		return buf;
	}

	// 文件保存时，某些格式可能不兼容当前的图片格式，因此转换为标准色图形格式
	private void convertToRgbFormat() {
		if (img.getType() == BufferedImage.TYPE_CUSTOM) {
			BufferedImage img1 = newBufferedImageWithRGB(img);
			img1.getGraphics().drawImage(img, 0, 0, null);
			img = img1;
		}
	}

	// 某些操作，比如颜色替换处理，亮度对比调节，需要均匀分颜色空间，因此源图如果是采用索引色空间的话将无法正确处理，
	// 因此需要让结果图使用初始化的色板
	private static BufferedImage newBufferedImageWithRGB(BufferedImage src) {
		return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
	}

	public boolean saveAs(ImageOutputStream out, String saveType, int quality) {
		try {
			Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix(saveType);
			if (it.hasNext()) {
				ImageWriter iw = it.next();
				iw.setOutput(out);
				if (iw instanceof JPEGImageWriter) {
					convertToRgbFormat();
					ImageWriteParam iwp = iw.getDefaultWriteParam();
					iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					iwp.setCompressionQuality(quality / 100f);
					iw.write(null, new javax.imageio.IIOImage(img, null, null), iwp);
				} else if (iw.getClass().getSimpleName().equals("BMPImageWriter")) {
					convertToRgbFormat();
					iw.write(img);
				} else {
					iw.write(img);
				}
				iw.dispose();
				out.flush();
				out.close();
			}
			return true;
		} catch (IOException e) {
			LogUtil.exception(e);
			return false;
		}
	}

	private void initStream(InputStream in) throws IOException {
		ImageInputStream imgIn = ImageIO.createImageInputStream(in);
		Iterator<ImageReader> readers = ImageIO.getImageReaders(imgIn);
		if (readers.hasNext()) {
			ImageReader reader = readers.next();
			ImageReadParam param = reader.getDefaultReadParam();
			reader.setInput(imgIn, true, true);
			type = ImageType.valueOf(reader.getFormatName().toUpperCase());
			try {
				source = reader.read(0, param);
				img = source;
			} finally {
				reader.dispose();
				imgIn.close();
			}
		} else {
			throw new IOException("Unknow image format.");
		}
	}

	public boolean isSaved() {
		return saved;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}

	public int getWidth() {
		return img.getWidth();
	}

	public int getHeight() {
		return img.getHeight();
	}

	/**
	 * 获得距离灰度线的差距面积（即色彩丰富程度，以点计算）
	 * 
	 * @param scanMode
	 * @return 0～255的数值 (如果返回值=0:灰度图像，返回值15以内，近似灰度图像)
	 */
	public int getColorSpaceSize(int lineSkip) {
		if (lineSkip < 1)
			lineSkip = 1;
		RGB rgb = new RGB(0, 0, 0);
		for (int i = 0; i < img.getWidth(); i += lineSkip) {
			for (int j = 0; j < img.getHeight(); j++) {
				RGB point = RGB.getInstance(img.getRGB(i, j));
				int minVar = Math.min(point.red, Math.min(point.blue, point.green));
				int redless = point.red - minVar;
				int blueless = point.blue - minVar;
				int greenless = point.green - minVar;
				if (redless > rgb.red)
					rgb.red = redless;
				if (blueless - minVar > rgb.blue)
					rgb.blue = blueless;
				if (greenless - minVar > rgb.green)
					rgb.green = greenless;
			}
		}
		return (rgb.red * rgb.blue + rgb.blue * rgb.green + rgb.green * rgb.red) / 765;
	}
}
