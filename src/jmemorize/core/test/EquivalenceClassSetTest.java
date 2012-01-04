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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jmemorize.util.EquivalenceClassSet;
import junit.framework.TestCase;

/**
 * @author bret5
 * 
 */
public class EquivalenceClassSetTest extends TestCase
{
    private EquivalenceClassSet mod3EqvSet;

    protected void setUp()
    {
        Comparator mod3cmp = new Comparator()
        {
            public int compare(Object arg0, Object arg1)
            {
                if (!(arg0 instanceof Integer) && (arg1 instanceof Integer))
                {
                    throw new ClassCastException();
                }
                int arg0mod3 = ((Integer)arg0).intValue() % 3;
                int arg1mod3 = ((Integer)arg1).intValue() % 3;
                if (arg0mod3 < arg1mod3)
                    return -1;
                if (arg0mod3 == arg1mod3)
                    return 0;
                return 1;
            }
        };
        
        mod3EqvSet = new EquivalenceClassSet(mod3cmp);
        assert mod3EqvSet.getComparator().equals(mod3cmp);
        
        // now populate the set
        int expectedSize = 0;
        for (int i = 3; i <= 9; i++)
        {
            assertTrue(mod3EqvSet.add(new Integer(i)));
            expectedSize += 1;
            if (i > 3)
            {
                assertFalse(mod3EqvSet.add(new Integer(i - 1)));
            }
            assertEquals(expectedSize, mod3EqvSet.size());
        }
    }

    public void testIterator()
    {
        Iterator iter = mod3EqvSet.iterator();
        int lastModVal = 0;
        int nValues = 0;
        while (iter.hasNext())
        {
            int value = ((Integer)(iter.next())).intValue();
            nValues += 1;
            int modVal = value % 3;
            assertTrue(modVal >= lastModVal);
            lastModVal = modVal;
        }
        assertEquals(nValues, mod3EqvSet.size());
    }

    public void testAddAll()
    {
        EquivalenceClassSet newSet = new EquivalenceClassSet(mod3EqvSet.getComparator());
        List randomOrderList = new ArrayList(mod3EqvSet);
        Collections.shuffle(randomOrderList);
        newSet.addAll(randomOrderList);
        Set interSet = new HashSet(newSet);
        interSet.retainAll(mod3EqvSet);
        assertTrue(interSet.size() == mod3EqvSet.size());
    }

    protected void internalSanityTestLoopIterator(int iters, boolean isShuffle)
    {
        if (!isShuffle)
        {
            mod3EqvSet.setShuffleEquivalenceClasses(isShuffle);
        }
        
        int hashCode = mod3EqvSet.hashCode();
        Iterator loopIter = mod3EqvSet.loopIterator();
        int items = mod3EqvSet.size();
        for (int i = 0; i < iters; i++)
        {
            int lastModVal = 0;
            int lastVal = 0;
            for (int j = 0; j < items; j++)
            {
                assertTrue(loopIter.hasNext());
                int value = ((Integer)(loopIter.next())).intValue();
                int modVal = value % 3;
                assertTrue(modVal >= lastModVal);
                if (!isShuffle)
                {
                    assertTrue(modVal != lastModVal || value >= lastVal);
                    lastVal = value;
                }
                lastModVal = modVal;
            }
            assertTrue(mod3EqvSet.hashCode() == hashCode);
        }
    }

    public void testLoopIteratorNoChanges()
    {
        internalSanityTestLoopIterator(4, true);
    }

    public void testNoShuffle()
    {
        internalSanityTestLoopIterator(4, false);
    }

