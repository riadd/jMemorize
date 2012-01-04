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

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for jmemorize.core.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(CardTest.class);
        suite.addTestSuite(CategoryTest.class);
        suite.addTestSuite(LessonProviderTest.class);
        suite.addTestSuite(FormattedTextTest.class);
        
        suite.addTestSuite(EquivalenceClassSetTest.class);
        suite.addTestSuite(EquivalenceClassSetTest2.class);
        
        suite.addTestSuite(LearnSessionTest.class);
        suite.addTestSuite(LearnSettingsTest.class);
        suite.addTestSuite(LearnSessionLargeTest.class);
        suite.addTestSuite(LearnSessionShufflingTest.class);
        suite.addTestSuite(LearnHistoryTest.class);
        
        suite.addTestSuite(LocalizationTest.class);
        suite.addTestSuite(ImageRepositoryTest.class);
        suite.addTestSuite(CSVToolkitTest.class);
        //$JUnit-END$
        return suite;
    }
}
