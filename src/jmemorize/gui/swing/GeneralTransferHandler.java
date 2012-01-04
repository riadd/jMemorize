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
package jmemorize.gui.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.TransferHandler;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.FormattedText;
import jmemorize.core.Main;
import jmemorize.gui.swing.panels.CardSidePanel;
import jmemorize.gui.swing.widgets.CardTable;
import jmemorize.gui.swing.widgets.CategoryTree;

/**
 * Organizes datatransfers between the card table and the category tree.
 * 
 * @author djemili
 */
public class GeneralTransferHandler extends TransferHandler
{
    public class CardsTransferable implements Transferable
    {
        private List<Card> m_cards;

        public CardsTransferable(List<Card> cards)
        {
            m_cards = cards;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
        {
            if (!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            
            if (CARDS_FLAVOR.equals(flavor))
                return m_cards;
            
            StringBuffer buffer = new StringBuffer();
            for (Card card : m_cards)
            {
                buffer.append(card.getFrontSide().getText().getUnformatted());
                buffer.append(" - ");
                buffer.append(card.getBackSide().getText().getUnformatted());
                buffer.append('\n');
            }
            
            return buffer.toString();
        }

        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] {CARDS_FLAVOR, DataFlavor.stringFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return CARDS_FLAVOR.equals(flavor) ||
                DataFlavor.stringFlavor.equals(flavor);
        }
    }
    
    public class CategoryTransferable implements Transferable
    {
        private Category m_category;

        public CategoryTransferable(Category category)
        {
            m_category = category;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
        {
            if (!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            
            if (CATEGORY_FLAVOR.equals(flavor))
                return m_category;
            
            return m_category.getName();
        }

        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] {CATEGORY_FLAVOR, DataFlavor.stringFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return CATEGORY_FLAVOR.equals(flavor) ||
                DataFlavor.stringFlavor.equals(flavor);
        }
    }
    
    /**
     * Represents a formatted text and it source document. We need the source 
     * for CUT-operations where we need to remove the formatted section from 
     * the original document.
     */
    public class FormattedTextSection
    {
        private FormattedText m_text;
        private Document      m_document;
        private int           m_start;
        private int           m_end;
        
        public FormattedTextSection(StyledDocument doc, int start, int end)
        {
            m_document = doc;
            m_start = start;
            m_end = end;
            
            m_text = FormattedText.formatted(doc, start, end);;
        }

        public FormattedText getText()
        {
            return m_text;
        }

        public Document getDocument()
        {
            return m_document;
        }

        public int getStart()
        {
            return m_start;
        }

        public int getEnd()
        {
            return m_end;
        }
    }
    
    public class FormattedTextTransferable implements Transferable
    {
        private FormattedTextSection m_formattedText;

        public FormattedTextTransferable(StyledDocument doc, int start, int end)
        {
            m_formattedText = new FormattedTextSection(doc, start, end);
        }

        public Object getTransferData(DataFlavor flavor) 
            throws UnsupportedFlavorException
        {
            if (!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            
            if (FORMATTED_TEXT_FLAVOR.equals(flavor))
                return m_formattedText;
            
            return m_formattedText.getText().getUnformatted();
        }

        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] {FORMATTED_TEXT_FLAVOR,
                DataFlavor.stringFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return FORMATTED_TEXT_FLAVOR.equals(flavor) ||
                DataFlavor.stringFlavor.equals(flavor);
        }
    }
    
    public final static DataFlavor CARDS_FLAVOR    = 
        new DataFlavor(Card.class, "Card"); //$NON-NLS-1$
    
    public final static DataFlavor CATEGORY_FLAVOR = 
        new DataFlavor(Category.class, "Category"); //$NON-NLS-1$
    
    public final static DataFlavor FORMATTED_TEXT_FLAVOR = 
        new DataFlavor(FormattedTextSection.class, "FormattedText"); //$NON-NLS-1$

    private CardSidePanel m_cardSidePanel;
    
    public GeneralTransferHandler()
    {
    }
    
    public GeneralTransferHandler(CardSidePanel cardSidePanel)
    {
        m_cardSidePanel = cardSidePanel;
    }
    
    /*
     * @see javax.swing.TransferHandler
     */
    public int getSourceActions(JComponent c)
    {
        return COPY_OR_MOVE;
    }
    
