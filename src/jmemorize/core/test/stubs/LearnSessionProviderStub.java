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
package jmemorize.core.test.stubs;

import java.util.List;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSessionObserver;
import jmemorize.core.learn.LearnSessionProvider;
import jmemorize.core.learn.LearnSettings;

public class LearnSessionProviderStub implements LearnSessionProvider
{
    private boolean m_sessionEnded = false;

    public void startLearnSession(LearnSettings settings, 
        List<Card> selectedCards, Category category,
        boolean learnUnlearned, boolean learnExpired)
    {
    }

    public void sessionEnded(LearnSession session)
    {
        m_sessionEnded = true;
    }

    public void addLearnSessionObserver(LearnSessionObserver observer)
    {
    }

    public void removeLearnSessionObserver(LearnSessionObserver observer)
    {
    }

    public boolean isSessionEnded()
    {
        return m_sessionEnded;
    }

    public boolean isSessionRunning()
    {
        return false;
    }
}
