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
import java.util.List;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.SelectionProvider.SelectionObserver;
import jmemorize.gui.swing.actions.AbstractAction2;
import jmemorize.gui.swing.frames.EditCardFrame;

/**
 * An action that show the window for editting cards with the currently selected
 * card.
 * 
 * @author djemili
 */
public class EditCardAction extends AbstractAction2 implements SelectionObserver
{
    private SelectionProvider m_selectionProvider;
    
    public EditCardAction(SelectionProvider selectionProvider)
    {
        m_selectionProvider = selectionProvider;
        m_selectionProvider.addSelectionObserver(this);
        
        setValues();
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        // TODO move editCards in SelectionProvider!?
        Card card = (Card)m_selectionProvider.getSelectedCards().get(0);
        List<Card> cards = m_selectionProvider.getRelatedCards();
        Category category = m_selectionProvider.getCategory();
        
        EditCardFrame.getInstance().showCard(card, cards, category);
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
        setEnabled(m_selectionProvider.getSelectedCards() != null && 
            m_selectionProvider.getSelectedCards().size() > 0);
    }

    private void setValues()
    {
        setName(Localization.get("MainFrame.EDIT_CARD")); //$NON-NLS-1$
        setDescription(Localization.get("MainFrame.EDIT_CARD_DESC")); //$NON-NLS-1$
        setIcon("/resource/icons/card_edit.gif"); //$NON-NLS-1$
        setMnemonic(1);
        setAccelerator(KeyEvent.VK_ENTER, 0);
    }
}