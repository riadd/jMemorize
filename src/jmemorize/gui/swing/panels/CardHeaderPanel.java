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

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import jmemorize.core.Card;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.ColorConstants;
import jmemorize.util.TimeSpan;

/**
 * Displays a expandable card header.
 * 
 * @author djemili
 */
public class CardHeaderPanel extends JPanel
{
    private JLabel  m_label         = new JLabel();
    private JLabel  m_iconLabel     = new JLabel();
    
    private boolean m_expanded      = false;
    private Card    m_card;

    private Icon    m_expandedIcon  = UIManager.getIcon("Tree.expandedIcon"); //$NON-NLS-1$
    private Icon    m_collapsedIcon = UIManager.getIcon("Tree.collapsedIcon"); //$NON-NLS-1$

    public CardHeaderPanel()
    {
        initComponents();
    }
    
    public void setCard(Card card)
    {
        m_card = card;
        
        String text = m_expanded ? longCardSummary(card) : shortCardSummary(card); 
        m_label.setText(text);
        m_iconLabel.setIcon(m_expanded ? m_expandedIcon : m_collapsedIcon);
    }

    private void initComponents()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(ColorConstants.SIDEBAR_COLOR);
        
        m_label.setVerticalTextPosition(JLabel.TOP);
        m_label.setBorder(new EmptyBorder(5, 10, 5, 5));
        m_label.setAlignmentY(Component.TOP_ALIGNMENT);
        
        m_iconLabel.setBorder(new EmptyBorder(8, 5, 5, 0));
        m_iconLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        
        add(m_iconLabel);
        add(m_label);

        m_iconLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                m_expanded = !m_expanded;
                setCard(m_card);
            }
        });
    }
    
    private String longCardSummary(Card card)
    {
        // fill history data
        String ratio = "-";
        if (card.getTestsTotal() > 0)
        {
            ratio = String.format("%d%%    (%d/%d)", //$NON-NLS-1$
                card.getPassRatio(), card.getTestsPassed(), card.getTestsTotal());
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");  //$NON-NLS-1$
        sb.append("<table cellpadding=\"1\">");  //$NON-NLS-1$
        
        appendTR(sb, Localization.get(LC.DECK),      Integer.toString(card.getLevel()));
        appendTR(sb, Localization.get(LC.EXPIRES),   dateString(card.getDateExpired()));
        appendTR(sb, Localization.get(LC.LAST_TEST), dateString(card.getDateTested()));
        appendTR(sb, Localization.get(LC.CREATED),   dateString(card.getDateCreated()));
        appendTR(sb, Localization.get(LC.MODIFIED),  dateString(card.getDateModified()));
        appendTR(sb, Localization.get(LC.RATIO),     ratio);
        
        sb.append("</table>");  //$NON-NLS-1$
        sb.append("</html>");  //$NON-NLS-1$
        
        return sb.toString();
    }
    
    private void appendTR(StringBuffer sb, String key, String value)
    {
        sb.append("<tr><td><b>").  //$NON-NLS-1$
            append(key).
            append(":</b>&nbsp;&nbsp;&nbsp;</td><td>").  //$NON-NLS-1$
            append(value).
            append("</td></tr>");  //$NON-NLS-1$
    }
    
    private String shortCardSummary(Card card)
    {
        String status = ""; //$NON-NLS-1$
        if (card.getDateExpired() == null)
        {
            status = Localization.get(LC.UNLEARNED);
        }
        else
        {
            String span = TimeSpan.format(new Date(), card.getDateExpired());
            if (card.isLearned())
            {
                status = String.format("%s (%s %s)",  //$NON-NLS-1$
                    Localization.get(LC.LEARNED),Localization.get(LC.EXPIRES), span);
            }
            else if (card.isExpired())
            {
                status = String.format("%s (%s)",  //$NON-NLS-1$ 
                    Localization.get(LC.EXPIRED), span);
            }
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append("<html>"); //$NON-NLS-1$
        sb.append("<b>"); //$NON-NLS-1$
        sb.append(Localization.get(LC.DECK));
        sb.append(":</b> "); //$NON-NLS-1$
        sb.append(card.getLevel());
        sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"); //$NON-NLS-1$
        sb.append("<b>Status:</b> "); //$NON-NLS-1$
        sb.append(status);
        sb.append("</html>"); //$NON-NLS-1$
        
        return sb.toString();
    }
    
    private String dateString(Date date)
    {
        return date != null ? 
            Localization.LONG_DATE_FORMATER.format(date) : "-"; //$NON-NLS-1$
    }
}
