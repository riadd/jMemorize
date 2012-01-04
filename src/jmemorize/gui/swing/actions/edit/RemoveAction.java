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
import java.util.List;

import javax.swing.JOptionPane;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.Main;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.SelectionProvider.SelectionObserver;
import jmemorize.gui.swing.actions.AbstractAction2;

/**
 * An action that removes currently selected items (whether they are cards or
 * categories).
 * 
 * @author djemili
 */
public class RemoveAction extends AbstractAction2 implements SelectionObserver
{
    private SelectionProvider m_selectionProvider;
    
    public RemoveAction(SelectionProvider selectionProvider)
    {
        m_selectionProvider = selectionProvider;
        m_selectionProvider.addSelectionObserver(this);
        selectionChanged(m_selectionProvider);
        
        setValues();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed
     */
    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        List<Card> selectedCards = m_selectionProvider.getSelectedCards();
        List<Category> selectedCategories = m_selectionProvider.getSelectedCategories();
        
        if (selectedCards != null && selectedCards.size() > 0)
        {
            Object[] args = {new Integer(selectedCards.size())};
            MessageFormat form = new MessageFormat(
                Localization.get("MainFrame.DELETE_CARDS_WARN"));      //$NON-NLS-1$

            int n = JOptionPane.showConfirmDialog(
                m_selectionProvider.getFrame(), 
                form.format(args), 
                Localization.get("MainFrame.DELETE_CARDS_WARN_TITLE"), //$NON-NLS-1$
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE);

            if (n != JOptionPane.OK_OPTION)
            {
                return;
            }

            for (Card card : selectedCards)
            {
                card.getCategory().removeCard(card); // HACK
            }
        }
        else if (selectedCategories.size() > 0)
        {
            
            for (Category category : selectedCategories)
            {
                int cardCount = category.getCards().size();

                if (cardCount > 0)
                {
                    Object[] args = {category.getName(), new Integer(cardCount)};
                    MessageFormat form = new MessageFormat(
                        Localization.get("MainFrame.DELETE_CATEGORY_WARN")); //$NON-NLS-1$
                    
                    int n = JOptionPane.showConfirmDialog(
                        m_selectionProvider.getFrame(), 
                        form.format(args), 
                        Localization.get("MainFrame.REMOVE_CATEGORY_TITLE"), //$NON-NLS-1$
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);

                    if (n != JOptionPane.OK_OPTION)
                    {
                        continue;
                    }
                }

                category.remove();
            }
        }
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider.SelectionObserver#selectionChanged
     */
    public void selectionChanged(SelectionProvider source)
    {
        Category rootCategory = Main.getInstance().getLesson().getRootCategory();
        
        setEnabled((source.getSelectedCards() != null && 
            source.getSelectedCards().size() > 0) || 
            (source.getSelectedCategories() != null && 
            !source.getSelectedCategories().contains(rootCategory)));
    }

    private void setValues()
    {
        setName(Localization.get("MainFrame.DELETE")); //$NON-NLS-1$
        setIcon("/resource/icons/remove.gif"); //$NON-NLS-1$
        setAccelerator(KeyEvent.VK_DELETE, 0);
        setMnemonic(1);
    }
}
