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
package jmemorize.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jmemorize.util.NaturalOrderComparator;

/**
 * A card category can hold a number of 0 to n decks. The cards in deck 0 are
 * called <b>unlearned</b>. Cards in higher decks are called <b>learned</b> or
 * <b>expired</b>, depending on the fact of wether their expiration timer
 * has already passed by or not.
 * 
 * A category can have a number of child categories. Fetching cards from a
 * parent category will also fetch all cards of its child categories.
 * 
 * For example category B is child of category A. A holds a card CA and B holds
 * a card CB. Getting all cards of category A will then return CA <i>and</i>
 * CB.
 * 
 * Observers can be hooked to categories and will be notified when a card or
 * category event in this category or one of its child categories happens.
 * 
 * @author djemili
 */
public class Category implements Events
{
    // TODO use CopyOnWriteArrayList in Java1.5
    private List<CategoryObserver> m_observers       = new ArrayList<CategoryObserver>();

    private String                 m_name;
    private int                    m_depth           = 0;                     // is 0 for root category

    private List<List<Card>>       m_decks           = new ArrayList<List<Card>>(); // list of card lists

    private Category               m_parent;
    private List<Category>         m_childCategories = new LinkedList<Category>();
    
    /**
     * Creates a new Category.
     * 
     * @param name The name of the new category.
     */
    public Category(String name)
    {
        m_name   = name;
    }
    
    /*
     * Card related methods.
     */
    
    /**
     * Adds a card to the level 0 deck of this category. The card will be
     * considered as unlearned after being added to deck level 0.
     * 
     * Fires a ADDED_EVENT.
     */
    public void addCard(Card card)
    {
        addCard(card, 0);
    }
    
    /**
     * Adds a card to the deck with given level and fires an event.
     * 
     * Fires a ADDED_EVENT.
     */
    public void addCard(Card card, int level)
    {
        addCardInternal(card, level);
        
        fireCardEvent(ADDED_EVENT, card, card.getCategory(), level);
    }
    
    /**
     * Removes a card from its associated deck and fires an event.
     */
    public void removeCard(Card card)
    {
        int level = card.getLevel();
        Category category = card.getCategory();
        removeCardInternal(card);
        
        fireCardEvent(REMOVED_EVENT, card, category, level);
    }
    
    /**
     * Moves the card to a new category, preserving all its fields and its
     * level. This is different from removing and then adding a card because it
     * triggers a single MOVE event instead of a REMOVE and ADD event.
     */
    public static void moveCard(Card card, Category newCategory)
    {
        int level = card.getLevel();
        Category category = card.getCategory();
        
        category.removeCardInternal(card);
        newCategory.addCardInternal(card, level);
        
        category.fireCardEvent(MOVED_EVENT, card, category, level);
        newCategory.fireCardEvent(MOVED_EVENT, card, category, level);
    }
    
    /**
     * Removes the card from its current deck and adds it to the next deck. The
     * given date is used as new expiration date.
     * 
     * Fires a DECK_EVENT.
     */
    public static void raiseCardLevel(Card card, Date testDate, Date newExpirationDate)
    {
        card.incStats(1, 1);
        changeCardLevel(card, card.getLevel() + 1, testDate, newExpirationDate);
    }
    
    /**
     * Removes the card from its current deck and appends it to deck 0 (even if
     * its already at level 0). TotalTests is increased by one.
     * 
     * Fires a DECK_EVENT.
     */
    public static void resetCardLevel(Card card, Date testDate)
    {
        card.incStats(0, 1);
        changeCardLevel(card, 0, testDate, null); // CHECK use null for testdate!?
    }
    
    /**
     * Removes the card from its current deck and reappends it to the same deck
     * again. This doesnt change any values besides DateTouched.
     * 
     * Fires a DECK_EVENT.
     */
    public static void reappendCard(Card card)
    {
        card.setDateTouched(new Date());
        
        card.getCategory().fireCardEvent(DECK_EVENT, card, card.getCategory(), card.getLevel());
    }
    
