package jef.net;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.Test;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import jef.image.ImageType;
import jef.image.JefImage;
import jef.tools.IOUtils;

public class ImageTest {
	@Test
	public void rotatePicture() {
		FileOutputStream out = null;
		try {
			File file = new File(
					"E:/10.19.133.104bgImgUrl.jpg");
			if (!file.exists()) {
				return;
			}
			// 如果是本地图片处理的话，这个地方直接把file放到ImageIO.read(file)中即可，如果是执行上传图片的话，
			// Formfile formfile=获得表单提交的Formfile
			// ,然后 ImageIO.read 方法里参数放
			// formfile.getInputStream()
			long t100 = System.currentTimeMillis(); // 获取开始时间
			InputStream in=IOUtils.getInputStream(file);
			BufferedImage image=null;
			try {
				 image = ImageIO.read(in);
			}finally {
				IOUtils.closeQuietly(in);
			}
			long t101 = System.currentTimeMillis(); // 获取开始时间
			System.out.println("程序运行时间100： " + (t101 - t100) + "ms");

			// 判断图片格式是否正确
			if (image.getWidth(null) == -1) {
				return;
			} else {
				int originalWidth = image.getWidth(null);
				int originalHeight = image.getHeight(null);
				long t102 = System.currentTimeMillis(); // 获取开始时间
				BufferedImage tag = rotate(image, 90);
				long t103 = System.currentTimeMillis(); // 获取开始时间
				System.out.println("程序运行时间102： " + (t103 - t102) + "ms");
				out = new FileOutputStream("c:/111.jpg");
				// JPEGImageEncoder可适用于其他图片类型的转换
				long t104 = System.currentTimeMillis(); // 获取开始时间
				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
				long t105 = System.currentTimeMillis(); // 获取开始时间
				System.out.println("程序运行时间103： " + (t105 - t104) + "ms");
				encoder.encode(tag);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		} finally {
			try {
				if (null != out) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 旋转通过目标图片对象和角度旋转图片返回结果图片
	 * 
	 * @param src
	 *            Image 目标图片
	 * @param angel
	 *            int 旋转角度
	 * @author dongpeichao
	 * @return BufferedImage
	 */
	public BufferedImage rotate(Image src, int angle) {
		while (angle < 0) {
			angle += 360;
		}
		int src_width = src.getWidth(null);
		int src_height = src.getHeight(null);
		// calculate the new image size
		Rectangle rect_des = calcRotatedSize(src_width, src_height, angle);

		BufferedImage res = null;
		res = new BufferedImage(rect_des.width, rect_des.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = res.createGraphics();
		// 填充背景色
		g2.setColor(Color.white);
		g2.fillRect(0, 0, rect_des.width, rect_des.height);

		// 转换画笔开始坐标
		g2.translate((rect_des.width - src_width) / 2,
				(rect_des.height - src_height) / 2);
		g2.rotate(Math.toRadians(angle), src_width / 2, src_height / 2);

		g2.drawImage(src, null, null);
		// 保存输出图片的宽和高
		int outputWidth = rect_des.width;
		int outputHeight = rect_des.height;
		return res;
	}
	
	public Rectangle calcRotatedSize(int src_width, int src_height, int angle) {
		int[] ret = getRotatedSize(src_width, src_height, angle);
		return new java.awt.Rectangle(new Dimension(ret[0], ret[1]));
	}
	
	public static int[] getRotatedSize(double width, double height, int angle) {
		double w = width;
		double h = height;
		while (angle < 0) {
			angle += 360;
		}
		if (angle >= 90) {
			if (angle / 90 % 2 == 1) {
				h = width;
				w = height;
			}
			angle = angle % 90;
		}
		double r = Math.sqrt(h * h + w * w) / 2;
		double len1 = 2 * Math.sin(Math.toRadians(angle) / 2) * r;

		double angel_alpha = (Math.PI - Math.toRadians(angle)) / 2;
		double angel_dalta_width = Math.atan(h / w);
		double angel_dalta_height = Math.atan(w / h);

		int len_dalta_width = (int) (len1 * Math.cos(Math.PI - angel_alpha
				- angel_dalta_width));

		int len_dalta_height = (int) (len1 * Math.cos(Math.PI - angel_alpha
				- angel_dalta_height));

		double des_width = w + len_dalta_width * 2;// 旋转后车位的宽度
		double des_height = h + len_dalta_height * 2;// 旋转后的车位高度
		int[] ret = new int[2];
		ret[0] = (int) des_width;
		ret[1] = (int) des_height;
		return ret;
	}
	
	@Test
	public void test1() throws IOException {
		long time=System.currentTimeMillis();
		JefImage image=new JefImage(IOUtils.getInputStream( new File("E:/10.19.133.104bgImgUrl.jpg")));
		image.rotate(Math.PI/2);
		image.saveAsJpg(new File("e:/111.jpg"), 80);
		System.out.println((System.currentTimeMillis()-time)+"ms");
	}
	
	@Test
	public void create1() throws IOException {
		
		JefImage image=new JefImage(IOUtils.getInputStream( new File("C:\\test.png")));
		
		image.resize(120, 120);
		image.saveAs(new File("c:/11.bmp"), ImageType.PNG);
		
//		createThumbnailImage(new File("C:\\test.png"), "C:\\test.jpg.thumbnail.jpg", 120, 120);
//		System.out.println("222222222222222");
//		createThumbnailImage(new File("C:\\test1.png"), , 120, 120);
	}
	
}
