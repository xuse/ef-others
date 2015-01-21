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
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//import javax.imageio.ImageIO;
//
//import jef.image.support.AnimatedGifEncoder;
//import jef.image.support.GifDecoder;
//
//import com.sun.image.codec.jpeg.ImageFormatException;
//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;
//
//public abstract class GifUtils {
//	/**
//	 * @param args
//	 * @throws IOException 
//	 * @throws ImageFormatException 
//	 */
//	public static void gif2Jpg(String filepath) throws ImageFormatException, IOException {
//		GifDecoder decoder = new GifDecoder();
//		decoder.read(filepath);
//		int n = decoder.getFrameCount(); // 得到frame的个数
//		for (int i = 0; i < n; i++) {
//			BufferedImage frame = decoder.getFrame(i); // 得到帧
//			//int delay = decoder.getDelay(i); //得到延迟时间
//
//			String outName = filepath + String.valueOf(i) + ".jpg";
//			saveAsJpg(frame,outName);//将每帧另存为jpg文件
//		}
//	}
//	
//	
//	//将指定Image另存为JPG
//	public static void saveAsJpg(BufferedImage frame,String outName) throws ImageFormatException, IOException {
//		int width = frame.getWidth();
//		int height = frame.getHeight();
//		BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		tag.getGraphics().drawImage(frame, 0, 0, width, height, null);
//		FileOutputStream out = new FileOutputStream(outName);
//		
//		//TODO 尝试一下用JPEGImageEncoder输入和用ImageIO输出有什么不同
//		// ImageIO.write(frame, "jpeg", out); //为啥不使用ImageIO而是JPEGImageEncoder?
//		
//		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//		
//		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(tag);
//		param.setQuality(75f, true);
//		encoder.encode(tag,param); // 存盘
//		
//		
//		out.flush();
//		out.close();
//	}
//	
//	/**
//	 * 把多张jpg图片合成一张动画GIF
//	 * @param pic
//	 *            String[] 多个jpg文件名 包含路径
//	 * @param newPic
//	 *            String 生成的gif文件名 包含路径
//	 * @throws IOException 
//	 */
//	public static void jpgToAnimeGif(String pic[], String newPic) throws IOException {
//		AnimatedGifEncoder e = new AnimatedGifEncoder(); 
//		e.setRepeat(0);
//		e.start(newPic);
//		BufferedImage src[] = new BufferedImage[pic.length];
//		for (int i = 0; i < src.length; i++) {
//			e.setDelay(200); // 设置播放的延迟时间
//			src[i] = ImageIO.read(new File(pic[i])); // 读入需要播放的jpg文件
//			e.addFrame(src[i]); // 添加到帧中
//		}
//		e.finish();
//	}
//}
