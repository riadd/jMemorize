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
package jmemorize.core;

/**
 * Interface for observers of a category. Categories can signal either card or
 * category hierarchy related events. Observing a specific category will
 * automaticly listen to all of its child categories too.
 * 
 * @author djemili
 */
public interface CategoryObserver extends Events
{
    /**
     * Gets notified when a card event happens in the observed category or in
     * one of its child categories.
     * 
     * @param type Either EDITED_EVENT, ADDED_EVENT, REMOVED_EVENT,
     * EXPIRED_EVENT or DECK_EVENT.
     * @param card The card that changed.
     * @param category TODO
     * @param deck The deck that held the card, when the event happend.
     */
    void onCardEvent(int type, Card card, Category category, int deck);
    
    /**
     * Gets notified when a category event happens in the observed category or
     * in one of its child categories.
     * 
     * @param type Either EDITED_EVENT, ADDED_EVENT or REMOVED_EVENT.
     * @param category The category that created the event.
     */
    void onCategoryEvent(int type, Category category);
}
