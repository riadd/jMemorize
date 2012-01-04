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
package jmemorize.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.FormattedText;
import jmemorize.core.Lesson;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.csvreader.CsvReader.CatastrophicException;
import com.csvreader.CsvReader.FinalizedException;

/**
 * A class for importing and exporting character-separated-values (CSV).
 * 
 * @author djemili
 */
public class CsvBuilder
{
    public static final String FRONTSIDE_COL = "Frontside";
    public static final String FLISIDE_COL   = "Flipside";
    public static final String CATEGORY_COL  = "Category";
    public static final String LEVEL_COL     = "Level";
    
    /**
     * Is thrown when the header is missing or malformatted.
     */
    public static class BadHeaderException extends Exception
    {
        public BadHeaderException(String message)
        {
            super(message);
        }
    }
    
    /**
     * Exports the given lesson to a CSV-file with given delimiter and given
     * character set.
     */
    public static void exportLesson(OutputStream out, Lesson lesson, 
        char delimiter, Charset charset) throws IOException
    {
        try
        {
            CsvWriter writer = new CsvWriter(out, delimiter, charset);
            writeHeader(writer);
            
            List<Card> cards = lesson.getRootCategory().getCards();
            for (Card card : cards)
            {
                writer.write(card.getFrontSide().getText().getFormatted());
                writer.write(card.getBackSide().getText().getFormatted());
                
                if (lesson.getRootCategory() == card.getCategory())
                    writer.write("");
                else
                    writer.write(card.getCategory().getName());
                
                writer.write(Integer.toString(card.getLevel()));
                writer.endRecord();
            }
            
            writer.close();
        }
        catch (com.csvreader.CsvWriter.FinalizedException e)
        {
            throw new IOException(e.getMessage());
        }
    }
    
    
    /**
     * Parses the given file that holds text values that are delimited by given
     * delimiter. The values are used to contruct a lesson.
     * @param delimiter the delimiter that is used to separate values in the
     * file.
     * @param charset the character set that the file contents are using.
     * @param file the file that should be read.
     * @param charset the character set that is used in the file. Use
     * <code>null</code> to use the default charset.
     * @param the lesson to which the contents of the file will be added.
     * 
     * @throws IOException if the file couldn't be read or the values are
     * malformatted.
     */
    public static void importLesson(InputStream in, char delimiter, 
        Charset charset, Lesson lesson) throws IOException, BadHeaderException 
    {
        CsvReader reader = new CsvReader(in, delimiter, charset);
        
        Category rootCategory = lesson.getRootCategory();
        Map<String, Category> categories = new HashMap<String, Category>();
        
        List<Category> childCategories = rootCategory.getChildCategories();
        for (Category category : childCategories)
        {
            categories.put(category.getName(), category);
        }
        
        try
        {
            reader.readHeaders();
            
            String[] headers = reader.getHeaders();
            validateHeader(headers);
            
            while (reader.readRecord())
            {
                FormattedText frontSide = FormattedText.formatted(reader.get(FRONTSIDE_COL));
                FormattedText flipSide = FormattedText.formatted(reader.get(FLISIDE_COL));
                
                if (frontSide.getUnformatted().length() == 0 || flipSide.getUnformatted().length() == 0)
                    throw new IOException("You have to specify at least a front " +
                        "side and flip side for every card "+getLineString(reader)+".");
                
                Card card = new Card(frontSide, flipSide);
                
                Category category;
                String categoryName = reader.get(CATEGORY_COL);
                if (categoryName.length() == 0 || 
                    categoryName.equalsIgnoreCase(rootCategory.getName()))
                {
                    category = rootCategory;
                }
                else
                {
                    if (categories.containsKey(categoryName))
                    {
                        category = (Category)categories.get(categoryName);
                    }
                    else
                    {
                        category = new Category(categoryName);
                        rootCategory.addCategoryChild(category);
                        categories.put(categoryName, category);
                    }
                }
                
                String level = reader.get(LEVEL_COL);
                if (level.length() > 0)
                {
                    category.addCard(card, Integer.parseInt(level));
                }
                else
                {
                    category.addCard(card);
                }
            }

            reader.close();
        } 
        catch (FinalizedException e)
        {
            throw new IOException(e.toString());
        } 
        catch (CatastrophicException e)
        {
            throw new IOException(e.toString());
        }
    }

    private static void writeHeader(CsvWriter writer) throws IOException, 
        com.csvreader.CsvWriter.FinalizedException
    {
        writer.write(FRONTSIDE_COL);
        writer.write(FLISIDE_COL);
        writer.write(CATEGORY_COL);
        writer.write(LEVEL_COL);
        writer.endRecord();
    }


    private static void validateHeader(String[] headers) throws BadHeaderException
    {
        boolean hasFront = false;
        boolean hasFlip = false;
        for (int i = 0; i < headers.length; i++)
        {
            if (headers[i].equalsIgnoreCase(FRONTSIDE_COL))
            {
                hasFront = true;
                continue;
            }
                
            if (headers[i].equalsIgnoreCase(FLISIDE_COL))
            {
                hasFlip = true;
                continue;
            }
            
            if (!headers[i].equalsIgnoreCase(CATEGORY_COL) && 
                !headers[i].equalsIgnoreCase(LEVEL_COL))
                    throw new BadHeaderException("Unknown header column: "+headers[i]);
        }
        
        if (!hasFront || !hasFlip)
            throw new BadHeaderException("The first line needs to specify "+
                "the header, which must have at least contain the columns '"+
                FRONTSIDE_COL+ "' and '"+FLISIDE_COL+"'.");

    }
    
    private static String getLineString(CsvReader reader)
    {
        return "(line "+ reader.getCurrentRecord() +")";
    }
}
