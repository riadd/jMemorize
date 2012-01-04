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
package jmemorize.util;

import java.text.MessageFormat;
import java.util.Date;

import jmemorize.gui.Localization;

/**
 * A time span.
 * 
 * @author djemili
 */
public class TimeSpan
{
    public final static long MINUTE = 60 * 1000;
    public final static long HOUR   = 60 * MINUTE;
    public final static long DAY    = 24 * HOUR;
    
    private long m_ticks;
    
    public TimeSpan(Date from, Date to)
    {
        m_ticks = to.getTime() - from.getTime();
    }
    
    public long getDays()
    {
        return m_ticks / DAY;
    }
    
    public int getHours()
    {
        return (int)((m_ticks % DAY) / HOUR);
    }
    
    public int getMinutes()
    {
        return (int)((m_ticks % HOUR) / MINUTE);
    }
    
    public long getTicks()
    {
        return m_ticks;
    }
    
    /**
     * @return A string that represents the timespan in a nice and readable
     * format.
     */
    public static String format(Date dateNow, Date dateOther)
    {
        TimeSpan span = new TimeSpan(dateNow, dateOther);
        StringBuffer result = new StringBuffer();
        
        // if one or more days left
        long d = Math.abs(span.getDays());
        if (d >= 1)
        {
            result.append(d == 1 ? 
                Localization.get("Time.ONE_DAY") : //$NON-NLS-1$
                d + " " + Localization.get("Time.DAYS")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // if today
        else
        {
            int h = Math.abs(span.getHours());
            if (h >= 1)
            {
                result.append(h == 1 ?
                    Localization.get("Time.ONE_HOUR") : //$NON-NLS-1$
                    h + " " + Localization.get("Time.HOURS")); //$NON-NLS-1$ //$NON-NLS-2$ 
            }
            // if less then one hour left
            else
            {
                int m = Math.abs(span.getMinutes());
                result.append(m == 1 ? 
                    Localization.get("Time.ONE_MINUTE") : //$NON-NLS-1$
                    m + " " + Localization.get("Time.MINUTES")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        
        if (span.getTicks() >= 0) // future
        {
            MessageFormat form = new MessageFormat(Localization.get("Time.IN")); //$NON-NLS-1$
            return form.format(new Object[]{result.toString()});
        }
        else // past
        {
            MessageFormat form = new MessageFormat(Localization.get("Time.AGO")); //$NON-NLS-1$
            return form.format(new Object[]{result.toString()});
        }
    }
}
