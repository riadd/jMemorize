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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.learn.DefaultLearnSession;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSettings;
import jmemorize.core.test.stubs.LearnSessionProviderStub;
import junit.framework.TestCase;

public class LearnSessionTest extends TestCase
{
    private Category                 m_category;
    private LearnSession             m_session;
    private LearnSettings            m_settings;
    private LearnSessionProviderStub m_provider;
    
    private Card                     m_card0;
    private Card                     m_card1;
    private Card                     m_card2;

    protected void setUp() throws Exception
    {
        m_card0 = new Card("front0", "flip0");
        m_card1 = new Card("front1", "flip1");
        m_card2 = new Card("front2", "flip2");
        
        m_category = new Category("testCategory");
        m_category.addCard(m_card0);
        m_category.addCard(m_card1);
        m_category.addCard(m_card2);

        m_settings = new LearnSettings();
        m_settings.setShuffleRatio(0);
        
        m_provider = new LearnSessionProviderStub();
        m_session = createSession();
    }
    
    public void testEndCallsProvider()
    {
        assertFalse(m_provider.isSessionEnded());
        m_session.startLearning();
        assertFalse(m_provider.isSessionEnded());
        m_session.endLearning();
        assertTrue(m_provider.isSessionEnded());
    }
    
    public void testHasStartDate()
    {
        assertNull(m_session.getStart());
        m_session.startLearning();
        assertNotNull(m_session.getStart());
    }
    
    public void testHasEndDateAfterLessonEndOnly()
    {
        m_session.startLearning();
        assertNull(m_session.getEnd());
        
        m_session.endLearning();
        assertNotNull(m_session.getEnd());
    }
    
    public void testGetCardsBeforeLessonStart()
    {
        TestHelper.assertSet(new Card[]{m_card0, m_card1, m_card2}, m_session.getCardsLeft());
    }

//    public void testGetPassedNoShuffle()
//    {
//        // force set card test dates so none are the same 
//        Date date = new Date();
//        long t = date.getTime();
//        m_card0.setDateTouched(date);
//        m_card1.setDateTouched(new Date(t+1));
//        m_card2.setDateTouched(new Date(t+2));
//        
//        // create a new session so that the eq. classes get setup correctly
//        m_session = createSession();
//        m_session.startLearning();
//
//        m_session.cardSkipped();             // card 0 skipped
//        m_session.cardChecked(true, false);  // card 1 passed
//        m_session.cardChecked(false, false); // card 2 failed
//        m_session.cardChecked(true, false);  // card 0 passed
//
//        TestHelper.assertSet(new Card[]{m_card1, m_card0}, m_session.getPassedCards());
//    }

    public void testGetPassedWithShuffle()
    {
        m_session.startLearning();
        
        Card card0 = m_session.getCurrentCard();
        m_session.cardSkipped();             // card 0 skipped
        
        Card card1 = m_session.getCurrentCard();
        m_session.cardChecked(true, false);  // card 1 passed
        m_session.cardChecked(false, false); // card 2 failed
        
        while (m_session.getCurrentCard() != card0) 
        {
            m_session.cardChecked(false, false); // card 2 failed            
        }
        m_session.cardChecked(true, false);  // card 0 passed
        
        TestHelper.assertSet(new Card[]{card1, card0}, m_session.getPassedCards());
    }
    
    public void testRemoveFromSkippedIfPassedOrFailed()
    {
        m_session.startLearning();
        
        m_session.cardSkipped();             // card 0 skipped
        m_session.cardSkipped();             // card 1 skipped
        m_session.cardSkipped();             // card 2 skipped
        
        for (int i = 0; i < 3; i++) 
        {
            Card card = m_session.getCurrentCard();
            if (card == m_card0) 
            {
                m_session.cardChecked(true, false);  // card 0 passed
            } 
            else if (card == m_card1) 
            {
                m_session.cardSkipped();             // card 1 skipped
            } 
            else 
            {
                m_session.cardChecked(false, false); // card 2 unlearned
            }
        }
        
        TestHelper.assertSet(new Card[]{m_card0}, m_session.getPassedCards());
        TestHelper.assertSet(new Card[]{m_card1}, m_session.getSkippedCards());
        assertEquals(0, m_session.getFailedCards().size());
    }
    