    /**
     * Resets the card by moving it back to level 0 and deleting all its stats.
     * 
     * Fires a DECK_EVENT.
     */
    public void resetCard(Card card) //HACK
    {
        card.resetStats();
        changeCardLevel(card, 0, null, null);
    }
    
    /*
     * Card getter methods
     */
    
    /**
     * @return All cards of all decks in this category.
     */
    public List<Card> getCards()
    {
        List<Card> cardList = new ArrayList<Card>();
        
        //get cards from all decks
        for (int i=0; i < m_decks.size(); i++)
        {
            cardList.addAll(getCards(i));
        }
        
        return cardList;
    }
    
    /**
     * @param level the deck level.
     * 
     * @return all cards in the given deck level in this category and its child
     * categories. Returns all cards of all decks if -1 is given as level.
     */
    public List<Card> getCards(int level)
    {
        if (level >= getNumberOfDecks())
        {
            return new ArrayList<Card>(); //HACK
        }
        
        if (level == -1)
        {
            return getCards();
        }
        
        //get cards in this category
        List<Card> cardList = new ArrayList<Card>(m_decks.get(level));
        
        //get cards in child categories
        for (Category child : getChildCategories())
        {
            if (child.getNumberOfDecks() > level)
            {
                cardList.addAll(child.getCards(level));
            }
        }
        
        return cardList;
    }
    
    /**
     * @return all expired cards of all decks in this category and its child
     * categories.
     */
    public List<Card> getExpiredCards()
    {
        List<Card> expiredCards = getCards();
        
        for (Iterator<Card> it = expiredCards.iterator(); it.hasNext();)
        {
            Card card = it.next();
            if (!card.isExpired())
            {
                it.remove();
            }
        }
        
        return expiredCards;
    }
    
    /**
     * @return all expired cards of given deck in this category and its child
     * categories.
     */
    public List<Card> getExpiredCards(int level)
    {
        List<Card> expiredCards = getCards(level);
        
        for (Iterator<Card> it = expiredCards.iterator(); it.hasNext();)
        {
            Card card = (Card)it.next();
            if (!card.isExpired())
            {
                it.remove();
            }
        }
        
        return expiredCards;
    }
    
    /**
     * @return all learned cards of all decks in this category and its child
     * categories.
     */
    public List<Card> getLearnedCards()
    {
        List<Card> learnedCards = getCards();
        
        for (Iterator<Card> it = learnedCards.iterator(); it.hasNext();)
        {
            Card card = (Card)it.next();
            if (!card.isLearned())
            {
                it.remove();
            }
        }
        
        return learnedCards;
    }
    
    /**
     * Learned cards are cards that are learned and haven't expired yet.
     * 
     * @param level the level of the deck of who's cards you want to get.
     * @return all learned cards in deck with given level.
     */
    public List<Card> getLearnedCards(int level)
    {
        // level 0 decks have no learned cards
        if (level == 0)
        {
            return new ArrayList<Card>();
        }
        
        List<Card> learnedCards = getCards(level);        
        for (Iterator<Card> it = learnedCards.iterator(); it.hasNext();)
        {
            Card card = (Card)it.next();
            if (!card.isLearned())
            {
                it.remove();
            }
        }
        
        return learnedCards;        
    }
    
    /**
     * Unlearned cards (all cards in deck 0) and expired cards are learnable.
     * 
     * @return all learnable cards in deck with given level.
     */
    public List<Card> getLearnableCards(int level)
    {
        return level == 0 ? getCards(0) : getExpiredCards(level);
    }
    
    /**
     * Unlearned cards (all cards in deck 0) and expired cards are learnable.
     * 
     * @return all learnable cards in this category.
     * 
     * @see #getLearnableCards(int)
     */
    public List<Card> getLearnableCards()
    {
        List<Card> learnableCards = new LinkedList<Card>();
        for (int i = 0; i < getNumberOfDecks(); i++)
        {
            learnableCards.addAll(getLearnableCards(i));
        }
        
        return learnableCards;
    }
    
    /**
     * @return all unlearned cards of this category and its child categories.
     */
    public List<Card> getUnlearnedCards()
    {
        return m_decks.size() > 0 ? getCards(0) : new ArrayList<Card>();
    }
    
