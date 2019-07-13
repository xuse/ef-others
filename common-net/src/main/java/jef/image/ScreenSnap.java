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

/* 
 作者:java编写的截图工具
 */
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jef.tools.Exceptions;
@SuppressWarnings("unused")
public class ScreenSnap extends Frame implements MouseListener, MouseMotionListener, ActionListener {

	private static final long serialVersionUID = 8457465761228912094L;

	private int firstX, firstY, frameWidth, frameHeight;

	private int firstWith, firstHeight, firstPointx, firstPointy;

	private BufferedImage bi, sbi, original;

	private Robot robot;

	private Rectangle rectangle;

	private Rectangle rectangleCursor, rectangleCursorUp, rectangleCursorDown, rectangleCursorLeft, rectangleCursorRight;

	private Rectangle rectangleCursorRU, rectangleCursorRD, rectangleCursorLU, rectangleCursorLD;

	private Image bis;

	private Dimension dimension;

	private Button button, button2, clearButton;

	private Point[] point = new Point[3];

	private int width, height;

	private int nPoints = 5;

	private Panel panel;

	private boolean drawHasFinish = false, change = false;

	private int changeFirstPointX, changeFirstPointY, changeWidth, changeHeight;

	private boolean changeUP = false, changeDOWN = false, changeLEFT = false, changeRIGHT = false, changeRU = false, changeRD = false, changeLU = false, changeLD = false;

	private boolean clearPicture = false, redraw = false;

	private FileDialog fileDialog;

