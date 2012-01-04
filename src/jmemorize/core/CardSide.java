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

import java.util.LinkedList;
import java.util.List;

/**
 * A card is made up of two card sides which can contain various contents, the
 * most important being text.
 * 
 * @author djemili
 */
public class CardSide implements Cloneable
{
    public interface CardSideObserver
    {
        public void onTextChanged(CardSide cardSide, FormattedText text);
        public void onImagesChanged(CardSide cardSide, List<String> imageIDs);
    }
    
    private FormattedText          m_text;
    private List<String>           m_imageIDs  = new LinkedList<String>();
    private List<CardSideObserver> m_observers = new LinkedList<CardSideObserver>();
    
    public CardSide()
    {
    }
    
    public CardSide(FormattedText text)
    {
        setText(text);
    }
    
    public FormattedText getText()
    {
        return m_text;
    }
    
    /**
     * Note that using this method won't modify the modification date of the
     * card. Use {@link Card#setSides(String, String)} instead for modifications 
     * done by the user.
     */
    public void setText(FormattedText text)
    {
        if (text.equals(m_text))
            return;
        
        m_text = text;
        
        for (CardSideObserver observer : m_observers)
        {
            observer.onTextChanged(this, m_text);
        }
    }
    
    /**
     * @return the IDs of all images of this card side.
     */
    public List<String> getImages()
    {
        return m_imageIDs;
    }
    
    public void setImages(List<String> ids)
    {
        if (m_imageIDs.equals(ids))
            return;
        
        m_imageIDs.clear();
        m_imageIDs.addAll(ids);
        
        for (CardSideObserver observer : m_observers)
        {
            observer.onImagesChanged(this, m_imageIDs);
        }
    }
    
    public void addObserver(CardSideObserver observer)
    {
        m_observers.add(observer);
    }
    
    public void removeObserver(CardSideObserver observer)
    {
        m_observers.remove(observer);
    }
    
    /** 
     * @return the unformatted string representation of the formatted text.
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return m_text.getUnformatted();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        CardSide cardSide = new CardSide();
        cardSide.m_text = (FormattedText)m_text.clone();
        cardSide.m_imageIDs.addAll(m_imageIDs);
        
        return cardSide;
    }
}
