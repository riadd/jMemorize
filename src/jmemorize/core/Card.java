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
package jmemorize.core;

import java.util.Date;
import java.util.List;

import jmemorize.core.CardSide.CardSideObserver;

/**
 * A flash card that has a front/flip side and can be learned.
 * 
 * @author djemili
 * @version $Id: Card.java 1048 2008-01-21 21:40:00Z djemili $
 */
public class Card implements Events, Cloneable
{
    public static final long    ONE_DAY     = 1000 * 60 * 60 * 24;
    public static final boolean CLONE_DATES = Main.isDevel();

    private Category m_category;
    private int      m_level;

    // content
    private CardSide m_frontSide = new CardSide();
    private CardSide m_backSide  = new CardSide();
    
    // dates
    private Date     m_dateTested;
    private Date     m_dateExpired;
    private Date     m_dateCreated;
    private Date     m_dateModified;
    private Date     m_dateTouched; //this date is used internaly to order cards

    // stats
    private int      m_testsTotal;
    private int      m_testsHit;    //succesfull learn repetitions
    private int      m_frontHitsCorrect;
    private int      m_backHitsCorrect;

    /**
     * Assumes formatted front- and backsides
     */
    public Card(String front, String back)
    {
        this(FormattedText.formatted(front), FormattedText.formatted(back));
    }
    
    public Card(FormattedText front, FormattedText back) 
    {
        this(new Date(), front, back);
    }
    
    /**
     * The card sides are given in a formatted state.
     */
    public Card(Date created, String front, String back)
    {
        this(created, FormattedText.formatted(front), FormattedText.formatted(back));
    }
    
    public Card(Date created, FormattedText front, FormattedText back)
    {
        this(created, new CardSide(front), new CardSide(back));
    }
    
    public Card(Date created, CardSide frontSide, CardSide backSide)
    {
        m_dateCreated = cloneDate(created);
        m_dateModified = cloneDate(created);
        m_dateTouched = cloneDate(created);

        m_frontSide = frontSide;
        m_backSide = backSide;
        
        attachCardSideObservers();
    }

    /**
     * The given card sides are assumend to be unformatted.
     * 
     * @throws IllegalArgumentException If frontSide or backSide has no text.
     */
    public void setSides(String front, String back)
    {
        FormattedText frontSide = FormattedText.unformatted(front);
        FormattedText backSide = FormattedText.unformatted(back);
        
        setSides(frontSide, backSide);
    }
    
    /**
     * @throws IllegalArgumentException If front or back has no text.
     */
    public void setSides(FormattedText front, FormattedText back) 
        throws IllegalArgumentException
    {
        if (front.equals(m_frontSide.getText()) && 
            back.equals(m_backSide.getText()))
        {
            return;
        }
        
        m_frontSide.setText(front);
        m_backSide.setText(back);
        
        if (m_category != null)
        {
            m_dateModified = new Date();
            m_category.fireCardEvent(EDITED_EVENT, this, getCategory(), m_level);
        }
    }
    
    /**
     * Get the number of times a specific card side was already learned in its
     * deck.
     * 
     * @param frontside <code>true</code> if it should deliver the fronside
     * value, <code>false</code> if it should deliver the backside value.
     * 
     * @return the amount of times that the specified side was learned in this
     * deck.
     */
    public int getLearnedAmount(boolean frontside)
    {
        // TODO move to CardSide class
        
        return frontside ? m_frontHitsCorrect :  m_backHitsCorrect;
    }

    /**
     * Set the number of times a specific card side was already learned in its
     * deck.
     * 
     * @param frontside <code>true</code> if it should deliver the fronside
     * value, <code>false</code> if it should deliver the backside value.
     * 
     * @param amount the amount of times that the specified side was learned in
     * this deck.
     */
    public void setLearnedAmount(boolean frontside, int amount)
    {
//      TODO move to CardSide class
        
        if (frontside)
        {
            m_frontHitsCorrect = amount;
        }
        else
        {
            m_backHitsCorrect = amount;
        }

        if (m_category != null)
        {
            m_category.fireCardEvent(DECK_EVENT, this, getCategory(), m_level);
        }
    }

    /**
     * Increment the number of times a specific card side was already learned in
     * its deck by one.
     * 
     * @param frontside <code>true</code> if it should deliver the fronside
     * value, <code>false</code> if it should deliver the backside value.
     */
    public void incrementLearnedAmount(boolean frontside)
    {
//      TODO move to CardSide class
        
        setLearnedAmount(frontside, getLearnedAmount(frontside) + 1);
    }

    /**
     * Resets the amount of times that the card sides were learned in this deck
     * to 0.
     */
    public void resetLearnedAmount()
    {
        setLearnedAmount(true, 0);
        setLearnedAmount(false, 0);
    }

    public CardSide getFrontSide()
    {
        return m_frontSide;
    }

    public CardSide getBackSide()
    {
        return m_backSide;
    }
    
    /**
     * @return the date that this card appeared the last time in a test and was
     * either passed or failed (skip doesn't count).
     */
    public Date getDateTested()
    {
        return cloneDate(m_dateTested);
    }

    public void setDateTested(Date date)
    {
        m_dateTested = cloneDate(date);
        m_dateTouched = cloneDate(date);
    }

    /**
     * @return can be <code>null</code>.
     */
    public Date getDateExpired()
    {
        return cloneDate(m_dateExpired);
    }

