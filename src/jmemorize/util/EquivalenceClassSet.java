/**
 * EquivalenceClassSet.java
 * 
 *  @author bret5
 *  Copyright(C) 2007 bret5
 *  
 *  This is a specialized set class which groups the elements into
 *  equivalence classes based on the by the comparator provided at 
 *  set creation time.  Iterators created thru the standard set interface
 *  will return elements sorted by comparator order.  Elements that
 *  are equivalent may be returned in any order.
 *  
 *  The class also provides a special loopIterator iteration 
 *  which will return each element once in comparator order, then loop back
 *  around to the first element at the end.  The loopIterator continues to be valid
 *  as add/remove operations are performed.  If shuffleEquivalenceClasses is set,
 *  it will randomly shuffles the elements in each equivalence class 
 *  every time that equivalence class is reached during iteration.  If not,
 *  each equivalence class will be returned in the same order every time.
 *  shuffleEquivalenceClasses defaults to true.
 *  
 *  This Set does not allow null elements.
 *  
 *  This was written for a program that uses jdk 1.4, so it doesn't yet use the generic style.  
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jmemorize.util;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * @author bret5
 *
 */
public class EquivalenceClassSet<T> extends AbstractSet<T> 
{

    // the list backing the class.  This is a list of the 
    // equivalence classes - each equivalence class is a sublist
    // which contains all the elements equivalent to each other.
    // The sublists appear in order corresponding to the ordering
    // provided by the comparator provided at set creation time. 
    private List                m_equivalenceClasses;
    private int                 m_size; // cached for efficiency
    private Comparator          m_comparator;
    private ListIterator        m_loopEqvClassIter; // non-null index used by the loop iterator
    private List                m_loopCurrentEqvClass; // ptr to current class, null ok
    private ListIterator<T>     m_loopItemIter; // non-null index used by the loop iterator
    private final static List   m_emptyList = new ArrayList(); // emptyList used for gen-ing iterators
    
    // In order to maintain some sanity in the face of objects changing with respect to
    // the comparator after being added to the set, we keep track of which class every object
    // is in.  This way, the behavior of contains and remove are undisturbed by changes to the 
    // objects.
    private HashMap         m_itemToClassMap;
    
    // TODO - size is now redundant with the size of the item-class map, remove...
    
    private int m_changeID; // supports iterator fail-fast, increment on each modification

    private boolean m_shuffleEquivalenceClasses;
    
    /*
     * Class invariants:
     *   Individual equivalence classes may not be empty
     *   The cached size is equal to the sum of all items in the sublists
     *   The changeID increments montonically whenever the set contents are changed,
     *   although it does reset on a clear()
     */
    
    public EquivalenceClassSet(Comparator<T> c) 
    {
        super();
        m_comparator = c;
        m_equivalenceClasses = new LinkedList();
        m_size = 0;
        m_itemToClassMap = new HashMap();
        resetLoopIterator();
        m_changeID = 0;
        m_shuffleEquivalenceClasses = true;
    }

    protected class OnePassIterator implements Iterator<T> 
    {
        private int localChangeID;
        private ListIterator localEqvClassIter; // class iter
        private ListIterator<T> localItemIter; // item iter

        protected OnePassIterator() 
        {
            localChangeID = m_changeID;
            localEqvClassIter = m_equivalenceClasses.listIterator(); // index used by the loop iterator
            localItemIter = null; // null means before the next class
        }
        
        public boolean hasNext() 
        {
            if (m_changeID != localChangeID) 
            {
                throw new ConcurrentModificationException();
            }
            
            if (localEqvClassIter.hasNext() || 
                    (localItemIter != null && localItemIter.hasNext())) 
            {
                return true;
            }
            
            return false;
        }

        public T next() 
        {
            if (m_changeID != localChangeID) 
            {
                throw new ConcurrentModificationException();
            }
            
            if (localItemIter == null || !localItemIter.hasNext()) 
            {
                localItemIter = ((List)localEqvClassIter.next()).listIterator();
            }
            
            return localItemIter.next();
        }

        public void remove() 
        {
            throw new UnsupportedOperationException();
        }            
    }
    
    public Iterator<T> iterator() 
    {
        return new OnePassIterator();
    }
    
    // Note that this iterator is intended to continue iteration after element addition 
    // and removal.  Therefore we put the underlying iterators in the main class so that 
    // they can be adjusted/replaced when necessary
    // invariants:
    //   if size = 0, currentEqvClassIter is null.
    //   if size not null, then both currentEqvClassIter and currentItemIter
    //     are not null.
    protected class LoopIterator implements Iterator<T> 
    {
        public boolean hasNext() 
        {
            return ( m_size > 0 );
        }

