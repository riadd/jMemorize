/*
 * jMemorize - Learning made easy (and fun) - A Leitner flashcards tool
 * Copyright(C) 2004-2008 Riad Djemili
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;
import jmemorize.util.EquivalenceClassSet;

/**
 * A learn session is instantiated with a LearnSettings object which defines the
 * rules which the session should handle cards.
 * 
 * The workflow for this class is as following:
 * 
 * <ol>
 * <li>Learn Session fetches a card according to its LearnSettings. To get
 * notified of this card, use the {@link LearnCardObserver}.</li>
 * <li>The learn session waits for a call to either {{@link #cardChecked(boolean, 
 * boolean)} or {@link #cardSkipped()}. This makes the learn session perform some 
 * action on the card.</li>
 * <li>This action results in some category event, which the learn session gets
 * notified of, because it attached itself also as a category observer to the
 * category that is to be learned. This category event results in either
 * fetching the next card (see above) or all call to the
 * {@link LearnSessionProvider} to inform it that the session has ended. The
 * {@link LearnSessionProvider} then notifies all of its
 * {@link LearnSessionObserver}.</li>
 * </ol>
 * 
 * Note that when a card is neither learned or skipped, but i.e. deleted or
 * resetted, step 2 is skipped and step 3 comes into play directly.
 * 
 * The order of a card in the learn session depends on the shuffle and category order settings:
 * [Shuffle: Off, Category Order: Off] Deck, Last test date
 * [Shuffle: Off, Category Order: On ] Category, Deck, Last test date
 * [Shuffle: On,  Category Order: Off] Deck, Random number
 * [Shuffle: On,  Category Order: On ] Category, Deck, Random number
 * 
 * @author djemili
 */
public class DefaultLearnSession implements CategoryObserver, LearnSession
{
    /**
     * A Comparator that is used for sorting cards in learn sessions.
     * This is used to sort the cards into equivalence classes, from
     * which the next card will be drawn randomly.
     */
    private class CardComparator implements Comparator<CardInfo>
    {
        private Map<Category, Integer> m_categoryGroupOrder;

        public CardComparator(Map<Category, Integer> categoryGroupOrder)
        {
            m_categoryGroupOrder = categoryGroupOrder;
        }
        
        /*
         * @see java.util.Comparator
         */
        public int compare(CardInfo card0, CardInfo card1)
        {
            if (card0.getLevel() < card1.getLevel() )
            {
                return -1;
            }
            else if (card0.getLevel() > card1.getLevel() )
            {
                return 1;
            }
            // else card0.getLevel() == card1.getLevel()
            
            if (m_settings.isGroupByCategory())
            {
                Integer cat0 = m_categoryGroupOrder.get(card0.getCategory());
                Integer cat1 = m_categoryGroupOrder.get(card1.getCategory());
                
                if (cat0 != null && cat1 != null)
                {
                    if (cat0.intValue() <  cat1.intValue())
                    {
                        return -1;
                    }
                    else if (cat0.intValue() > cat1.intValue())
                    {
                        return 1;
                    }
                }
                
            }
            
            return 0;
            
//            if (m_bIgnoreDate) 
//            {
//                return 0;
//            }
//            
//            Date date0 = card0.getDateTouched();
//            Date date1 = card1.getDateTouched();
//            
//            if (date0.equals(date1))
//            {
//                return 0;
//            }
//            
//            return (date0.before(date1) ? -1 : 1);
        }
    }
    
    /**
     * This class is a wrapper for a card. It allows to associate additional
     * data to a card, that is only relevant during a single specifc learn
     * session.
     */
    private class CardInfo
    {
        private Card m_card;
        
        /**
         * For learning this variable should be used instead of the real level
         * of the card. This allows for some special shuffling techniques.
         */
        private int  m_level;

        public CardInfo(Card card)
        {
            m_card = card;
            m_level = card.getLevel();
        }

        public Card getCard()
        {
            return m_card;
        }

