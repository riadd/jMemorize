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
package jmemorize.core.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.Lesson;
import jmemorize.core.Main;
import jmemorize.core.Settings;
import jmemorize.gui.swing.CardFont.FontType;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;

/**
 * @author jan stamer
 * @author djemili
 */
public class PdfRtfBuilder
{
    private final static int PDF_MODE = 0;
    private final static int RTF_MODE = 1;

    // These get set in export() prior to building the pdf/rtf
    private static Logger logger;
    private static Font frontFont;
    private static Font backFont;
    
    /**
     * Export lesson to pdf
     * 
     * @param lesson given lesson
     * @param file resulting pdf file
     * @throws IOException writing to file fails
     * @throws DocumentException 
     */
    public static void exportLessonToPDF(Lesson lesson, File file) throws IOException
    {
        export(lesson, PDF_MODE, file);
    }

    public static void exportLessonToRTF(Lesson lesson, File file) throws IOException
    {
        export(lesson, RTF_MODE, file);
    }
    
    private static void export(Lesson lesson, int mode, File file) throws IOException 
    {
        logger = Main.getLogger();

        FontFactory.registerDirectories();
        // set up the fonts we will use to write the front and back of cards
        String frontFontName = Settings.loadFont(FontType.CARD_FRONT).getFont().getFamily();
        frontFont = FontFactory.getFont(frontFontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        
        if (frontFont == null) 
        {
            logger.warning("FontFactory returned null (front) font for: " + frontFontName);
        }
        
        String backFontName = Settings.loadFont(FontType.CARD_FLIP).getFont().getFamily();
        backFont = FontFactory.getFont(backFontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        
        if (backFont == null) 
        {
            logger.warning("FontFactory returned null (back) font for: " + backFontName);
        }

        try
        {
            Document doc = new Document();
            OutputStream out = new FileOutputStream(file);
            
            switch(mode)
            {
            case PDF_MODE:
                PdfWriter.getInstance(doc, out);
                break;
                
            case RTF_MODE:
                RtfWriter2.getInstance(doc, out);
                break;
            }

            doc.setHeader(new HeaderFooter(new Phrase(file.getName()), false));
            doc.open();

            // add cards in subtrees
            List<Category> subtree = lesson.getRootCategory().getSubtreeList();
            for (Category category : subtree)
            {
                writeCategory(doc, category);
            }

            doc.close();

        }
        catch (Throwable t)
        {
            throw (IOException)new IOException("Could not export to PDF").initCause(t);
        }
    }

    /**
     * Adds given category to document
     * 
     * @param doc document to add to
     * @param category given category
     */
    private static void writeCategory(Document doc, Category category) 
        throws DocumentException
    {
        // ignore empty categories
        if (category.getLocalCards().size() == 0)
        {
            return;
        }
    
        writeCategoryHeader(doc, category);
        
        for (Card card : category.getLocalCards())
        {
            writeCard(doc, card);
        }
    }

    private static void writeCategoryHeader(Document doc, Category category) 
        throws DocumentException
    {
        Chunk chunk = new Chunk(category.getPath());
        chunk.setFont(new Font(Font.HELVETICA, 12, Font.BOLD));
    
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setSpacingBefore(1f);
        
        doc.add(paragraph);
    }

    /**
     * Adds given card to document
     * 
     * @param doc document to add to
     * @param card given card
     */
    private static void writeCard(Document doc, Card card) 
        throws DocumentException
    {
        Table table = new Table(2);
    
        table.setPadding(3f);
        table.setBorderWidth(1.0f);
        table.setTableFitsPage(true);
        table.complete();
    
        Phrase front = new Phrase(card.getFrontSide().getText().getUnformatted(), frontFont);
        table.addCell(front);
        Phrase back = new Phrase(card.getBackSide().getText().getUnformatted(), backFont);
        table.addCell(back);
    
        doc.add(table);
    }
}