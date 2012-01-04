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
package jmemorize.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jmemorize.core.Main;
import jmemorize.core.Settings;

/**
 * This class is used to query language/locale related strings and date
 * formatters.
 * 
 * @author djemili
 */
public class Localization
{
    static
    {
        Locale.setDefault(Settings.loadLocale());
    }
    
    public static final DateFormat SHORT_DATE_FORMATER = 
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    
    public static final DateFormat LONG_DATE_FORMATER  = 
        DateFormat.getDateTimeInstance(DateFormat.FULL,  DateFormat.MEDIUM);
    
    public static final DateFormat SHORT_TIME_FORMATER = 
        DateFormat.getTimeInstance(DateFormat.SHORT); // just hours:minutes
    
    private static final String RESOURCE_FORMAT = 
        "/resource/text/translation_{0}.properties";
    
    private static final String LANGS_FILE = "/resource/text/langs.txt";
    
    private static Map<?, ?> m_defaultBundle  = getBundleOrNull(Locale.getDefault());
    private static Map<? ,?> m_fallbackBundle = getBundleOrNull(Locale.ENGLISH);
    
    /**
     * Return the string translation that belongs to the key.
     */
    public static String get(String key)
    {
        if (m_defaultBundle != null)
        {
            String val = (String)m_defaultBundle.get(key);
            
            if (val != null)
                return val;
        }
        
        if (m_fallbackBundle != null)
        {
            String val = (String)m_fallbackBundle.get(key);
            return val != null ? val : '!' + key + '!';
        }
        
        return '#' + key + '#';
    }
    
    /**
     * @return the string associated with key in the default or child bundle or
     * an empty string if no such string available.
     */
    public static String getEmpty(String key)
    {
        if (m_defaultBundle != null)
        {
            String val = (String)m_defaultBundle.get(key);
            return val != null ? val : "";
        }
        
        return '#' + key + '#';
    }
    
    /**
     * @return the string associated with given key1 or the string assoiated
     * with given key2 if no string is associated with key1 in the sub bundle or
     * the string associated with given key in the default bundle or a debug
     * string when no string is available for any of the two given keys.
     */
    public static String get(String key, String alternateKey)
    {
        if (m_defaultBundle != null)
        {
            String val = (String)m_defaultBundle.get(key);
            if (val != null)
                return val;
            
            val = (String)m_defaultBundle.get(alternateKey);
            if (val != null)
                return val;
        }
        
        if (m_fallbackBundle != null)
        {
            String val = (String)m_fallbackBundle.get(key);
            return val != null ? val : '!' + key + '!';
        }
        
        return '#' + key + '#';
    }
    
    public static void setBundles(Map defaultBundle, Map fallbackBundle)
    {
        m_fallbackBundle = fallbackBundle;
        m_defaultBundle = defaultBundle;
    }
    
    /**
     * The list of available languages is stored in the file
     * <tt>/resource/text/langs.txt</tt>. Every line holds a string that
     * represents the language (and optionally the country) code.
     * 
     * @return A list of all available locales/translations.
     */
    public static List<Locale> getAvailableLocales()
    {
        List<Locale> locales = new ArrayList<Locale>();
        BufferedReader in = null;

        try
        {
            // load available locales from file
            in = new BufferedReader(new InputStreamReader(
                Localization.class.getResourceAsStream(LANGS_FILE)));
            Pattern p = Pattern.compile("([a-z]{2})(?:_([A-Z]{2,3}))?"); //$NON-NLS-1$

            String line;
            while ((line = in.readLine()) != null)
            {
                Matcher m = p.matcher(line);
                
                if (m.matches())
                {
                    String language = m.group(1);
                    String country  = m.group(2);

                    locales.add(country != null ? 
                        new Locale(language, country) : new Locale(language));
                }
            }
            
            // sort locales by display language
            Collections.sort(locales, new Comparator<Locale>()
            {
                public int compare(Locale l1, Locale l2)
                {
                    String ls1 = l1.getDisplayLanguage();
                    String ls2 = l2.getDisplayLanguage();
                    
                    return ls1.compareTo(ls2);
                }
            });
        }
        catch (IOException e)
        {
            Main.logThrowable("failed loading available locales", e); //$NON-NLS-1$
        }
        finally 
        {
            try 
            {
                if (in != null)
                    in.close ();
            } 
            catch (IOException e) 
            {
                Main.logThrowable("failed loading available locales", e); //$NON-NLS-1$
            }
        }
        
        return locales;
    }
    
    /**
     * Compares the machines default locale with all available translations. If
     * the default locale is available as translation the machines locale is
     * returned. Otherwise the english language locale is returned as default.
     */
    public static Locale getDefaultLocale()
    {
        Locale defaultLocale = Locale.getDefault();
        for (Locale locale : getAvailableLocales())
        {
            if (defaultLocale.getLanguage().equals(locale.getLanguage()))
            {
                return locale;
            }
        }
        
        return Locale.ENGLISH;
    }
    
    public static void main(String[] args)
    {
        Map<?, ?> defaultBundle = getBundleOrNull(Locale.ENGLISH);
        
        List<Locale> locales = getAvailableLocales();
        for (Locale locale : locales)
        {
            Map<?, ?> bundle = getBundleOrNull(locale);
            Set<?> bundleKeys = bundle.keySet();
            
            List defaultKeys = new ArrayList(defaultBundle.keySet());
            defaultKeys.removeAll(bundleKeys);
            Collections.sort(defaultKeys);            
            
            System.out.print("Locale: "+locale.getLanguage()+ " --> ");
            if (defaultKeys.isEmpty())
            {
                System.out.println("OK");
            }
            else
            {
                System.out.println(defaultKeys.size() + " missing Keys");
                for (Iterator it2 = defaultKeys.iterator(); it2.hasNext();)
                {
                    String key = (String)it2.next();
                    System.out.println(key + " = " + defaultBundle.get(key));
                }
                System.out.println();
            }
            
            
            System.out.println();
        }
    }
    
    /**
     * Tries to load ResourceBundle from given baseName.
     * 
     * @param baseName the name of the bundle to load.
     * @return the ResourceBundle if found. <code>null</code> otherwise.
     */
    private static Properties getBundleOrNull(Locale locale)
    {
        try
        {
            Object[] args = new Object[]{locale.getLanguage()};
            String path = MessageFormat.format(RESOURCE_FORMAT, args);
            
            Properties properties = new Properties();
            properties.load(Localization.class.getResourceAsStream(path));
            
            return properties;
        }
        catch (IOException e)
        {
            return null;
        }
    }
    
    private Localization() // should not be called
    {
    }
}
