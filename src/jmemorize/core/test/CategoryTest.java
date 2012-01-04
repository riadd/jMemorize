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
package jmemorize.core.test;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;
import junit.framework.TestCase;


/**
 * @author djemili
 */
public class CategoryTest extends TestCase implements CategoryObserver
{
    /**
     * Tag for test events.
     */
    interface TestEvent
    {
    }
    
    class CardEvent implements TestEvent
    {
        private int  m_type;
        private Card m_card;
        private int  m_level;
        
        CardEvent(int type, Card card, int level)
        {
            m_type  = type;
            m_card  = card;
            m_level = level;
        }
        
        public void assertEvent(int type, Card card, int level, Category category)
        {
            assertEquals(type, m_type);
            assertEquals(card, m_card);
            assertEquals(level, m_level);
            assertEquals(category, card.getCategory());
        }
    }
    
    class CategoryEvent implements TestEvent
    {
        private int      m_type;
        private Category m_category;
        
        CategoryEvent(int type, Category category)
        {
            m_type     = type;
            m_category = category;
        }
        
        public void assertCategory(int type, Category category)
        {
            assertEquals(type, m_type);
            assertEquals(category, m_category);
        }
    }
    
    
    private Category        m_rootCategory;
    private Category        m_childCategory;

    private Card            m_rootCard;
    private Card            m_childCard;

    private List<TestEvent> m_events = new LinkedList<TestEvent>();
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        m_rootCategory = new Category("root_category");
        m_childCategory = m_rootCategory.addCategoryChild(new Category("child_category"));
        
        m_rootCategory.addObserver(this);
        m_childCategory.addObserver(this);
        
        Calendar past = Calendar.getInstance();
        past.set(2000, 1, 1);
        
        m_rootCard  = new Card(past.getTime(), "root_card","bla");
        m_childCard = new Card(new Date(), "child_card","bla");
    
