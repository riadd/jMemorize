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

public class LearnSessionLargeTest extends TestCase
{
    private Category                 m_category;
    private LearnSession             m_session;
    private LearnSettings            m_settings;
    private LearnSessionProviderStub m_provider;
    
    private int                      m_unlearnedCount;
    private int                      m_learnedCount;
    private int                      m_expiredCount;
    private int                      m_selectedCount;
    
    private final static Date        LAST_TEST = createDate(-1);
    private final static Date        EXPIRATION = createDate(-5);
    private final static Date        FUTURE = createDate(5);

    protected void setUp() throws Exception
    {
        m_category = new Category("testCategory");

        m_settings = new LearnSettings();
        m_settings.setShuffleRatio(0);
        
        m_provider = new LearnSessionProviderStub();
        
        m_unlearnedCount = 0;
        m_learnedCount = 0;
        m_expiredCount = 0;
        m_selectedCount = 0;
    }
    
    public void testSessionWithLearned()
    {
        addUnlearned(50);
        startSession();
        
        learn(50);
        
        assertSummary(true, 50, 0, 0, 0);
    }
    
    public void testSessionWithLearnedAndFailedAndLearned()
    {
        addUnlearned(50);
        m_settings.setRetestFailedCards(true);
        startSession();
        
        learn(20);
        fail(15);
        learn(30);
        
        assertSummary(true, 50, 0, 0, 0);
    }
    
    public void testSessionWithLearnedAndFailedAndLearned2()
    {
        addUnlearned(50);
        m_settings.setRetestFailedCards(false);
        startSession();
        
        learn(20);
        fail(15);
        learn(15);
        
        assertSummary(true, 35, 0, 0, 0);
    }
    
    public void testSessionWithSkipped()
    {
        addUnlearned(50);
        startSession();
        
        skip(50);
        
        assertSummary(false, 0, 0, 50, 0);
    }
    
    public void testSessionWithSkipped2()
    {
        addUnlearned(50);
        startSession();
        
        skip(80);
        
        assertSummary(false, 0, 0, 50, 0);
    }
    
    public void testSessionWithSkippedAndLearned()
    {
        addUnlearned(50);
        startSession();
        
        skip(50);
        learn(25);
        
        assertSummary(false, 25, 0, 25, 0);
    }
    
    public void testSessionWithFailed()
    {
        addExpired(10, 1);
        addExpired(15, 2);
        addExpired(25, 5);
        startSession();
        
        fail(10);
        
        assertSummary(false, 0, 10, 0, 0);
    }
    
    public void testSessionWithFailed2()
    {
        addExpired(10, 1);
        addExpired(15, 2);
        addExpired(25, 5);
        m_settings.setRetestFailedCards(false);
        startSession();
        
        fail(50);
        
        assertSummary(true, 0, 50, 0, 0);
    }
    
    public void testSessionWithFailed3()
    {
        addExpired(35, 3);
        addExpired(35, 4);
        m_settings.setRetestFailedCards(true);
        startSession();
        
        fail(100);
        
        assertSummary(false, 0, 70, 0, 0);
    }
    
    public void testSessionWithFailedAndLearned()
    {
        addExpired(10, 1);
        addExpired(15, 2);
        addExpired(25, 5);
        startSession();
        
        fail(10);
        learn(30);
        
        assertSummary(false, 30, 10, 0, 0);
    }
    
    public void testSessionWithFailedAndLearned2()
    {
        addExpired(10, 1);
        addExpired(15, 2);
        addExpired(25, 5);
        m_settings.setRetestFailedCards(true);
        startSession();
        
        fail(50);
        learn(30);
        
        assertSummary(false, 0, 20, 0, 30);
    }
    
    public void testSessionWithFailedAndLearned3()
    {
        addUnlearned(5);
        addLearned(100, 7);
        addExpired(20, 2);
        
        m_settings.setShuffleRatio(0);
        m_settings.setRetestFailedCards(true);
        startSession();
        
        fail(25);
        learn(25);
        
        assertSummary(true, 5, 0, 0, 20);
    }

    private static Date createDate(int monthDiff)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthDiff);
        return calendar.getTime();
    }
    
    private void startSession()
    {
        m_session = new DefaultLearnSession(m_category, m_settings, 
            new ArrayList<Card>(), true, true, m_provider);
        
        m_session.startLearning();
    }
    
    private void assertSummary(boolean isEnded, int passed, int failed, 
        int skipped, int relearned)
    {
        String actual = createSummaryString(
            m_provider.isSessionEnded(), 
            m_session.getPassedCards().size(),  
            m_session.getFailedCards().size(),
            m_session.getSkippedCards().size(),
            m_session.getRelearnedCards().size());
        
        String expected = createSummaryString(
            isEnded, 
            passed, 
            failed, 
            skipped, 
            relearned);
        
        assertEquals(expected, actual);
    }
    
    private String createSummaryString(boolean isEnded, int passed, int failed, 
        int skipped, int relearned)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(isEnded ? "+ " : "- ");
        
        sb.append(String.format("% 3d", passed));
        sb.append("p ");
        
        sb.append(String.format("% 3d", failed));
        sb.append("f ");
        
        sb.append(String.format("% 3d", skipped));
        sb.append("s ");
        
        sb.append(String.format("% 3d", relearned));
        sb.append("r");
        
        return sb.toString();
    }
    
    private void learn(int count)
    {
        for (int i = 0; i < count; i++)
            m_session.cardChecked(true, false);
    }
    
    private void fail(int count)
    {
        for (int i = 0; i < count; i++)
            m_session.cardChecked(false, false);
    }
    
    private void skip(int count)
    {
        for (int i = 0; i < count; i++)
            m_session.cardSkipped();
    }
    
    private void addUnlearned(int count)
    {
        for (int i = 0; i < count; i++)
        {
            Card card = new Card("unlearned"+m_unlearnedCount, "back");
            m_category.addCard(card);
            
            m_unlearnedCount++;
        }
    }

    private void addLearned(int count, int level)
    {
        for (int i = 0; i < count; i++)
        {
            Card card = new Card("learned"+m_learnedCount, "back");
            card.setDateTested(LAST_TEST);
            card.setDateExpired(FUTURE);
            
            m_category.addCard(card, level);
            m_learnedCount++;
        }
    }
    
    private void addExpired(int count, int level)
    {
        for (int i = 0; i < count; i++)
        {
            Card card = new Card("expired"+m_expiredCount, "back");
            card.setDateTested(LAST_TEST);
            card.setDateExpired(EXPIRATION);
            
            m_category.addCard(card, level);
            m_expiredCount++;
        }
    }
}
