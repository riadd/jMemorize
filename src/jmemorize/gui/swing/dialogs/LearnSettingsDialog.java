/*
 * jMemorize - Learning made easy (and fun) - A Leitner flashcards tool
 * Copyright(C) 2004-2008 Riad Djemili
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
package jmemorize.gui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.Settings;
import jmemorize.core.learn.LearnSettings;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.frames.MainFrame;
import jmemorize.gui.swing.panels.LearnSettingPanels;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A modal dialog that allows to change the learn session settings for the next
 * learn session.
 * 
 * @author djemili
 */
public class LearnSettingsDialog extends JDialog
{
    private static final String[] PANEL_NAMES = new String[] {
        Localization.get(LC.GENERAL),
        Localization.get(LC.LEARN_SETTINGS_ADVANCED),
        Localization.get(LC.LEARN_SETTINGS_SCHEDULING)
    };
    
    private JList              m_sideList       = new JList(PANEL_NAMES);
    
    private LearnSettings      m_settings;
    private MainFrame          m_frame;
    
    private LearnSettingPanels m_learnSettingsPanels;
    private JPanel             m_settingsPanel  = new JPanel(new CardLayout());
    private JButton            m_applyButton    = new JButton(Localization.get(LC.APPLY));
    
    private List<Card>         m_selectedCards;
    
    public LearnSettingsDialog(MainFrame frame, LearnSettings strategy, 
        SelectionProvider provider)
    {
        super(frame, true);
        
        m_frame = frame;
        m_learnSettingsPanels = new LearnSettingPanels();
        
        initComponents();
        
        m_settings = strategy;
        m_learnSettingsPanels.setStrategy(m_settings);
        m_selectedCards = provider.getSelectedCards();
        
        m_learnSettingsPanels.setProvider(provider);
        
        setLocationRelativeTo(frame);
        setVisible(true);
    }
    
    private void initComponents()
    {
        getContentPane().add(buildMainPanel(), BorderLayout.CENTER);
        getContentPane().add(buildButtonBar(), BorderLayout.SOUTH);
        
        // on ESC key close dialog
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel"); //$NON-NLS-1$
        getRootPane().getActionMap().put("Cancel", new AbstractAction(){ //$NON-NLS-1$
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });
        
        setTitle(Localization.get(LC.LEARN_SETTINGS_TITLE));
        setResizable(false);
        
        pack();
    }
    
    private JPanel buildMainPanel()
    {
        // build settings panel
        m_settingsPanel.add(buildGeneralSettingsPanel(),  PANEL_NAMES[0]);
        m_settingsPanel.add(buildAdvancedPanel(),         PANEL_NAMES[1]);
        m_settingsPanel.add(buildSchedulesPanel(),        PANEL_NAMES[2]);
        m_settingsPanel.setBorder(new EtchedBorder());
        
        // build side list
        m_sideList.setBorder(new EtchedBorder());
        m_sideList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_sideList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                CardLayout layout = (CardLayout)m_settingsPanel.getLayout();
                layout.show(m_settingsPanel, PANEL_NAMES[m_sideList.getSelectedIndex()]);
            }
        });
        m_sideList.setSelectedIndex(0);
        
        // build main panel
        FormLayout layout = new FormLayout(
            "70dlu, 3dlu, p:grow", // columns //$NON-NLS-1$
            "fill:p:grow"); // rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.add(m_sideList,      cc.xy(1,1));
        builder.add(m_settingsPanel, cc.xy(3,1));
        
        return builder.getPanel();
    }
    
    private JPanel buildGeneralSettingsPanel()
    {
        FormLayout layout = new FormLayout(
            "p:grow", // columns //$NON-NLS-1$
            "p, 15dlu, p"); // rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.add(m_learnSettingsPanels.buildCardsPanel(),   cc.xy(1, 1));
        builder.add(m_learnSettingsPanels.buildLimiterPanel(), cc.xy(1, 3));
        
        return builder.getPanel();
    }
    
    private JPanel buildAdvancedPanel()
    {
        FormLayout layout = new FormLayout(
            "p:grow", // columns //$NON-NLS-1$
            "p, 9dlu, p"); // rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.add(m_learnSettingsPanels.buildCardOrderPanel(), cc.xy(1, 1));
        builder.add(m_learnSettingsPanels.buildSidesModePanel(), cc.xy(1, 3));
        
        return builder.getPanel();
    }
    
    private JPanel buildSchedulesPanel()
    {
        FormLayout layout = new FormLayout(
            "p:grow", // columns //$NON-NLS-1$
            "p, 9dlu, p"); // rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.add(m_learnSettingsPanels.buildSchedulePanel(),      cc.xy(1, 1));
        builder.add(m_learnSettingsPanels.buildFixedDueTimePanel(), cc.xy(1, 3));
        
        return builder.getPanel();
    }
    
    private JPanel buildButtonBar()
    {
        JButton okayButton = new JButton(Localization.get(LC.LEARN_SETTINGS_START));
        okayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                apply();
                dispose();
                
                Category category = m_learnSettingsPanels.getCategory();
                boolean learnUnlearned = m_learnSettingsPanels.isLearnUnlearnedCards();
                boolean learnExpired = m_learnSettingsPanels.isLearnExpiredCards();
                
                m_frame.startLearning(category, m_selectedCards, learnUnlearned, learnExpired);
            }
        });
        
        JButton cancelButton = new JButton(Localization.get(LC.CANCEL));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                dispose();
            }
        });
        
        m_applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                apply();
            }
        });
        
        JPanel buttonPanel = ButtonBarFactory.buildOKCancelApplyBar(
            okayButton, cancelButton, m_applyButton);
        buttonPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        
        getRootPane().setDefaultButton(okayButton);
        
        return buttonPanel;
    }
    
    /**
     * Applies the entered settings to the LearnSettings object.
     */
    private void apply()
    {
        m_learnSettingsPanels.applySettings();
        Settings.storeStrategy(m_settings);
    }
}
