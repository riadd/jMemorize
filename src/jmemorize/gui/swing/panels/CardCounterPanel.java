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
import java.awt.Dimension;
import java.awt.Font;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import jmemorize.core.Card;
import jmemorize.core.Main;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSessionObserver;
import jmemorize.core.learn.LearnSettings;
import jmemorize.core.learn.LearnSession.LearnCardObserver;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.ColorConstants;
import jmemorize.gui.swing.widgets.ExtentProgressBar;
import jmemorize.gui.swing.widgets.PartialProgressBar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

/**
 * A panel that is used in the learn sidebar and shows how many cards have been
 * learned in the running learn session.
 * 
 * @author djemili
 */
public class CardCounterPanel extends JPanel
{
    private static boolean     USE_PIECHART    = false;
    private static boolean     USE_EXTENT_BAR  = false;
    private static boolean     USE_PARTIAL_BAR = false;

    private int                m_cardsPassed;
    private int                m_cardsPartiallyPassed;
    private int                m_cardsTarget;
    private boolean            m_showPartiallyPassed;

    private LearnSession       m_session;
    private List<Card>         m_cards;
    
    private DefaultPieDataset  m_pieDataset;
    private JProgressBar       m_bar;
    private JTextField         m_textField     = new JTextField();

    public CardCounterPanel()
    {
        if (USE_PARTIAL_BAR)
            attachPartialProgressBar();
    }
    
    public void start(int target)
    {
        m_cardsTarget = target;
        m_cardsPassed = 0;
        m_cardsPartiallyPassed = 0;
        m_showPartiallyPassed = false;

        initComponents(target);
    }

    public void start()
    {
        start(-1);
    }

    public void setCardsPassed(int passed, int partiallyPassed)
    {
        m_cardsPassed = passed;
        m_cardsPartiallyPassed = partiallyPassed;
        
        if (partiallyPassed > 0) 
        {
            m_showPartiallyPassed = true;
        }
        
        if (m_cardsTarget > -1)
        {
            if (USE_PIECHART) 
            {
                int cardsUnlearned = m_cardsTarget - (m_cardsPassed + m_cardsPartiallyPassed);
                m_pieDataset.setValue(Localization.get(LC.STATUS_LEARNED), m_cardsPassed);
                m_pieDataset.setValue(Localization.get(LC.STATUS_PARTIAL), m_cardsPartiallyPassed);
                m_pieDataset.setValue(Localization.get(LC.STATUS_UNLEARNED), cardsUnlearned);    
            } 
            else
            {
                if (!USE_PARTIAL_BAR)
                    m_bar.setValue(m_cardsPassed);
                
                
                m_bar.setString(getCardString());
                m_bar.setForeground(ColorConstants.LEARNED_CARDS.darker());
                
                if (USE_EXTENT_BAR)
                {
                    ExtentProgressBar bar = (ExtentProgressBar)m_bar;
                    bar.setValue(m_cardsPartiallyPassed);                    
                    bar.setExtent(m_cardsPassed);
                }
            }
        }
        else
        {
            m_textField.setText(getCardString());
        }
    }

    private void initComponents(int target)
    {
        removeAll();
        setLayout(new BorderLayout());
        
        // if there is a card limit show a progess bar or pie chart
        if (m_cardsTarget > -1)
        {
            if (USE_PIECHART) 
            {
                m_pieDataset = new DefaultPieDataset();
                m_pieDataset.setValue(Localization.get(LC.STATUS_LEARNED), 0);
                m_pieDataset.setValue(Localization.get(LC.STATUS_PARTIAL), 0);
                m_pieDataset.setValue(Localization.get(LC.STATUS_UNLEARNED) , m_cardsTarget);
                
                add(buildPiePanel(), BorderLayout.CENTER);                
            } 
            else
            {
                if (USE_EXTENT_BAR)
                    m_bar = buildExtentProgressBar();
                else if (USE_PARTIAL_BAR)
                    m_bar = buildPartialProgressBar();
                else
                    m_bar = new JProgressBar();
                
                m_bar.setMaximum(target);
                m_bar.setMinimum(0);
                    
                m_bar.setStringPainted(true);
                m_bar.setString(getCardString());
                m_bar.setValue(0);
                
                add(m_bar, BorderLayout.CENTER);
            }
        }
        // otherwise show a textfield
        else
        {
            m_textField.setText(getCardString());
            m_textField.setHorizontalAlignment(JTextField.CENTER);
            m_textField.setEditable(false);
            add(m_textField, BorderLayout.CENTER);
        }
        
        setPreferredSize(new Dimension(140, USE_PIECHART ? 140 : 22));
    }

    private ChartPanel buildPiePanel()
    {
        JFreeChart chart = ChartFactory.createPieChart(null, m_pieDataset,
            true, false, false );
        
        setupPiePlot((PiePlot)chart.getPlot());
        setupPieLegend(chart);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setDisplayToolTips(false);
        
        /*
         * RED_FLAG this is a WORKAROUND TO a JFreeChart bug (as of 1.0.4) which
         * causes an affine transform to be applied to charts smaller than
         * 300x200. If the minimum sizes are not set to the same value, the
         * chart and legend are scaled by the proprtion! see
         * http://www.jfree.org/phpBB2/viewtopic.php?t=16972
         */
        chartPanel.setMinimumDrawHeight(300);
        chartPanel.setMinimumDrawWidth(300);
        
        return chartPanel;
    }

