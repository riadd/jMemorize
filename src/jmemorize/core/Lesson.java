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

import java.io.File;

import jmemorize.core.learn.LearnHistory;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;

/**
 * @author djemili
 */
public class Lesson implements CategoryObserver 
{
    /** True if this lesson has been modified since last save or load process */
    private boolean           m_canSave = false;
    private File              m_file;
    
    private Category          m_rootCategory;
    private LearnHistory      m_learnHistory = new LearnHistory();
    
    public Lesson(boolean canSave)
    {
        this(new Category(Localization.get(LC.ROOT_CATEGORY)), canSave);
    }
    
    /**
     * @param rootCategory the root category for the new lesson.
     */
    public Lesson(Category rootCategory, boolean canSave)
    {
        setRootCategory(rootCategory);
        setCanSave(canSave);
    }
    
    /**
     * @return Returns the file.
     */
    public File getFile()
    {
        return m_file;
    }
    
    /**
     * @param file The file to set.
     */
    public void setFile(File file)
    {
        m_file = file;
    }
    
    public LearnHistory getLearnHistory()
    {
        return m_learnHistory;
    }
    
    /**
     * @return <code>true</code> if saving is needed for this lesson.
     */
    public boolean canSave()
    {
        return m_canSave;
    }
    
    /**
     * @return Returns the root rategory.
     */
    public Category getRootCategory()
    {
        return m_rootCategory;
    }
    
    /*
     * @see jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        setCanSave(true);
    }

    /*
     * @see jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        if (type != EXPIRED_EVENT)
        {
            setCanSave(true);
        }
    }
    
    /**
     * Returns a clone of this lesson. The clone contains all cards and
     * categories of the original lesson, but has all cards reset to have no
     * learn stats.
     */
    public Lesson cloneWithoutProgress()
    {
        return new Lesson(m_rootCategory.cloneWithoutProgress(), true);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof Lesson)
        {
            Lesson other = (Lesson)obj;
            return other.getRootCategory().getCards().equals(
                getRootCategory().getCards());
        }
        
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() 
    {
        return getRootCategory().hashCode(); 
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "Lesson("+getFile()+")";
    }
    
    public void setCanSave(boolean canSave) // TODO make private
    {
        m_canSave = canSave;
    }
    
    /**
     * Sets a new root category and add this lesson as it's obvserver.
     * RootCategory must be <code>null</code> prior to calling this method,
     * because lessons don't support changes to the root category.
     * 
     * @param rootCategory The new root. Can't be <code>null</code>.
     */
    private void setRootCategory(Category rootCategory)
    {
        assert m_rootCategory == null;
        
        m_rootCategory = rootCategory;
        m_rootCategory.addObserver(this);
    }
}

