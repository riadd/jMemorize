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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import jmemorize.core.FormattedText;
import junit.framework.TestCase;

/**
 * @author djemili
 */
public class FormattedTextTest extends TestCase
{
    private DefaultStyledDocument m_doc;

    protected void setUp() throws Exception
    {
        m_doc = new DefaultStyledDocument();
    }
    
    public void testFormattedStringWithBoldRoundtrip() throws BadLocationException
    {
        assertStyleRoundtrip(StyleConstants.Bold);
    }
    
    public void testFormattedStringWithItalicRoundtrip() throws BadLocationException
    {
        assertStyleRoundtrip(StyleConstants.Italic);
    }
    
    public void testFormattedStringWithSubRoundtrip() throws BadLocationException
    {
        assertStyleRoundtrip(StyleConstants.Subscript);
    }
    
    public void testFormattedStringWithSupRoundtrip() throws BadLocationException
    {
        assertStyleRoundtrip(StyleConstants.Superscript);
    }
    
    public void testFormattedStringWithUnderlineRoundtrip() throws BadLocationException
    {
        assertStyleRoundtrip(StyleConstants.Underline);
    }
    
    public void testFormattedStringWithNoFormatRoundtrip() throws BadLocationException
    {
        m_doc.insertString(0, "Foobar", SimpleAttributeSet.EMPTY);
        
        StyledDocument doc = roundtrip(m_doc);
        assertEquals("Foobar", doc.getText(0, doc.getLength()));
    }
    
    public void testEscapeCharsThatAreUsedForMarkupInFormattedText() throws BadLocationException
    {
        m_doc.insertString(0, "Foo<b>ba</b>r", SimpleAttributeSet.EMPTY);
        
        StyledDocument doc = roundtrip(m_doc);
        assertEquals("Foo<b>ba</b>r", doc.getText(0, doc.getLength()));
    }
    
    public void testEscapeCharsThatAreUsedForMarkupInUnformattedText() throws BadLocationException
    {
        FormattedText fText = FormattedText.unformatted("Foo<b>ba</b>r");
        assertEquals("Foo<b>ba</b>r", fText.getUnformatted());
    }
    
    public void testEscapeCharsThatAreUsedForMarkupInDocumentText() throws BadLocationException
    {
        m_doc.insertString(0, "Foo<b>ba</b>r", SimpleAttributeSet.EMPTY);
        
        String encoding = FormattedText.formatted(m_doc).getFormatted();
        assertEquals("Foo<b>ba</b>r", FormattedText.formatted(encoding).getUnformatted());
    }
    
    public void testRemoveRedundantTagsFromEncoding() throws BadLocationException
    {
        StyledDocument doc = FormattedText.formatted("F<b>oo</b><b>ba</b>r").getDocument();
        assertEquals("F<b>ooba</b>r", FormattedText.formatted(doc).getFormatted());
    }
    
    public void testRemoveRedundantTagsFromDoc() throws BadLocationException
    {
        m_doc.insertString(0, "Foobar", SimpleAttributeSet.EMPTY);
        
        SimpleAttributeSet actualAttr = new SimpleAttributeSet();
        actualAttr.addAttribute(StyleConstants.Bold, Boolean.TRUE);
        m_doc.setCharacterAttributes(1, 2, actualAttr, true);
        
        actualAttr = new SimpleAttributeSet();
        actualAttr.addAttribute(StyleConstants.Bold, Boolean.TRUE);
        m_doc.setCharacterAttributes(3, 2, actualAttr, true);
        
        assertEquals("F<b>ooba</b>r", FormattedText.formatted(m_doc).getFormatted());
    }    
    
    /**
     * Asserts that the style is still set correctly on a styled document after
     * encoding the document into a string representation and decoding it back
     * into a styled document again.
     */
    public void assertStyleRoundtrip(Object style) throws BadLocationException
    {
        m_doc.insertString(0, "Foobar Test", SimpleAttributeSet.EMPTY);
        
        SimpleAttributeSet actualAttr = new SimpleAttributeSet();
        actualAttr.addAttribute(style, Boolean.TRUE);
        m_doc.setCharacterAttributes(2, 2, actualAttr, true);
        
        StyledDocument doc = roundtrip(m_doc);
        
        AttributeSet exceptedAttr = doc.getCharacterElement(2).getAttributes();
        Boolean hasStyle = (Boolean)exceptedAttr.getAttribute(style);
        if (hasStyle != null)
        {
            assertTrue(hasStyle.booleanValue());
        }
        else
        {
            fail("Style not set");
        }
    }

    private static StyledDocument roundtrip(StyledDocument doc)
    {
        String encoding = FormattedText.formatted(doc).getFormatted();
        return FormattedText.formatted(encoding).getDocument();
    }
}
