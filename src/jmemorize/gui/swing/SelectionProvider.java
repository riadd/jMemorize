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
package jmemorize.gui.swing;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import jmemorize.core.Card;
import jmemorize.core.Category;

// TODO use this to replace the propertiesChanged and editCards mess

/**
 * A simple interface that provides a card context.
 * 
 * What a selection is depends on the context. In most cases you should only
 * declare cards and categories as selected if they were activily selected by
 * the user (i.e. by clicking with the mouse). Sometimes there are also implicit
 * selections though. For example while learning the currently learned card is
 * always considered as selected.
 * 
 * If there are cards selected there should never also be categories selected
 * and vice-versa.
 * 
 * @author djemili
 */
public interface SelectionProvider
{
    /**
     * A listener that gets notified when the card selection for a selection
     * provider changes. Note that the methods isn't fired when related cards
     * are changed.
     */
    public interface SelectionObserver
    {
        public void selectionChanged(SelectionProvider source);
    }
    
    /**
     * @return a list of cards that is related to the currently selected cards.
     * What this is this is depending on the context (i.e. while learning the
     * related cards are all cards that have been learned so far in this
     * session).
     */
    public List<Card> getRelatedCards();
    
    /**
     * @return the category associated to this provider.
     */
    public Category getCategory();
    
    /**
     * This should only be used if the user has activly selected the categories.
     * Most SelectionProvider won't deal with category selections.
     * 
     * @return the currently selected categories or <code>null</code> if there
     * are none.
     */
    public List<Category> getSelectedCategories(); // TODO clear up relation with getCategory method
    
    // TODO return null if no selected cards
    // TODO check if this condition is respected by all parties
    /**
     * The list of selected cards must always also include the card that is
     * given by getSelectedCard if that card is not null.
     * 
     * @return all selected cards or <code>null</code> if no cards are
     * selected.
     */
    public List<Card> getSelectedCards();
    
    /**
     * Add a listener that gets notified when the card selection is changed.
     */
    public void addSelectionObserver(SelectionObserver observer);
    
    /**
     * Removes a listener.
     * 
     * @param observer the listener that should be removed.
     */
    public void removeSelectionObserver(SelectionObserver observer);
    
    /**
     * @return the JComponent that can handles copy, cut and paste swing action.
     */
    public JComponent getDefaultFocusOwner(); // TODO rename
    
    /**
     * @return the frame that should be used to display modal dialogs.
     */
    public JFrame getFrame(); // TODO remove
}
