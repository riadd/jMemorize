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

import java.util.prefs.Preferences;

/**
 * Just some helper methods for storing/loading Preferences.
 * 
 * @author djemili
 */
public class PreferencesTool
{
    public static void putIntArray(Preferences node, String key, int[] ints)
    {
        StringBuffer buffer = new StringBuffer();
        int index = 0;
        
        while (index < ints.length - 1)
        {
            buffer.append(ints[index++]);
            buffer.append(',');
        }
        buffer.append(ints[index]);
        
        node.put(key, buffer.toString());
    }
    
    public static int[] getIntArray(Preferences node, String key) //TODO add fallbackvalue
    {
        String intArrayString = node.get(key, null);
        if (intArrayString == null)
        {
            return null;
        }
        
        String[] strings = intArrayString.split(","); //$NON-NLS-1$
        int[] ints = new int[strings.length];
        for (int i = 0; i < strings.length; i++)
        {
            ints[i] = Integer.parseInt(strings[i]);
        }
        
        return ints;
    }
}
