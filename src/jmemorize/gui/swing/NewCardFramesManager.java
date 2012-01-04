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

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;
import jmemorize.core.Events;
import jmemorize.core.Lesson;
import jmemorize.core.LessonObserver;
import jmemorize.core.Main;
import jmemorize.core.Settings;
import jmemorize.gui.swing.frames.EditCardFrame;
import jmemorize.gui.swing.frames.NewCardFrame;

/**
 * This class manages all instances of new-card frames.
 * 
 * @author  djemili
 */
public class NewCardFramesManager implements LessonObserver, CategoryObserver
{
    private static final String FRAME_ID        = "newcard";

    private List<NewCardFrame>  m_newCardFrames = new LinkedList<NewCardFrame>();
    private List<Card>          m_createdCards  = new ArrayList<Card>();

    public NewCardFramesManager()
    {
        Main main = Main.getInstance();
        main.getLesson().getRootCategory().addObserver(this);
        main.addLessonObserver(this);
    }
    
    public void addNewCardWindow(Category category)
    {
        if (m_newCardFrames.isEmpty())
        {
            NewCardFrame frame = new NewCardFrame(this, category);
            Settings.loadFrameState(frame, FRAME_ID);
            frame.setVisible(true);
            
            m_newCardFrames.add(0, frame); // insert at head
        }
        else
        {
            addNewCardWindow((NewCardFrame)m_newCardFrames.get(0), category);
        }
    }
    
    public void addNewCardWindow(NewCardFrame father, Category category)
    {
        NewCardFrame frame = new NewCardFrame(this, category);
        frame.setLocation(new Point(father.getX() + 25, father.getY() + 25));
        frame.setSize(father.getSize());
        frame.setVisible(true);
        
        // insert at head
        m_newCardFrames.add(0, frame);
    }
    
    public void editRecentlyCreatedCards()
    {
        if (!m_createdCards.isEmpty())
        {
            EditCardFrame.getInstance().showCard(
                (Card)m_createdCards.get(m_createdCards.size() - 1), 
                m_createdCards,
                Main.getInstance().getLesson().getRootCategory());
        }
    }
    
    /**
     * @return <code>true</code> if frames were closed. <code>false</code>
     * if operation was canceld by user.
     */
    public boolean closeAllFrames()
    {
        List<NewCardFrame> frames = new ArrayList<NewCardFrame>(m_newCardFrames);
        for (NewCardFrame frame : frames)
        {
            if (!frame.close())
            {
                return false; // user canceled closing of this new card frame
            }
        }
        
        return true;
    }
    
    public void newCardCreated(Card card)
    {
        m_createdCards.add(card);
    }
    
    public void newCardFrameClosed(NewCardFrame frame)
    {
        m_newCardFrames.remove(frame);
        Settings.storeFrameState(frame, FRAME_ID);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonLoaded(Lesson lesson)
    {
        lesson.getRootCategory().addObserver(this);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonClosed(Lesson lesson)
    {
        lesson.getRootCategory().removeObserver(this);
        m_createdCards.clear();
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonModified(Lesson lesson)
    {
        // ignore
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonSaved(Lesson lesson)
    {
        // ignore
    }

    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        if (type == Events.REMOVED_EVENT)
        {
            m_createdCards.remove(card);
        }
    }

    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        // ignore
    }
}