        m_events.clear();
    }

    public void testAddChildCategory()
    {
        Category root = new Category("test root");
        assertEquals(0, root.getChildCategories().size());
        assertEquals(0, root.getDepth());
        
        Category child = root.addCategoryChild(new Category("test child"));
        assertEquals(1, root.getChildCategories().size());
        assertEquals(1, child.getDepth());
        
        Category child2 = root.addCategoryChild(new Category("test child2"));
        assertEquals(2, root.getChildCategories().size());
        assertEquals(1, child2.getDepth());
        
        Category child3 = child.addCategoryChild(new Category("test child3"));
        assertEquals(2, root.getChildCategories().size());
        assertEquals(2, child3.getDepth());
    }
    
    public void testRemoveChildCategory()
    {
        Category root = new Category("test root");
        root.addCard(m_rootCard);
        
        Category child = root.addCategoryChild(new Category("test child"));
        child.addCard(m_childCard);
        Category child2 = root.addCategoryChild(new Category("test child2"));
        
        child2.remove();
        assertEquals(1, root.getChildCategories().size());
        assertEquals(2, root.getCards().size());
        
        child.remove();
        assertEquals(0, root.getChildCategories().size());
        assertEquals(1, root.getCards().size());
    }
    
    public void testGetSubtreeList()
    {
        /*
         *     A
         *    / \
         *   B   C
         *      / \
         *     D   E
         */
        
        Category a = new Category("a");
        Category b = a.addCategoryChild(new Category("b"));
        Category c = a.addCategoryChild(new Category("c"));
        Category d = c.addCategoryChild(new Category("d"));
        Category e = c.addCategoryChild(new Category("e"));
        
        List<Category> list = d.getSubtreeList();
        assertEquals(1, list.size());
        assertEquals(d, list.get(0));
        
        list = c.getSubtreeList();
        assertEquals(3, list.size());
        assertEquals(c, list.get(0));
        assertEquals(d, list.get(1));
        assertEquals(e, list.get(2));
        
        list = a.getSubtreeList();
        assertEquals(5, list.size());
        assertEquals(a, list.get(0));
        assertEquals(b, list.get(1));
        assertEquals(c, list.get(2));
        assertEquals(d, list.get(3));
        assertEquals(e, list.get(4));
    }
    
    public void testNaturalAlphabeticalCategoryOrder()
    {
        Category a = m_childCategory.addCategoryChild(new Category("test"));
        Category b = m_childCategory.addCategoryChild(new Category("taste"));
        Category c = m_childCategory.addCategoryChild(new Category("xyz"));
        Category d = m_childCategory.addCategoryChild(new Category("10utest"));
        Category e = m_childCategory.addCategoryChild(new Category("1utest"));
        Category f = m_childCategory.addCategoryChild(new Category("10 utest"));
        
        List<Category> childs = m_childCategory.getChildCategories();
        assertEquals(e, childs.get(0));
        assertEquals(d, childs.get(1));
        assertEquals(f, childs.get(2));
        assertEquals(b, childs.get(3));
        assertEquals(a, childs.get(4));
        assertEquals(c, childs.get(5));
    }
    
    public void testAddCard()
    {   
        m_rootCategory.addCard(m_rootCard);
        m_childCategory.addCard(m_childCard);
        
        assertEquals(1, m_childCategory.getCards().size());
        assertTrue(m_childCategory.getCards().contains(m_childCard));
        
        assertEquals(2, m_rootCategory.getCards().size());
        assertTrue(m_rootCategory.getCards().contains(m_rootCard));
        assertTrue(m_rootCategory.getCards().contains(m_childCard));
    }
    
    public void testAddCardToDeck()
    {   
        m_rootCategory.addCard(m_rootCard, 3);
        m_childCategory.addCard(m_childCard, 5);
        assertEquals(6, m_rootCategory.getNumberOfDecks());
        assertEquals(2, m_rootCategory.getCards().size());
        
        m_childCategory.remove();
        assertEquals(4, m_rootCategory.getNumberOfDecks());
        assertEquals(1, m_rootCategory.getCards().size());
        
        m_rootCategory.removeCard(m_rootCard);
        assertEquals(0, m_rootCategory.getNumberOfDecks());
        assertEquals(0, m_rootCategory.getCards().size());
    }
    
    public void testCardAddedEvent()
    {
        m_rootCategory.addCard(m_rootCard);
        assertEquals(1, m_events.size());
        CardEvent event = (CardEvent)m_events.get(0);
        event.assertEvent(ADDED_EVENT, m_rootCard, 0, m_rootCategory);
        
        m_events.clear();
        m_childCategory.addCard(m_childCard);
        assertEquals(2, m_events.size());
        event = (CardEvent)m_events.get(0);
        event.assertEvent(ADDED_EVENT, m_childCard, 0, m_childCategory);
        
        event = (CardEvent)m_events.get(1);
        event.assertEvent(ADDED_EVENT, m_childCard, 0, m_childCategory);
    }
    
    public void textGetCards()
    {
        m_rootCategory.addCard(m_rootCard, 3);
        m_childCategory.addCard(m_childCard, 5);
        assertEquals(1, m_rootCategory.getCards(4).size());
        assertEquals(2, m_rootCategory.getCards(2).size());
        assertEquals(2, m_rootCategory.getCards().size());
    }
    
    public void testRemoveCard()
    {
        m_rootCategory.addCard(m_rootCard);
        m_childCategory.addCard(m_childCard);

        m_rootCategory.removeCard(m_rootCard);
        assertFalse(m_rootCategory.getCards().contains(m_rootCard));
        assertTrue(m_rootCategory.getCards().contains(m_childCard));
        
        m_rootCategory.removeCard(m_childCard);
        assertFalse(m_childCategory.getCards().contains(m_childCard));
        assertFalse(m_rootCategory.getCards().contains(m_childCard));
    }
    
    public void testCardRemovedEvent()
    {
        m_rootCategory.addCard(m_rootCard);
        m_childCategory.addCard(m_childCard);
        m_events.clear();
        
        // event is only fired by root category deck
        m_rootCategory.removeCard(m_rootCard);
        assertEquals(1, m_events.size());
        CardEvent event = (CardEvent)m_events.get(0);
        event.assertEvent(REMOVED_EVENT, m_rootCard, 0, null);
        
        // event is fired by root and child category decks
        m_events.clear();        
        m_childCategory.removeCard(m_childCard);
        assertEquals(2, m_events.size());
        event = (CardEvent)m_events.get(0);
        event.assertEvent(REMOVED_EVENT, m_childCard, 0, null);
        
        event = (CardEvent)m_events.get(1);
        event.assertEvent(REMOVED_EVENT, m_childCard, 0, null);
    }
    
    public void testMoveCard()
    {
        m_childCategory.addCard(m_rootCard, 3);
        m_childCategory.moveCard(m_rootCard, m_rootCategory);
        
        assertTrue(m_rootCategory.getCards().contains(m_rootCard));
        assertFalse(m_childCategory.getCards().contains(m_rootCard));
        assertEquals(m_rootCategory, m_rootCard.getCategory());
        assertEquals(3, m_rootCard.getLevel());
    }
    
    public void testMoveCardEvents()
    {
        m_rootCategory.addCard(m_rootCard, 3);
        m_events.clear();
        m_rootCategory.moveCard(m_rootCard, m_childCategory);
        
        CardEvent event = (CardEvent)m_events.get(0);
        event.assertEvent(MOVED_EVENT, m_rootCard, 3, m_childCategory);
        // TODO test number of events
    }
    
    public void testCardEditedEvent()
    {
        m_rootCategory.addCard(m_rootCard);
        m_childCategory.addCard(m_childCard);
        m_events.clear();
        
        m_rootCard.setSides("test frontside", "test backside");
        assertEquals(1, m_events.size());
        CardEvent event = (CardEvent)m_events.get(0);
        event.assertEvent(EDITED_EVENT, m_rootCard, 0, m_rootCategory);
    }
    
    public void testRaiseCardRootCategory()
    {
        m_rootCategory.addCard(m_rootCard);
        assertEquals(0, m_rootCard.getLevel());
        
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());        
        assertEquals(1, m_rootCard.getLevel());
        
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());        
        assertEquals(2, m_rootCard.getLevel());
    }
    
    public void testRaiseCardInChildCategory()
    {
        m_childCategory.addCard(m_childCard);
        assertEquals(0, m_childCard.getLevel());
        
        Category.raiseCardLevel(m_childCard, new Date(), new Date());        
        assertEquals(1, m_childCard.getLevel());
        
        Category.raiseCardLevel(m_childCard, new Date(), new Date());        
        assertEquals(2, m_childCard.getLevel());
    }
    
    public void testResetCardLevel()
    {
        m_rootCategory.addCard(m_rootCard);
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        assertEquals(2, m_rootCard.getLevel());
        
        Category.resetCardLevel(m_rootCard, new Date());
        assertEquals(0, m_rootCard.getLevel());
    }
    
    public void testResetCard()
    {
        m_rootCategory.addCard(m_rootCard);
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        
        Category.resetCardLevel(m_rootCard, new Date());
        assertEquals(0, m_rootCard.getLevel());
    }
    
    public void testCardDateCreated()
    {
        assertTrue(m_rootCard.getDateCreated().before(m_childCard.getDateCreated()));
    }
    
    public void testCardDateTested()
    {
        m_rootCategory.addCard(m_rootCard);
        assertNull(m_rootCard.getDateTested());
        assertTrue(m_rootCard.isUnlearned());
        assertFalse(m_rootCard.isLearned());
        
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        Date lastTest = m_rootCard.getDateTested();
        assertNotNull(lastTest);
        
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        assertNotSame(m_rootCard.getDateTouched(), lastTest);
        lastTest = m_rootCard.getDateTested();
        assertFalse(m_rootCard.isUnlearned());
        
        Category.resetCardLevel(m_rootCard, new Date());
        assertNotSame(m_rootCard.getDateTouched(), lastTest);
        assertTrue(m_rootCard.isUnlearned());
        
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        m_rootCategory.resetCard(m_rootCard);
        assertNull(m_rootCard.getDateTested());
        assertTrue(m_rootCard.isUnlearned());
    }
    
    public void testCardDateExpired()
    {
        Calendar future = Calendar.getInstance();
        future.set(3000, 1, 1);
        
        Calendar past   = Calendar.getInstance();
        past.set(1000, 1, 1);
        
        m_rootCategory.addCard(m_rootCard);
        Category.raiseCardLevel(m_rootCard, new Date(), future.getTime());
        assertEquals(future.getTime(), m_rootCard.getDateExpired());
        assertFalse(m_rootCard.isExpired());
        
        Category.resetCardLevel(m_rootCard, new Date());
        assertNull(m_rootCard.getDateExpired());
        assertFalse(m_rootCard.isLearned());
        assertFalse(m_rootCard.isExpired());
        
        Category.raiseCardLevel(m_rootCard, new Date(), future.getTime());
        m_rootCategory.resetCard(m_rootCard);
        assertNull(m_rootCard.getDateExpired());
        assertFalse(m_rootCard.isExpired());
    }
    
    public void testCardDateTouched()
    {
        Date lastTouch = m_rootCard.getDateTouched();
        assertEquals(m_rootCard.getDateCreated(), lastTouch);
        
        m_rootCategory.addCard(m_rootCard);
        assertEquals(m_rootCard.getDateTouched(), lastTouch);
        
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        assertNotSame(m_rootCard.getDateTouched(), lastTouch);
        
        lastTouch = m_rootCard.getDateTouched();
        Category.resetCardLevel(m_rootCard, new Date());
        assertNotSame(m_rootCard.getDateTouched(), lastTouch);
        
        lastTouch = m_rootCard.getDateTouched();
        Category.reappendCard(m_rootCard);
        assertNotSame(m_rootCard.getDateTouched(), lastTouch);
    } 
    
    public void testCardStats()
    {
        m_rootCategory.addCard(m_rootCard);
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        Category.resetCardLevel(m_rootCard, new Date());
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        assertEquals(3, m_rootCard.getTestsPassed());
        assertEquals(4, m_rootCard.getTestsTotal());
        
        m_rootCard.incStats(10, 11);
        assertEquals(13, m_rootCard.getTestsPassed());
        assertEquals(15, m_rootCard.getTestsTotal());
    }
    
    public void testNumberOfDecks()
    {
        assertEquals(0, m_rootCategory.getNumberOfDecks());
        
        m_rootCategory.addCard(m_rootCard);
        assertEquals(1, m_rootCategory.getNumberOfDecks());
        
        Category.raiseCardLevel(m_rootCard, new Date(), new Date());
        assertEquals(2, m_rootCategory.getNumberOfDecks());
    }
    
    /*
     * @see jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        m_events.add(new CategoryEvent(type, category));
    }

    /*
     * @see jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        m_events.add(new CardEvent(type, card, deck));
    }
}
