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
package jmemorize.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * @author djemili
 */
public class Arrow implements Icon
{
    private boolean descending;
    private int     size;

    public Arrow(boolean descending, int size)
    {
        this.descending = descending;
        this.size = size;
    }

    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Color color = c == null ? Color.GRAY : c.getBackground();
        
        int dx = (int)(size / 2);
        int dy = descending ? dx : -dx;
        
        // Align icon (roughly) with font baseline.
        y = y + 5 * size / 6 + (descending ? -dy : 0);
        
        g.translate(x, y);
        g.setColor(Color.GRAY);
        g.fillPolygon(new int[]{dx/2, dx, 0}, new int[]{dy, 0, 0}, 3);
        g.translate(-x, -y);
        g.setColor(color);
    }

    public int getIconWidth()
    {
        return size;
    }

    public int getIconHeight()
    {
        return size;
    }
}