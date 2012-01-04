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
package jmemorize.gui.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.JSplitPane;

import jmemorize.gui.swing.frames.MainFrame;

/**
 * @author djemili
 */
public class SplitMainFrameAction extends AbstractAction2
{
    private static final String NOT_SPLIT_ICON = 
        "/resource/icons/application_split.png";
    private static final String SPLIT_ICON = 
        "/resource/icons/application_xp.png";
    
    private MainFrame m_frame;

    public SplitMainFrameAction(MainFrame frame)
    {
        m_frame = frame;
        setIcon(SPLIT_ICON);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(ActionEvent e)
    {
        JSplitPane splitPane = getSplitPane();
        if (splitPane.getDividerLocation() > 0)
        {
            splitPane.setDividerLocation(0);
            
            splitPane.setDividerSize(0);
            splitPane.getTopComponent().setVisible(false);
        }
        else
        {
            splitPane.setDividerLocation(splitPane.getLastDividerLocation());
            
            splitPane.setDividerSize(5);
            splitPane.getTopComponent().setVisible(true);
        }
        
        updateIcon();
    }

    private JSplitPane getSplitPane()
    {
        return m_frame.getVerticalSplitPane();
    }
    
    private void updateIcon()
    {
        boolean isSplit = getSplitPane().getDividerLocation() > 0;
        setIcon(isSplit ? SPLIT_ICON : NOT_SPLIT_ICON);
    }
}