	private ScreenSnap() {
		// 取得屏幕大小
		dimension = Toolkit.getDefaultToolkit().getScreenSize();
		frameWidth = dimension.width;
		frameHeight = dimension.height;

		fileDialog = new FileDialog(this, "Screen snapper", FileDialog.SAVE);
		rectangle = new Rectangle(frameWidth, frameHeight);
		panel = new Panel();
		button = new Button("Exit");
		button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		button.setBackground(Color.green);
		button2 = new Button("Snap");
		button2.setBackground(Color.darkGray);
		button2.addActionListener(new MyTakePicture(this));
		button2.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		button.addActionListener(this);
		clearButton = new Button("Redraw");
		clearButton.setBackground(Color.green);
		clearButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		clearButton.addActionListener(new MyClearPicture(this));
		panel.setLayout(new BorderLayout());
		panel.add(clearButton, BorderLayout.SOUTH);

		panel.add(button, BorderLayout.NORTH);
		panel.add(button2, BorderLayout.CENTER);
		try {
			robot = new Robot();
		} catch (AWTException e) {
			Exceptions.log(e);
		}

		// 截取全屏
		bi = robot.createScreenCapture(rectangle);
		original = bi;
		this.setSize(frameWidth, frameHeight);
		this.setUndecorated(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.add(panel, BorderLayout.EAST);
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		this.setVisible(true);
		this.repaint();
	}

	public static void main(String[] args) {
		new ScreenSnap();
	}

	public void paint(Graphics g) {

		this.drawR(g);

	}

	// 缓存图片
	public void update(Graphics g) {
		if (bis == null) {
			bis = this.createImage(frameWidth, frameHeight);
		}
		Graphics ga = bis.getGraphics();
		Color c = ga.getColor();
		ga.setColor(Color.black);
		ga.fillRect(0, 0, frameWidth, frameHeight);
		ga.setColor(c);
		paint(ga);
		g.drawImage(bis, 0, 0, frameWidth, frameHeight, null);
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e) {
		if (!drawHasFinish) {
			if (point[1].x < point[2].x && point[1].y < point[2].y) {
				firstPointx = point[1].x;
				firstPointy = point[1].y;
			}
			if (point[1].x > point[2].x && point[1].y < point[2].y) {
				firstPointx = point[2].x;
				firstPointy = point[1].y;
			}
			if (point[1].x < point[2].x && point[1].y > point[2].y) {
				firstPointx = point[1].x;
				firstPointy = point[2].y;
			}
			if (point[1].x > point[2].x && point[1].y > point[2].y) {
				firstPointx = point[2].x;
				firstPointy = point[2].y;
			}
			changeFirstPointX = firstPointx;
			changeFirstPointY = firstPointy;
			if (point[1] != null && point[2] != null) {
				rectangleCursorUp = new Rectangle(firstPointx + 20, firstPointy - 10, width - 40, 20);
				rectangleCursorDown = new Rectangle(firstPointx + 20, firstPointy + height - 10, width - 40, 20);
				rectangleCursorLeft = new Rectangle(firstPointx - 10, firstPointy + 10, 20, height - 20);
				rectangleCursorRight = new Rectangle(firstPointx + width - 10, firstPointy + 10, 20, height - 20);
				rectangleCursorLU = new Rectangle(firstPointx - 10, firstPointy - 10, 30, 20);
				rectangleCursorLD = new Rectangle(firstPointx - 10, firstPointy + height - 10, 30, 20);
				rectangleCursorRU = new Rectangle(firstPointx + width - 10, firstPointy - 10, 20, 20);
				rectangleCursorRD = new Rectangle(firstPointx + width - 10, firstPointy + height - 10, 20, 20);
				drawHasFinish = true;
			}

		}
		// 确定每边能改变大小的矩形
		if (drawHasFinish) {
			rectangleCursorUp = new Rectangle(changeFirstPointX + 20, changeFirstPointY - 10, changeWidth - 40, 20);
			rectangleCursorDown = new Rectangle(changeFirstPointX + 20, changeFirstPointY + changeHeight - 10, changeWidth - 40, 20);
			rectangleCursorLeft = new Rectangle(changeFirstPointX - 10, changeFirstPointY + 10, 20, changeHeight - 20);
			rectangleCursorRight = new Rectangle(changeFirstPointX + changeWidth - 10, changeFirstPointY + 10, 20, changeHeight - 20);
			rectangleCursorLU = new Rectangle(changeFirstPointX - 2, changeFirstPointY - 2, 10, 10);
			rectangleCursorLD = new Rectangle(changeFirstPointX - 2, changeFirstPointY + changeHeight - 2, 10, 10);
			rectangleCursorRU = new Rectangle(changeFirstPointX + changeWidth - 2, changeFirstPointY - 2, 10, 10);
			rectangleCursorRD = new Rectangle(changeFirstPointX + changeWidth - 2, changeFirstPointY + changeHeight - 2, 10, 10);
		}

	}

	public void mouseDragged(MouseEvent e) {
		point[2] = e.getPoint();
		// if(!drawHasFinish){
		this.repaint();
		// }

		// 托动鼠标移动大小
		if (change) {
			if (changeUP) {
				changeHeight = changeHeight + changeFirstPointY - e.getPoint().y;
				changeFirstPointY = e.getPoint().y;

			}
			if (changeDOWN) {
				changeHeight = e.getPoint().y - changeFirstPointY;
			}
			if (changeLEFT) {
				changeWidth = changeWidth + changeFirstPointX - e.getPoint().x;
				changeFirstPointX = e.getPoint().x;
			}
			if (changeRIGHT) {
				changeWidth = e.getPoint().x - changeFirstPointX;
			}
			if (changeLU) {
				changeWidth = changeWidth + changeFirstPointX - e.getPoint().x;
				changeHeight = changeHeight + changeFirstPointY - e.getPoint().y;
				changeFirstPointX = e.getPoint().x;
				changeFirstPointY = e.getPoint().y;
			}
			if (changeLD) {
				changeWidth = changeWidth + changeFirstPointX - e.getPoint().x;
				changeHeight = e.getPoint().y - changeFirstPointY;
				changeFirstPointX = e.getPoint().x;

			}
			if (changeRU) {
				changeWidth = e.getPoint().x - changeFirstPointX;
				changeHeight = changeHeight + changeFirstPointY - e.getPoint().y;
				changeFirstPointY = e.getPoint().y;
			}
			if (changeRD) {
				changeWidth = e.getPoint().x - changeFirstPointX;
				changeHeight = e.getPoint().y - changeFirstPointY;

			}
			this.repaint();
		}

	}

	public void mouseMoved(MouseEvent e) {
		point[1] = e.getPoint();
		// 改变鼠标的形状
		if (rectangleCursorUp != null && rectangleCursorUp.contains(point[1])) {

			this.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
			change = true;
			changeUP = true;
		} else if (rectangleCursorDown != null && rectangleCursorDown.contains(point[1])) {
			this.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
			change = true;
			changeDOWN = true;
		} else if (rectangleCursorLeft != null && rectangleCursorLeft.contains(point[1])) {
			this.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
			change = true;
			changeLEFT = true;
		} else if (rectangleCursorRight != null && rectangleCursorRight.contains(point[1])) {
			this.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
			change = true;
			changeRIGHT = true;
		} else if (rectangleCursorLU != null && rectangleCursorLU.contains(point[1])) {
			this.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
			change = true;
			changeLU = true;
		} else if (rectangleCursorLD != null && rectangleCursorLD.contains(point[1])) {
			this.setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
			change = true;
			changeLD = true;
		} else if (rectangleCursorRU != null && rectangleCursorRU.contains(point[1])) {
			this.setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
			change = true;
			changeRU = true;
		} else if (rectangleCursorRD != null && rectangleCursorRD.contains(point[1])) {
			this.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
			change = true;
			changeRD = true;
		} else {
			this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			changeUP = false;
			changeDOWN = false;
			changeRIGHT = false;
			changeLEFT = false;
			changeRU = false;
			changeRD = false;
			changeLU = false;
			changeLD = false;
		}
		redraw = false;
	}

	public void actionPerformed(ActionEvent e) {
		System.exit(0);

	}

	class MyTakePicture implements ActionListener {
		ScreenSnap aWTpicture;

		MyTakePicture(ScreenSnap aWTpicture) {
			this.aWTpicture = aWTpicture;
		}

		// 保存图片
		public void actionPerformed(ActionEvent e) {
			fileDialog.setVisible(true);
			if (changeWidth > 0) {
				sbi = bi.getSubimage(changeFirstPointX, changeFirstPointY, changeWidth, changeHeight);

				File file = new File(fileDialog.getDirectory());
				file.mkdir();

				try {
					ImageIO.write(sbi, "jpeg", new File(file, fileDialog.getFile() + ".jpg"));
				} catch (IOException e1) {
					Exceptions.log(e1);
				}
			}

		}

	}

	class MyClearPicture implements ActionListener {
		ScreenSnap aWTpicture;

		MyClearPicture(ScreenSnap aWTpicture) {
			this.aWTpicture = aWTpicture;
		}

		public void actionPerformed(ActionEvent e) {
			drawHasFinish = false;
			change = false;
			redraw = true;
			rectangleCursorUp = null;
			rectangleCursorDown = null;
			rectangleCursorLeft = null;
			rectangleCursorRight = null;
			rectangleCursorRU = null;
			rectangleCursorRD = null;
			rectangleCursorLU = null;
			rectangleCursorLD = null;
			changeWidth = 0;
			changeHeight = 0;

			aWTpicture.repaint();

		}

	}

	public void drawR(Graphics g) {
		g.drawImage(bi, 0, 0, frameWidth, frameHeight, null);

		if (point[1] != null && point[2] != null && !drawHasFinish && !redraw) {
			int[] xPoints = { point[1].x, point[2].x, point[2].x, point[1].x, point[1].x };
			int[] yPoints = { point[1].y, point[1].y, point[2].y, point[2].y, point[1].y };
			width = (point[2].x - point[1].x) > 0 ? (point[2].x - point[1].x) : (point[1].x - point[2].x);
			height = (point[2].y - point[1].y) > 0 ? (point[2].y - point[1].y) : (point[1].y - point[2].y);
			changeWidth = width;
			changeHeight = height;
			Color c = g.getColor();
			g.setColor(Color.red);
			g.drawString(width + "*" + height, point[1].x, point[1].y - 5);
			// 画点
			/*
			 * int i; if()
			 */
			if (point[1].x < point[2].x && point[1].y < point[2].y) {
				firstPointx = point[1].x;
				firstPointy = point[1].y;
			}
			if (point[1].x > point[2].x && point[1].y < point[2].y) {
				firstPointx = point[2].x;
				firstPointy = point[1].y;
			}
			if (point[1].x < point[2].x && point[1].y > point[2].y) {
				firstPointx = point[1].x;
				firstPointy = point[2].y;
			}
			if (point[1].x > point[2].x && point[1].y > point[2].y) {
				firstPointx = point[2].x;
				firstPointy = point[2].y;
			}

			g.fillRect(firstPointx - 2, firstPointy - 2, 5, 5);
			g.fillRect(firstPointx + (width) / 2, firstPointy - 2, 5, 5);
			g.fillRect(firstPointx + width - 2, firstPointy - 2, 5, 5);
			g.fillRect(firstPointx + width - 2, firstPointy + height / 2 - 2, 5, 5);
			g.fillRect(firstPointx + width - 2, firstPointy + height - 2, 5, 5);
			g.fillRect(firstPointx + (width) / 2, firstPointy + height - 2, 5, 5);
			g.fillRect(firstPointx - 2, firstPointy + height - 2, 5, 5);
			g.fillRect(firstPointx - 2, firstPointy + height / 2 - 2, 5, 5);
			// 画矩形
			// g.drawString("fafda", point[1].x-100, point[1].y-5);
			g.drawPolyline(xPoints, yPoints, nPoints);

		}

		if (change) {
			g.setColor(Color.red);
			g.drawString(changeWidth + "*" + changeHeight, changeFirstPointX, changeFirstPointY - 5);

			g.fillRect(changeFirstPointX - 2, changeFirstPointY - 2, 5, 5);
			g.fillRect(changeFirstPointX + (changeWidth) / 2, changeFirstPointY - 2, 5, 5);
			g.fillRect(changeFirstPointX + changeWidth - 2, changeFirstPointY - 2, 5, 5);
			g.fillRect(changeFirstPointX + changeWidth - 2, changeFirstPointY + changeHeight / 2 - 2, 5, 5);
			g.fillRect(changeFirstPointX + changeWidth - 2, changeFirstPointY + changeHeight - 2, 5, 5);
			g.fillRect(changeFirstPointX + (changeWidth) / 2, changeFirstPointY + changeHeight - 2, 5, 5);
			g.fillRect(changeFirstPointX - 2, changeFirstPointY + changeHeight - 2, 5, 5);
			g.fillRect(changeFirstPointX - 2, changeFirstPointY + changeHeight / 2 - 2, 5, 5);

			g.drawRect(changeFirstPointX, changeFirstPointY, changeWidth, changeHeight);
		}
	}

}