    /**
     * @param date can be <code>null</code>.
     */
    public void setDateExpired(Date date) // CHECK should this throw a event?
    {
        m_dateExpired = cloneDate(date);
    }

    /**
     * @return the creation date. Is never <code>null</code>.
     */
    public Date getDateCreated()
    {
        return cloneDate(m_dateCreated);
    }

    public void setDateCreated(Date date)
    {
        if (date == null) 
            throw new NullPointerException();
        
        m_dateCreated = cloneDate(date);
    }
    
    /**
     * @return the modification date. Is never <code>null</code>.
     */
    public Date getDateModified()
    {
        return m_dateModified;
    }

    /**
     * @param date must be equal or after the creation date.
     */
    public void setDateModified(Date date)
    {
        if (date.before(m_dateCreated))
            throw new IllegalArgumentException(
                "Modification date can't be before creation date.");
        
        m_dateModified = date;
    }

    /**
     * @return DateTouched is the date that this card was learned, skipped,
     * reset or created the last time. This value is used to sort cards by a
     * global value that is unique for all categories and decks.
     */
    public Date getDateTouched()
    {
        return cloneDate(m_dateTouched);
    }

    public void setDateTouched(Date date)
    {
        m_dateTouched = cloneDate(date);
    }

    /**
     * @return Number of times this card has been tested.
     */
    public int getTestsTotal()
    {
        return m_testsTotal;
    }

    /**
     * @return Number of times this card has been tested succesfully.
     */
    public int getTestsPassed()
    {
        return m_testsHit;
    }

    /**
     * @return The percentage of times that this card has passed learn tests in
     * comparison to failed tests.
     */
    public int getPassRatio()
    {
        return (int)Math.round(100.0 * m_testsHit / m_testsTotal);
    }

    public void incStats(int hit, int total)
    {
        m_testsTotal += total;
        m_testsHit += hit;
    }

    public void resetStats()
    {
        m_testsTotal = 0;
        m_testsHit = 0;
        
        m_frontHitsCorrect = 0;
        m_backHitsCorrect = 0;
    }

    public Category getCategory()
    {
        return m_category;
    }

    protected void setCategory(Category category)
    {
        m_category = category;
    }

    /**
     * A card is expired when it was learned/repeated succesfully, but its learn
     * time has expired (is in the past from current perspective).
     * 
     * @return True if the card has expired.
     */
    public boolean isExpired()
    {
        Date now = Main.getNow();
        return m_dateExpired != null && 
            (m_dateExpired.before(now) || m_dateExpired.equals(now));
    }

    /**
     * A card is learned when it was learned/repeated succesfully and its learn 
     * time hasnt expired.
     * 
     * @return True if the card is learned.
     */
    public boolean isLearned()
    {
        return m_dateExpired != null && m_dateExpired.after(Main.getNow());
    }

    /**
     * A card is unlearned when it wasnt succesfully repeated or never been l
     * earned at all.
     * 
     * @return True if the card is unlearned.
     */
    public boolean isUnlearned()
    {
        return m_dateExpired == null;
    }

    /**
     * @return Returns the level.
     */
    public int getLevel()
    {
        return m_level;
    }

    /**
     * @param level The level to set.
     */
    protected void setLevel(int level)
    {
        m_level = level;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        Card card = null;
        try
        {
            card = (Card)super.clone();
            card.m_frontSide = (CardSide)m_frontSide.clone();
            card.m_backSide = (CardSide)m_backSide.clone();
            
            card.m_dateCreated = cloneDate(m_dateCreated);
            card.m_dateExpired = cloneDate(m_dateExpired);
            card.m_dateModified = cloneDate(m_dateModified);
            card.m_dateTested = cloneDate(m_dateTested);
            card.m_dateTouched = cloneDate(m_dateTouched);
            
            card.m_category = null; // don't clone category
        }
        catch (CloneNotSupportedException e) 
        {
            assert false;
        }
        
        return card;
    }
    
    /**
     * Clones the card without copying its user-dependent progress stats.
     * 
     * This includes the following data: Fronside, Flipside, Creation date.
     * Setting the right category needs to be handled from the out side.
     */
    public Card cloneWithoutProgress()
    {
        try
        {
            return new Card(m_dateCreated, 
                (CardSide)m_frontSide.clone(), (CardSide)m_backSide.clone());
        }
        catch (CloneNotSupportedException e)
        {
            assert false;
            return null; // satisfy compiler
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "("+m_frontSide+"/"+m_backSide+")";
    }
    
    private void attachCardSideObservers()
    {
        CardSideObserver observer = new CardSideObserver() {
            public void onImagesChanged(CardSide cardSide, List<String> imageIDs)
            {
                if (m_category != null)
                {
                    m_dateModified = new Date();
                    m_category.fireCardEvent(EDITED_EVENT, Card.this, getCategory(), m_level);
                }
            }

            public void onTextChanged(CardSide cardSide, FormattedText text)
            {
                // already handled by set sides
                // TODO handle event notfying here
            }
        };
        
        m_frontSide.addObserver(observer);
        m_backSide.addObserver(observer);
    }

    /**
     * @return clone of given date or <code>null</code> if given date was
     * <code>null</code>.
     */
    private Date cloneDate(Date date)
    {
        if (CLONE_DATES)
        {
            return date == null ? null : (Date)date.clone();
        }
        
        return date;
    }
}