    /**
     * @return All cards that are local to this category. That is all cards 
     * that directly belong to this category and not to any of this child 
     * categories.
     */
    public List<Card> getLocalCards()
    {
        List<Card> localCards = new ArrayList<Card>();
        for (int i = 0; i < getNumberOfDecks(); i++)
        {
            localCards.addAll(getLocalCards(i));
        }
        
        return localCards;
    }
    
    /**
     * @return All cards in the level that are local to this category. That is
     * all cards that directly belong to this category and not to any of this
     * child categories.
     */
    public List<Card> getLocalCards(int level)
    {
        return m_decks.get(level);
    }
    
    /**
     * @return The number of decks of this category and its child categories.
     * That means that no child categoriy can have more number of decks then
     * its parent category.
     */
    public int getNumberOfDecks()
    {
        return m_decks.size();
    }    
    
    /*
     * Category related methods.
     */
    
    /**
     * @return Returns a unmodifiable list of the child categories.
     */
    public List<Category> getChildCategories()
    {
        return Collections.unmodifiableList(m_childCategories);
    }
   
    
    /**
     * @return the child category with given name. <code>null</code> if there
     * is child category with given name.
     */
    public Category getChildCategory(String name)
    {
        for (Category category : m_childCategories)
        {
            if (category.getName().equals(name))
                return category;
        }
        
        return null;
    }
    
    /**
     * Appends the category at end of category child list.
     */
    public Category addCategoryChild(Category category)
    {
        category.m_parent = this;
        category.m_depth  = m_depth + 1;
        
        Comparator comp = new NaturalOrderComparator();
        
        int position = 0;
        for (Category childCategory : m_childCategories)
        {
            if (comp.compare(category.getName(), childCategory.getName()) < 0)
                break;
            
            position++;            
        }

        m_childCategories.add(position, category);

        fireCategoryEvent(ADDED_EVENT, category);
        
        return category;
    }
    
    /**
     * Removes this category. Note that the root category can't be removed.
     * 
     * Fires a REMOVED_EVENT.
     */
    public void remove()
    {
        assert m_parent != null : "Root category can't be deleted"; //$NON-NLS-1$

        m_parent.m_childCategories.remove(this);
        
        fireCategoryEvent(REMOVED_EVENT, this);
        m_parent = null; // have to release parent AFTER firing event
    }
    
