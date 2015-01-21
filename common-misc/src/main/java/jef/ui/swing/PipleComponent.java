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
package jef.ui.swing;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.JFrame;

import jef.common.RGB;


public class PipleComponent extends JComponent { 
	private static final long serialVersionUID = 1L;

	public void paint(Graphics g) { 
        Graphics2D g2d = (Graphics2D) g; 
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        Shape parentHollowShape=createPiple(g2d,100,100,200,200,RGB.randomColor().toColor(),null); 
        createPiple(g2d,120,120,280,40,RGB.randomColor().toColor(),parentHollowShape); 
        createPiple(g2d,130,170,310,40,RGB.randomColor().toColor(),parentHollowShape); 
        createPiple(g2d,140,220,290,50,RGB.randomColor().toColor(),parentHollowShape); 
        createPiple(g2d,130,190,300,30,RGB.randomColor().toColor(),parentHollowShape); 
    } 

    private Shape createPiple(Graphics2D g2d,int x, int y, int width, int height,Color color,Shape parentHollowShape) { 
        if(parentHollowShape!=null){ 
            Rectangle bounds=parentHollowShape.getBounds(); 
            Rectangle rightClip=new Rectangle(bounds.x+bounds.width/2,bounds.y,3000,bounds.height); 
            Area clip=new Area(parentHollowShape); 
            clip.add(new Area(rightClip)); 
            g2d.setClip(clip); 
        } 
        int circleWidth = height/3; 
        GradientPaint paint = new GradientPaint(x, 
                                                y, 
                                                color.brighter(), 
                                                x, 
                                                y + (int) (height * 0.65), 
                                                color.darker(), 
                                                true); 
        g2d.setPaint(paint); 
        Ellipse2D.Double leftCircle = new Ellipse2D.Double(x - circleWidth / 2, y, circleWidth, height); 
        Ellipse2D.Double rightCircle = new Ellipse2D.Double(x + width - circleWidth / 2, y, circleWidth, height); 
        
        int thickness=4; 
        Ellipse2D.Double rightHollowCircle = new Ellipse2D.Double(rightCircle.getX()+thickness, 
            rightCircle.getY()+thickness, 
            rightCircle.getWidth()-thickness*2, 
            rightCircle.getHeight()-thickness*2); 
        
        Rectangle rect = new Rectangle(x, y, width, height); 
        Area area = new Area(leftCircle); 
        area.add(new Area(rect)); 
        area.subtract(new Area(rightCircle)); 
        g2d.fill(area); 
        g2d.setColor(color.darker()); 
        g2d.fill(rightCircle); 
        
        paint = new GradientPaint(x, 
                                  y, 
                                  Color.darkGray, 
                                  x, 
                                  y + (int) (height * 0.4), 
                                  Color.lightGray, 
                                  true); 

        g2d.setPaint(paint); 
        g2d.fill(rightHollowCircle); 
        
        g2d.setClip(null); 
        
        return rightHollowCircle; 
    } 

    public static void main(String[] args) { 
        JFrame frame = new JFrame(); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.setSize(800, 600); 
        frame.add(new PipleComponent()); 
        frame.setVisible(true); 
    } 
}
