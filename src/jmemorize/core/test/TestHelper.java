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
package jmemorize.core.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;

public class TestHelper
{
    public static <T> void assertList(T[] expected, List<?> actual)
    {
        boolean equals = expected.length == actual.size();

        if (equals)
        {
            for (int i = 0; i < expected.length; i++)
            {
                if (!expected[i].equals(actual.get(i)))
                {
                    equals = false;
                    break;
                }
            }
        }

        if (!equals)
        {
            List<T> expectedList = new ArrayList<T>(expected.length);
            for (int i = 0; i < expected.length; i++)
            {
                expectedList.add(expected[i]);
            }

            throw new AssertionFailedError("expected:" + expectedList + " but was " + actual);
        }
    }
    
    // note that expected is converted to a *set*, so duplicates will be ignored and so will order.
    public static <T> void assertSet(T[] expected, Collection<T> actual)
    {
        boolean equals = expected.length == actual.size();

        List<T> expectedList = Arrays.asList(expected);
        if (equals)
        {
            Set<T> testSet = new HashSet<T>(expectedList);
            testSet.retainAll(actual); // set intersection
            equals = testSet.size() == actual.size();
        }

        if (!equals)
        {
            throw new AssertionFailedError("expected:" + expectedList + " but was " + actual);
        }
    }
    
    public static <T> void assertSet(Collection<T> actual, T ... expected)
    {
        boolean equals = expected.length == actual.size();

        List<T> expectedList = Arrays.asList(expected);
        if (equals)
        {
            Set<T> testSet = new HashSet<T>(expectedList);
            testSet.retainAll(actual); // set intersection
            equals = testSet.size() == actual.size();
        }

        if (!equals)
        {
            throw new AssertionFailedError("expected:" + expectedList + " but was " + actual);
        }
    }
}
