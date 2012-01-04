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
package jmemorize.core.learn;

import java.util.Date;
import java.util.List;
import java.util.Set;

import jmemorize.core.Card;
import jmemorize.core.Category;

/**
 * A LearnSession is where the learning actually happens. It should be
 * instantiated with a LearnSettings object that defines how the session should 
 * work.
 * 
 * See {@link DefaultLearnSession} for the default implementation.
 * 
 * @author djemili
 */
public interface LearnSession
{
    /**
     * Observes which card is currently fetched by the learn session.
     */
    public interface LearnCardObserver
    {
        public void nextCardFetched(Card card, boolean flippedMode);
    }

    /**
     * Starts the learn session by fetching the first card. The lesson doesn't
     * start automatically so that observers have the chance to attach
     * themselves before the first card is fetched.
     */
    public void startLearning();

    /**
     * End the learn session. This results in a call to the
     * {@link LearnSessionProvider}, which then notifies all its
     * {@link LearnSessionObserver}.
     */
    public void endLearning();

    /**
     * @return the settings which are used for this learn session.
     */
    public LearnSettings getSettings();

    /**
     * @return the date when this session started.
     */
    public Date getStart();

    /**
     * @return the date this session ended or <code>null</code> if it hasn't
     * ended yet.
     */
    public Date getEnd();

    /**
     * @return the card that is currently checked/shown.
     */
    public Card getCurrentCard();

    /**
     * @return all cards that are left to be learned in this session.
     */
    public Set<Card> getCardsLeft();

    /** 
     * @return the category (subset of cards) that is currently being learned.
     */
    public Category getCategory();

    /**
     * Is fired when the current card was checked.
     * 
     * @param passed <code>true</code> if the test was passed.
     * <code>false</code> if it failed.
     * @param shownFlipped <code>true</code> if the card was shown in flipped
     * mode. <code>false</code> if shown in regular mode.
     */
    public void cardChecked(boolean passed, boolean shownFlipped);

    /**
     * Is fired when if the current is skipped.
     */
    public void cardSkipped();

    /**
     * Is fired when the optional time limit passed up.
     */
    public void onTimer();

    /**
     * @return the list of all cards that were checked in this learn session
     * until now. The list only contains unique cards, that is if a card was
     * checked more then once, it appears at the position of its last check. A
     * card is considered checked, when it is shown.  This is a list so that 
     * the cards can be enumerated in the sequence they were shown. 
     */
    public List<Card> getCheckedCards();
    
    /**
     * A card is passed when it came up in the session and was answered
     * successfully. It can't have been failed in the session before. Otherwise
     * it is called relearned (see {@link #getRelearnedCards()}.
     * 
     * @return the list of all cards that have been passed in this learn session
     * until now.
     */
    public Set<Card> getPassedCards();

    /**    
    * Return the number of cards learned, which is equal to passed.size() +
    * relearned.size()
    * 
    * @return the number of cards learned so far.
    */
   public int getNCardsLearned();   
    
    /**
     * A card is partially passed when it came up in the session and was
     * answered successfully, but has not been fully learned yet.
     * 
     * @return the number of active cards in this learn session that have been
     * partially learned.
     */
    public int getNCardsPartiallyLearned();
    
    /**
     * A card is failed when it wasn't unlearned at session start, came up in
     * the session and the user failed at answering it.
     * 
     * @return the list of all cards that have been failed and not relearned in
     * this learn session until now.
     */
    public Set<Card> getFailedCards();
    
    /**
     * A card is skipped when it has been skipped at every of its apperances in
     * this session. Otherwise it automatically falls into the the learned,
     * failed or relearned category.
     * 
     * @return the list of all cards that have been skipped in this learn
     * session until now.
     */
    public Set<Card> getSkippedCards();
    
    /**
     * A card is relearned if it was first failed and then learned in the
     * <i>same</i> session.
     * 
     * @return the list of all cards that have been relearned in this learn
     * session until now.
     */
    public Set<Card> getRelearnedCards();
    
    /**
     * @return <code>true</code> if this session should be considered
     * relevant. A session is relevant when at least one card was passed or
     * failed.
     */
    public boolean isRelevant();
    
    /**
     * @return <code>true</code> when session has ended.
     */
    public boolean isQuit();
    
    /**
     * Adds an learn card observer.
     */
    public void addObserver(LearnCardObserver observer);

    /**
     * Removes an learn card observer.
     */
    public void removeObserver(LearnCardObserver observer);
}
