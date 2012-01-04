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
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import jmemorize.core.Card;
import jmemorize.core.CardSide;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;
import jmemorize.core.Events;
import jmemorize.core.Main;
import jmemorize.core.Settings;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSessionObserver;
import jmemorize.core.learn.LearnSession.LearnCardObserver;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.CardFont;
import jmemorize.gui.swing.Quiz;
import jmemorize.gui.swing.CardFont.FontType;
import jmemorize.gui.swing.actions.AbstractAction2;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author djemili
 */
public class QuizPanel extends JPanel implements Events, 
    LearnCardObserver, CategoryObserver, LearnSessionObserver
{
    private abstract class AbstractLearnAction extends AbstractAction2
    {
        private boolean m_shortCutAdded = false; 
        
        protected void setFocusedWindowShortcut(String action, char key)
        {
            getInputMap(JButton.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(key), action);
            
            getActionMap().put(action, this);
            
            if (!m_shortCutAdded)
            {
                String name = (String)getValue(NAME);
                setName(String.format("<html>%s&nbsp;<font color=gray>(%s)</font></html>", name, key));
                
                m_shortCutAdded = true;
            }
        }
    }
    
    private class ShowAction extends AbstractLearnAction
    {
        public ShowAction()
        {
            setName(Localization.get(LC.LEARN_SHOW));
            setFocusedWindowShortcut("showButton", 'a'); //$NON-NLS-1$
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if (m_isShowQuestion)
                showAnswer();
        }
    }
    
    private class SkipAction extends AbstractLearnAction
    {
        public SkipAction()
        {
            setName(Localization.get(LC.LEARN_SKIP));
            setFocusedWindowShortcut("skipButton", 's'); //$NON-NLS-1$
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if (m_isShowQuestion)
                m_session.cardSkipped();
        }
    }
    
    private class YesAction extends AbstractLearnAction
    {
        public YesAction()
        {
            setName(Localization.get(LC.LEARN_YES));
            setFocusedWindowShortcut("yesButton", 'q'); //$NON-NLS-1$
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if (m_isShowAnswer)
                m_session.cardChecked(true, m_showFlipped);
        }
    }

    private class NoAction extends AbstractLearnAction
    {
        public NoAction()
        {
            setName(Localization.get(LC.LEARN_NO));
            setFocusedWindowShortcut("noButton", 'w'); //$NON-NLS-1$
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if (m_isShowAnswer)
                m_session.cardChecked(false, m_showFlipped);
        }
    }
    
    private static final String ANSWER_CARD       = "answerCard";   //$NON-NLS-1$
    private static final String QUESTION_CARD     = "questionCard"; //$NON-NLS-1$
    
    //  swing elements
    private JButton           m_showButton        = new JButton(new ShowAction());
    private JButton           m_yesButton         = new JButton(new YesAction());
    
    private TwoSidesCardPanel m_questionCardPanel = new TwoSidesCardPanel(false, false);
    private Quiz              m_quiz              = new ThinkQuiz();
    private JPanel            m_answerBarPanel    = buildAnswerButtonBar();
    private JPanel            m_questionBarPanel  = buildQuestionButtonBar();
    private JPanel            m_barPanel          = new JPanel();
    
    private LearnSession      m_session;
    private Card              m_currentCard;
    private boolean           m_showFlipped;
    private boolean           m_frontWasVisible;
    
    private JCheckBox         m_categoryCheckBox  = new JCheckBox(
        Localization.get(LC.LEARN_SHOW_CATEGORY));
    private JTextField        m_categoryField     = new JTextField();
    
    private boolean           m_isShowQuestion;
    private boolean           m_isShowAnswer;
    
    private static String     PREFS_SHOW_CARD_CATEGORY = "show.card-category"; //$NON-NLS-1$

    public QuizPanel()
    {
        initComponents();
        Main.getInstance().addLearnSessionObserver(this);
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.learn.LearnSessionObserver
     */
    public void sessionStarted(LearnSession session)
    {
        m_session = session;
        session.addObserver(this);
        
        m_session.getCategory().addObserver(this);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.learn.LearnSessionObserver
     */
    public void sessionEnded(LearnSession session)
    {
        m_isShowQuestion = false;
        m_isShowAnswer = false;
        
        m_session.getCategory().removeObserver(this);
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
        m_showFlipped = flipped;
        
        updateFonts();
        updateCardSidePanels();
        updateCategoryField();
        
        showQuestion();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        if (card == m_currentCard)
        {
            if (type == EDITED_EVENT)
                updateCardSidePanels();
            
            if (type == MOVED_EVENT)
                updateCategoryField();
        }
    }

    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        assert false; // no category events should occur while learning
    }

    private void updateFonts()
    {
        CardFont frontFont = Settings.loadFont(FontType.LEARN_FRONT);
        CardFont flipFont = Settings.loadFont(FontType.LEARN_FLIP);
        
        CardFont questionFont = !m_showFlipped ?  frontFont : flipFont;
        CardFont answerFont = !m_showFlipped ? flipFont : frontFont;

        m_questionCardPanel.fontChanged(FontType.CARD_FRONT, questionFont);
        m_questionCardPanel.fontChanged(FontType.CARD_FLIP, answerFont);
        
        m_quiz.setQuestionFont(questionFont);
        m_quiz.setAnswerFont(answerFont);
    }

    private void initComponents() 
    {
        m_questionCardPanel.setEditable(false);
        m_questionCardPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        m_questionCardPanel.addCardSide(Localization.get(LC.FLIPSIDE), m_quiz.getVisual());
        
        setLayout(new BorderLayout());
        
        m_barPanel.setLayout(new CardLayout());
        m_barPanel.add(m_questionBarPanel, QUESTION_CARD);
        m_barPanel.add(m_answerBarPanel,   ANSWER_CARD);

        JPanel mainCardPanel = new JPanel(new BorderLayout());
        mainCardPanel.setBorder(new EtchedBorder());
        
        JPanel catPanel = buildCategoryPanel();
        mainCardPanel.add(catPanel, BorderLayout.NORTH);
        mainCardPanel.add(m_questionCardPanel, BorderLayout.CENTER);
        mainCardPanel.add(m_barPanel, BorderLayout.SOUTH);

        add(mainCardPanel, BorderLayout.CENTER);
    }
    
    /**
     * Fills the text panes with the card side texts of the currently shown
     * card.
     */
    private void updateCardSidePanels()
    {
        if (m_currentCard != null)
        {
            CardSide questionSide = !m_showFlipped ? 
                m_currentCard.getFrontSide() : 
                m_currentCard.getBackSide();
                
            CardSide answerSide = !m_showFlipped ? 
                m_currentCard.getBackSide() : 
                m_currentCard.getFrontSide();
            
            m_questionCardPanel.setTextSides(questionSide.getText(), answerSide.getText());
            m_questionCardPanel.setImages(questionSide.getImages(), answerSide.getImages());
            
            m_quiz.showQuestion(answerSide);
        }
        
        m_questionCardPanel.setFlipped(m_showFlipped);
    }
    
    private void updateCategoryField()
    {
        m_categoryField.setEnabled(m_categoryCheckBox.isSelected());
        m_categoryField.setText(m_categoryCheckBox.isSelected() ? 
            m_currentCard.getCategory().getPath() 
            +" ("+ m_currentCard.getLevel() +")" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private void showQuestion()
    {
        if (m_isShowAnswer)
        {
            m_frontWasVisible = m_questionCardPanel.isCardSideVisible(0) && 
                m_questionCardPanel.isCardSideVisible(1);
        }
        
        m_questionCardPanel.setCardSideVisible(0, true);
        m_questionCardPanel.setCardSideVisible(1, false);
        m_questionCardPanel.setCardSideEnabled(1, false);
        
        ((CardLayout)m_barPanel.getLayout()).show(m_barPanel, QUESTION_CARD);
        
        m_isShowQuestion = true;
        m_isShowAnswer = false;
        
        m_showButton.requestFocus();
    }
    
    private void showAnswer()
    {
        m_questionCardPanel.setCardSideVisible(0, m_frontWasVisible);
        m_questionCardPanel.setCardSideVisible(1, true);
        m_questionCardPanel.setCardSideEnabled(1, true);
        
        ((CardLayout)m_barPanel.getLayout()).show(m_barPanel, ANSWER_CARD);
        
        m_isShowQuestion = false;
        m_isShowAnswer = true;
        
        float result = m_quiz.showAnswer();
        if (result >= 0) // HACK
        {
            m_session.cardChecked((result >= 0.5f), m_showFlipped);
        }
        
        m_yesButton.requestFocus();
    }
    
    private JPanel buildCategoryPanel()
    {
        // prepare category field and checkbox
        m_categoryCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                boolean showCategory = m_categoryCheckBox.isSelected();
                Main.USER_PREFS.putBoolean(PREFS_SHOW_CARD_CATEGORY, showCategory);
                updateCategoryField();
            }
        });
        m_categoryField.setEditable(false);
        boolean showCat = Main.USER_PREFS.getBoolean(PREFS_SHOW_CARD_CATEGORY, true);
        m_categoryCheckBox.setSelected(showCat);
        
        // build it using the forms layout
        FormLayout layout = new FormLayout(
            "38dlu, 3dlu, p:grow, 3dlu, right:p", // columns //$NON-NLS-1$
            "20px");                              // rows    //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setBorder(new EmptyBorder(10, 10, 7, 10));
        
        builder.addLabel(Localization.get(LC.CATEGORY), cc.xy(1, 1));
        builder.add(m_categoryField,                    cc.xy(3, 1));
        builder.add(m_categoryCheckBox ,                cc.xy(5, 1));
        
        return builder.getPanel();
    }
    
    private JPanel buildQuestionButtonBar()
    {
        JButton skipButton = new JButton(new SkipAction());
        
        // build it using forms layout
        FormLayout layout = getBottomFormLayout();
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setBorder(new EmptyBorder(5, 5, 5, 10));
        
        builder.addLabel(m_quiz.getHelpText(), cc.xy(1, 1));
        builder.add(m_showButton, cc.xy(3,1));
        builder.add(skipButton,   cc.xy(5,1));
        
        return builder.getPanel();
    }
    
    private JPanel buildAnswerButtonBar()
    {
        // preapre no button
        JButton noButton = new JButton(new NoAction());
        
        // build it using forms layout
        FormLayout layout = getBottomFormLayout();
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setBorder(new EmptyBorder(5, 5, 5, 10));
        
        builder.addLabel(Localization.get(LC.LEARN_DID_YOU_KNOW), cc.xy(1,1));
        builder.add(m_yesButton, cc.xy(3,1));
        builder.add(noButton,    cc.xy(5,1));
        
        return builder.getPanel();
    }
    
    private FormLayout getBottomFormLayout()
    {
        return new FormLayout(
            "right:p:grow, 3dlu, 80dlu, 3dlu, 80dlu", //$NON-NLS-1$ 
            "p"); //$NON-NLS-1$
    }
}