        public T next() 
        {
            assert m_loopEqvClassIter != null;
            assert m_loopItemIter != null;
            
            if (m_size <= 0) 
            {
                throw new NoSuchElementException();
            }
            
            // first, move to a new class if necessary, resetting the item iter.
            // if switching classes, shuffle.
            if (!m_loopItemIter.hasNext()) 
            {
                if (!m_loopEqvClassIter.hasNext()) 
                {
                    // recycle to beginning
                    m_loopEqvClassIter = m_equivalenceClasses.listIterator();
                }
                
                m_loopCurrentEqvClass = (List)m_loopEqvClassIter.next();
                assert m_loopCurrentEqvClass.size() > 0;
                
                if (m_shuffleEquivalenceClasses) 
                {
                    java.util.Collections.shuffle(m_loopCurrentEqvClass);
                }
                m_loopItemIter = m_loopCurrentEqvClass.listIterator();                
            }
            
            return m_loopItemIter.next();
        }

        public void remove() 
        {
            throw new UnsupportedOperationException();
        }                               
    }
    
    /**
     * The loopIterator is a special iteration for which hasNext() returns 
     * true if the size is greater than 0.  The next() method 
     * traverses the equivalence classes in comparator order.  Within
     * each equivalence class, the items are returned randomly 
     * (by shuffling the elements in the equivalence class every time 
     * that equivalence class is reached during iteration).
     * 
     * Iteration can be reset to the first equivalence class by using
     * the resetLoopIterator method of the main class.
     *   
     * @return the iterator
     */
    public Iterator<T> loopIterator() 
    {
        return new LoopIterator();
    }
   
    public void resetLoopIterator() 
    {
        m_loopEqvClassIter = m_equivalenceClasses.listIterator();
        m_loopCurrentEqvClass = null;
        m_loopItemIter = m_emptyList.listIterator();        
    }
    
    /**
     * If shuffleEquivalenceClasses is set, the loopItertor will randomly shuffle 
     * the elements in each equivalence class every time that equivalence class 
     * is reached during iteration.
     * 
     * @return the value of shuffleEquivalenceClasses
     */
    public boolean isShuffleEquivalenceClasses() 
    {
        return m_shuffleEquivalenceClasses;
    }

