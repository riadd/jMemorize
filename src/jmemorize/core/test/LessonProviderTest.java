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
package jmemorize.core.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jmemorize.core.Card;
import jmemorize.core.Lesson;
import jmemorize.core.LessonObserver;
import jmemorize.core.LessonProvider;
import jmemorize.core.Main;
import junit.framework.TestCase;

public class LessonProviderTest extends TestCase implements LessonObserver
{
    private LessonProvider m_lessonProvider;
    private StringBuffer   m_log;

    protected void setUp() throws Exception
    {
        m_lessonProvider = new Main();
        m_lessonProvider.addLessonObserver(this);

        m_log = new StringBuffer();
    }
    
    public void testLessonNewEvent()
    {
        m_lessonProvider.createNewLesson();
        assertEquals("loaded ", m_log.toString());
    }

    public void testLessonLoadedEvent() throws IOException
    {
        m_lessonProvider.loadLesson(new File("test/fixtures/simple_de.jml"));
        assertEquals("loaded ", m_log.toString());
    }
    
    public void testLessonLoadedCardsAlwaysHaveExpiration() throws IOException
    {
        m_lessonProvider.loadLesson(new File("test/fixtures/no_expiration.jml"));
        Lesson lesson = m_lessonProvider.getLesson();
        List<Card> cards = lesson.getRootCategory().getCards();
        
        for (Card card : cards)
        {
            if (card.getLevel() > 0)
                assertNotNull(card.getDateExpired());
            else
                assertNull(card.getDateExpired());
        }
    }
    
    public void testLessonLoadedClosedNewEvents() throws IOException
    {
        m_lessonProvider.loadLesson(new File("test/fixtures/simple_de.jml"));
        m_lessonProvider.createNewLesson();
        assertEquals("loaded closed loaded ", m_log.toString());
        
        // TODO also check lesson param
    }
    
    public void testLessonLoadedClosedLoadEvents() throws IOException
    {
        m_lessonProvider.loadLesson(new File("test/fixtures/simple_de.jml"));
        m_lessonProvider.loadLesson(new File("test/fixtures/test.jml"));
        assertEquals("loaded closed loaded ", m_log.toString());
        
        // TODO also check lesson param
    }
    
    public void testLessonSavedEvent() throws Exception
    {
        m_lessonProvider.loadLesson(
            new File("test/fixtures/simple_de.jml"));
        
        Lesson lesson = m_lessonProvider.getLesson();
        m_lessonProvider.saveLesson(lesson, new File("./test.jml"));
        
        assertEquals("loaded saved ", m_log.toString());
    }
    
    public void testLessonModifiedEvent() throws Exception
    {
        m_lessonProvider.loadLesson(
            new File("test/fixtures/simple_de.jml"));
        
        Lesson lesson = m_lessonProvider.getLesson();
        lesson.getRootCategory().addCard(new Card("front", "flip"));
        
        assertEquals("loaded modified ", m_log.toString());
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonLoaded(Lesson lesson)
    {
        m_log.append("loaded ");
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonModified(Lesson lesson)
    {
        m_log.append("modified ");
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonSaved(Lesson lesson)
    {
        m_log.append("saved ");
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonClosed(Lesson lesson)
    {
        m_log.append("closed ");
    }
}
