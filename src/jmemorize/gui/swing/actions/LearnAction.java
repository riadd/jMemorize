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
import java.awt.event.KeyEvent;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;
import jmemorize.core.Main;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.SelectionProvider.SelectionObserver;
import jmemorize.gui.swing.dialogs.LearnSettingsDialog;
import jmemorize.gui.swing.frames.MainFrame;

/**
 * An action that shows the learn session settings window.
 * 
 * @author djemili
 */
public class LearnAction extends AbstractSessionDisabledAction 
    implements SelectionObserver, CategoryObserver
{
    private SelectionProvider m_selectionProvider;
    private Category          m_category;

    public LearnAction(SelectionProvider selectionProvider)
    {
        setValues();
        
        m_selectionProvider = selectionProvider;
        selectionProvider.addSelectionObserver(this);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(ActionEvent e)
    {
        Main main = Main.getInstance();
        MainFrame frame = main.getFrame();
        
        new LearnSettingsDialog(frame, main.getLearnSettings(), m_selectionProvider);
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider.SelectionObserver
     */
    public void selectionChanged(SelectionProvider source)
    {
        if (m_category != null)
            m_category.removeObserver(this);
        
        m_category = source.getCategory();
        
        if (m_category != null)
            m_category.addObserver(this);
        
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.actions.AbstractSessionDisabledAction
     */
    protected void updateEnablement()
    {
        boolean runningSession = Main.getInstance().isSessionRunning();
        
        if (m_selectionProvider == null)
        {
            setEnabled(false);
            return;
        }
        
        boolean hasSelectedCards = m_selectionProvider.getSelectedCards() != null &&
            !m_selectionProvider.getSelectedCards().isEmpty();
            
        boolean hasLearnableCards = m_selectionProvider.getCategory() != null && 
            !m_selectionProvider.getCategory().getLearnableCards().isEmpty();
        
        setEnabled(!runningSession && (hasLearnableCards || hasSelectedCards)); 
    }

    private void setValues()
    {
        setName(Localization.get(LC.LEARN));
        setDescription(Localization.get(LC.LEARN_DESC));
        setIcon("/resource/icons/learn.gif"); //$NON-NLS-1$
        setAccelerator(KeyEvent.VK_L, SHORTCUT_KEY);
        setMnemonic(1);
    }
}