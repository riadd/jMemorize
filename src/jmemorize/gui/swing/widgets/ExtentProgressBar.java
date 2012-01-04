/**
 *  @author bret5
 */
package jmemorize.gui.swing.widgets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 * @author bret5 Copyright(C) 2007 bret5
 * 
 * This class extends JProgressBar in order to show two values for progress,
 * which are presumably related so that they can be shown on a stacked bar.
 * 
 * The underlying data object, the BoundedRangeModel, already has a data member,
 * extent, representing the length of an inner range, so all we have to do is
 * expose methods for adjusting the value of extent and implement a paint method
 * which paints the inner range in a separate color.
 * 
 * The drawback to this painting implementation is that if isStringPainted, then
 * the rectangle representing the extent paints over the text. - Workaround - if
 * you want the the text to be visible through the extent rectangle, set the
 * extentForeground to a partially transparent color. See the included main for
 * an example.
 * 
 * TODO - fixing this requires fully implementing paint (background, both
 * rectangles, and the text in three colors).
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 1, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */
public class ExtentProgressBar extends JProgressBar
{

    private Color extentForeground;

    public void setExtent(int n)
    {
        getModel().setExtent(n);
        repaint();
    }

    public int getExtent()
    {
        return getModel().getExtent();
    }

    /**
     * Set the color used to paint the bar representing the extent value
     * 
     * @param fg
     */
    public void setExtentForeground(Color fg)
    {
        extentForeground = fg;
    }

    /**
     * Get the color used to paint the extent value
     * 
     * @return the color used to paint the extent value
     */
    public Color getExtentForeground()
    {
        return extentForeground;
    }

    protected void paintComponent(Graphics g)
    {
        // first use the superclass to paint the bar
        super.paintComponent(g);
        Insets insets = getInsets();
        // get the old color
        Color oldColor = g.getColor();
        // set the color
        g.setColor(extentForeground);
        // figure the bar
        double value = getModel().getValue();
        double extent = getModel().getExtent();
        double maximum = getModel().getMaximum();
        double minimum = getModel().getMinimum();
        double outerRange = maximum - minimum;
        int x = getX() + insets.left;
        int y, w, h = 0;
        int barHeight = getHeight() - (insets.top + insets.bottom);
        int barWidth = getWidth() - (insets.left + insets.right);
        if (getOrientation() == HORIZONTAL)
        {
            y = getY() + insets.top;
            x += (int)(barWidth * (value / outerRange));
            w = (int)(barWidth * (extent / outerRange));
            h = barHeight;
        }
        else
        {
            // Vertical bars start at the bottom and go up
            y = getY() + barHeight;
            y -= (int)(barHeight * (value / outerRange));
            h = (int)(barHeight * (extent / outerRange));
            y -= h;
            w = barWidth;
        }
        g.fillRect(x, y, w, h);
        // reset the color
        g.setColor(oldColor);
    }

    private static void createAndShowGUI()
    {
        // Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        // Create and set up the window.
        JFrame frame = new JFrame("Demo ExtentProgressBar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add the ubiquitous "Hello World" label.
        final ExtentProgressBar bar = new ExtentProgressBar();
        bar.setMinimum(0);
        bar.setMaximum(10);
        bar.setValue(3);
        bar.setExtent(4);

        Color grn = Color.GREEN.darker();
        Color transparentGreen = new Color(grn.getRed(), grn.getGreen(), grn.getBlue(), 128);
        bar.setForeground(Color.BLUE);
        bar.setExtentForeground(transparentGreen);

        bar.setString("Hello World");
        bar.setStringPainted(true);

        frame.getContentPane().add(bar);
        frame.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent arg0)
            {
                switch (arg0.getKeyCode())
                {
                case KeyEvent.VK_LEFT:
                    bar.setValue(bar.getValue() - 1);
                    break;
                case KeyEvent.VK_RIGHT:
                    bar.setValue(bar.getValue() + 1);
                    break;
                case KeyEvent.VK_UP:
                    bar.setExtent(bar.getExtent() + 1);
                    break;
                case KeyEvent.VK_DOWN:
                    bar.setExtent(bar.getExtent() - 1);
                    break;
                case KeyEvent.VK_1:
                    bar.setOrientation(JProgressBar.HORIZONTAL);
                    break;
                case KeyEvent.VK_2:
                    bar.setOrientation(JProgressBar.VERTICAL);
                    break;
                }
            }
        });

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args)
    {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                createAndShowGUI();
            }
        });
    }

}
