///*
// * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package jef.image.util;
//
//import java.awt.Color;
//import java.awt.Font;
//import java.awt.Graphics2D;
//import java.awt.Image;
//import java.awt.RenderingHints;
//import java.awt.geom.AffineTransform;
//import java.awt.image.AffineTransformOp;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.List;
//
//import javax.imageio.ImageIO;
//import javax.swing.ImageIcon;
//
//import jef.common.log.LogUtil;
//import jef.image.support.PSDReader;
//import jef.tools.IOUtils;
//
//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;
//
//public class ImgUtils {
//	
//	public static void psdtest(File psdFile) {
//		try {
//			PSDReader r = new PSDReader();
//			r.read(new FileInputStream(psdFile));
//			int n = r.getFrameCount();
//			for (int i = 0; i < n; i++) {
//				BufferedImage image = r.getLayer(i);
//				//Point offset = r.getLayerOffset(i);
//				// point是这个图层在大图上的位置
//				String outFilePath = "c:\\testPsd_" + i + ".jpg";
//				FileOutputStream out = new FileOutputStream(outFilePath);
//				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//				encoder.encode(image);
//			}
//		} catch (Exception e) {
//			LogUtil.exception(e);
//		}
//	}
//
//	public static boolean CreateThumbnail(String filename, String todir) throws Exception {
//		double Ratio = 0.0;
//
//		File F = new File(filename);
//		if (!F.isFile())
//			throw new Exception(F + " is not image file error in CreateThumbnail!");
//
//		String ext = IOUtils.getExtName(F.getName());
//
//		File ThF = new File(todir, IOUtils.removeExt(F.getName()) + "_thumb." + ext);
//		BufferedImage Bi = ImageIO.read(F);
//		// 图片  锟轿?20 120
//		Image Itemp = Bi.getScaledInstance(120, 120, BufferedImage.SCALE_SMOOTH);
//		if ((Bi.getHeight() > 120) || (Bi.getWidth() > 120)) {
//			if (Bi.getHeight() > Bi.getWidth())
//				Ratio = 120.0 / Bi.getHeight();
//			else
//				Ratio = 120.0 / Bi.getWidth();
//		}
//		AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(Ratio, Ratio), null);
//		Itemp = op.filter(Bi, null);
//		try {
//			ImageIO.write((BufferedImage) Itemp, ext, ThF);
//		} catch (Exception ex) {
//			throw new Exception(" ImageIo.write error in CreatThum.: " + ex.getMessage());
//		}
//		return (true);
//	}
//
//	public static void resize(String file_name, String new_filename, int heightPercent, int widthPercent) throws IOException {
//		File imagefile = new File(file_name);
//		Image src_image = javax.imageio.ImageIO.read(imagefile);
//
//		int width = src_image.getWidth(null); // 锟矫碉拷源图 (锟竭筹拷锟铰匡拷锟皆凤拷一鄄呓锟饺?
//		int height = src_image.getHeight(null); // 锟矫碉拷源图
//
//		// 没锟街碉拷碌锟酵计拷母叨群涂
//		width = width * widthPercent / 100;
//		height = height * heightPercent / 100;
//
//		// 锟酵计拷鼗锟酵计?
//		BufferedImage buf_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		buf_image.getGraphics().drawImage(src_image, 0, 0, width, height, null);
//
//		// 锟铰斤拷b锟侥硷拷锟?
//		FileOutputStream out = new FileOutputStream(new_filename);
//		JPEGImageEncoder jpg_encoder = JPEGCodec.createJPEGEncoder(out);
//		jpg_encoder.encode(buf_image);
//		out.close();
//	}
//
//	// imgFile:图片锟侥硷拷, outDir:转锟酵计拷路, type:转锟缴碉拷目锟绞?// : C:/a.bmp C:/images
//	// PNG
//	public static void convert(File imgFile, File outDir, String type) throws Exception {
//		BufferedImage img = ImageIO.read(imgFile);
//		// 锟侥硷拷
//		String srcName = imgFile.getName() + "." + type;
//		// int lastDot = srcName.lastIndexOf('.');
//		// srcName = srcName.substring(0,lastDot);
//		File out = new File(outDir + File.separator + srcName);
//		ImageIO.write(img, type, out);
//	}
//
//	/**
//	 * 峁╋拷jpg图片
//	 * 
//	 * @param s
//	 *            String 
//	 * @param smallWidth
//	 *            int 每锟街的匡拷群透叨锟揭伙拷
//	 * @param bgcolor
//	 *            Color 色
//	 * @param fontcolor
//	 *            Color 色
//	 * @param fontPath
//	 *            String 锟侥硷拷
//	 * @param jpgname
//	 *            String jpg图片
//	 * @return
//	 */
//	public static void createJpgByFont(String s, int smallWidth, Color bgcolor, Color fontcolor, String fontPath, String jpgname) {
//		try { // 锟?锟竭讹拷
//			BufferedImage bimage = new BufferedImage(s.length() * smallWidth, smallWidth, BufferedImage.TYPE_INT_RGB);
//			Graphics2D g = bimage.createGraphics();
//			g.setColor(bgcolor); // 色
//			g.fillRect(0, 0, smallWidth, smallWidth); // 一锟?
//			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 去(锟矫碉拷锟绞憋拷锟?志)
//			g.setColor(fontcolor); // 锟街碉拷色
//			File file = new File(fontPath); // 锟侥硷拷
//			Font font = Font.createFont(Font.TRUETYPE_FONT, file); // 募锟轿伙拷锟?锟铰碉拷锟?锟絡dk1.5锟街э拷锟?1.4只使系统
//			g.setFont(font.deriveFont((float) smallWidth)); // font.deriveFont(float
//			// f)锟狡碉拷前
//			// Font 应拇锟叫?
//			g.drawString(s, 0, smallWidth); // 指锟?
//			g.dispose();
//			FileOutputStream out = new FileOutputStream(jpgname); // 指募锟?
//			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bimage);
//			param.setQuality(50f, true);
//			encoder.encode(bimage, param); // 
//			out.flush();
//			out.close();
//		} catch (Exception e) {
//			System.out.println("createJpgByFont Failed!");
//			LogUtil.exception(e);
//		}
//	}
//
//	/**
//	 * 小jpg图锟较筹拷一锟脚达拷JPG图源锟酵贾伙拷疲锟竭讹拷 锟叫⊥计拷铣锟揭伙拷糯锟絡pg图 (小jpg图片顺平)
//	 * 
//	 * @param smallJPG
//	 *            ArrayList 一小jpg图片
//	 * @param bigWidth
//	 *            int 图锟?
//	 * @param smallWidth
//	 *            int 傻锟叫⊥硷拷目锟饺和高讹拷一锟铰碉拷
//	 * @return
//	 */
//	public static void mergeJPG(List<List<String>> smallJPG, int bigWidth, int smallWidth, Color bgColor, String picName) {
//		try {
//			if (bigWidth < smallWidth) // 图片锟侥高度憋拷小图片锟侥高度伙拷小 直锟接凤拷
//				return;
//			int colCount = bigWidth / smallWidth; // 每锟叫凤拷锟矫碉拷
//			int leftMargin = (int) ((bigWidth - colCount * smallWidth) / 2f); // 呔锟?
//			int rowCount = smallJPG.size(); // 小图
//			int setWidth = bigWidth; // 每锟叫间不锟较讹拷锟街伙拷冶呔锟?
//			int setHeight = smallWidth * rowCount;
//			// 锟秸达拷图片呋锟揭伙拷图片
//			BufferedImage bufImage = new BufferedImage(setWidth, setHeight, BufferedImage.TYPE_INT_RGB);
//			Graphics2D g = bufImage.createGraphics();
//			g.setColor(bgColor); // 色
//			g.fillRect(0, 0, setWidth, setHeight);
//			int y = 0; // 锟?
//			for (int i = 0; i < rowCount; i++) { // 每
//				List<String> col =  smallJPG.get(i);
//				int x = leftMargin; // 锟?锟杰伙拷锟竭撅拷
//				for (int j = 0; j < col.size(); j++) {
//					String jpgname = col.get(j);
//					ImageIcon icon = new ImageIcon(jpgname);
//					Image img = icon.getImage();
//					int imgWidth = img.getHeight(null);
//					g.drawImage(img, x, y, null);
//					x += imgWidth;
//				}
//				y += (smallWidth);
//			}
//			g.dispose();
//			FileOutputStream out = new FileOutputStream(picName); // 指募锟?
//			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); // 锟侥硷拷式
//			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufImage); // 图片锟叫讹拷取
//			param.setQuality(50f, true);
//			encoder.encode(bufImage, param); // 
//			out.flush();
//			out.close();
//		} catch (Exception e) {
//			System.out.println("createBigJPG Failed!");
//			LogUtil.exception(e);
//		}
//	}
//}
