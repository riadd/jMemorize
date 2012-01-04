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
package jmemorize.core.test;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jmemorize.core.learn.LearnHistory;
import jmemorize.core.learn.LearnHistory.SessionSummary;
import junit.framework.TestCase;

public class LearnHistoryTest extends TestCase
{
    private static long      MINUTE = 1000*60;
    
    private LearnHistory     m_history;
    
    private Date             m_date0 = createDate(14, 30);
    private Date             m_date1 = createDate(14, 50);
    private Date             m_date2 = createDate(14, 55);
    private Date             m_date3 = createDate(15, 00);

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        m_history = new LearnHistory(null);
    }

    public void testGetLastSummaryValues()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        
        SessionSummary summary = m_history.getLastSummary();
        assertEquals(2, (int)summary.getPassed());
        assertEquals(0, (int)summary.getFailed());
        assertEquals(1, (int)summary.getSkipped());
        assertEquals(1, (int)summary.getRelearned());
    }
    
    public void testGetLastSummaryDates()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        
        SessionSummary summary = m_history.getLastSummary();
        assertEquals(m_date0, summary.getStart());
        assertEquals(m_date1, summary.getEnd());
    }
    
    public void testAllSummariesDuration()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);
        
        SessionSummary sessions = m_history.getSessionsSummary();
        assertEquals(25, sessions.getDuration());
    }
    
    public void testAllsingleSummaryDuration()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        
        SessionSummary session = m_history.getLastSummary();
        assertEquals(20, session.getDuration());
    }
    
    public void testGetSummariesLimit()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);
        m_history.addSummary(m_date2, m_date3, 1, 3, 0, 1);
        
        List<SessionSummary> summaries = m_history.getSummaries(2);
        assertEquals(4, (int)(summaries.get(0)).getPassed());
        assertEquals(1, (int)(summaries.get(1)).getPassed());
        assertEquals(2, summaries.size());
    }
    
    public void testGetSingleDailySummaries()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);
        m_history.addSummary(m_date2, m_date3, 1, 3, 0, 1);
        
        List<SessionSummary> summaries = m_history.getSummaries(LearnHistory.DATE_COMP);
        SessionSummary summary = summaries.get(0);
        
        assertSession(7, 6, 1, 3, summary);
        assertEquals(1, summaries.size());
    }
    
    public void testGetSingleDailySummariesDates()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);
        m_history.addSummary(m_date2, m_date3, 1, 3, 0, 1);
        
        List<SessionSummary> summaries = m_history.getSummaries(LearnHistory.DATE_COMP);
        SessionSummary summary = summaries.get(0);
        
        assertEquals(m_date0, summary.getStart());
        assertEquals(m_date3, summary.getEnd());
        assertEquals(1, summaries.size());
    }
    
    public void testGetTwoDailySummaries()
    {
        Date today0 = new Date(System.currentTimeMillis() - MINUTE * 10);
        Date today1 = new Date(System.currentTimeMillis() - MINUTE * 5);
        
        m_history.addSummary(today0, today1, 2, 0, 1, 1);
        m_history.addSummary(m_date0, m_date1, 4, 3, 0, 1);
        m_history.addSummary(m_date1, m_date2, 1, 1, 0, 1);
        
        List<SessionSummary> summaries = m_history.getSummaries(LearnHistory.DATE_COMP);
        assertSession(2, 0, 1, 1, summaries.get(0));
        assertSession(5, 4, 0, 2, summaries.get(1));
        assertEquals(2, summaries.size());
    }
    
    public void testGetSummariesInOrder()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);
        
        List<SessionSummary> summaries = m_history.getSummaries();
        assertEquals(2, (int)(summaries.get(0)).getPassed());
        assertEquals(4, (int)(summaries.get(1)).getPassed());
    }

    public void testGetAverageValues()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);
        
        SessionSummary average = m_history.getAverage();
        assertEquals(3.0, average.getPassed(), 0.1f);
        assertEquals(1.5, average.getFailed(), 0.1f);
        assertEquals(0.5, average.getSkipped(), 0.1f);
        assertEquals(1.0, average.getRelearned(), 0.1f);
    }
    
    public void testGetAverageDates()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);
        
        SessionSummary average = m_history.getAverage();
        assertEquals(m_date0, average.getStart());
        assertEquals(m_date2, average.getEnd());
    }
    
    public void testGetSessionsSummaryValues()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);
        
        SessionSummary summary = m_history.getSessionsSummary();
        assertSession(6, 3, 1, 2, summary);
    }
    
    public void testGetSessionsSummaryDates()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);

        
        SessionSummary all = m_history.getSessionsSummary();
        assertEquals(m_date0, all.getStart());
        assertEquals(m_date2, all.getEnd());
    }
    
    public void testSaveLoadRoundtrip() throws Exception
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);
        
        File file = new File("test_stats.xml");
        m_history.save(file);
        
        LearnHistory stats = new LearnHistory(file);
        
        assertEquals(m_history, stats);
    }
    
    public void testGetSessionSummaryByDate()
    {
        m_history.addSummary(m_date0, m_date1, 2, 0, 1, 1);
        m_history.addSummary(m_date1, m_date2, 4, 3, 0, 1);
        
        SessionSummary summary = m_history.getSummary(m_date0, LearnHistory.DATE_COMP);
        assertSession(6, 3, 1, 2, summary);
    }
    
    private void assertSession(int passed, int failed, int skipped, 
        int relearned, SessionSummary summary)
    {
        assertEquals(passed, (int)summary.getPassed());
        assertEquals(failed, (int)summary.getFailed());
        assertEquals(skipped, (int)summary.getSkipped());
        assertEquals(relearned, (int)summary.getRelearned());
    }
    
    private Date createDate(int hour, int minute)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007, 1, 1, hour, minute);
        
        return calendar.getTime();
    }
}