        public int getLevel()
        {
            return m_level;
        }

        public void setLevel(int level)
        {
            m_level = level;
        }
        
        public Category getCategory()
        {
            return m_card.getCategory();
        }
        
        @Override
        public String toString()
        {
            return "CardInfo("+m_card.toString()+")";
        }
    }
    
    // learn session settings
    private Category                       m_category;
    
    // the root category of the lesson
    private Category                       m_rootCategory;
    private LearnSettings                  m_settings;
    private LearnSessionProvider           m_provider;

    // current learn session state
    private boolean                        m_quit;
    private boolean                        m_learningStarted = false;
    private CardInfo                       m_currentCardInfo;

    // The cards in the set are partitioned into the following exclusive 
    // subsets. Cards move from reserve to active, from active to reserve or 
    // learned, but do not move after reaching learned.
    private EquivalenceClassSet<CardInfo>  m_cardsActive;
    private EquivalenceClassSet<CardInfo>  m_cardsReserve;
    
    // the list of all cards that have been checked in the order last seen. Does 
    // not include cards that were skipped and never passed/failed.
    private List<Card>                     m_cardsChecked = new ArrayList<Card>();
    private Set<Card>                      m_cardsLearned = new HashSet<Card>();
    private Map<Card, CardInfo>            m_cardsInfoMap = new HashMap<Card, CardInfo>();

    // NOTE - m_cardsLearned is the set of all cards successfully learned
    // this session, which is the union of "passed" and "relearned".
    // We don't track of those two categories internally.
    // "Passed" = Learned - EverFailed
    // "ReLearned" = Learned intersect EverFailed
    // "Failed" = EverFailed - Learned
    
    // These sets are non exclusive markers that indicate the status of a card
    // Note that these are Sets not Lists.  Two reasons:
    //  1)  The order is not important.
    //  2)  Lookup efficiency is better.
    
    // Cards do not get removed from the EverFailed list.
    private Set<Card>            m_cardsEverFailed  = new HashSet<Card>();
    private Set<Card>            m_cardsSkipped     = new HashSet<Card>();

    // NOTE - this is only the *active* cards which are partially learned -
    // there may be others in the reserve set.
    private Set<Card>            m_cardsActivePartiallyLearned = new HashSet<Card>();
     
    // Further invariants:
    //   - Learned intsersection Skipped = NULL
    //   - partialPassed intersection Learned = NULL

    // etc
    private Random               m_rand             = new Random();
    private List<LearnCardObserver> m_cardObservers = new LinkedList<LearnCardObserver>();

    private Date                 m_start;
    private Date                 m_end;
    
    private Logger               m_logger = Logger.getLogger("jmemorize.session");
    
