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
package jmemorize.gui.swing.actions.edit;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import jmemorize.core.Main;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.GeneralTransferHandler;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.SelectionProvider.SelectionObserver;
import jmemorize.gui.swing.actions.AbstractAction2;
import jmemorize.gui.swing.frames.MainFrame;

/** 
 * An action that invokes the generic Swing PASTE action on the current focus
 * owner.
 * 
 * @author djemili
 */
public class PasteAction extends AbstractAction2 implements SelectionObserver
{
    SelectionProvider m_selectionProvider;
    
    public PasteAction(SelectionProvider selectionProvider)
    {
        m_selectionProvider = selectionProvider;
        
        setValues();
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        GeneralTransferHandler.getPasteAction().actionPerformed(new ActionEvent(
            m_selectionProvider.getDefaultFocusOwner(), 
            ActionEvent.ACTION_PERFORMED, "paste")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider.SelectionObserver
     */
    public void selectionChanged(SelectionProvider source)
    {
        updateEnablement();
    }
    
    private void updateEnablement()
    {
        MainFrame mainFrame = Main.getInstance().getFrame();
        
        if (mainFrame == null) 
            return; // HACK
        
        Transferable contents = mainFrame.getToolkit().
            getSystemClipboard().getContents(null);
        
        setEnabled(mainFrame != null && m_selectionProvider != null && 
            !Main.getInstance().isSessionRunning() && 
            contents.isDataFlavorSupported(GeneralTransferHandler.CARDS_FLAVOR) || 
            contents.isDataFlavorSupported(GeneralTransferHandler.CATEGORY_FLAVOR));
    }

    private void setValues()
    {
        setName(Localization.get(LC.PASTE));
        setIcon("/resource/icons/edit_paste.gif"); //$NON-NLS-1$
        setAccelerator(KeyEvent.VK_V, SHORTCUT_KEY);
        setMnemonic(1);
    }
}