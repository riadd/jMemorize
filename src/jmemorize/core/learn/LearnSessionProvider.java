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
package jmemorize.core.learn;

import java.util.List;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.Main;

/**
 * A LearnSessionProvider manages learn sessions. Use it to start new learn
 * sessions and for observing session starts and ends.
 * 
 * Use this interface by calling <code>Main.getInstance()</code>.
 * {@link Main} implements this interface.
 * 
 * @author djemili
 */
public interface LearnSessionProvider
{
    /**
     * Creates a new learn session.
     * 
     * @param settings the settings that should be used in this session.
     * @param the cards that were selected in the main card table when starting
     * the learn session (these will be learned if learnUnlearned and 
     * learnExpired are false).
     * @param category the base category that holds the cards for this session.
     * @param learnUnlearned <code>true</code> if unlearned cards should
     * appear in this session. <code>false</code> otherwise.
     * @param learnExpired <code>true</code> if expired cards should appear in
     * this session. <code>false</code> otherwise.
     */
    public void startLearnSession(LearnSettings settings, List<Card> selectedCards, 
        Category category, boolean learnUnlearned, boolean learnExpired);
    
    /**
     * This method should only be called by the learn session itself to notify
     * its provider to notify all observers about the session end.
     */
    public void sessionEnded(LearnSession session);
    
    /**
     * @return <code>true</code> if there is currently are running learn
     * session. <code>false</code> otherwise.
     */
    public boolean isSessionRunning();
    
    /**
     * Add a learn session observer.
     */
    public void addLearnSessionObserver(LearnSessionObserver observer);
    
    /**
     * Removes a learn session observer
     */
    public void removeLearnSessionObserver(LearnSessionObserver observer);
}