    public void internalTestLoopIteratorAddToClass(int addVal, boolean expired,
        boolean expectedInCycle)
    {
        // add something to a following class (increases the number of items
        // till back to 0)
        Iterator loopIter = mod3EqvSet.loopIterator();
        int expectedCount = mod3EqvSet.size();
        int expectedSize = mod3EqvSet.size() + 1;
        if (expectedInCycle)
        {
            expectedCount += 1;
        }
        
        int lastModVal = 0;
        boolean isAdded = false;
        boolean isFound = false;
        
        for (int j = 0; j < expectedCount + 1; j++)
        {
            // note extra item - we want to wrap around to verify the first item
            assertTrue(loopIter.hasNext());
            int value = ((Integer)(loopIter.next())).intValue();
            if (value == addVal && (j < expectedCount))
            {
                isFound = true;
            }
            int modVal = value % 3;
            if (j < expectedCount)
            {
                assertTrue(modVal >= lastModVal);
            }
            else
            {
                assertTrue(modVal == 0);
            }
            lastModVal = modVal;
            if (modVal == 1 && !isAdded)
            {
                isAdded = true;
                if (!expired)
                {
                    assertTrue(mod3EqvSet.add(new Integer(addVal)));
                }
                else
                {
                    assertTrue(mod3EqvSet.addExpired(new Integer(addVal)));
                }
            }
        }
        
        if (expectedInCycle)
        {
            assertTrue(isFound);
        }
        else
        {
            assertFalse(isFound);
        }
        
        assertTrue(mod3EqvSet.contains(new Integer(addVal)));
        assertTrue(new ArrayList(mod3EqvSet).size() == expectedSize);
        mod3EqvSet.resetLoopIterator();
        internalSanityTestLoopIterator(1, true);
    }

    public void testLoopIteratorAddToFollowingClass()
    {
        // add something to a following class (increases the number of items
        // till back to 0)
        internalTestLoopIteratorAddToClass(32, false, true);
    }

    public void testLoopIteratorAddToPrevClass()
    {
        // add something to a prior class (doesn't increase the number of items
        // till back to 0)
        internalTestLoopIteratorAddToClass(30, false, false);
    }

    public void testLoopIteratorAddToCurrentClass()
    {
        // add something to the current class - add
        internalTestLoopIteratorAddToClass(31, false, true);
        // add something to the current class - addExpired
        // test remove!
    }

    public void testLoopIteratorAddToCurrentClassExpired()
    {
        // add something to the current class - addExpired
        internalTestLoopIteratorAddToClass(31, true, false);
    }

    public void internalTestLoopIteratorRemoveFromClass(int remVal, boolean expectedToReduceCycle,
        boolean removeAlreadyFound)
    {
        // remove something from a class and test loop iterator behavior
        Iterator loopIter = mod3EqvSet.loopIterator();
        int expectedSize = mod3EqvSet.size() - 1;
        int expectedCount = mod3EqvSet.size();
        if (expectedToReduceCycle)
        {
            expectedCount -= 1;
        }
        
        int lastModVal = 0;
        boolean isRemoved = false;
        for (int j = 0; j < expectedCount + 1; j++)
        {
            // note extra item - we want to wrap around to verify the first item
            assertTrue(loopIter.hasNext());
            int value = ((Integer)(loopIter.next())).intValue();
            int modVal = value % 3;
            
            if (j < expectedCount)
            {
                assertTrue(modVal >= lastModVal);
            }
            else
            {
                assertTrue(modVal == 0);
            }
            
            lastModVal = modVal;
            if (modVal == 1 && !isRemoved)
            {
                if (remVal % 3 == 1)
                {
                    if (value % 3 == modVal)
                    {
                        isRemoved = true;
                        if (removeAlreadyFound)
                        {
                            remVal = value;
                        }
                        else
                        {
                            // this relies on knowing that the two values in the
                            // set are 11 and 4
                            remVal = 11 - value;
                        }
                        assertTrue(mod3EqvSet.remove(new Integer(remVal)));
                    }
                }
                else
                {
                    isRemoved = true;
                    assertTrue(mod3EqvSet.remove(new Integer(remVal)));
                }
            }
        }
        
        assertFalse(mod3EqvSet.contains(new Integer(remVal)));
        assertTrue(new ArrayList(mod3EqvSet).size() == expectedSize);
        mod3EqvSet.resetLoopIterator();
        internalSanityTestLoopIterator(1, true);
    }

    public void testLoopIteratorRemoveFromPrevClass()
    {
        // remove something from a prior class (no effect on items till back to 0)
        internalTestLoopIteratorRemoveFromClass(6, false, false);
    }

    public void testLoopIteratorRemoveFromFollowingClass()
    {
        // remove something from a following class (reduces items till back to 0)
        internalTestLoopIteratorRemoveFromClass(8, true, false);
    }