    /**
     * @return True if given category is a child of this category. False otherwise.
     */
    public boolean contains(Category category)
    {
        if (this == category)
        {
            return true;
        }
        
        for (Category cat : m_childCategories)
        {
            if (cat.contains(category))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * @return The parent of this category or <code>null</code> if it has no
     * parent.
     */
    public Category getParent()
    {
        return m_parent;
    }
    
    /**
     * Sets a new name for this category.
     * 
     * Fires a EDITED_EVENT.
     */
    public void setName(String newName)
    {
        assert newName != null;
        
        if (!m_name.equals(newName))
        {
            m_name = newName;
            
            fireCategoryEvent(EDITED_EVENT, this);
        }
    }
    
    /**
     * @return The name of this category.
     */
    public String getName()
    {
        return m_name;
    }
    
    /**
     * @return a textual representation of the category path of this category.
     * Starting from the root, every category in the path is separated with a
     * slash. The path includes the name of this category as last part.
     */
    public String getPath()
    {
        return m_parent != null ? m_parent.getPath() +  "/" + getName() : getName(); //$NON-NLS-1$
    }
    
    /**
     * @return Number of hops from this node to root.
     */
    public int getDepth()
    {
        return m_depth;
    }
    
    /**
     * @return A list of all child categories and their childs etc.
     */
    public List<Category> getSubtreeList() // TODO rename to getChildCategoriesTree
    {
        List<Category> list = new ArrayList<Category>(m_childCategories.size() + 1);
        
        list.add(this);
        for (Category category : m_childCategories)
        {
            list.addAll(category.getSubtreeList());
        }
        
        return list;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "Category("+m_name+")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /*
     * Event related methods
     */
    
    public void addObserver(CategoryObserver observer)
    {
        m_observers.add(observer);
    }
    
    public void removeObserver(CategoryObserver observer)
    {
        m_observers.remove(observer);
    }
    
    /**
     * @return a clone of this category. The clone contains the same child
     * categories and the same cards as this category, but without any user
     * dependent-stats(e.g. all cards are at level 0 and have no date_tested).
     */
    public Category cloneWithoutProgress()
    {
        Category clonedCategory = new Category(m_name);
        
        for (List<Card> cards : m_decks)
        {
            for (Card card : cards)
            {
                clonedCategory.addCard(card.cloneWithoutProgress());
            }
        }
        
        for (Category childCategory : getChildCategories())
        {
            clonedCategory.addCategoryChild((Category)childCategory.cloneWithoutProgress());
        }
        
        return clonedCategory;
    }
    
    void fireCardEvent(int type, Card card, Category category, int deck)
    {
        if (type != EDITED_EVENT)
        {
            adjustNumberOfDecks();
        }
        
        if (m_parent != null)
        {
            m_parent.fireCardEvent(type, card, category, deck);
        }
        
        List<CategoryObserver> observersCopy = new ArrayList<CategoryObserver>(m_observers);
        for (CategoryObserver observer : observersCopy)
        {
            observer.onCardEvent(type, card, category, deck);
        }
    }
    
    void fireCategoryEvent(int type, Category category)
    {
        adjustNumberOfDecks();
        
        if (m_parent != null)
        {
            m_parent.fireCategoryEvent(type, category);
        }
        
        List<CategoryObserver> observersCopy = new ArrayList<CategoryObserver>(m_observers);
        for (CategoryObserver observer : observersCopy)
        {
            observer.onCategoryEvent(type, category);
        }
    }
    
    /**
     * Adds a card to this category without emitting a ADDED_EVENT. 
     */
    private void addCardInternal(Card card, int level)
    {
        // check boundary
        while (m_decks.size() <= level)
        {
            m_decks.add(new ArrayList<Card>());
        }
        
        List<Card> cards = m_decks.get(level);
        cards.add(card);
        
        card.setCategory(this);
        card.setLevel(level);
        
        // sanity checks
        if (level > 0 && card.getDateExpired() == null)
            card.setDateExpired(new Date());
        
        if (level == 0)
            card.setDateExpired(null);
    }
    
    /**
     * Removes a card from this category without emitting a REMOVED_EVENT. 
     */
    private void removeCardInternal(Card card)
    {
        Category cat = card.getCategory();
        if (cat == this)
        {
            int level = card.getLevel();
            List<Card> cards = m_decks.get(level);
            cards.remove(card);
            
            card.setCategory(null);
        }
        else
        {
            cat.removeCardInternal(card);
        }
    }
    
    /**
     * Changes the deck level of card and fires a DECK_EVENT. 
     */
    private static void changeCardLevel(Card card, int newLevel, 
        Date newTest, Date newExpiration)
    {
        Category category = card.getCategory();
        int level = card.getLevel();
        
        category.removeCardInternal(card);
        
        card.setDateTested(newTest);
        card.setDateExpired(newExpiration);
        card.setDateTouched(new Date());
        card.resetLearnedAmount();
        
        // note also that new expiration date is set before adding again
        category.addCardInternal(card, newLevel);
        
        category.fireCardEvent(DECK_EVENT, card, category, level);
    }
    
    private void adjustNumberOfDecks()
    {
        // find child category with most decks
        int maxChildDecks = 0;
        for (Category child : m_childCategories)
        {
            if (child.getNumberOfDecks() > maxChildDecks)
            {
                maxChildDecks = child.getNumberOfDecks();
            }
        }
        
        //grow decks
        while (maxChildDecks > getNumberOfDecks())
        {
            m_decks.add(new ArrayList<Card>());
        }
        
        //trim decks
        while (maxChildDecks < getNumberOfDecks() 
            && (m_decks.get(getNumberOfDecks()-1)).isEmpty() )
        {
            m_decks.remove(getNumberOfDecks()-1);
        }
    }
}
