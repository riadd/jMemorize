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

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.learn.DefaultLearnSession;
import jmemorize.core.learn.LearnSettings;
import jmemorize.core.test.stubs.LearnSessionProviderStub;
import junit.framework.TestCase;

public class LearnSessionShufflingTest extends TestCase
{
    private Category                 m_category;
    private DefaultLearnSession      m_session;
    private LearnSettings            m_settings;
    private LearnSessionProviderStub m_provider;
    
    protected void setUp() throws Exception
    {
        m_category = new Category("testCategory");
        m_settings = new LearnSettings();
        m_provider = new LearnSessionProviderStub();
    }
    
    protected void tearDown() throws Exception
    {
    }
    
    public void testCardOrderWithNoEntropy()
    {
        createCards(1, 0, 1, 0, 2);
        m_settings.setShuffleRatio(0);
        
        m_session = createSession();
        m_session.startLearning();
        
        assertLevelOfCurrentCard(0);
        
        m_session.cardChecked(true, false);
        assertLevelOfCurrentCard(2);
        
        m_session.cardChecked(true, false);
        assertLevelOfCurrentCard(4);
        
        m_session.cardChecked(true, false);
        assertLevelOfCurrentCard(4);
    }
    
    public void testCardOrderWithPartialEntropy()
    {
        createCards(2, 2, 2, 2, 2, 2, 2, 2, 2, 2);
        m_settings.setShuffleRatio(0.2f);

        m_session = createSession();
        m_session.startLearning();

        assertShuffledCount(16, 4);
    }
    
    public void testCardOrderWithPartialEntropy2()
    {
        createCards(1, 2, 3, 4, 5, 6, 7, 8, 9);
        m_settings.setShuffleRatio(0.6f);

        m_session = createSession();
        m_session.startLearning();

        assertShuffledCount(18, 27);
    }
    
    public void testCardOrderWithFullEntropy()
    {
        createCards(1, 2, 3, 4, 5);
        m_settings.setShuffleRatio(1.0f);

        m_session = createSession();
        m_session.startLearning();

        assertShuffledCount(0, 15);
    }
    
    private void assertShuffledCount(int actualUnshuffled, int actualShuffled)
    {
        int unshuffledLevels = 0;
        int shuffledLevels = 0;

        for (int i = 0; i < actualUnshuffled + actualShuffled; i++)
        {
            int cardLevel = m_session.getCurrentCard().getLevel();
            int shuffleLevel = m_session.getCurrentShuffleLevel();
            
            if (cardLevel == shuffleLevel)
                unshuffledLevels++;
            else
                shuffledLevels++;

            m_session.cardChecked(true, false);
        }
        
        assertEquals(actualShuffled, shuffledLevels);
        assertEquals(actualUnshuffled, unshuffledLevels);
    }
    
    private void assertLevelOfCurrentCard(int level)
    {
        assertEquals(level, m_session.getCurrentCard().getLevel());
    }
    
    private DefaultLearnSession createSession()
    {
        return new DefaultLearnSession(m_category, m_settings, 
            new ArrayList<Card>(), true, true, m_provider);
    }
    
    private void createCards(int ... levels)
    {
        for (int i = 0; i < levels.length; i++)
        {
            for (int j = 0; j < levels[i]; j++)
            {
                Card card = new Card("testFront"+i, "testFlip"+i);
                m_category.addCard(card, i);
            }
        }
    }
}
