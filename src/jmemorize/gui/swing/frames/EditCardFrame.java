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
package jmemorize.gui.swing.frames;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import jmemorize.core.Card;
import jmemorize.core.CardSide;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;
import jmemorize.core.FormattedText;
import jmemorize.core.ImageRepository;
import jmemorize.core.Main;
import jmemorize.core.Settings;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.actions.AbstractAction2;
import jmemorize.gui.swing.actions.edit.AddCardAction;
import jmemorize.gui.swing.actions.edit.RemoveAction;
import jmemorize.gui.swing.actions.edit.ResetCardAction;
import jmemorize.gui.swing.panels.CardHeaderPanel;
import jmemorize.gui.swing.panels.CardPanel;
import jmemorize.gui.swing.panels.TwoSidesCardPanel;
import jmemorize.gui.swing.widgets.CategoryComboBox;
import jmemorize.util.EscapableFrame;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * The window that is used to edit cards. Note this is a singleton class. The
 * same window will be reused for all editting.
 * 
 * @author djemili
 */
public class EditCardFrame extends EscapableFrame implements CategoryObserver, SelectionProvider
{
    private class NextCardAction extends AbstractAction2
    {
        public NextCardAction()
        {
            setName(Localization.get(LC.NEXT_CARD));
            setDescription(Localization.get(LC.NEXT_CARD_DESC));
            setIcon("/resource/icons/card_next.gif"); //$NON-NLS-1$
            setMnemonic(1);
        }

        public void actionPerformed(java.awt.event.ActionEvent e) 
        {
            if (confirmCardSides())
                showNext();
        }
    }
    
    private class PreviousCardAction extends AbstractAction2
    {
        public PreviousCardAction()
        {
            setName(Localization.get(LC.PREV_CARD));
            setDescription(Localization.get(LC.PREV_CARD_DESC));
            setIcon("/resource/icons/card_prev.gif"); //$NON-NLS-1$
            setMnemonic(1);
        }

        public void actionPerformed(java.awt.event.ActionEvent e) 
        {
            if (confirmCardSides())
                showPrevious();
        }        
    }
    
    private static final int     MAX_TITLE_LENGTH     = 80;
    private static final String  FRAME_ID             = "editcard"; //$NON-NLS-1$

    private List<SelectionObserver> m_selectionObservers = new ArrayList<SelectionObserver>();

    private Action                  m_nextCardAction     = new NextCardAction();
    private Action                  m_previousCardAction = new PreviousCardAction();

    private Card                    m_currentCard;
    private int                     m_currentCardIndex;
    private ArrayList<Card>         m_cards;
    private Category                m_category;

    // swing elements
    private JButton                 m_applyButton        = new JButton(Localization.get(LC.APPLY));

    private CardHeaderPanel         m_headerPanel        = new CardHeaderPanel();
    private TwoSidesCardPanel       m_cardPanel          = new TwoSidesCardPanel(true);
    
    private static EditCardFrame    m_instance;
    
    /**
     * @return The singleton instance.
     */
    public static EditCardFrame getInstance()
    {
        if (m_instance == null)
        {
            m_instance = new EditCardFrame();
        }
        
        return m_instance;
    }
    
    /**
     * Shows the Edit Card Frame and allows user to edit the card card.
     * 
     * @param card The card that is to be shown and editted.
     */
    public void showCard(Card card)
    {
        List<Card> cards = new ArrayList<Card>(1);
        cards.add(card);
        showCard(card, cards, card.getCategory());
    }
    
    /**
     * Shows the Edit Card Frame and allows user to edit the card card.
     * 
     * @param card the card that is to be shown and editted.
     * 
     * @param cards the cards that belong to the context of the card that is to
     * be edited. These cards are used to allow browsing to next and previous
     * card. Therefore the card given on the former parameter is usually also
     * part of this list. The usual mode is to show the currently selected card
     * and to give all other cards that are part of the same card table/learn
     * history etc. as additional cards.
     * 
     * @param category The category that includes all cards from former
     * parameters.
     */
    public void showCard(Card card, List<Card> cards, Category category)
    {
        showCard(card, cards, category, null, 0, true); //HACK
    }
    
