/*
 * jMemorize - Learning made easy (and fun) - A Leitner flashcards tool
 * Copyright(C) 2004-2008 Riad Djemili and contributors
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package jmemorize.gui.swing.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * @author rd
 *
 */
public class PartialProgressBar extends JProgressBar
{
    private float[] m_values;

    public PartialProgressBar()
    {
        setBorder(null);
    }
    
    public void setValues(float[] values)
    {
        m_values = values;
        
        repaint();
    }
    
    public void setOrientation(int newOrientation)
    {
        if (getOrientation() != HORIZONTAL)
            throw new UnsupportedOperationException();
    }
    
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        Insets insets = getInsets();
        int barHeight = getHeight() - (insets.top + insets.bottom);
        int barWidth = getWidth() - (insets.left + insets.right);
        
        int x = getX() + insets.left;
        int y = getY() + insets.top;
        int w = barWidth / m_values.length;
        int remaining = barWidth - (m_values.length * w);
        
        Color color = getForeground();
        for (int i = 0; i < m_values.length; i++)
        {
            Color c = new Color(color.getRed(), color.getGreen(), 
                color.getBlue(), (int)(m_values[i] * 255));
            g.setColor(c);
            
            // we distribute the remaing pixels among the first columns
            int tw = (remaining--) > 0 ? w + 1 : w;
            g.fillRect(x, y, tw, barHeight);
            
            x += tw;
        }
        
//        if (isStringPainted())
//        {
//            FontMetrics metrics = getFontMetrics(getFont());
//            int strWidth = metrics.stringWidth(getString());
//            int strHeight = metrics.getAscent() - metrics.getDescent() - metrics.getLeading();
//            
//            int fx = getX() + insets.left + barWidth/2 - strWidth/2;
//            int fy = getY() + insets.top + ((barHeight+strHeight)/2);
//            
//            g.setColor(Color.WHITE);
//            g.drawString(getString(), fx, fy);
//        }
    }

    public static void main(String[] args)
    {
        PartialProgressBar bar = new PartialProgressBar();
        bar.setString("Hello World");
        
        Random rand = new Random();
        float[] vals = new float[100];
        for (int i = 0; i < vals.length; i++)
        {
            vals[i] = rand.nextFloat();
        }
        bar.setValues(vals);
        bar.setForeground(Color.BLUE);
        bar.setStringPainted(true);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(bar);
        
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.setSize(500, 100);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
