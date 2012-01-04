/**
 *  @author bret5
 * Copyright(C) 2007 bret5
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
 * */
package jmemorize.core.test;

import java.util.Comparator;
import java.util.Iterator;

import jmemorize.util.EquivalenceClassSet;
import junit.framework.TestCase;

/**
 * @author bret5
 * 
 */
public class EquivalenceClassSetTest2 extends TestCase
{

    class IntWrapper
    {
        public int val;

        IntWrapper(int value)
        {
            val = value;
        }
    }

    private EquivalenceClassSet intWrapEqvSet;
    private IntWrapper[]        iwHandles;

    protected void setUp()
    {
        Comparator intWCmp = new Comparator()
        {
            public int compare(Object arg0, Object arg1)
            {
                if (!(arg0 instanceof IntWrapper) && (arg1 instanceof IntWrapper))
                {
                    throw new ClassCastException();
                }
                int arg0val = ((IntWrapper)arg0).val;
                int arg1val = ((IntWrapper)arg1).val;
                if (arg0val < arg1val)
                    return -1;
                if (arg0val == arg1val)
                    return 0;
                return 1;
            }
        };
        intWrapEqvSet = new EquivalenceClassSet(intWCmp);
        iwHandles = new IntWrapper[3];
        assert intWrapEqvSet.getComparator().equals(intWCmp);
        // now populate the set
        int expectedSize = 0;
        int j = 0;
        for (int i = 1; i <= 12; i++)
        {
            IntWrapper iw = new IntWrapper(i % 4);
            assertTrue(intWrapEqvSet.add(iw));
            expectedSize += 1;
            if (i > 4)
            {
                assertFalse(intWrapEqvSet.add(iw));
            }
            if (i % 4 == 2)
            {
                iwHandles[j++] = iw;
            }
            assertEquals(expectedSize, intWrapEqvSet.size());
        }
    }

    public void sanityTestIterator(int expectedSize)
    {
        Iterator iter = intWrapEqvSet.iterator();
        int lastVal = 0;
        int nValues = 0;
        while (iter.hasNext())
        {
            int value = ((IntWrapper)(iter.next())).val;
            nValues += 1;
            assertTrue(value >= lastVal);
            lastVal = value;
        }
        assertEquals(nValues, intWrapEqvSet.size());
        assertEquals(nValues, expectedSize);
    }

    public void testModifyAndRemoveSetMembers()
    {
        for (int i = 0; i < 3; i++)
        {
            iwHandles[i].val = 5 - i;
            assertTrue(intWrapEqvSet.remove(iwHandles[i]));
            sanityTestIterator(12 - (i + 1));
        }
    }

    public void testModifyAndResetSetMembers()
    {
        for (int i = 0; i < 3; i++)
        {
            iwHandles[i].val = 5 - i;
            assertTrue(intWrapEqvSet.resetEquivalenceClass(iwHandles[i]));
            sanityTestIterator(12);
        }
    }
}
