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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.Main;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSessionObserver;
import jmemorize.core.learn.LearnSettings;
import jmemorize.core.learn.LearnSession.LearnCardObserver;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.ColorConstants;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.actions.AbstractAction2;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The panel that shows learning sessions (learning preferences are handled by
 * the LearnSettings classes.
 * 
 * @author djemili
 */
public class LearnPanel extends JPanel implements SelectionProvider, 
    LearnSessionObserver, LearnCardObserver
{    
    private class StopAction extends AbstractAction2
    {
        public StopAction()
        {
            setName(Localization.get(LC.LEARN_STOP));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            m_session.endLearning();
        }
    }
    
    private LearnSession      m_session;
    private Card              m_currentCard;
    
    private TimerPanel        m_timerPanel        = new TimerPanel();
    private CardCounterPanel  m_cardCounterPanel  = new CardCounterPanel();
    
    private JLabel            m_flippedLabel      = new JLabel(
        Localization.get(LC.LEARN_FLIPPED),
        new ImageIcon(getClass().getResource("/resource/icons/card_flipped.gif")), //$NON-NLS-1$
        SwingConstants.CENTER
    );
    
    // member variables so we can make them invisible
    private JLabel                  m_currentCardProgressLabel;
    private JProgressBar            m_currentCardProgressBar;
    private boolean                 m_isPartialProgressMode;

    private StatusBar               m_statusBar;
    private List<SelectionObserver> m_selectionListeners = new LinkedList<SelectionObserver>();
    
    public LearnPanel() 
    {
        initComponents();
        Main.getInstance().addLearnSessionObserver(this);
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LearnSessionObserver
     */
    public void sessionStarted(LearnSession session)
    {
        m_session = session;
        m_session.addObserver(this);
        
        LearnSettings settings = session.getSettings();
        if (settings.isTimeLimitEnabled())
        {
            m_timerPanel.start(session, settings.getTimeLimit() * 60);
        }
        else
        {
            m_timerPanel.start(session);
        }
        
        // test always showing the extent progress bar
        int targetCards = m_session.getCardsLeft().size();
        if (session.getSettings().isCardLimitEnabled())
        {
            targetCards = Math.min(session.getSettings().getCardLimit(), targetCards);
        }
        m_cardCounterPanel.start(targetCards);
    
        int sidesToTest = settings.getAmountToTest(true) + 
            settings.getAmountToTest(false);
        
        m_isPartialProgressMode = settings.getSidesMode() == LearnSettings.SIDES_BOTH &&
            (sidesToTest > 1);
        
        m_currentCardProgressLabel.setVisible(m_isPartialProgressMode);
        m_currentCardProgressBar.setVisible(m_isPartialProgressMode);
        m_currentCardProgressBar.setMinimum(0);
        m_currentCardProgressBar.setMaximum(sidesToTest);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LearnSessionObserver
     */
    public void sessionEnded(LearnSession session)
    {
        m_timerPanel.stop();
        
        if (m_statusBar != null)
        {
            m_statusBar.setLeftText(""); //$NON-NLS-1$
        }
    }

    /**
     * Show the card.
     * 
     * @param flipped <code>true</code> if card should be shown with reversed
     * sides (that is the frontside will be shown as flipside and vice versa)
     * <code>false</code> otherwise.
     */
    public void nextCardFetched(Card card, boolean flipped)
    {
        m_currentCard = card;
        m_flippedLabel.setVisible(flipped);
        
        for (SelectionObserver listener : m_selectionListeners)
        {
            listener.selectionChanged(this);
        }
        
        updateStatusBar();
        updateProgressBars();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Card> getRelatedCards()
    {
        List<Card> l = new ArrayList<Card>();
        l.addAll(m_session.getCheckedCards());
        return l;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public Category getCategory()
    {
        return m_session.getCategory();
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Category> getSelectedCategories()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Card> getSelectedCards()
    {
        List<Card> list = new ArrayList<Card>(1);
        list.add(m_currentCard);
        return list.size() > 0 ? list : null;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public void addSelectionObserver(SelectionObserver observer)
    {
        m_selectionListeners.add(observer);
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public void removeSelectionObserver(SelectionObserver observer)
    {
        m_selectionListeners.remove(observer);
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public JComponent getDefaultFocusOwner()
    {
        return this;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public JFrame getFrame()
    {
        return Main.getInstance().getFrame();
    }
    
    /**
     * Set the status bar for use with this panel.
     */
    public void setStatusBar(StatusBar statusBar)
    {
        m_statusBar = statusBar;
    }
    
    private void initComponents() 
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buildSidebarPanel(), BorderLayout.WEST);
        mainPanel.add(new QuizPanel(), BorderLayout.CENTER);
        
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel buildSidebarPanel()
    {
        m_timerPanel.setBackground(ColorConstants.SIDEBAR_COLOR);
        m_timerPanel.setPreferredSize(new Dimension(140, 22));
        
        m_currentCardProgressLabel = new JLabel(Localization.getEmpty(LC.LEARN_CARD));
        m_currentCardProgressLabel.setVisible(false);
        
        m_currentCardProgressBar = new JProgressBar();
        m_currentCardProgressBar.setMinimum(0);
        m_currentCardProgressBar.setMaximum(2);
        m_currentCardProgressBar.setValue(0);
        m_currentCardProgressBar.setVisible(false);
        
        // force to solid rendering (default is ugly stripe)
        m_currentCardProgressBar.setStringPainted(true);
        m_currentCardProgressBar.setPreferredSize(new Dimension(140, 22));
        
        m_cardCounterPanel.setBackground(ColorConstants.SIDEBAR_COLOR);
        JButton stopLearningButton = new JButton(new StopAction());
        
        // build it using FormLayout
        FormLayout layout = new FormLayout(
            "center:170px:grow", // columns //$NON-NLS-1$
            "9dlu, p, 3dlu, p, 12dlu, p, 3dlu, p, 12dlu, p, 3dlu, " + //$NON-NLS-1$
            "p, 12dlu, fill:p:grow, 5dlu, p, 5px"); // rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.addLabel(Localization.get(LC.LEARN_TIMER), cc.xy(1,  2));
        builder.add(m_timerPanel,                          cc.xy(1,  4));
        
        String title = Localization.get(LC.LEARN_SESSION, LC.LEARN_CARD_COUNTER);
        builder.addLabel(title,                            cc.xy(1,  6));
        
        builder.add(m_cardCounterPanel,                    cc.xy(1,  8));
        builder.add(m_currentCardProgressLabel,            cc.xy(1, 10));
        builder.add(m_currentCardProgressBar,              cc.xy(1, 12));
        builder.add(m_flippedLabel,                        cc.xy(1, 14));
        builder.add(stopLearningButton,                    cc.xy(1, 16));
        
        JPanel sidePanel = builder.getPanel();
        sidePanel.setBackground(ColorConstants.SIDEBAR_COLOR);
        sidePanel.setBorder(new EtchedBorder());
        return sidePanel;
    }
    
    private void updateProgressBars() 
    {
        int cardsLearned = m_session.getNCardsLearned();
        int cardsPartiallyLearned = m_session.getNCardsPartiallyLearned();
        
        m_cardCounterPanel.setCardsPassed(cardsLearned, cardsPartiallyLearned);
        if (!m_isPartialProgressMode || m_currentCard == null)
            return;
        
        int amtLearned = m_currentCard.getLearnedAmount(true) + m_currentCard.getLearnedAmount(false);
        
        m_currentCardProgressBar.setValue(amtLearned);
        m_currentCardProgressBar.setString(amtLearned + " / " +  //$NON-NLS-1$
            m_currentCardProgressBar.getMaximum());
    }
    
    private void updateStatusBar()
    {
        if (m_statusBar != null)
        {
            Object[] args = {
                Localization.get(LC.STATUS_LEARNING_CATEGORY),
                m_session.getCategory().getName(),
                Localization.get(LC.STATUS_CARDS_LEFT), 
                new Integer(m_session.getCardsLeft().size())};
            
            MessageFormat form = new MessageFormat("{0}: {1}  {2}: {3}"); //$NON-NLS-1$
            m_statusBar.setLeftText(form.format(args));
        }
    }
}
