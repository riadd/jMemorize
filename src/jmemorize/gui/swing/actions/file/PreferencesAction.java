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
package jmemorize.gui.swing.actions.file;

import java.awt.event.KeyEvent;

import jmemorize.core.Main;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.actions.AbstractSessionDisabledAction;
import jmemorize.gui.swing.dialogs.PreferencesDialog;

/**
 * An action that shows the preferences dialog.
 * 
 * @author djemili
 */
public class PreferencesAction extends AbstractSessionDisabledAction
{
    public PreferencesAction()
    {
        setValues();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        Main main = Main.getInstance();
        
        new PreferencesDialog(main.getFrame());
    }
    
    private void setValues()
    {
        setName(Localization.get("MainFrame.PREFERENCES") + ".."); //$NON-NLS-1$ //$NON-NLS-2$ 
        setDescription(Localization.get("MainFrame.PREFERENCES_DESC")); //$NON-NLS-1$ 
        setIcon("/resource/icons/settings.gif"); //$NON-NLS-1$ 
        setAccelerator(KeyEvent.VK_P, SHORTCUT_KEY);
        setMnemonic(1);
    }
}