    public void testLoopIteratorRemoveFromCurrentClass1()
    {
        // remove something not yet found from current class (reduces items till back to 0)
        internalTestLoopIteratorRemoveFromClass(7, true, false);
    }

    public void testLoopIteratorRemoveFromCurrentClass2()
    {
        // remove something found from current class (no effect on items till back to 0)
        internalTestLoopIteratorRemoveFromClass(7, false, true);
    }

    public void internalTestLoopIteratorRemoveList(List remValues, 
        int expectedCycleReduction, int expectedFirstModVal)
    {
        // remove something from a class and test loop iterator behavior
        Iterator loopIter = mod3EqvSet.loopIterator();
        int expectedCount = mod3EqvSet.size() - expectedCycleReduction;
        int expectedSize = mod3EqvSet.size() - remValues.size();
        int lastModVal = 0;
        boolean isRemoved = false;
        
        for (int j = 0; j < expectedCount + 1; j++)
        {
            // note extra item - we want to wrap around to verify the first item
            assertTrue(loopIter.hasNext());
            int value = ((Integer)(loopIter.next())).intValue();
            int modVal = value % 3;
            
            if (j < expectedCount)
            {
                assertTrue(modVal >= lastModVal);
            }
            else
            {
                assertTrue(modVal == expectedFirstModVal);
            }
            
            lastModVal = modVal;
            if (modVal == 1 && !isRemoved)
            {
                isRemoved = true;
                
                Iterator iter = remValues.iterator();
                while (iter.hasNext())
                {
                    Integer remVal = (Integer)iter.next();
                    assertTrue(mod3EqvSet.remove(remVal));
                }
            }
        }
        
        Iterator iter = remValues.iterator();
        while (iter.hasNext())
        {
            Integer remVal = (Integer)iter.next();
            assertFalse(mod3EqvSet.contains(remVal));
        }
        
        assertTrue(new ArrayList(mod3EqvSet).size() == expectedSize);
        mod3EqvSet.resetLoopIterator();
        internalSanityTestLoopIterator(1, true);
    }

    public void testLoopIteratorRemoveEntireFollowingClass()
    {
        Integer[] vals = { new Integer(8), new Integer(5) };
        internalTestLoopIteratorRemoveList(Arrays.asList(vals), vals.length, 0);
    }

    public void testLoopIteratorRemoveEntirePrevClass()
    {
        Integer[] vals = { new Integer(6), new Integer(3), new Integer(9) };
        internalTestLoopIteratorRemoveList(Arrays.asList(vals), 0, 1);
    }

    public void testLoopIteratorRemoveEntireCurrentClass()
    {
        Integer[] vals = { new Integer(7), new Integer(4) };
        internalTestLoopIteratorRemoveList(Arrays.asList(vals), vals.length - 1, 0);
    }

    public void testPartition()
    {
        int originalSize = mod3EqvSet.size();
        int NUM_TO_REMOVE = 4;
        EquivalenceClassSet newSet = mod3EqvSet.partition(NUM_TO_REMOVE);
        assertTrue(NUM_TO_REMOVE == newSet.size());
        assertTrue(mod3EqvSet.size() + newSet.size() == originalSize);
        
        // now make sure the order is right
        Iterator iter = mod3EqvSet.iterator();
        Iterator newSetIter = newSet.iterator();
        int lastModVal = 0;
        int nValues = 0;
        
        while (newSetIter.hasNext())
        {
            int value = ((Integer)(newSetIter.next())).intValue();
            nValues += 1;
            int modVal = value % 3;
            assertTrue(modVal >= lastModVal);
            lastModVal = modVal;
        }
        
        assertEquals(nValues, NUM_TO_REMOVE);
        nValues = 0;
        
        while (iter.hasNext())
        {
            int value = ((Integer)(iter.next())).intValue();
            nValues += 1;
            int modVal = value % 3;
            assertTrue(modVal >= lastModVal);
            lastModVal = modVal;
        }
        
        assertEquals(nValues, originalSize - NUM_TO_REMOVE);
        Set interSet = new HashSet(newSet);
        interSet.retainAll(mod3EqvSet);
        assertTrue(interSet.size() == 0);
    }
}
