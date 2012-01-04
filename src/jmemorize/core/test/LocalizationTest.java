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

import java.util.HashMap;
import java.util.Map;

import jmemorize.gui.Localization;
import junit.framework.TestCase;

/**
 * @author djemili
 */
public class LocalizationTest extends TestCase
{
    protected void setUp() throws Exception
    {
        Map defaultBundle = new HashMap();
        defaultBundle.put("abc", "ABC");
        defaultBundle.put("def", "DEF");
        
        Map childBundle = new HashMap();
        childBundle.put("abc", "XYZ");
        
        Localization.setBundles(childBundle, defaultBundle);
    }
    
    public void testSimpleGet()
    {
        assertEquals("XYZ", Localization.get("abc"));
    }
    
    public void testGetFallthrough()
    {
        assertEquals("DEF", Localization.get("def"));
    }
    
    public void testGetWithAlternateKey()
    {
        assertEquals("XYZ", Localization.get("def", "abc"));
    }
    
    public void testGetEmpty()
    {
        assertEquals("XYZ", Localization.getEmpty("abc"));
        assertEquals("", Localization.getEmpty("def"));
    }
    
    public void testGetDebugString()
    {
        assertEquals("!foo!", Localization.get("foo"));
    }
    
    public void testNullBundles()
    {
        Localization.setBundles(null, null);
        assertEquals("#abc#", Localization.get("abc"));
    }
    
    public void testNullDefaultBundle()
    {
        Map defaultBundle = new HashMap();
        defaultBundle.put("abc", "ABC");
        Localization.setBundles(defaultBundle, null);
        
        assertEquals("ABC", Localization.get("abc"));
        assertEquals("#def#", Localization.get("def"));
    }
}
