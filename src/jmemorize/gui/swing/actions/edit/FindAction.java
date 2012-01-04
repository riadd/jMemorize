/*
 * jMemorize - Learning made easy (and fun) - A Leitner flashcards tool
 * Copyright(C) 2004-2006 Riad Djemili
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
package jmemorize.gui.swing.actions.edit;

import java.awt.event.KeyEvent;

import jmemorize.core.Category;
import jmemorize.core.Main;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.actions.AbstractAction2;
import jmemorize.gui.swing.frames.FindFrame;
import jmemorize.gui.swing.frames.MainFrame;

/**
 * An action that shows the search window.
 * 
 * @author djemili
 */
public class FindAction extends AbstractAction2
{
    public FindAction()
    {
        setValues();
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        Main main = Main.getInstance();
        MainFrame frame = main.getFrame();
        
        Category rootCategory = main.getLesson().getRootCategory();
        FindFrame.getInstance().show(rootCategory, frame.getCategory());
    }

    private void setValues()
    {
        setName(Localization.get("MainFrame.FIND")); //$NON-NLS-1$
        setIcon("/resource/icons/find.gif"); //$NON-NLS-1$
        setDescription(Localization.get("MainFrame.FIND_DESC")); //$NON-NLS-1$
        setAccelerator(KeyEvent.VK_F, SHORTCUT_KEY);
    }
}