    public void showCard(Card card, List<Card> cards, Category category,
        String searchText, int side, boolean ignoreCase)
    {
        if (isVisible() && !confirmCardSides())
            return;
        
        m_currentCard = card;
        m_currentCardIndex = cards.indexOf(card);
        m_cards = new ArrayList<Card>(cards);
        
        if (m_category != null)
        {
            m_category.removeObserver(this);
        }
        m_category = category;
        if (m_category != null)
        {
            category.addObserver(this);
        }
        
        updatePanel();
        setVisible(true);
    }
    
    /**
     * @return True if window was closed. False if this was prevented by user
     * option.
     */
    public boolean close() 
    {
        if (confirmCardSides())
        {
            hideFrame();
            return true;
        }
        
        return false;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        if (type == REMOVED_EVENT)
        {
            // if current card was in a deleted category branch
            if (category.contains(m_currentCard.getCategory()))
            {
                hideFrame();
            }
            
            // delete all cards that are part of a deleted category branch
            for (Card card : m_cards)
            {
                if (category.contains(card.getCategory()))
                {
                    m_cards.remove(card);
                }
            }
            
            m_currentCardIndex = m_cards.indexOf(m_currentCard);
            updateActions();
        }
        else if (type == EDITED_EVENT)
        {
            updateCardHeader();
        }
    }

    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        if (type == DECK_EVENT && m_currentCard == card)
        {
            updateCardHeader();
        }
        