    /**
     * Set the value of shuffleEquivalenceClasses.
     * @see isShuffleEquivalenceClasses()
     */
    public void setShuffleEquivalenceClasses(boolean shuffleEquivalenceClasses) 
    {
        this.m_shuffleEquivalenceClasses = shuffleEquivalenceClasses;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    public int size() 
    {
        return m_size;
    }

    /* 
     * Adds the argument to the collection.  If the element is not already
     * a member of the set, and is equivalent to the current equivalence 
     * class, it is added so that it will be returned before any element
     * from another class is returned.
     */
    public boolean add(T arg0) 
    {
        return addPositional(arg0, true);
    }

    /* 
     * Adds the argument to the collection.  If the element is not already
     * a member of the set, and is equivalent to the current equivalence 
     * class, it is added so that it will not be returned before an element
     * from another class is returned (if there are any other classes).
     */
    public boolean addExpired(T arg0) 
    {
        return addPositional(arg0, false);
    }

    protected boolean addPositional(Object arg0, boolean atEnd) 
    {
        EqvPosition eqvPosition = findEqvClass(arg0);
        boolean isChanged = false;
        if (eqvPosition.matchingEqvClass != null) 
        {
            int iterIdx = 0;
            boolean replaceLoopItemIter = false;
            
            if (eqvPosition.matchingEqvClass == m_loopCurrentEqvClass) 
            {
                // we have to replace the item loop iterator for this class, so get the position
                replaceLoopItemIter = true;
                iterIdx = m_loopItemIter.nextIndex();
            }
            
            if (!eqvPosition.matchingEqvClass.contains(arg0)) 
            {
                if (atEnd) 
                {
                    eqvPosition.matchingEqvClass.add(arg0);
                } 
                else
                {
                    eqvPosition.matchingEqvClass.add(0, arg0);
                    iterIdx += 1;
                }
                
                isChanged = true;
                
                if (replaceLoopItemIter) 
                {
                    m_loopItemIter = m_loopCurrentEqvClass.listIterator(iterIdx);
                }
            }
        } 
        else 
        {
            // there is no matching class, so add one
            ArrayList newEqvClass = new ArrayList();
            newEqvClass.add(arg0);
            eqvPosition.matchingEqvClass = newEqvClass;  // cache the eqv class ref for adding to map
            int iterIdx, addIdx;
            iterIdx = 0;
            addIdx = 0;
            
            if (m_size >= 1) 
            {
                iterIdx = m_loopEqvClassIter.nextIndex();
                addIdx = eqvPosition.eqvClassIter.nextIndex();
                if (addIdx < iterIdx) 
                {
                    iterIdx += 1;
                }
            } 
            eqvPosition.eqvClassIter.add(newEqvClass);                
            isChanged = true;
            
            // replace the class loop iterator
            m_loopEqvClassIter = m_equivalenceClasses.listIterator(iterIdx);
            
            // if the new class is next and the add is "expired"/(not atEnd), 
            // advance the iterator past the just added item
            if (iterIdx == addIdx && !atEnd && !m_loopItemIter.hasNext()) 
            {
                m_loopCurrentEqvClass = (List)m_loopEqvClassIter.next();
                m_loopItemIter = m_loopCurrentEqvClass.listIterator(1);
            }
        }
        
        if (isChanged) 
        {
            m_size += 1;
            m_changeID += 1;
            m_itemToClassMap.put(arg0, eqvPosition.matchingEqvClass);
        }
        
        return isChanged;
    }

    // represents the location of the equivalence class matching a value.
    // returned from findEqvClass, so we only have to write that code once.
    // If there is a matching class, then matchingEqvClass will be non-null.
    // If not, then eqvClassIter holds the position that eqv class would have.
    protected class EqvPosition 
    {
        protected List matchingEqvClass;
        protected ListIterator eqvClassIter;
    }

    // If there is a matching class, then matchingEqvClass will be non-null.
    // If not, then eqvClassIter holds the position that eqv class would have.
    protected EqvPosition findEqvClass(Object arg0) 
    {
        EqvPosition eqvPosition = new EqvPosition();
        if (m_itemToClassMap.containsKey(arg0)) 
        {
            eqvPosition.matchingEqvClass = (List)m_itemToClassMap.get(arg0);
            return eqvPosition;  // note that the iterator will be null.
        }
        
        eqvPosition.eqvClassIter = m_equivalenceClasses.listIterator();
        while (eqvPosition.eqvClassIter.hasNext()) 
        {
            List testEqvClass = (List)eqvPosition.eqvClassIter.next();
            assert testEqvClass.size() > 0;
            int comparison = m_comparator.compare(arg0, testEqvClass.get(0));
            if (comparison < 0) 
            {
                // there is no matching class, to insert before this one, return the previous position
                if (eqvPosition.eqvClassIter.hasPrevious()) 
                {
                    eqvPosition.eqvClassIter.previous();
                }
                break;
            } 
            else if (comparison == 0) 
            {
                eqvPosition.matchingEqvClass = testEqvClass;
                return eqvPosition; 
            }
            // and fall through to the next eqvClass
        }
        return eqvPosition;
    }
    
    /* (non-Javadoc)
     * @see java.util.AbstractCollection#clear()
     */
    public void clear() 
    {
        m_equivalenceClasses.clear();
        m_itemToClassMap.clear();
        m_size = 0;
        m_changeID = 0; // can reset to original state
        resetLoopIterator();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    public boolean contains(Object arg0) 
    {
        EqvPosition eqvPosition = findEqvClass(arg0);
        if (eqvPosition.matchingEqvClass != null &&
                eqvPosition.matchingEqvClass.contains(arg0)) 
        {
            return true; // already a member, do nothing
        }   
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    public boolean remove(Object arg0) 
    {
        assert m_size == m_itemToClassMap.size();
        EqvPosition eqvPosition = findEqvClass(arg0);
        return removeAtPosition(eqvPosition, arg0);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    private boolean removeAtPosition(EqvPosition eqvPosition, Object arg0) 
    {
        // when removing an object, we may have to replace the item loop iterator
        // if we removed an item from the current loop class
        // also, if the removal results in removal of a class, we may have to 
        // replace the class loop iter 
        boolean isChanged = false;
        boolean replaceLoopItemIter = false;
        if (eqvPosition.matchingEqvClass != null) 
        {
            int itemLocationIdx = 0;
            int loopNextItemIdx = 0;
            int itemClassIdx = 0;
            int loopNextClassIdx = 0;
            itemLocationIdx = eqvPosition.matchingEqvClass.indexOf(arg0);
            if (itemLocationIdx >= 0) 
            {
                // the item is a member of this class and will be removed 
                isChanged = true; 
                if (eqvPosition.matchingEqvClass == m_loopCurrentEqvClass) 
                {
                    // we may have to replace the item loop iterator for this class, so get the position
                    replaceLoopItemIter = true;
                    loopNextItemIdx = m_loopItemIter.nextIndex();
                    if (itemLocationIdx < loopNextItemIdx) 
                    {
                        loopNextItemIdx -= 1;
                    }
                }
                eqvPosition.matchingEqvClass.remove(arg0);
                if (eqvPosition.matchingEqvClass.size() <= 0) 
                {
                    // the class is now empty, remove it
                    loopNextClassIdx = m_loopEqvClassIter.nextIndex();
                    itemClassIdx = m_equivalenceClasses.indexOf(eqvPosition.matchingEqvClass);
                    if (itemClassIdx < loopNextClassIdx) 
                    {
                        loopNextClassIdx -= 1;
                    }
                    
                    m_equivalenceClasses.remove(eqvPosition.matchingEqvClass);
                    
                    // and replace the loop iterator, and maybe the item iterator 
                    if (m_equivalenceClasses.size() == 0) 
                    {
                        resetLoopIterator();
                    } 
                    else 
                    {                        
                        m_loopEqvClassIter = m_equivalenceClasses.listIterator(loopNextClassIdx);
                        if (eqvPosition.matchingEqvClass == m_loopCurrentEqvClass) 
                        {
                            m_loopCurrentEqvClass = null;
                            m_loopItemIter = m_emptyList.listIterator(); 
                        }                        
                    }
                } 
                else if (replaceLoopItemIter) 
                {
                    // replace the item iterator for the class
                    m_loopItemIter = m_loopCurrentEqvClass.listIterator(loopNextItemIdx); 
                }   
            }
        }
        
        if (isChanged) 
        {
            m_itemToClassMap.remove(arg0);
            m_size -= 1;
            assert m_size >= 0;
            assert m_size == m_itemToClassMap.size();
            m_changeID += 1;
        }
        
        return isChanged;
    }
    
    public Comparator<T> getComparator() 
    {
        return m_comparator;
    }
    
    /**
     * Partition this set into two sets, returning the new one.
     * 
     * The argument specifies how many elements to put in the new set.  Elements will be 
     * chosen in comparator order.  All elements put into the new set will be removed from 
     * this set.  If the original set contained less elements than the argument, then
     * after the partition the new set will contain all the elements and the original set
     * will be empty.
     *  
     * If the partition is non-trivial (that is, if the new set contains at least one 
     * element), then the counters for the loop iterator will be reset.
     *  
     * @param numberToRemove number of elements to remove from the original set
     * 
     * @return the new set
     */
    public EquivalenceClassSet<T> partition(int numberToRemove) 
    {
        EquivalenceClassSet<T> newSet = new EquivalenceClassSet<T>(m_comparator);
        while (numberToRemove > 0 && m_size > 0) 
        {
            ArrayList firstEqvClass = (ArrayList)(m_equivalenceClasses.get(0));
            int sizeOfFEqvClass = firstEqvClass.size();
            int numberMoved = 0;
            List movedEqvClass;
            if (numberToRemove >= sizeOfFEqvClass) 
            {
                movedEqvClass = (List)m_equivalenceClasses.remove(0);
                newSet.m_equivalenceClasses.add(movedEqvClass);
                numberMoved = sizeOfFEqvClass;
                
            } 
            else 
            {
                // shuffle the equivalence class prior to a partial selection
                if (m_shuffleEquivalenceClasses) 
                {
                    java.util.Collections.shuffle(firstEqvClass);
                }
                
                movedEqvClass = new ArrayList(firstEqvClass.subList(0, numberToRemove));
                firstEqvClass.subList(0, numberToRemove).clear();
                newSet.m_equivalenceClasses.add(movedEqvClass);
                numberMoved = numberToRemove;
            }
            
            m_size -= numberMoved;
            newSet.m_size += numberMoved;
            numberToRemove -= numberMoved;
            
            // now fix up the item to class map
            Iterator iter = movedEqvClass.iterator();
            while (iter.hasNext()) 
            {
                Object obj = iter.next();
                m_itemToClassMap.remove(obj);                
                newSet.m_itemToClassMap.put(obj, movedEqvClass);                
            }
        }
        
        if (newSet.size() > 0) 
        {
            newSet.resetLoopIterator();
            resetLoopIterator();
            m_changeID += 1;
            newSet.m_changeID += 1;
        }
        assert m_size == m_itemToClassMap.size(); 
        assert newSet.m_size == newSet.m_itemToClassMap.size(); 
        return newSet;
    }
    
    /**
     * In some cases, the equivalence class of an object will change.  This can leave
     * the list in an inconsistent state.  It is essential to fix the problem. 
     * This method moves the object to the correct equivalence class, keeping the 
     * loopIterator where it was.
     * 
     * @param arg0 The object to move.
     * @return true if the object is a member of the set, false otherwise.
     */
    public boolean resetEquivalenceClass(T arg0) 
    {
        boolean found = false;
        EqvPosition eqvPosition = new EqvPosition();
        eqvPosition.matchingEqvClass = (List)m_itemToClassMap.get(arg0);
        
        if (eqvPosition.matchingEqvClass != null) 
        {
            removeAtPosition(eqvPosition, arg0);
            found = true;
        }
        
        if (found) 
        {
            addExpired(arg0);
        }
        
        return found;
    }
}
