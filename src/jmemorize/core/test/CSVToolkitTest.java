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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.Lesson;
import jmemorize.core.io.CsvBuilder;
import jmemorize.core.io.CsvBuilder.BadHeaderException;
import jmemorize.gui.Localization;
import junit.framework.TestCase;

public class CSVToolkitTest extends TestCase
{
    private static final Charset CHARSET = Charset.forName("UTF-8");
    
    public void testImportNeedsHeader() throws IOException
    {
        InputStream in = getFileStream("bad_noheader.csv");
        
        try 
        {
            CsvBuilder.importLesson(in, ',', CHARSET, new Lesson(false));
            fail("Expected exception not thrown.");
            
        } 
        catch (BadHeaderException e)
        {
            // fallthrough, expected
        }
    }
    
    public void testImportNeedsFrontAndFlipHeaderColumns() throws IOException
    {
        InputStream in = getFileStream("bad_missingheadercolumn.csv");
        
        try 
        {
            CsvBuilder.importLesson(in, ',', CHARSET, new Lesson(false));
            fail("Expected exception not thrown.");
            
        } 
        catch (BadHeaderException e)
        {
            // fallthrough, expected
        }
    }    
    
    public void testImportWithoutCategories() throws IOException, BadHeaderException
    {
        InputStream in = getFileStream("withoutCategories.csv");
        Lesson lesson = new Lesson(false);
        CsvBuilder.importLesson(in, ',', CHARSET, lesson);
        List<Card> cards = lesson.getRootCategory().getCards();
        
        Card card = (Card)cards.get(0);
        assertEquals("germany", card.getFrontSide().getText().getUnformatted());
        assertEquals("berlin", card.getBackSide().getText().getUnformatted());
        assertEquals(lesson.getRootCategory(), card.getCategory());
        
        card = (Card)cards.get(1);
        assertEquals("france", card.getFrontSide().getText().getUnformatted());
        assertEquals("paris", card.getBackSide().getText().getUnformatted());
        assertEquals(lesson.getRootCategory(), card.getCategory());
    }
    
    public void testImportWithCategories() throws IOException, BadHeaderException
    {
        InputStream in = getFileStream("withCategories.csv");
        Lesson lesson = new Lesson(false);
        CsvBuilder.importLesson(in, ',', CHARSET, lesson);
        
        List<Card> cards = lesson.getRootCategory().getCards();
        
        assertCard("germany", "berlin", "capital",  (Card)cards.get(0));
        assertCard("france",  "paris",  "capital",  (Card)cards.get(1));
        assertCard("germany", "german", "language", (Card)cards.get(2));
    }
    
    public void testImportWithCategoriesTwice() 
        throws IOException, BadHeaderException
    {
        Lesson lesson = new Lesson(false);
        
        InputStream in = getFileStream("withCategories.csv");
        CsvBuilder.importLesson(in, ',', CHARSET, lesson);
        in = getFileStream("withCategories.csv");
        CsvBuilder.importLesson(in, ',', CHARSET, lesson);
        
        List<Card> cards = lesson.getRootCategory().getCards();
        sortByFrontside(cards);
        
        assertCard("france",  "paris",  "capital",  (Card)cards.get(0));
        assertCard("france",  "paris",  "capital",  (Card)cards.get(1));
        
        assertCard("germany", "berlin", "capital",  (Card)cards.get(2));
        assertCard("germany", "berlin", "capital",  (Card)cards.get(3));
        
        assertCard("germany", "german", "language", (Card)cards.get(4));
        assertCard("germany", "german", "language", (Card)cards.get(5));
    }
    
    public void testImportWithDecks() throws IOException, BadHeaderException
    {
        InputStream in = getFileStream("withDecks.csv");
        Lesson lesson = new Lesson(false);
        CsvBuilder.importLesson(in, ',', CHARSET, lesson);
        
        List<Card> cards = lesson.getRootCategory().getCards();
        sortByFrontside(cards);
        
        String root = Localization.get("General.ROOT_CATEGORY");
        assertCard("england", "london", root, 0, (Card)cards.get(0));
        assertCard("france",  "paris",  root, 0, (Card)cards.get(1));
        assertCard("germany", "berlin", root, 2, (Card)cards.get(2));
        assertCard("spain",   "madrid", root, 5, (Card)cards.get(3));
        
        assertNull(((Card)cards.get(0)).getDateExpired());
        assertNull(((Card)cards.get(1)).getDateExpired());
        assertNotNull(((Card)cards.get(2)).getDateExpired());
        assertNotNull(((Card)cards.get(3)).getDateExpired());
    }
    
    public void testImportDontCreateEmptyCard() throws BadHeaderException
    {
        try
        {
            FileInputStream in =
                new FileInputStream("test/fixtures/csv/bad_emptycard.csv");
            CsvBuilder.importLesson(in, ',', CHARSET, new Lesson(false));
            fail("Expected exception not thrown.");
        } 
        catch (IOException e)
        {
            // fallthrough, expected
        }
    }
    
    public void testExportLesson() throws IOException, BadHeaderException
    {
        Category rootCategory = new Category("alltest");
        rootCategory.addCard(new Card("front1", "flip1"));
        rootCategory.addCard(new Card("front2", "flip2"), 2);
        Lesson lesson = new Lesson(rootCategory, true);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvBuilder.exportLesson(out, lesson, ',', CHARSET);
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Lesson lesson2 = new Lesson(false);
        CsvBuilder.importLesson(in, ',', CHARSET, lesson2);
        List<Card> cards2 = lesson2.getRootCategory().getCards();
        
        String root = Localization.get("General.ROOT_CATEGORY");
        assertCard("front1", "flip1", root, 0, (Card)cards2.get(0));
        assertCard("front2", "flip2", root, 2, (Card)cards2.get(1));
    }
    
    private InputStream getFileStream(String file) throws FileNotFoundException
    {
        return new FileInputStream("test/fixtures/csv/"+file);
    }
    
    private static void sortByFrontside(List<Card> cards)
    {
        Collections.sort(cards, new Comparator<Card>(){
            public int compare(Card c1, Card c2)
            {
                if (!c1.getFrontSide().equals(c2.getFrontSide()))
                    return c1.getFrontSide().getText().getUnformatted().compareTo(
                        c2.getFrontSide().getText().getUnformatted());
                else
                    return c1.getBackSide().getText().getUnformatted().compareTo(
                        c2.getBackSide().getText().getUnformatted());
            }
        });
    }

    private static void assertCard(String front, String flip, String category, Card card)
    {
        assertEquals(front, card.getFrontSide().getText().getUnformatted());
        assertEquals(flip, card.getBackSide().getText().getUnformatted());
        assertEquals(category, card.getCategory().getName());
    }
    
    private static void assertCard(String front, String flip, String category, 
        int level, Card card)
    {
        assertCard(front, flip, category, card);
        assertEquals(level, card.getLevel());
    }
}