    private void setupPieLegend(JFreeChart chart)
    {
        LegendTitle legend = chart.getLegend();
        Font legendFont = legend.getItemFont();
        legend.setItemFont(legendFont.deriveFont(Font.BOLD, 20));
    }

    private void setupPiePlot(PiePlot plot)
    {
        plot.setMaximumLabelWidth(0.0);
        plot.setLabelGap(0.0);
        plot.setInteriorGap(0.0);
        //piePlot.setLabelLinkMargin(0.0);
        plot.setCircular(true);
        plot.setLabelGenerator(new PieSectionLabelGenerator() {
            public String generateSectionLabel(PieDataset arg0, Comparable arg1)
            {
                return null;
            }
            
            public AttributedString generateAttributedSectionLabel(
                PieDataset arg0, Comparable arg1)
            {
                return null;
            }
        });
        //piePlot.setForegroundAlpha(0.5f);
        
        plot.setSectionPaint(Localization.get(LC.STATUS_LEARNED), ColorConstants.LEARNED_CARDS);
        plot.setSectionPaint(Localization.get(LC.STATUS_PARTIAL), ColorConstants.PARTIAL_LEARNED_CARDS);
        plot.setSectionPaint(Localization.get(LC.STATUS_UNLEARNED), ColorConstants.UNLEARNED_CARDS);
    }

    private ExtentProgressBar buildExtentProgressBar()
    {
        ExtentProgressBar bar = new ExtentProgressBar();
        
        bar.setForeground(Color.BLUE);
        Color grn = Color.GREEN.darker();
        Color transparentGreen = new Color(grn.getRed(), grn.getGreen(), grn.getBlue(), 128);
        bar.setExtentForeground(transparentGreen);
        bar.setExtent(0);
        
        return bar;
    }
    
    private float[] getValues()
    {
        float[] vals = new float[m_cards.size()];
        LearnSettings settings = m_session.getSettings();
        int frontTargetAmount = settings.getAmountToTest(true);
        int backTargetAmount = settings.getAmountToTest(false);
        float targetAmount = frontTargetAmount + backTargetAmount;

        for (int i = 0; i < m_cards.size(); i++)
        {
            Card card = (Card)m_cards.get(i);
            if (m_session.getPassedCards().contains(card)  || 
                m_session.getRelearnedCards().contains(card)) 
            {
                vals[i] = 1f;
            } 
            else 
            {                        
                int frontLearnedAmount = card.getLearnedAmount(true); 
                int backLearnedAmount = card.getLearnedAmount(false);

                frontLearnedAmount = Math.min(frontLearnedAmount, frontTargetAmount);
                backLearnedAmount = Math.min(backLearnedAmount, backTargetAmount);
                vals[i] = (frontLearnedAmount + backLearnedAmount) / targetAmount;
            }
        }
        
        //Arrays.sort(vals);
        
        return vals;
    }
    
    private void attachPartialProgressBar()
    {
        Main.getInstance().addLearnSessionObserver(new LearnSessionObserver() {
            class LearnCardObs implements LearnCardObserver
            {
                public void nextCardFetched(Card nextCard, boolean flippedMode)
                {
                    if (m_cards == null) 
                    {
                        m_cards = new ArrayList<Card>();
                        m_cards.addAll(m_session.getCardsLeft());
                    }
                    
                    if (! m_cards.contains(nextCard) ) 
                    {
                        // if the new card is not in m_cards, then a card has been skipped and
                        // this card added.  We have to figure out the skipped card
                        // this is kind of inefficient, but only happens on a skipped card... 
                        // note also that we don't necessarily see the new card immediately 
                        // after the skip, so the card positions may not update immediately.
                        Set<Card> cardsToRemove = new HashSet<Card>();
                        cardsToRemove.addAll(m_session.getSkippedCards());
                        cardsToRemove.retainAll(m_cards);
                        m_cards.removeAll(cardsToRemove);
                        m_cards.add(nextCard);
                    }
                    
                    PartialProgressBar bar = (PartialProgressBar)m_bar;
                    bar.setValues(getValues());
                }
            }

            private LearnCardObserver m_obs;
            
            public void sessionEnded(LearnSession session)
            {
                session.removeObserver(m_obs);
            }

            public void sessionStarted(LearnSession session)
            {
                m_session = session;
                m_cards = null;
                
                m_obs = new LearnCardObs();
                session.addObserver(m_obs);
            }
        });
    }
    
    private PartialProgressBar buildPartialProgressBar()
    {
        return new PartialProgressBar();
    }
    
    /**
     * @return The string that is used to show the number of cards in progressbar.
     */
    private String getCardString()
    {
        // if progress bar
        if (m_cardsTarget > -1)
        {
//            if (m_showPartiallyPassed) 
//            {
//                return m_cardsPartiallyPassed + " + " + m_cardsPassed + " / " 
//                    + m_cardsTarget; //$NON-NLS-1$
//            } 
//            else 
            {
                return m_cardsPassed + " / " + m_cardsTarget; //$NON-NLS-1$
            }
        }
        // else text field
        else
        {
            // TODO consider adding lang specific strings 
            if (m_showPartiallyPassed) 
            {
                return m_cardsPartiallyPassed + " / " + m_cardsPassed;
            }
            
            return Integer.toString(m_cardsPassed);
        }
    }
}