    public void testGetSkipped() 
    {
        m_session.startLearning();
        
        m_session.cardChecked(true, false);  // card 0 passed
        Card card0 = m_session.getCurrentCard();
        m_session.cardSkipped();             // card 1 skipped
        m_session.cardChecked(false, false); // card 2 failed
        
        TestHelper.assertSet(new Card[]{card0}, m_session.getSkippedCards());
    }
    
    public void testSkippedUnlearnedCardsAreReshownAfterExpiredCards()
    {
        Category.raiseCardLevel(m_card1, new Date(), new Date());
        m_session = createSession();
        m_session.startLearning();
        
        m_session.cardSkipped();            // card 0 skipped
        m_session.cardSkipped();            // card 2 skipped
        
        assertEquals(m_card1, m_session.getCurrentCard());
    }
    
    public void testFailedUnlearnedCardsAreReshownAfterExpiredCards()
    {
        Category.raiseCardLevel(m_card1, new Date(), new Date());
        m_session = createSession();
        m_session.startLearning();
        
        m_session.cardChecked(false, false); // card 0 failed
        m_session.cardChecked(false, false); // card 2 failed
        
        assertEquals(m_card1, m_session.getCurrentCard());
    }
    
    public void testGetFailed()
    {
        Category.raiseCardLevel(m_card0, new Date(), new Date());
        Category.raiseCardLevel(m_card1, new Date(), new Date());
        m_session = createSession();
        m_session.startLearning();
        
        m_session.cardChecked(false, false); // card 0 failed
        Card card = m_session.getCurrentCard();
        m_session.cardChecked(false, false); // card 1 failed
        m_session.cardChecked(true, false);  // card 2 passed
        
        TestHelper.assertSet(new Card[]{card}, m_session.getFailedCards());
    }
    
    public void testGetFailedAndLearnedUnlearnedIsNotRelearned()
    {
        Category.raiseCardLevel(m_card0, new Date(), createDate(1));
        Category.raiseCardLevel(m_card1, new Date(), createDate(1));
        m_session = createSession();
        m_settings.setRetestFailedCards(true);
        m_session.startLearning();
        
        m_session.cardChecked(false, false); // unlearned m_card2 failed
        m_session.cardChecked(true, false);  // unlearned m_card2 passed
        
        TestHelper.assertSet(new Card[]{m_card2}, m_session.getPassedCards());
        TestHelper.assertSet(new Card[]{}, m_session.getFailedCards());
        TestHelper.assertSet(new Card[]{}, m_session.getRelearnedCards());
    }

    public void testGetRelearned2()
    {
        Category.raiseCardLevel(m_card2, new Date(), new Date());
        m_session = createSession();
        m_settings.setRetestFailedCards(true);
        m_session.startLearning();
        
        m_session.cardChecked(true, false);  // card 0 (level 0) passed
        m_session.cardChecked(true, false);  // card 1 (level 0) passed
        m_session.cardChecked(false, false); // card 2 (level 1) failed
        
        m_session.cardChecked(true, false);  // card 2 (level 0) passed
        
        TestHelper.assertSet(new Card[]{m_card0, m_card1}, m_session.getPassedCards());
        TestHelper.assertSet(new Card[]{m_card2}, m_session.getRelearnedCards());
    }
    
    public void testMoveCardDuringSession()
    {
        Category newCategory = new Category("outside of learn category");
        m_category.addCategoryChild(newCategory);
        
        m_settings.setGroupByCategory(true);
        
        m_session = createSession();
        m_session.startLearning();
        
        Card card0 = m_session.getCurrentCard();
        int oldLevel = card0.getLevel();
        
        Category.moveCard(card0, newCategory);
        assertSame(card0, m_session.getCurrentCard());
        
        m_session.cardChecked(true, false);
        assertEquals(oldLevel + 1, card0.getLevel());
        
        assertNotSame(card0, m_session.getCurrentCard());
        m_session.cardChecked(true, false);
        m_session.cardChecked(true, false);
        
        assertTrue(m_session.isQuit());
    }
    
    private static Date createDate(int monthDiff)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthDiff);
        return calendar.getTime();
    }
    
    private DefaultLearnSession createSession()
    {
        return new DefaultLearnSession(m_category, m_settings, 
            new ArrayList<Card>(), true, true, m_provider);
    }
}
