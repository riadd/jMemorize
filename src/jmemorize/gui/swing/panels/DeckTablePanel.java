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
package jmemorize.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;
import jmemorize.core.Main;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.actions.AbstractAction2;
import jmemorize.gui.swing.frames.EditCardFrame;
import jmemorize.gui.swing.frames.MainFrame;
import jmemorize.gui.swing.widgets.CardTable;

/**
 * A panel that shows a table which is filled with cards. Shows two buttons at
 * the top that go to previous/next deck.
 * 
 * @author djemili
 */
public class DeckTablePanel extends JPanel implements CategoryObserver
{
    private class NextDeckAction extends AbstractAction2
    {
        public NextDeckAction()
        {
            setName(Localization.get("DeckTable.NEXT_DECK")); //$NON-NLS-1$
            setDescription(Localization.get("DeckTable.NEXT_DECK_DESC")); //$NON-NLS-1$
            setIcon("/resource/icons/card_next.gif"); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener
         */
        public void actionPerformed(ActionEvent e)
        {
            int nextLevel = m_currentDeckLevel + 1;
            while (m_category.getCards(nextLevel).isEmpty())
            {
                nextLevel++;
            } 
            
            m_frame.setDeck(nextLevel);
        }        
    }
    
    private class PreviousDeckAction extends AbstractAction2
    {
        public PreviousDeckAction()
        {
            setName(Localization.get("DeckTable.PREV_DECK")); //$NON-NLS-1$
            setDescription(Localization.get("DeckTable.PREV_DECK_DESC")); //$NON-NLS-1$
            setIcon("/resource/icons/card_prev.gif"); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener
         */
        public void actionPerformed(ActionEvent e)
        {
            int prevLevel = m_currentDeckLevel - 1;
            while (prevLevel >= 0 && m_category.getCards(prevLevel).isEmpty() )
            {
                prevLevel--;
            }
            
            m_frame.setDeck(prevLevel); //HACK better use showAllDecks!?
        }        
    }
    
    private NextDeckAction     m_nextDeckAction     = new NextDeckAction();
    private PreviousDeckAction m_previousDeckAction = new PreviousDeckAction();
    
    private int                m_currentDeckLevel = 0;
    private Category           m_category;
    
    private MainFrame          m_frame;
    
    // swing elements
    private CardTable          m_cardTable;
    
    /** 
     * Creates new form CardsPanel 
     */
    public DeckTablePanel(MainFrame mainFrame)
    {
        m_frame = mainFrame;
        
        m_cardTable = new CardTable(m_frame, 
            Main.USER_PREFS.node("main.table"), //$NON-NLS-1$ 
            new int[]{ 
                CardTable.COLUMN_FRONTSIDE, 
                CardTable.COLUMN_DECK, 
                CardTable.COLUMN_CATEGORY, 
                CardTable.COLUMN_TESTED, 
                CardTable.COLUMN_EXPIRES});
        
        initComponents();
    }
    
    /**
     * @param deck If deck is -1 then all decks are shown, otherwise only given 
     * deck is shown. 
     */
    public void setDeck(int deck)
    {
        m_cardTable.getView().setCards(m_category.getCards(deck), m_category);
        m_currentDeckLevel = deck;
        updateButtons();
    }
    
    public void editCards() //HACK
    {
        Card card = (Card)m_cardTable.getSelectedCards().get(0);
        List<Card> cards = m_cardTable.getView().getCards();
        Category category = m_cardTable.getView().getCategory();
        
        EditCardFrame.getInstance().showCard(card, cards, category);
    }
    
    public void setCategory(Category category)
    {
        if (m_category != null)
        {
            m_category.removeObserver(this);
        }
        m_category = category;
        m_category.addObserver(this);
        
        m_frame.setDeck(-1);
    }
    
    public CardTable getCardTable()
    {
        return m_cardTable;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        if (m_category.getSubtreeList().contains(category))
            setDeck(m_currentDeckLevel);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        // TODO make this finer
        setDeck(m_currentDeckLevel);
    }
    
    private void updateButtons()
    {
        m_previousDeckAction.setEnabled(m_currentDeckLevel >= 0);
        m_nextDeckAction.setEnabled(m_currentDeckLevel < m_category.getNumberOfDecks() - 1);
    }
    
    private void initComponents() 
    {
        JButton  leftButton  = new JButton(m_previousDeckAction);
        JButton  rightButton = new JButton(m_nextDeckAction);

        JToolBar buttonBar   = new JToolBar();
        buttonBar.setFloatable(false);
        buttonBar.add(leftButton);
        buttonBar.add(rightButton);

        JPanel mainPanel   = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EtchedBorder());
        mainPanel.add(buttonBar, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(m_cardTable);
        Color color = UIManager.getColor("Table.background"); //$NON-NLS-1$
        scrollPane.getViewport().setBackground(color);
        
        m_cardTable.hookCardContextMenu(scrollPane);
        m_cardTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                tableMouseClicked(evt);
            }
        });

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        
        // overwrite moving to next row when pressing ENTER
        InputMap map = m_cardTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ""); //$NON-NLS-1$
    }
    
    private void tableMouseClicked(MouseEvent evt)
    {
        if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2)
        {
            editCards();
        }
    }
}
