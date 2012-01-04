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
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import jmemorize.core.Card;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A status bar that can be linked to card tables and other objects to display
 * additional data about them.
 * 
 * @author djemili
 */
public class StatusBar extends JPanel
{
    // TODO use cards provider
    
    private JLabel m_leftLabel  = new JLabel();
    private JLabel m_rightLabel = new JLabel();
    
    public StatusBar()
    {
        setLayout(new BorderLayout());
        add(buildStatusBar(), BorderLayout.CENTER);
    }
    
    /**
     * Sets the text that should appear on the left part of the status bar. The
     * right side is always reserved to show the number of cards.
     * 
     * @param text
     */
    public void setLeftText(String text)
    {
        m_leftLabel.setText(text);
    }
    
    /**
     * Sets currently shown cards. This is used for the right part of the status
     * bar that shows the number of currently displayed cards.
     * 
     * @param cards The cards that are currently displayed.
     */
    public void setCards(List<Card> cards)
    {
        int unlearned = 0;
        int learned = 0;
        int expired = 0;
        
        for (Card card : cards)
        {
            if (card.isUnlearned())
            {
                unlearned++;
            }
            else if (card.isLearned())
            {
                learned++;
            }
            else //if (card.isExpired())
            {
                expired++;
            }
        }
        
        Object[] args = {
            Localization.get(LC.STATUS_CARDS),     new Integer(cards.size()),
            Localization.get(LC.STATUS_LEARNED),   new Integer(learned),
            Localization.get(LC.STATUS_EXPIRED),   new Integer(expired),
            Localization.get(LC.STATUS_UNLEARNED), new Integer(unlearned)};
        
        MessageFormat form = new MessageFormat("{0}: {1} ({2}: {3}  {4}: {5}  {6}: {7})"); //$NON-NLS-1$
        
        m_rightLabel.setText(form.format(args));
    }
    
    private JPanel buildStatusBar()
    {
        FormLayout layout = new FormLayout(
            "3dlu, p, 9dlu:grow, p, 3dlu", // columns //$NON-NLS-1$
            "p");                          // rows    //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setBorder(new EmptyBorder(4, 5, 2, 5));
        
        builder.add(m_leftLabel,   cc.xy(2,1));
        builder.add(m_rightLabel,  cc.xy(4,1));
        
        return builder.getPanel();
    }
}
