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
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import jmemorize.core.Card;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.SelectionProvider.SelectionObserver;
import jmemorize.gui.swing.actions.AbstractAction2;

/**
 * An action that resets all currently selected cards.
 * 
 * @author djemili
 */
public class ResetCardAction extends AbstractAction2 implements SelectionObserver
{
    private SelectionProvider m_selectionProvider;
    
    public ResetCardAction(SelectionProvider selectionProvider)
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
        Object[] args = { new Integer(m_selectionProvider.getSelectedCards().size()) };
        MessageFormat form = new MessageFormat(Localization.get(LC.RESET_WARN));

        int n = JOptionPane.showConfirmDialog(
            m_selectionProvider.getFrame(), 
            form.format(args), 
            Localization.get(LC.RESET), 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);

        if (n == JOptionPane.OK_OPTION)
        {
            for (Card card : m_selectionProvider.getSelectedCards())
            {
                m_selectionProvider.getCategory().resetCard(card);
            }
        }
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
        setName(Localization.get(LC.RESET));
        setDescription(Localization.get(LC.RESET_DESC));
        setIcon("/resource/icons/card_reset.gif"); //$NON-NLS-1$
        setMnemonic(1);
        setAccelerator(KeyEvent.VK_R, SHORTCUT_KEY);
    }
}