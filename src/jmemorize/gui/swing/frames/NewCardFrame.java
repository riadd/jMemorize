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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.FormattedText;
import jmemorize.core.ImageRepository;
import jmemorize.core.Main;
import jmemorize.core.Settings;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.NewCardFramesManager;
import jmemorize.gui.swing.actions.AbstractAction2;
import jmemorize.gui.swing.panels.TwoSidesCardPanel;
import jmemorize.gui.swing.widgets.CategoryComboBox;
import jmemorize.util.EscapableFrame;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;

/**
 * A frame that is used to create new cards.
 * 
 * @author djemili
 */
public class NewCardFrame extends EscapableFrame 
{
    private class NewWindowAction extends AbstractAction2
    {
        public NewWindowAction()
        {
            setName(Localization.get(LC.NEW_CARD_NEW_WINDOW));
            setDescription(Localization.get(LC.NEW_CARD_NEW_WINDOW_DESC));
            setIcon("/resource/icons/card_add.gif"); //$NON-NLS-1$
            setAccelerator(KeyEvent.VK_N, InputEvent.CTRL_MASK);
            setMnemonic(7);
        }

        public void actionPerformed(ActionEvent e) 
        {
            m_manager.addNewCardWindow(NewCardFrame.this, 
                m_cardPanel.getCategoryComboBox().getSelectedCategory());
        }
    }
    
    private class EditRecentCardAction extends AbstractAction2
    {
        public EditRecentCardAction()
        {
            setName(Localization.get(LC.NEW_CARD_EDIT_RECENTLY));
            setDescription(Localization.get(LC.NEW_CARD_EDIT_RECENTLY_DESC));
            setIcon("/resource/icons/card_edit.gif"); //$NON-NLS-1$
            setAccelerator(KeyEvent.VK_ENTER, 0);
            setMnemonic(1);
        }

        public void actionPerformed(ActionEvent e) 
        {
            m_manager.editRecentlyCreatedCards();
        }
    }

    // swing elements
    private NewCardFramesManager m_manager;
    private TwoSidesCardPanel    m_cardPanel        = new TwoSidesCardPanel(true);
    private JButton              m_createMoreButton;
    private Action               m_editRecentAction = new EditRecentCardAction();
    
    /**
     * Creates new form NewCardFrame 
     */
    public NewCardFrame(NewCardFramesManager manager, Category currentCategory) 
    {
        m_manager = manager;
        
        initComponents();
        
        CategoryComboBox categoryComboBox = m_cardPanel.getCategoryComboBox();
        categoryComboBox.setRootCategory(Main.getInstance().getLesson().getRootCategory());
        categoryComboBox.setSelectedCategory(currentCategory);

        m_cardPanel.reset();
        pack();
    }
    
    /**
     * @return True if window was closed. False if it was left open by user 
     * decision.
     */
    public boolean close()
    {
        if (m_cardPanel.getFrontText().getUnformatted().trim().length() > 0 || 
            m_cardPanel.getBackText().getUnformatted().trim().length() > 0)
        {
            int n = JOptionPane.showConfirmDialog(this, 
                Localization.get(LC.NEW_CARD_DISMISS_WARN),
                Localization.get(LC.NEW_CARD_CLOSE_WARN),
                JOptionPane.YES_NO_CANCEL_OPTION, 
                JOptionPane.WARNING_MESSAGE); 

            if (n == JOptionPane.CANCEL_OPTION)
            {
                return false; //window isnt closed
            }
            
            if (n == JOptionPane.YES_OPTION)
            {
                return createCard(false);
            }
        }
        
        // if card was empty or NO was chosen
        closeWindow();
        return true;
    }
    
    private void initComponents() 
    {        
        getContentPane().add(buildToolBar(),   BorderLayout.NORTH);
        getContentPane().add(buildMainPanel(), BorderLayout.CENTER);
        getContentPane().add(buildButtonBar(), BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(m_createMoreButton);
        pack();
        
        setTitle(Localization.get(LC.NEW_CARD_TITLE));
        setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/resource/icons/card_add.gif"))); //$NON-NLS-1$
    }
    
    private JToolBar buildToolBar()
    {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EtchedBorder());
        
        toolBar.add(new JButton(new NewWindowAction()));
        toolBar.add(new JButton(m_editRecentAction));
        
        return toolBar;
    }
    
    private JPanel buildMainPanel()
    {
        m_cardPanel.setBorder(Borders.DIALOG_BORDER);
        return m_cardPanel;
    }
    
    private JPanel buildButtonBar()
    {
        // buttons
        m_createMoreButton = new JButton(
            Localization.get("NewCard.ADD_AND_NEXT")); //$NON-NLS-1$
        m_createMoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                createCard(true);
            }
        });

        JButton createButton = new JButton(
            Localization.get("NewCard.ADD")); //$NON-NLS-1$
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                createCard(false);
            }
        });

        JButton cancelButton = new JButton(
            Localization.get(LC.CANCEL));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                // directly close without confirmation
                closeWindow();
            }
        });
        
        // button bar
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addGlue();
        builder.addFixedNarrow(m_createMoreButton);
        builder.addRelatedGap();
        builder.addGridded(createButton);
        builder.addRelatedGap();
        builder.addGridded(cancelButton);
        builder.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        return builder.getPanel();
    }
    
    /**
     * @return <code>true</code> if card was created.
     */
    private boolean createCard(boolean keepOpen) 
    {
        if (m_cardPanel.isValidCard())
        {
            FormattedText frontside = m_cardPanel.getFrontText();
            FormattedText backside = m_cardPanel.getBackText();
            Category category = m_cardPanel.getCategoryComboBox().getSelectedCategory();

            Card card = new Card(frontside, backside);
            
            ImageRepository repo = ImageRepository.getInstance();
            card.getFrontSide().setImages(repo.addImages(m_cardPanel.getFrontImages()));
            card.getBackSide().setImages(repo.addImages(m_cardPanel.getBackImages()));
        
            category.addCard(card);
            
            m_manager.newCardCreated(card);
            m_cardPanel.reset();
            
            if (!keepOpen)
            {
                closeWindow();
            }

            return !keepOpen;    
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
    
    private void closeWindow()
    {
        m_manager.newCardFrameClosed(this);
        Settings.removedCardFontObserver(m_cardPanel);
        dispose();
    }
}
