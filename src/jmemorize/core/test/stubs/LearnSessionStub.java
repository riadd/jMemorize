package jmemorize.core.test.stubs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSettings;

public class LearnSessionStub implements LearnSession
{
    private Date m_start;
    private Date m_end;
    
    private Set<Card> m_passed    = new HashSet<Card>();
    private Set<Card> m_failed    = new HashSet<Card>();
    private Set<Card> m_skipped   = new HashSet<Card>();
    private Set<Card> m_relearned = new HashSet<Card>();
    
    private static final Set<Card> m_emptySet = new HashSet<Card>(); 

    public LearnSessionStub(Date start, Date end, int passed, int failed, 
        int skipped, int relearned)
    {
        m_start = start;
        m_end = end;
        
        for (int i = 0; i < passed; i++)
        {
            m_passed.add(new Card("test card"+i, "test"));
        }
        
        for (int i = 0; i < failed; i++)
        {
            m_failed.add(new Card("test card"+i, "test"));
        }

        for (int i = 0; i < skipped; i++)
        {
            m_skipped.add(new Card("test card"+i, "test"));
        }

        for (int i = 0; i < relearned; i++)
        {
            m_relearned.add(new Card("test card"+i, "test"));
        }
    }
    
    public Date getStart()
    {
        return m_start;
    }

    public Date getEnd()
    {
        return m_end;
    }

    public Set<Card> getPassedCards()
    {
        return m_passed;
    }

    public Set<Card> getFailedCards()
    {
        return m_failed;
    }

    public Set<Card> getSkippedCards()
    {
        return m_skipped;
    }

    public Set<Card> getRelearnedCards()
    {
        return m_relearned;
    }
    
    public int getNCardsLearned() 
    {
        return m_relearned.size() + m_passed.size();
    }

    public int getNCardsPartiallyLearned() 
    {
        return 0;
    }

    public void startLearning()
    {
    }

    public void endLearning()
    {
    }

    public LearnSettings getSettings()
    {
        return null;
    }

    public Card getCurrentCard()
    {
        return null;
    }

    public Set<Card> getCardsLeft()
    {
        return m_emptySet;
    }

    public Category getCategory()
    {
        return null;
    }

    public void cardChecked(boolean passed, boolean shownFlipped)
    {
    }

    public void cardSkipped()
    {
    }

    public void onTimer()
    {
    }

    public List<Card> getCheckedCards()
    {
        return new ArrayList<Card>();
    }

    public void addObserver(LearnCardObserver observer)
    {
    }

    public void removeObserver(LearnCardObserver observer)
    {
    }

    public boolean isRelevant()
    {
        return true;
    }
    
    public boolean isQuit()
    {
        return true;
    }
}
