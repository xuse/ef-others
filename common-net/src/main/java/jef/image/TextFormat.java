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

import java.awt.Color;
import java.io.File;



public class TextFormat{
	private String fontName="Arial";
	private Color fontColor=Color.BLACK;
	private int fontSize=12;
	private boolean bold=false;
	private boolean italic=false;
	private File fontFile;
	
	public static final TextFormat getInstance(String fontName,int size){
		TextFormat f=new TextFormat();
		f.fontName=fontName;
		f.fontSize=size;
		return f;
	}
	public static final TextFormat getInstance(String fontName,int size,Color color){
		TextFormat f=new TextFormat();
		f.fontName=fontName;
		f.fontSize=size;
		f.fontColor=color;
		return f;
	}
	
	public final File getFontFile() {
		return fontFile;
	}
	public final void setFontFile(File fontFile) {
		this.fontFile = fontFile;
	}
	public final boolean isBold() {
		return bold;
	}
	public final void setBold(boolean bold) {
		this.bold = bold;
	}
	public final Color getFontColor() {
		return fontColor;
	}
	public final void setFontColor(Color fontColor) {
		this.fontColor = fontColor;
	}
	public final String getFontName() {
		return fontName;
	}
	public final void setFontName(String fontName) {
		this.fontName = fontName;
	}
	public final int getFontSize() {
		return fontSize;
	}
	public final void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public final boolean isItalic() {
		return italic;
	}
	public final void setItalic(boolean italic) {
		this.italic = italic;
	}
}
