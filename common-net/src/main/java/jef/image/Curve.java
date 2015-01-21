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

/**
 * 用于生成曲线调节的数组
 * @author admin
 *
 */
public class Curve {
	//使用两个点来表示曲线
//	private int x1;
//	private int y1;
//	private int x2;
//	private int y2;
	
	public static byte[] brightenTable = new byte[256];
	public static byte[] thresholdTable = new byte[256];
	public static byte[] f1 = new byte[256];
	public static byte[] f2 = new byte[256];
	public static byte[] f3 = new byte[256];
	
	static { // Initialize the arrays
		double y=0.333;
		for (int i = 0; i < 256; i++) {
			brightenTable[i] = (byte) (Math.sqrt(i / 256.0) * 256);
			thresholdTable[i] = (byte) ((i < 200) ? 0 : i);//将亮度小于200的点全部调正黑色
			
			f1[i]=(byte)(Math.sin(i*Math.PI/256-Math.PI/2)*128+128);
			f2[i]=(byte)(Math.sin((y*i-64)/64*Math.PI)/Math.sin(y*Math.PI)*128+128);
			f3[i]=(byte)i;
			
		}
	}
}
