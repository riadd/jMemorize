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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jmemorize.core.Main;
import jmemorize.core.io.XmlBuilder;
import jmemorize.gui.Localization;

import org.w3c.dom.Document;

/**
 * Stores the history of learn sessions and provides statistics.
 * 
 * @author djemili
 */
public class LearnHistory
{
    public class SessionSummary implements Cloneable
    {
        private final Date  m_start;
        private final Date  m_end;
        private final int   m_duration;
        
        private final float m_passed;
        private final float m_failed;
        private final float m_skipped;
        private final float m_relearned;
        

        private SessionSummary(Date start, Date end, float passed, float failed, 
            float skipped, float relearned)
        {
            this(start, end, 
                (int)((end.getTime() - start.getTime()) / (1000*60)),
                passed, failed, skipped, relearned);
        }
        
        private SessionSummary(Date start)
        {
            this(start, start, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        
        private SessionSummary(Date start, Date end, int duration, 
            float passed, float failed, float skipped, float relearned)
        {
            m_start = start;
            m_end = end;
            m_duration = duration;
            
            m_passed = passed;
            m_failed = failed;
            m_skipped = skipped;
            m_relearned = relearned;
        }

        public Date getStart()
        {
            return (Date)m_start.clone();
        }

        public Date getEnd()
        {
            return (Date)m_end.clone();
        }
        
        public int getDuration()
        {
            return m_duration;
        }

        public float getPassed()
        {
            return m_passed;
        }

        public float getFailed()
        {
            return m_failed;
        }

        public float getSkipped()
        {
            return m_skipped;
        }

        public float getRelearned()
        {
            return m_relearned;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        public Object clone() throws CloneNotSupportedException
        {
            return super.clone();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj)
        {
            if (!(obj instanceof SessionSummary))
            {
                return false;
            }
            
            SessionSummary other = (SessionSummary)obj;
            
            return m_passed == other.m_passed && m_failed == other.m_failed &&
                m_skipped == other.m_skipped && m_relearned == other.m_relearned;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() 
        {
            return m_start.hashCode();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return "summary("+m_start+", "+m_passed+"/"+m_failed+")";
        }
    }
    
    public abstract static class CalendarComparator implements Comparator<SessionSummary>
    {
        /* (non-Javadoc)
         * @see java.util.Comparator
         */
        public int compare(SessionSummary s1, SessionSummary s2)
        {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(s1.getStart());
            
            Calendar c2 = Calendar.getInstance();
            c2.setTime(s2.getStart());
            
            long v1 = toValue(c1);
            long v2 = toValue(c2);
            
            return v1 == v2 ? 0 : v1 > v2 ? 1 : -1;
        }
        
        public abstract long toValue(Calendar c);
        public abstract DateFormat getFormat();
        public abstract boolean showRotated();
        public abstract void decCalendarValue(Calendar c);
        
    }
    
    private static class SimpleComparator extends CalendarComparator
    {
        public long toValue(Calendar c)
        {
            return c.getTimeInMillis();
        }

        public DateFormat getFormat()
        {
            return Localization.SHORT_DATE_FORMATER;
        }

        public boolean showRotated()
        {
            return true;
        }

        @Override
        public void decCalendarValue(Calendar c)
        {
            throw new UnsupportedOperationException();
        }
    }
    
    private static class DateComparator extends CalendarComparator
    {
        public long toValue(Calendar c)
        {
            return c.get(Calendar.DAY_OF_YEAR)+ 1000 * c.get(Calendar.YEAR);
        }

        public DateFormat getFormat()
        {
            return DateFormat.getDateInstance(DateFormat.SHORT);
        }

        public boolean showRotated()
        {
            return true;
        }

        @Override
        public void decCalendarValue(Calendar c)
        {
            c.add(Calendar.DAY_OF_YEAR, -1);
        }
    }
    
    private static class WeekComparator extends CalendarComparator
    {
        public long toValue(Calendar c)
        {
            return c.get(Calendar.WEEK_OF_YEAR) + 1000 * c.get(Calendar.YEAR);
        }

        public DateFormat getFormat()
        {
            return new SimpleDateFormat("w/yyyy");
        }
        
        public boolean showRotated()
        {
            return true;
        }

        @Override
        public void decCalendarValue(Calendar c)
        {
            c.add(Calendar.WEEK_OF_YEAR, -1);
        }
    }
    
    private static class MonthComparator extends CalendarComparator
    {
        public long toValue(Calendar c)
        {
            return c.get(Calendar.MONTH) + 1000 * c.get(Calendar.YEAR);
        }

        public DateFormat getFormat()
        {
            return new SimpleDateFormat("M/yyyy");
        }
        
        public boolean showRotated()
        {
            return true;
        }

        @Override
        public void decCalendarValue(Calendar c)
        {
            c.add(Calendar.MONTH, -1);
        }
    }
    
    private static class YearComparator extends CalendarComparator
    {
        public long toValue(Calendar c)
        {
            return c.get(Calendar.YEAR);
        }

        public DateFormat getFormat()
        {
            return new SimpleDateFormat("yyyy");
        }
        
        public boolean showRotated()
        {
            return false;
        }

        @Override
        public void decCalendarValue(Calendar c)
        {
            c.add(Calendar.YEAR, -1);
        }
    }
    
    public static final CalendarComparator SIMPLE_COMP = new SimpleComparator();
    public static final CalendarComparator DATE_COMP   = new DateComparator();
    public static final CalendarComparator WEEK_COMP   = new WeekComparator();
    public static final CalendarComparator MONTH_COMP  = new MonthComparator();
    public static final CalendarComparator YEAR_COMP   = new YearComparator();
    
    // TODO enforce that m_summaries is always sorted in descending date order
    private List<SessionSummary>    m_summaries = new ArrayList<SessionSummary>();
    
    private File                    m_file;
    private boolean                 m_isLoaded; // false, if created from scratch
    
    public LearnHistory()
    {
        this(null);
    }
    
    public LearnHistory(File file)
    {
        try
        {
            m_file = file;
            
            if (m_file != null)
                load(m_file);
        } 
        catch (Exception e)
        {
            
            Main.logThrowable("Could not load learn history.", e);
        } 
    }
    
    public void addSummary(Date start, Date end, int passed, int failed, 
        int skipped, int relearned)
    {
        SessionSummary sessionSummary = new SessionSummary(
            start, end, passed, failed, skipped, relearned);
        
        m_summaries.add(sessionSummary);
    }
    
    public void setIsLoaded(boolean loaded)
    {
        m_isLoaded = loaded;
    }
    
    public boolean isLoaded()
    {
        return m_isLoaded;
    }

    public SessionSummary getLastSummary()
    {
        if (m_summaries.size() == 0)
            return null;
        
        return (SessionSummary)m_summaries.get(m_summaries.size() - 1);
    }

    public List<SessionSummary> getSummaries()
    {
        return m_summaries;
    }
    
    public List<SessionSummary> getSummaries(int limit)
    {
        int n = Math.min(limit, m_summaries.size());
        return m_summaries.subList(m_summaries.size() - n, m_summaries.size());
    }
    
    public List<SessionSummary> getSummaries(CalendarComparator comp)
    {
        List<SessionSummary> list = new LinkedList<SessionSummary>();
        
        SessionSummary lastSummary = null;
        SessionSummary aggregatedSummary = null;
        
        // TODO refactor and use getSummary(date, comp)
        for (SessionSummary summary : m_summaries)
        {
            if (lastSummary == null || comp.compare(summary, lastSummary) != 0)
            {
                if (aggregatedSummary != null)
                    list.add(aggregatedSummary);
                
                try
                {
                    aggregatedSummary = (SessionSummary)summary.clone();
                }
                catch (CloneNotSupportedException e)
                {
                    assert false;
                }
            }
            else
            {
                aggregatedSummary = new SessionSummary(
                    aggregatedSummary.m_start, summary.m_end,
                    aggregatedSummary.m_duration + summary.m_duration,
                    aggregatedSummary.m_passed + summary.m_passed,
                    aggregatedSummary.m_failed + summary.m_failed,
                    aggregatedSummary.m_skipped + summary.m_skipped,
                    aggregatedSummary.m_relearned + summary.m_relearned
                );
            }
            
            lastSummary = summary;
        }
        
        if (aggregatedSummary != null)
            list.add(aggregatedSummary);
        
        return list;
    }
    
    public List<SessionSummary> getSummaries(CalendarComparator comp, int limit,
        boolean showEmpty)
    {
        if (showEmpty && comp != SIMPLE_COMP)
        {
            List<SessionSummary> summaries = new ArrayList<SessionSummary>(limit);
            Calendar c = Calendar.getInstance();
            Date date = c.getTime();
            
            int lastEntry = 0;
            for (int i=0; i<limit; i++)
            {
                SessionSummary summary = getSummary(date, comp);
                
                if (summary == null)
                    summary = new SessionSummary(date);
                else
                    lastEntry = i;
                
                summaries.add(0, summary);
                
                comp.decCalendarValue(c);
                date = c.getTime();
            }
            
            int size = summaries.size();
            lastEntry = Math.max(2, lastEntry); // always show at least 3 entries
            
            return summaries.subList(size - lastEntry - 1, size);
        }
        else
        {
            // TODO optimize this; remove version without limit argument
            List<SessionSummary> summaries = getSummaries(comp);
            int n = Math.min(limit, summaries.size()); 
            return summaries.subList(summaries.size() - n, summaries.size());
        }
    }
    
    public SessionSummary getAverage()
    {
        float count = m_summaries.size();
        SessionSummary summary = getSessionsSummary();
        
        if (count > 0)
        {
            return new SessionSummary(summary.getStart(), summary.getEnd(), 
                (int)(summary.getDuration() / count),
                summary.getPassed()/count, summary.getFailed()/count,
                summary.getSkipped()/count, summary.getRelearned()/count);
        }
        else
        {
            return new SessionSummary(new Date(), new Date(), 
                0, 0, 0, 0);
        }
    }
    
    /**
     * @return a aggregated summary for given date and comparator.
     */
    public SessionSummary getSummary(Date date, CalendarComparator comp)
    {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date);
        
        Calendar c2 = Calendar.getInstance();
        
        int duration = 0;
        int failed = 0; 
        int passed = 0;
        int relearned = 0;
        int skipped = 0;
        boolean found = false; 
        
        for (SessionSummary summary : m_summaries)
        {
            c2.setTime(summary.m_start);
            
            if (comp.toValue(c1) == comp.toValue(c2))
            {
                duration += summary.m_duration;
                failed += summary.m_failed;
                passed += summary.m_passed;
                relearned += summary.m_relearned;
                skipped += summary.m_skipped;
                
                found = true;
            }
        }
        
        return !found ? null : 
            new SessionSummary(date, date, duration, 
                passed, failed, skipped, relearned);
    }

    public SessionSummary getSessionsSummary()
    {
        int duration = 0;
        float passed = 0;
        float failed = 0;
        float skipped = 0;
        float relearned = 0;
        
        for (SessionSummary summary : m_summaries)
        {
            duration += summary.getDuration();
            passed += summary.getPassed();
            failed += summary.getFailed();
            skipped += summary.getSkipped();
            relearned += summary.getRelearned();
        }
        
        SessionSummary first = (SessionSummary)m_summaries.get(0);
        SessionSummary last  = (SessionSummary)m_summaries.get(m_summaries.size() - 1);
        
        return new SessionSummary(first.getStart(), last.getEnd(), duration,
            passed, failed, skipped, relearned);
    }

    public void load(File file) throws Exception
    {
        if (!file.exists())
            return;
        
        InputStream in = new FileInputStream(file);
        
        // get lesson tag
        try
        {
            Document doc = DocumentBuilderFactory.newInstance().
                newDocumentBuilder().parse(in);
    
            XmlBuilder.loadLearnHistory(doc, this);
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }

    public void save(File file) throws Exception
    {
        OutputStream out = new FileOutputStream(file);
        
        try
        {
            Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
            
            XmlBuilder.writeLearnHistory(document, this);
            
            // transform document for file output
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            transformer.transform(new DOMSource(document), new StreamResult(out));
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (!(obj instanceof LearnHistory))
        {
            return false;
        }
        
        LearnHistory other = (LearnHistory)obj;
        
        return m_summaries.equals(other.m_summaries);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() 
    {
        return m_summaries.hashCode(); 
    }
}