    /*
     * @see javax.swing.TransferHandler
     */
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
    {
        if (comp instanceof CategoryTree)
        {
            for (int i = 0; i < transferFlavors.length; i++)
            {
                if (transferFlavors[i] == CARDS_FLAVOR || 
                    transferFlavors[i] == CATEGORY_FLAVOR)
                {
                    return true;
                }
            }
        }
        
        if (comp instanceof JTextPane)
        {
            for (int i = 0; i < transferFlavors.length; i++)
            {
                if (transferFlavors[i] == FORMATTED_TEXT_FLAVOR || 
                    transferFlavors[i] == DataFlavor.stringFlavor)
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /*
     * @see javax.swing.TransferHandler
     */
    @SuppressWarnings("unchecked")
    public boolean importData(JComponent comp, Transferable t)
    {
        try
        {
            Category targetCategory;
            if (comp instanceof CategoryTree)
            {
                CategoryTree tree = (CategoryTree)comp;
                targetCategory = tree.getSelectedCategory();
            }
            else if (comp instanceof CardTable)
            {
                CardTable table = (CardTable)comp;
                targetCategory = table.getView().getCategory();
            }
            else if (comp instanceof JTextPane)
            {
                JTextPane textPane = (JTextPane)comp;
                
                if (t.isDataFlavorSupported(FORMATTED_TEXT_FLAVOR))
                {
                    int start = textPane.getSelectionStart();
                    FormattedTextSection fText = (FormattedTextSection)t.getTransferData(
                        FORMATTED_TEXT_FLAVOR);
                    
                    fText.getText().insertIntoDocument(textPane.getStyledDocument(), start);
                }
                else if (t.isDataFlavorSupported(DataFlavor.imageFlavor))
                {
                    if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
                    {
                        String link = (String)t.getTransferData(DataFlavor.stringFlavor);
                        link = link.substring(0, link.indexOf('\n'));
                        
                        String lower = link.toLowerCase();
                        if (lower.startsWith("http://"))
                        {
                            if (lower.endsWith(".jpg") || 
                                lower.endsWith(".gif") ||
                                lower.endsWith(".png") || 
                                lower.endsWith(".jpeg") || 
                                lower.endsWith(".bmp"))
                            {
                                URL url = new URL(link);
                                ImageIcon icon = new ImageIcon(url);
                                icon.setDescription(link);
                                
                                m_cardSidePanel.addImage(icon);
                                m_cardSidePanel.getTextPane().requestFocus();
                                
                                return true;
                            }
                        }
                    }
                }
                else if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
                {
                    int start = textPane.getSelectionStart();
                    String text = (String)t.getTransferData(DataFlavor.stringFlavor);
                    
                    textPane.getDocument().insertString(start, text, null);
                }
                
                return true;
            }
            else
            {
                return false;
            }        
        
            if (t.isDataFlavorSupported(CARDS_FLAVOR))
            {
                List<Card> cards = (List<Card>)t.getTransferData(CARDS_FLAVOR);
                for (Card card : cards)
                {
                    targetCategory.addCard((Card)card.clone(), card.getLevel());
                }
    
                return true;
            }
            else if (t.isDataFlavorSupported(CATEGORY_FLAVOR))
            {
                Category category = (Category)t.getTransferData(CATEGORY_FLAVOR);
                if (!category.contains(targetCategory))
                {
                    targetCategory.addCategoryChild(copyCategories(category));
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        catch (Exception e)
        {
            Main.logThrowable("Error importing data from clipboard", e);
        }
        
        return false;
    }

    /*
     * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
     */
    protected Transferable createTransferable(JComponent c)
    {
        if (c instanceof CardTable)
        {
            CardTable table = (CardTable)c;
            return new CardsTransferable(table.getSelectedCards());
        }
        else if (c instanceof CategoryTree)
        {
            CategoryTree tree = (CategoryTree)c;
            Category category = tree.getSelectedCategory();
            
            // dont allow operations with root category
            return category.getParent() != null ? new CategoryTransferable(category) : null;
        }
        else if (c instanceof JTextPane)
        {
            JTextPane textPane = (JTextPane)c;
            
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            
            if (end - start <= 0)
                return null;
            
            StyledDocument doc = (StyledDocument)textPane.getDocument();
            return new FormattedTextTransferable(doc, start, end);
        }
            
        return null;
    }
    
    /*
     * @see javax.swing.TransferHandler#exportDone
     */
    @SuppressWarnings("unchecked")
    protected void exportDone(JComponent source, Transferable data, int action)
    {
        if (action != MOVE)
            return;
        
        try
        {
            if (data.isDataFlavorSupported(CARDS_FLAVOR))
            {
                CardTable table = (CardTable)source;
                Category category = table.getView().getCategory();

                List<Card> cards = (List<Card>)data.getTransferData(CARDS_FLAVOR);
                for (Card card : cards)
                {
                    category.removeCard(card);
                }
            }
            else if (data.isDataFlavorSupported(CATEGORY_FLAVOR))
            {
                Category category = (Category)data.getTransferData(CATEGORY_FLAVOR);
                category.remove();
            }
            else if (data.isDataFlavorSupported(FORMATTED_TEXT_FLAVOR))
            {
                FormattedTextSection formattedText = 
                    (FormattedTextSection)data.getTransferData(FORMATTED_TEXT_FLAVOR);
                
                int start = formattedText.getStart();
                int end = formattedText.getEnd();
                
                formattedText.getDocument().remove(start, end-start);
            }
        }
        catch (Exception e)
        {
            Main.logThrowable("Error exporting data to clipboard", e);
        }
    }
    

    private Category copyCategories(Category original) throws CloneNotSupportedException
    {
        Category copy = new Category(original.getName());
        
        // first copy categories..
        for (Category category : original.getChildCategories())
        {
            copy.addCategoryChild(copyCategories(category));
        }
        
        // ..then copy cards
        for (int i = 0; i < original.getNumberOfDecks(); i++)
        {
            for (Card card : original.getLocalCards(i))
            {
                copy.addCard((Card)card.clone(), i);
            }
        }
        
        return copy;
    }
}