        if (type == REMOVED_EVENT)
        {
            if (m_currentCard == card)
            {
                if (hasNext())
                {
                    showNext();
                }
                else if (hasPrevious())
                {
                    showPrevious();
                }
                else
                {
                    hideFrame();
                }
            }
            
            if (m_cards.remove(card)) // is this card is relevant
            {
                // we need to update index because cards changed
                m_currentCardIndex = m_cards.indexOf(m_currentCard);
                updateActions();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public void addSelectionObserver(SelectionObserver observer)
    {
        m_selectionObservers.add(observer);
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public void removeSelectionObserver(SelectionObserver observer)
    {
        m_selectionObservers.remove(observer);
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public Category getCategory()
    {
        return m_currentCard.getCategory();
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public JComponent getDefaultFocusOwner()
    {
        return null; // HACK
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public JFrame getFrame() 
    {
        return this;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Card> getRelatedCards()
    {
        return m_cards;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Card> getSelectedCards()
    {
        ArrayList<Card> list = new ArrayList<Card>(1);
        list.add(m_currentCard);
        return list;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Category> getSelectedCategories()
    {
        return null;
    }

    private void hideFrame()
    {
        Settings.storeFrameState(this, FRAME_ID);
        setVisible(false);
    }

    /**
     * If the content of the text panes differ from the currently saved card
     * entries, this will bring up a dialog that asks if the user wants to save 
     * the changes. If yes is selected the card sides are saved.
     * 
     * This should be called everytime there is the chance of losing card 
     * informations.
     * 
     * @return True if operation wasnt aborted by user.
     */
    private boolean confirmCardSides()
    {
        if (isChanged())
        {
            int n = JOptionPane.showConfirmDialog(this, 
                Localization.get("EditCard.MODIFIED_WARN"), //$NON-NLS-1$
                Localization.get("EditCard.MODIFIED_WARN_TITLE"), //$NON-NLS-1$
                JOptionPane.YES_NO_CANCEL_OPTION,  JOptionPane.WARNING_MESSAGE);

            if (n == JOptionPane.CANCEL_OPTION)
            {
                return false;
            }
            
            if (n == JOptionPane.YES_OPTION)
            {
                return saveCard();
            }
        }
        
        // if no changes or NO chosen
        return true;
    }
    
    /** 
     * Creates new form EditCardFrame 
     */
    private EditCardFrame()
    {
        initComponents();
        addChangeObservers();
        Settings.loadFrameState(this, FRAME_ID);
    }

    private void addChangeObservers()
    {
        m_cardPanel.addObserver(new CardPanel.CardPanelObserver(){
            public void onTextChanged()
            {
                updateApplyButton();
            }

            public void onImageChanged()
            {
                updateApplyButton();
            }
        });
        
        m_cardPanel.getCategoryComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                updateApplyButton();
            }
        });
    }
    
    private void updateApplyButton()
    {
        m_applyButton.setEnabled(isChanged());
    }
    
    private boolean isChanged()
    {
        boolean categoryChanged = m_currentCard.getCategory() !=
            m_cardPanel.getCategoryComboBox().getSelectedCategory();
        
        if (categoryChanged)
            return true;
        
        CardSide frontSide = m_currentCard.getFrontSide();
        CardSide backSide = m_currentCard.getBackSide();
        
        boolean textChanged = 
            !m_cardPanel.getFrontText().equals(frontSide.getText()) ||
            !m_cardPanel.getBackText().equals(backSide.getText());
        
        if (textChanged)
            return true;
        
        if (!ImageRepository.equals(m_cardPanel.getFrontImages(), frontSide.getImages()))
            return true;
        
        if (!ImageRepository.equals(m_cardPanel.getBackImages(), backSide.getImages()))
            return true;

        return false;
    }
    
    private void updatePanel()
    {
        updateTitle();
        
        CardSide frontSide = m_currentCard.getFrontSide();
        CardSide backSide = m_currentCard.getBackSide();
        
        // set sides
        m_cardPanel.setTextSides(frontSide.getText(), backSide.getText());
        m_cardPanel.setImages(frontSide.getImages(), backSide.getImages());
        
        highlightSearchText();
        updateActions();
        updateCardHeader();
        
        Category rootCategory = Main.getInstance().getLesson().getRootCategory();
        CategoryComboBox categoryComboBox = m_cardPanel.getCategoryComboBox();
        categoryComboBox.setRootCategory(rootCategory);
        categoryComboBox.setSelectedCategory(m_currentCard.getCategory());
        
        updateApplyButton();
    }

    /**
     * Update the title of this frame.
     */
    private void updateTitle()
    {
        // set title
        String title = m_currentCard.getFrontSide().getText().getUnformatted();
        title = title.replace('\n', ' ');
        if (title.length() > MAX_TITLE_LENGTH)
        {
            title = title.substring(0, MAX_TITLE_LENGTH) + "..."; //$NON-NLS-1$
        }
        setTitle(title);
        
//        Date dateExpired = m_currentCard.getDateExpired();
//        ImageIcon icon = CardStatusIcons.getInstance().getCardIcon(dateExpired);
//        setIconImage(icon.getImage());
    }
    
    /**
     * Updates the actions of this EditCardFrame i.e. enabling/disabling certain
     * buttons.
     */
    private void updateActions()
    {
        if (m_cards == null)
        {
            m_nextCardAction.setEnabled(false);
            m_previousCardAction.setEnabled(false);
        }
        else
        {
            m_previousCardAction.setEnabled(hasPrevious());
            m_nextCardAction.setEnabled(hasNext());
        }
    }
    
    /**
     * @return <code>true</code> if there is another card left after this one.
     */
    private boolean hasNext()
    {
        return m_currentCardIndex < m_cards.size() - 1;
    }
    
    /**
     * @return <code>true</code> if there is a another card before this one.
     */
    private boolean hasPrevious()
    {
        return m_currentCardIndex > 0;
    }
    
    /**
     * Show the next card of the card list of this EditCardFrame.
     */
    private void showNext()
    {
        m_currentCard = (Card)m_cards.get(++m_currentCardIndex);
        updatePanel();
    }
    
    /**
     * Show the previous card of the card list of this EditCardFrame.
     */
    private void showPrevious()
    {
        m_currentCard = (Card)m_cards.get(--m_currentCardIndex);
        updatePanel();    
    }
    
    private boolean saveCard()
    {
        if (m_cardPanel.isValidCard())
        {
            FormattedText frontText = m_cardPanel.getFrontText();
            FormattedText backText = m_cardPanel.getBackText();
            
            ImageRepository repo = ImageRepository.getInstance();
            
            List<String> frontIDs = repo.addImages(m_cardPanel.getFrontImages());
            List<String> backIDs = repo.addImages(m_cardPanel.getBackImages());
            
            m_currentCard.setSides(frontText, backText);
            m_currentCard.getFrontSide().setImages(frontIDs);
            m_currentCard.getBackSide().setImages(backIDs);
            
            CategoryComboBox categoryComboBox = m_cardPanel.getCategoryComboBox();
            Category newCategory = categoryComboBox.getSelectedCategory();
            if (newCategory != m_currentCard.getCategory())
            {
                m_currentCard.getCategory().moveCard(m_currentCard, newCategory);
            }
            
            updateTitle();
            updateCardHeader();
            updateApplyButton();
            
            return true;
        }
        else
        {
            JOptionPane.showMessageDialog(this, 
                Localization.get(LC.EMPTY_SIDES_ALERT),
                Localization.get(LC.EMPTY_SIDES_ALERT_TITLE),
                JOptionPane.ERROR_MESSAGE);
            
            return false;
        }
    }

    private void updateCardHeader()
    {
        m_headerPanel.setCard(m_currentCard);
    }

    private void initComponents() 
    {
        getContentPane().add(buildToolBar(), BorderLayout.NORTH);
        getContentPane().add(buildHeaderPanel(), BorderLayout.CENTER);
        getContentPane().add(buildBottomButtonBar(), BorderLayout.SOUTH);

        setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/resource/icons/card_edit.gif"))); //$NON-NLS-1$
        pack();
    }

    private JPanel buildHeaderPanel()
    {
        m_headerPanel.setBorder(new EtchedBorder());
        m_cardPanel.setBorder(Borders.DIALOG_BORDER);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_headerPanel, BorderLayout.NORTH);
        panel.add(m_cardPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JToolBar buildToolBar()
    {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        toolBar.add(new JButton(new AddCardAction(this)));
        toolBar.add(new JButton(m_previousCardAction));
        toolBar.add(new JButton(m_nextCardAction));
        toolBar.add(new JButton(new ResetCardAction(this)));
        toolBar.add(new JButton(new RemoveAction(this)));
        
        return toolBar;
    }
    
    private JPanel buildBottomButtonBar()
    {
        JButton okayButton = new JButton(Localization.get(LC.OKAY));
        okayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                saveCard();
                close();
            }
        });
        
        JButton cancelButton = new JButton(Localization.get(LC.CANCEL));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                hideFrame();
            }
        });
        
        m_applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                saveCard();
            }
        });
        
        JPanel buttonPanel = ButtonBarFactory.buildOKCancelApplyBar(
            okayButton, cancelButton, m_applyButton);
        buttonPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        
        getRootPane().setDefaultButton(okayButton);
        
        return buttonPanel;
    }
    
    private void highlightSearchText()
    {
//        if (m_searchText != null)
//        {
//            List frontPositions = null;
//            if (m_searchSide == SearchTool.FRONT_SIDE || m_searchSide == SearchTool.BOTH_SIDES)
//            {
//                frontPositions = SearchTool.search(m_currentCard.getFrontSide(),
//                    m_searchText, m_searchSide, m_searchCase);
//            }
//            
//            List backPositions = null;
//            if (m_searchSide == SearchTool.FLIP_SIDE|| m_searchSide == SearchTool.BOTH_SIDES)
//            {
//                backPositions = SearchTool.search(m_currentCard.getBackSide(),
//                    m_searchText, m_searchSide, m_searchCase);
//            }
//            
//            m_cardPanel.highlight(frontPositions, backPositions, m_searchText.length());
//        }
    }
}
