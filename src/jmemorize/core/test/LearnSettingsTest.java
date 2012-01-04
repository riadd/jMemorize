package jmemorize.core.test;

import java.util.Calendar;
import java.util.Date;

import jmemorize.core.learn.LearnSettings;
import jmemorize.core.learn.LearnSettings.SchedulePreset;
import junit.framework.TestCase;

public class LearnSettingsTest extends TestCase
{
    private LearnSettings m_settings;
    private Calendar      m_testCalendar;
    private Date          m_testDate;

    @Override
    protected void setUp() throws Exception
    {
        m_settings = new LearnSettings();

        m_testCalendar = Calendar.getInstance();
        m_testCalendar.set(2007, 5, 4, 13, 20);

        m_testDate = m_testCalendar.getTime();
    }

    public void testGetExpirationForConst() throws Exception
    {
        m_settings.setSchedulePreset(SchedulePreset.CONST);

        Date expirationDate = m_settings.getExpirationDate(m_testDate, 0);
        assertCalendar(2007, 5, 5, 13, 20, expirationDate);
        
        expirationDate = m_settings.getExpirationDate(m_testDate, 4);
        assertCalendar(2007, 5, 5, 13, 20, expirationDate);
    }
    
    public void testGetExpirationForLinear() throws Exception
    {
        m_settings.setSchedulePreset(SchedulePreset.LINEAR);

        Date expirationDate = m_settings.getExpirationDate(m_testDate, 0);
        assertCalendar(2007, 5, 5, 13, 20, expirationDate);
        
        expirationDate = m_settings.getExpirationDate(m_testDate, 2);
        assertCalendar(2007, 5, 7, 13, 20, expirationDate);
    }
    
    public void testGetExpirationForCustomDueTimeOneDayDelay()
    {
        m_settings.setFixedExpirationTime(20, 00);
        m_settings.setFixedExpirationTimeEnabled(true);
        m_settings.setSchedulePreset(SchedulePreset.CONST); // 1 day delay
        m_settings.getExpirationDate(m_testDate, 2);
        
        Date expirationDate = m_settings.getExpirationDate(m_testDate, 0);
        assertCalendar(2007, 5, 5, 20, 0, expirationDate);
    }
    
    public void testGetExpirationForCustomDueTimeZeroDayDelay()
    {
        m_settings.setFixedExpirationTime(20, 00);
        m_settings.setFixedExpirationTimeEnabled(true); // o day delay
        m_settings.setCustomSchedule(new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0});
        m_settings.getExpirationDate(m_testDate, 0);
        
        Date expirationDate = m_settings.getExpirationDate(m_testDate, 0);
        assertCalendar(2007, 5, 4, 20, 0, expirationDate);
    }
    
    private void assertCalendar(int year, int month, int day, int hour, 
        int minute, Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        
        assertEquals(year, cal.get(Calendar.YEAR));
        assertEquals(month, cal.get(Calendar.MONTH));
        assertEquals(day, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, cal.get(Calendar.MINUTE));
    }
}