    /**
     * Creates a new learn session. Use {@link #startLearning()} to start the
     * learning.
     */
    public DefaultLearnSession(Category category, 
        LearnSettings settings, List<Card> selectedCards, 
        boolean learnUnlearned, boolean learnExpired, 
        LearnSessionProvider provider)
    {
        m_rootCategory = category;
        while (m_rootCategory.getParent() != null)
            m_rootCategory = m_rootCategory.getParent();

        m_category = category;
        m_rootCategory.addObserver(this);
        
        m_settings = settings;
        m_provider = provider;
        
        setupLogger();
        
        Map<Category, Integer> order = m_settings.isGroupByCategory() ? 
            createCategoryGroupOrder() : null;
            
        m_cardsActive = fetchCards(selectedCards, learnUnlearned, learnExpired, order);
        m_cardsReserve = new EquivalenceClassSet<CardInfo>(m_cardsActive.getComparator());
        // Note that EquivalenceClassSets always default to shuffle mode (any card
        // from the current class may be chosen next.)  This is what we want here. 
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public void startLearning()
    {
        if (m_learningStarted) 
            throw new IllegalStateException("startLearning should only happen once!");
        
        m_learningStarted = true;
        m_start = new Date();
        
        // move all cards to cardsPastLimit, then fetch exactly as many as needed
        if (m_settings.isCardLimitEnabled() && 
            m_cardsActive.size() > m_settings.getCardLimit()) 
        {
            m_cardsReserve = m_cardsActive;
            m_cardsActive = m_cardsReserve.partition(m_settings.getCardLimit());
        }
        
        gotoNextCard();
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public void endLearning()
    {
        m_end = new Date();
        
        m_rootCategory.removeObserver(this);
        m_provider.sessionEnded(this);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public LearnSettings getSettings()
    {
        return m_settings;
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public Date getStart()
    {
        return m_start;
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public Date getEnd()
    {
        return m_end;
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public Card getCurrentCard()
    {
        return m_currentCardInfo.getCard();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public Set<Card> getCardsLeft()
    {
        return Collections.unmodifiableSet(toCardSet(m_cardsActive));
    }
        
    public int getNCardsPartiallyLearned() 
    {
        return m_cardsActivePartiallyLearned.size();     
    }
    
    public int getNCardsLearned() 
    {
        return m_cardsLearned.size();     
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public Category getCategory()
    {
        return m_category;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public void cardChecked(boolean passed, boolean shownFlipped)
    {
        Card currentCard = m_currentCardInfo.getCard();
        
        m_logger.fine(String.format("cardChecked: %b %s", 
            passed, currentCard.getFrontSide().getText()));
        
        assert !m_cardsLearned.contains(currentCard);
        assert !m_cardsReserve.contains(m_currentCardInfo);
        assert m_cardsActive.contains(m_currentCardInfo);
        
        m_cardsSkipped.remove(currentCard);
        m_cardsActivePartiallyLearned.remove(currentCard);

        if (passed)
        {
            boolean raiseLevel = true;

            // If we are using the 'Check both sides' strategy, we need to do
            // different calculations to work out whether the card is ready to
            // be raised a level
            if (m_settings.getSidesMode() == LearnSettings.SIDES_BOTH)
            {
                // Work out how much of it is learned
                int frontAmountLearned = currentCard.getLearnedAmount(true);
                int backAmountLearned = currentCard.getLearnedAmount(false);
                
                if (shownFlipped)
                    backAmountLearned++;
                else
                    frontAmountLearned++;
                
                if ((frontAmountLearned < m_settings.getAmountToTest(true))
                    || (backAmountLearned < m_settings.getAmountToTest(false)))
                {
                    // It's partially learned.
                    //  increment the amount it has been learned by
                    m_cardsActivePartiallyLearned.add(currentCard);
                    m_logger.fine("...partially passed.");
                    raiseLevel = false;

                    // incremenLearnedAmount fires a DECK_EVENT
                    currentCard.incrementLearnedAmount(!shownFlipped);
                }
            }

            if (raiseLevel)
            {
                m_logger.fine("...passed.");
                raiseCardLevel(currentCard);
            }
        }
        else
        {
            // TODO should this be renamed since currently only cards with 
            // level > 0 are called failed in session summaries
            if (!m_settings.isRetestFailedCards())
                m_cardsActive.remove(m_currentCardInfo);
        
            if (currentCard.getLevel() > 0)
            {
                m_cardsEverFailed.add(currentCard);
                m_logger.fine("...failed.");
            }
            
            /* NOTE - If the card is still active, the card may be in the wrong
             * equivalence class (i.e. the set is in an inconsistent state).
             * We can't fix it until after the reset, *but* the
             * resetCardLevel() method fires the event which results in the
             * observers reacting (checking for end of session, getting the 
             * next card, etc).  The card's equivalence class will be wrong,
             * but this should not be a problem for gotoNextCard.
             * We reset the equivalence class as soon as possible.
             */
            Category.resetCardLevel(currentCard, m_start);
            
            m_currentCardInfo.setLevel(currentCard.getLevel());
            m_cardsActive.resetEquivalenceClass(m_currentCardInfo);
        }
        
        m_logger.fine("...Cards remaining: " + m_cardsActive.size());
        m_logger.fine("...Cards partially learned: " + getNCardsPartiallyLearned());
        m_logger.fine("...num failed= " + m_cardsEverFailed.size());

        // note that raising/reseting card level will be noticed by onCardEvent.
        // program flow continues there.
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public void cardSkipped()
    {
        Card currentCard = m_currentCardInfo.getCard();
        
        // Note that we do not remove the card from m_cardsChecked.
        m_logger.fine("cardSkipped: " + currentCard.getFrontSide());
        
        assert !m_cardsLearned.contains(currentCard);
        assert !m_cardsReserve.contains(m_currentCardInfo);
        assert m_cardsActive.contains(m_currentCardInfo);

        m_cardsSkipped.add(currentCard);
        
        if (m_cardsReserve != null && m_cardsReserve.size() > 0) 
        {
            m_cardsActivePartiallyLearned.remove(m_currentCardInfo);
            
            CardInfo replacementCardInfo = m_cardsReserve.loopIterator().next();
            Card replacementCard = replacementCardInfo.getCard();
            
            if (replacementCard.getLearnedAmount(true) > 0 || 
                replacementCard.getLearnedAmount(false) > 0) 
            {
                m_cardsActivePartiallyLearned.add(replacementCard);                
            }
            
            m_cardsActive.add(replacementCardInfo);
            m_cardsReserve.remove(replacementCardInfo);
            m_cardsReserve.addExpired(m_currentCardInfo);
            m_cardsActive.remove(m_currentCardInfo);
            
            m_logger.fine("Moving to reserve: " + currentCard.getFrontSide());
            m_logger.fine("Moving to active: " + replacementCard.getFrontSide());
        }
        
        m_logger.fine("...cards remaining: " + m_cardsActive.size());
        
        Category.reappendCard(currentCard);
        
        // program flow continues in onCardEvent
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public Set<Card> getPassedCards()
    {
        // "passed" = Learned and not Failed
        Set<Card> tempSet = new HashSet<Card>(m_cardsLearned);
        tempSet.removeAll(m_cardsEverFailed);
        return Collections.unmodifiableSet(tempSet);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public Set<Card> getFailedCards()
    {
        Set<Card> tempSet = new HashSet<Card>(m_cardsEverFailed);
        tempSet.removeAll(m_cardsLearned);
        return Collections.unmodifiableSet(tempSet);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public Set<Card> getSkippedCards()
    {
        return Collections.unmodifiableSet(m_cardsSkipped);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public Set<Card> getRelearnedCards()
    {
        Set<Card> tempSet = new HashSet<Card>(m_cardsEverFailed);
        tempSet.retainAll(m_cardsLearned);
        return Collections.unmodifiableSet(tempSet);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public void onTimer()
    {
        m_quit = true;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        CardInfo cardInfo = getCardInfo(card);
        
        if (cardInfo == null) // this happens when a new card is created; ignore
            return;
        
        switch (type)
        {
        case ADDED_EVENT:
            // if there is a reserve and we have enough cards, add to the reserve
            int allCards = m_cardsLearned.size() + m_cardsActive.size();
            if (m_settings.isCardLimitEnabled() && allCards >= m_settings.getCardLimit())
            {
                m_cardsReserve.add(cardInfo);
            }
            else 
            {
                m_cardsActive.add(cardInfo);                    
            }
            break;
            
        case REMOVED_EVENT:
            // remove it from all sets
            m_cardsActive.remove(cardInfo);
            m_cardsReserve.remove(cardInfo);
            m_cardsLearned.remove(card);
            m_cardsActivePartiallyLearned.remove(card);
            m_cardsEverFailed.remove(card);
            m_cardsSkipped.remove(card);
            
            if (cardInfo == m_currentCardInfo)
            {
                gotoNextCard();
            }
            
            m_cardsChecked.remove(card);
            break;
            
        case DECK_EVENT:
            if (cardInfo == m_currentCardInfo)
            {
                gotoNextCard();
            }
            
            // TODO currently, resetting a learned card does not put it back in the
            // active set.  Should it?
            break;
        }
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        // no category events should occure while learning.
        // ignore
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public List<Card> getCheckedCards()
    {
        // TODO the meaning of this collides with the naming of checkCard(..)
        // because it also includes skipped cards
        return Collections.unmodifiableList(m_cardsChecked);
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public boolean isRelevant()
    {
        return m_cardsEverFailed.size() > 0 || m_cardsLearned.size() > 0;
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public void addObserver(LearnCardObserver observer)
    {
        m_cardObservers.add(observer);
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LearnSession
     */
    public void removeObserver(LearnCardObserver observer)
    {
        m_cardObservers.remove(observer);
    }
    
    /**
     * Note that this method is specialy for DefaultLearnSession and not part of
     * the LearnSession interface.
     * 
     * @return the shuffled 'fake' card level that is currently used for the
     * card.
     */
    public int getCurrentShuffleLevel()
    {
        return m_currentCardInfo.getLevel();
    }
    
    public boolean isQuit()
    {
        boolean noCardsLeft = m_cardsActive.size() == 0;
        boolean limitReached = m_settings.isCardLimitEnabled() && 
               m_cardsLearned.size() >= m_settings.getCardLimit();
        
        return m_quit || noCardsLeft || limitReached;
    }

    private void raiseCardLevel(Card card)
    {    
        CardInfo cardInfo = getCardInfo(card);
        
        assert cardInfo != null;
        
        m_cardsActive.remove(cardInfo);
        m_cardsLearned.add(card);
        
        int level = card.getLevel();
        Date expiration = m_settings.getExpirationDate(m_start, level);
        Category.raiseCardLevel(card, m_start, expiration);
    }
 
    private void gotoNextCard()
    {
        // check for end condition
        if (isQuit())
        {
            endLearning();
        }
        else
        {
            CardInfo lastCardInfo = m_currentCardInfo;
            
            m_currentCardInfo = m_cardsActive.loopIterator().next();
            
            // prevent the same card from occuring twice in a row
            if (m_cardsActive.size() > 1 && lastCardInfo == m_currentCardInfo)
            {
                m_currentCardInfo = m_cardsActive.loopIterator().next();
            }
            
            // add the new card to the checked list now so it can be edited as part of the set.
            // m_cardsChecked is ordered by last viewing, so remove prior to add 
            Card currentCard = m_currentCardInfo.getCard();
            
            m_cardsChecked.remove(currentCard);
            m_cardsChecked.add(currentCard);
            
            boolean flippedMode = checkIfFlipped();
            for (LearnCardObserver observer : m_cardObservers)
            {
                observer.nextCardFetched(currentCard, flippedMode);
            }
        }
    }

    /**
     * Checks whether the card should be displayed as flipped or not.
     * 
     * @return <code>true</code> if the card should be flipped.
     * <code>false</code> otherwise.
     */
    private boolean checkIfFlipped()
    {
        if (m_settings.getSidesMode() == LearnSettings.SIDES_RANDOM)
        {
            return m_rand.nextInt(2) == 1; // 50% chance
        }
        else if (m_settings.getSidesMode() == LearnSettings.SIDES_BOTH)
        {
            // allocate the side proportionally to the amount they have left to learn
            Card currentCard = m_currentCardInfo.getCard();
            
            int timesToLearnFront = 
                m_settings.getAmountToTest(true) - 
                currentCard.getLearnedAmount(true);
            
            int timesToLearnBack = 
                m_settings.getAmountToTest(false) - 
                currentCard.getLearnedAmount(false);
            
            if (timesToLearnBack < 0)
                timesToLearnBack = 0;
            
            if (timesToLearnFront < 0)
                timesToLearnFront = 0;
            
            if (timesToLearnFront + timesToLearnBack == 0)
                return false;
            
            int rand = m_rand.nextInt(timesToLearnFront + timesToLearnBack);
            return rand < timesToLearnBack;
        }
        else
        {
            return (m_settings.getSidesMode() == LearnSettings.SIDES_FLIPPED);
        }
    }
    
    /**
     * Fetch the cards that should be learned in this session according to given
     * params.
     */
    private EquivalenceClassSet<CardInfo> fetchCards(List<Card> selectedCards, 
        boolean learnUnlearnedCards, boolean learnExpiredCards, 
        Map<Category, Integer> categoryGroupOrder)
    {
        List<Card> cards = new ArrayList<Card>();
        
        if (learnUnlearnedCards)
            cards.addAll(m_category.getUnlearnedCards());
        
        if (learnExpiredCards)
            cards.addAll(m_category.getExpiredCards());
        
        if (!learnUnlearnedCards && !learnExpiredCards)
            cards.addAll(selectedCards);
        
        
        List<Integer> levels = new LinkedList<Integer>();
        List<CardInfo> cardInfos = new ArrayList<CardInfo>(cards.size());
        m_cardsInfoMap.clear();
        
        for (Card card : cards)
        {
            CardInfo cardInfo = new CardInfo(card);
            cardInfos.add(cardInfo);
            
            m_cardsInfoMap.put(card, cardInfo);
            
            if (!levels.contains(card.getLevel()))
                levels.add(card.getLevel());
        }
        
        // shuffle random cards
        float shuffleRatio = m_settings.getShuffleRatio();
        int shuffledCardsCount = (int)(shuffleRatio * cards.size());
        
        
        List<CardInfo> shuffledCardInfos = new ArrayList<CardInfo>(shuffledCardsCount);
        if (levels.size() > 1)
        {
            for (int i = 0; i < shuffledCardsCount; i++)
            {
                int randIndex = m_rand.nextInt(cardInfos.size()); 
                
                CardInfo cardInfo = cardInfos.remove(randIndex);
                shuffledCardInfos.add(cardInfo);
                
                // randomly find a new level, which ISN'T our current level
                int randLevel = m_rand.nextInt(levels.size() - 1);
                
                if (randLevel >= cardInfo.getLevel())
                    randLevel++; 
                
                cardInfo.setLevel(levels.get(randLevel));
            }
        }
            
        // create equivalence set
        EquivalenceClassSet<CardInfo> cardSet = 
            new EquivalenceClassSet<CardInfo>(new CardComparator(categoryGroupOrder));
        
        cardSet.addAll(cardInfos);
        cardSet.addAll(shuffledCardInfos);
        
        return cardSet;
    }
    
    private Set<Card> toCardSet(Collection<CardInfo> cardInfos)
    {
        HashSet<Card> set = new HashSet<Card>();
        for (CardInfo cardInfo : cardInfos)
        {
            set.add(cardInfo.getCard());
        }
        
        return set;
    }
    
    private CardInfo getCardInfo(Card card)
    {
        return m_cardsInfoMap.get(card); 
    }

    /**
     * @return Cards that are being learned can be grouped by categories. In
     * this case the map holds for every category the position when it should
     * appear.
     */
    private Map<Category, Integer> createCategoryGroupOrder()
    {
        List<Category> categories = m_category.getSubtreeList();
        
        if (m_settings.getCategoryOrder() == LearnSettings.CATEGORY_ORDER_RANDOM)
        {
            Collections.shuffle(categories);
        }
        
        HashMap<Category, Integer> map = new HashMap<Category, Integer>();
        int i = 0;
        for (Category category : categories)
        {
            map.put(category, new Integer(i++));
        }
        // cards that have no category will be last in order
        map.put(null, new Integer(i));
        
        return map;
    }
    
    private void setupLogger()
    {
        // TODO move to main?
        m_logger.setLevel(Level.FINE);
        Handler ch = new ConsoleHandler();
        ch.setLevel(Level.WARNING);
        Logger.getLogger("").addHandler(ch);
    }
}
