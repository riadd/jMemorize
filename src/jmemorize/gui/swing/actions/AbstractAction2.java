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

import java.awt.Toolkit;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * A simple abstract action that adds some simple helper methods to remove a lot
 * of clutter from the various action classes.
 * 
 * @author djemili
 */
public abstract class AbstractAction2 extends AbstractAction
{
    protected int SHORTCUT_KEY = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    protected void setName(String name)
    {
        putValue(NAME, name);
    }
    
    protected void setMnemonic(int i)
    {
        putValue(MNEMONIC_KEY, new Integer(i));
    }
    
    protected void setIcon(String path)
    {
        putValue(SMALL_ICON, new ImageIcon(getClass().getResource(path)));
    }
    
    protected void setDescription(String description)
    {
        putValue(SHORT_DESCRIPTION, description);
    }
    
    /**
     * @see KeyStroke#getKeyStroke(int, int)
     */
    protected void setAccelerator(int keyCode, int modifiers)
    {
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, modifiers));
    }
}
