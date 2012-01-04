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
package jmemorize.gui.swing.actions;

import java.awt.event.KeyEvent;

import jmemorize.core.Main;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.frames.MainFrame;

/**
 * Toggles the category tree (showing it when its currently hidden and vice
 * versa).
 * 
 * @author djemili
 */
//TODO rename to ToggleCategoryTreeAction
public class ShowCategoryTreeAction extends AbstractSessionDisabledAction
{
    public ShowCategoryTreeAction()
    {
        setValues();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        MainFrame frame = Main.getInstance().getFrame();
        frame.showCategoryTree(!frame.isShowCategoryTree());
    }
    
    private void setValues()
    {
        setName(Localization.get("MainFrame.CATEGORY_TREE")); //$NON-NLS-1$
        setDescription(Localization.get("MainFrame.CATEGORY_TREE_DESC")); //$NON-NLS-1$
        setIcon("/resource/icons/tree.gif"); //$NON-NLS-1$
        setAccelerator(KeyEvent.VK_T, 0);
    }
}