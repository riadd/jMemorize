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
package jmemorize.gui.swing.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.Main;
import jmemorize.core.learn.LearnSettings;
import jmemorize.core.learn.LearnSettings.SchedulePreset;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.widgets.CategoryComboBox;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author djemili
 */
public class LearnSettingPanels
{
    // general panel
    private JSpinner         m_cardLimitSpinner          = new JSpinner(
        new SpinnerNumberModel(20, 1, 300, 1));
    private JSpinner         m_timeLimitSpinner          = new JSpinner(
        new SpinnerNumberModel(20, 1, 300, 1));
    private boolean          m_updatingSpinners          = false;    

    // card list panel
    private CategoryComboBox m_categoryComboBox          = new CategoryComboBox();
    private JRadioButton     m_allCardsButton            = new JRadioButton(
        Localization.get(LC.LEARN_SETTINGS_LEARN_UNLEARNED_EXPIRED), true);
    private JRadioButton     m_unlearnedCardsButton      = new JRadioButton(
        Localization.get(LC.LEARN_SETTINGS_LEARN_UNLEARNED));
    private JRadioButton     m_expiredCardsButton        = new JRadioButton(
        Localization.get(LC.LEARN_SETTINGS_LEARN_EXPIRED));
    private JRadioButton     m_selectedCardsButton       = new JRadioButton(
        Localization.get(LC.LEARN_SETTINGS_LEARN_SELECTED));
    
    // limiter panel
    private JCheckBox        m_timeLimitCheckBox         = new JCheckBox(
        Localization.get(LC.LEARN_SETTINGS_TIME_LIMIT_TEXT));
    private JCheckBox        m_cardLimitCheckBox         = new JCheckBox(
        Localization.get(LC.LEARN_SETTINGS_CARD_LIMIT_TEXT));
    private JCheckBox        m_dontRetestCheckBox        = new JCheckBox(
        Localization.get(LC.LEARN_SETTINGS_DONT_RETEST));
    
    // side mods panel
    private JRadioButton     m_sidesNormalButton         = new JRadioButton(
        Localization.get(LC.LEARN_SETTINGS_MODE_NORMAL));
    private JRadioButton     m_sidesFlippedButton        = new JRadioButton(
        Localization.get(LC.LEARN_SETTINGS_MODE_FLIP));
    private JRadioButton     m_sidesRandomButton         = new JRadioButton(
        Localization.get(LC.LEARN_SETTINGS_MODE_RANDOM));
    private JRadioButton     m_sidesBothButton           = new JRadioButton(
        Localization.get(LC.LEARN_SETTINGS_MODE_BOTH));
    
    /*
     * There must be at least one side that is checked before raising the level.
     * Without loss of generality, we can assume this will be the front, so
     * enforce at least one check for the front. If the user wants it the other
     * way, he/she should enter the card the opposite way around.
     */
    private JSpinner          m_frontChecksAmountSpinner = new JSpinner(
        new SpinnerNumberModel(1, 1, 100, 1));
    private JSpinner          m_backChecksAmountSpinner  = new JSpinner(
        new SpinnerNumberModel(1, 0, 100, 1));
    
    // card order panel
    private JCheckBox        m_categoryGroupsCheckBox    = new JCheckBox(
        Localization.get(LC.LEARN_SETTINGS_GROUP_CARDS));
    private JRadioButton     m_categoryOrderFixedButton  = new JRadioButton(
        Localization.get(LC.LEARN_SETTINGS_NATURAL_CATEGORY_ORDER));
    private JRadioButton     m_categoryOrderRandomButton = new JRadioButton(
        Localization.get(LC.LEARN_SETTINGS_RANDOM_CATEGORY_ORDER));
    private JSlider          m_shuffleRatioSlider        = new JSlider(0, 100, 30);
    
    // schedule panel
    private JSpinner[]       m_scheduleDays              = new JSpinner[LearnSettings.SCHEDULE_LEVELS];
    private JSpinner[]       m_scheduleHours             = new JSpinner[LearnSettings.SCHEDULE_LEVELS];
    private JSpinner[]       m_scheduleMinutes           = new JSpinner[LearnSettings.SCHEDULE_LEVELS];
    private JComboBox        m_schedulePresetsComboBox   = new JComboBox(LearnSettings.SCHEDULE_PRESETS);
    
    private JCheckBox        m_fixedExpirationTimeCheckBox = 
        new JCheckBox(Localization.get(LC.LEARN_SETTINGS_FIXED_EXPIRATION_TIME));
    
    private JSpinner         m_fixedExpirationTimeSpinner  = new JSpinner();
    
    // other
    private LearnSettings    m_settings;
    private boolean          m_hasSelectedCards;

    public LearnSettingPanels()
    {
        for (int level = 0; level<LearnSettings.SCHEDULE_LEVELS; level++)
        {
            m_scheduleDays[level]    = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
            m_scheduleHours[level]   = new JSpinner(new SpinnerNumberModel(1, 0, 23 , 1));
            m_scheduleMinutes[level] = new JSpinner(new SpinnerNumberModel(1, 0, 59 , 1));
        }
        
        m_categoryComboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                categoryBoxActionPerformed();
            }
        });
    }
    
    /**
     * @param strategy The strategy that is shown and editted by the panels.
     */
    public void setStrategy(LearnSettings strategy)
    {
        m_settings = strategy;
        
        resetSettings();
    }
    
    public void setProvider(SelectionProvider provider)
    {
        Category rootCategory = Main.getInstance().getLesson().getRootCategory();
        
        m_categoryComboBox.setRootCategory(rootCategory);
        m_categoryComboBox.setSelectedCategory(provider.getCategory());
        
        List<Card> selected = provider.getSelectedCards();
        m_hasSelectedCards = selected != null && !selected.isEmpty();
        
        updateCardButtons();
    }
    
    /**
     * @return the currently selected category.
     */
    public Category getCategory()
    {
        return m_categoryComboBox.getSelectedCategory();
    }
    
    public boolean isLearnUnlearnedCards()
    {
        return m_allCardsButton.isSelected() || m_unlearnedCardsButton.isSelected();
    }
    
    public boolean isLearnExpiredCards()
    {
        return m_allCardsButton.isSelected() || m_expiredCardsButton.isSelected();
    }
    
    /**
     * Sets all learn settings widgets according to the settings of the current
     * strategy.
     */
    public void resetSettings()
    {
        // get limiter settings
        m_cardLimitCheckBox.setSelected(m_settings.isCardLimitEnabled());
        m_timeLimitCheckBox.setSelected(m_settings.isTimeLimitEnabled());
        updateLimiterCheckboxes();
        
        m_cardLimitSpinner.setValue(new Integer(m_settings.getCardLimit()));
        m_timeLimitSpinner.setValue(new Integer(m_settings.getTimeLimit()));
        m_dontRetestCheckBox.setSelected(!m_settings.isRetestFailedCards());
        
        switch (m_settings.getSidesMode())
        {
            case LearnSettings.SIDES_NORMAL:
                m_sidesNormalButton.setSelected(true);
                break;
                
            case LearnSettings.SIDES_FLIPPED:
                m_sidesFlippedButton.setSelected(true);
                break;
                
            case LearnSettings.SIDES_BOTH:
                m_sidesBothButton.setSelected(true);
                break;
                
            default: // SIDES.RANDOM
                m_sidesRandomButton.setSelected(true);
                break;
        }
        
        // get side amounts
        m_frontChecksAmountSpinner.setValue(new Integer(m_settings.getAmountToTest(true)));
        m_backChecksAmountSpinner.setValue(new Integer(m_settings.getAmountToTest(false)));
        
        // get schedule
        SchedulePreset preset = m_settings.getSchedulePreset();
        m_schedulePresetsComboBox.setSelectedIndex(preset.ordinal());
        updateScheduleSpinners(m_settings.getSchedule());
        
        // get fixed due times
        m_fixedExpirationTimeCheckBox.setSelected(m_settings.isFixedExpirationTimeEnabled());
        SpinnerDateModel model = (SpinnerDateModel)m_fixedExpirationTimeSpinner.getModel();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, m_settings.getFixedExpirationHour());
        calendar.set(Calendar.MINUTE, m_settings.getFixedExpirationMinute());
        model.setValue(calendar.getTime());
        
        updateFixedDueTimeSpinner();
        
        // get category settings
        m_categoryGroupsCheckBox.setSelected(m_settings.isGroupByCategory());
        
        if (m_settings.getCategoryOrder() == LearnSettings.CATEGORY_ORDER_FIXED)
        {
            m_categoryOrderFixedButton.setSelected(true);
        }
        else
        {
            m_categoryOrderRandomButton.setSelected(true);
        }
        updateCategoryOrderButtons();
        
        m_shuffleRatioSlider.setValue((int)(100 * m_settings.getShuffleRatio()));
    }
    
    /**
     * Applies all settings currently entered into the learn settings panels to 
     * the current strategy.
     */
    public void applySettings()
    {
        // apply limiter settings
        m_settings.setCardLimitEnabled(m_cardLimitCheckBox.isSelected());
        m_settings.setCardLimit(intValue(m_cardLimitSpinner));
        
        m_settings.setTimeLimitEnabled(m_timeLimitCheckBox.isSelected());
        m_settings.setTimeLimit(intValue(m_timeLimitSpinner));
        
        m_settings.setRetestFailedCards(!m_dontRetestCheckBox.isSelected());
        
        m_settings.setSidesMode(m_sidesNormalButton.isSelected() ? LearnSettings.SIDES_NORMAL : 
            m_sidesFlippedButton.isSelected() ? LearnSettings.SIDES_FLIPPED : 
            m_sidesBothButton.isSelected() ? LearnSettings.SIDES_BOTH : LearnSettings.SIDES_RANDOM
        );
        
        m_settings.setAmountToTest(true, intValue(m_frontChecksAmountSpinner));
        m_settings.setAmountToTest(false, intValue(m_backChecksAmountSpinner));
        
        // apply schedule
        int idx = m_schedulePresetsComboBox.getSelectedIndex();
        SchedulePreset preset = SchedulePreset.values()[idx];
        
        if (preset != SchedulePreset.CUSTOM)
        {
            m_settings.setSchedulePreset(preset);
        }
        else
        {
            // schedule holds the time spans in minutes
            int[] schedule = new int[LearnSettings.SCHEDULE_LEVELS];
            for (int i = 0; i < LearnSettings.SCHEDULE_LEVELS; i++)
            {
                schedule[i] = (24 * 60 * intValue(m_scheduleDays[i]))
                    + (60 * intValue(m_scheduleHours[i])) + intValue(m_scheduleMinutes[i]);
            }
            
            m_settings.setCustomSchedule(schedule);
        }
        
        // apply fixed due time
        m_settings.setFixedExpirationTimeEnabled(m_fixedExpirationTimeCheckBox.isSelected());
        
        SpinnerDateModel model = (SpinnerDateModel)m_fixedExpirationTimeSpinner.getModel();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(model.getDate());
        
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        m_settings.setFixedExpirationTime(hour, minute);
        
        // apply category settings
        m_settings.setGroupByCategory(m_categoryGroupsCheckBox.isSelected());
        m_settings.setCategoryOrder(m_categoryOrderRandomButton.isSelected() ?
            LearnSettings.CATEGORY_ORDER_RANDOM : LearnSettings.CATEGORY_ORDER_FIXED);
        
        m_settings.setShuffleRatio(m_shuffleRatioSlider.getValue() / 100.0f);
    }
    
    public Category getSelectedCategory()
    {
        return m_categoryComboBox.getSelectedCategory();
    }
    
    public JPanel buildCardsPanel()
    {
        // prepare widgets
        ButtonGroup group = new ButtonGroup();
        group.add(m_allCardsButton);
        group.add(m_unlearnedCardsButton);
        group.add(m_expiredCardsButton);
        group.add(m_selectedCardsButton);
        
        // build panel
        FormLayout layout = new FormLayout(
            "300dlu",       // columns //$NON-NLS-1$
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");  // rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.addSeparator(Localization.get("LearnSettings.CARDS_TO_LEARN"), cc.xy( 1,  1)); //$NON-NLS-1$
        builder.add(m_categoryComboBox,             cc.xy( 1,  3));
        builder.add(m_allCardsButton,               cc.xy( 1,  5));
        builder.add(m_unlearnedCardsButton,         cc.xy( 1,  7));
        builder.add(m_expiredCardsButton,           cc.xy( 1,  9));
        builder.add(m_selectedCardsButton,          cc.xy( 1, 11));
        
        return builder.getPanel();
    }
    
    public JPanel buildCardOrderPanel()
    {
        Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
        for (int i=0; i<=10; i++)
            labels.put(i*10, new JLabel(Integer.toString(i*10) + '%'));
        m_shuffleRatioSlider.setLabelTable(labels);
        
        // also see http://java.sun.com/docs/books/tutorial/uiswing/components/slider.html
        m_shuffleRatioSlider.setPaintLabels(true);
        m_shuffleRatioSlider.setPaintTicks(true);
        m_shuffleRatioSlider.setMinorTickSpacing(5);
        m_shuffleRatioSlider.setMajorTickSpacing(10);
        
        m_categoryGroupsCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                updateCategoryOrderButtons();
            }
        });
        
        ButtonGroup categoriesGroup = new ButtonGroup();
        categoriesGroup.add(m_categoryOrderFixedButton);
        categoriesGroup.add(m_categoryOrderRandomButton);
        
        // build panel
        FormLayout layout = new FormLayout(
            "18dlu, p:grow",                          // columns       //$NON-NLS-1$
            "p, 3dlu, p, 3dlu, p, 16dlu, p, 3dlu, p, 3dlu, p"); // grouping rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        
        String shuffleText = Localization.get(
            LC.LEARN_SETTINGS_SHUFFLE_RATIO, LC.LEARN_SETTINGS_SHUFFLE);
        
        builder.addSeparator(Localization.get(LC.LEARN_SETTINGS_CARDS_ORDER), cc.xyw( 1, 1, 2));
        builder.addLabel(shuffleText,            cc.xyw( 1, 3, 2));
        builder.add(m_shuffleRatioSlider,        cc.xyw( 1, 5, 2));
        
        builder.add(m_categoryGroupsCheckBox,    cc.xyw( 1,  7, 2));
        builder.add(m_categoryOrderFixedButton,  cc.xy ( 2,  9  ));
        builder.add(m_categoryOrderRandomButton, cc.xy ( 2, 11  ));
        
        return builder.getPanel();
    }
    
    public JPanel buildSidesModePanel()
    {
        // radio button groups
        ButtonGroup sidesModeGroup = new ButtonGroup();
        sidesModeGroup.add(m_sidesNormalButton);
        sidesModeGroup.add(m_sidesFlippedButton);
        sidesModeGroup.add(m_sidesRandomButton);
        sidesModeGroup.add(m_sidesBothButton);
        m_sidesNormalButton.setSelected(true);
        
        // add listener
        m_sidesBothButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                updateCheckAmountFields();
            }
        });
        updateCheckAmountFields();
        
        // build panel
        FormLayout layout = new FormLayout(
            "18dlu, d, 9dlu, left:d:grow",                              // columns        //$NON-NLS-1$
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // side mode rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        
        builder.addSeparator(Localization.get("LearnSettings.SIDE_MODE"), cc.xyw( 1,  1, 4)); //$NON-NLS-1$
        builder.add(m_sidesNormalButton,             cc.xyw( 1,  3, 4));
        builder.add(m_sidesFlippedButton,            cc.xyw( 1,  5, 4));
        builder.add(m_sidesRandomButton,             cc.xyw( 1,  7, 4));
        
        builder.add(m_sidesBothButton,               cc.xyw( 1,  9, 4));
        
        builder.addLabel(Localization.get(LC.FRONTSIDE), cc.xy( 2, 11));
        builder.add(m_frontChecksAmountSpinner,          cc.xy(  4, 11));
        
        builder.addLabel(Localization.get(LC.FLIPSIDE),  cc.xy( 2, 13));
        builder.add(m_backChecksAmountSpinner,           cc.xy( 4, 13));
        
        return builder.getPanel();
    }
    
    public JPanel buildLimiterPanel()
    {
        // add action listeners
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                updateLimiterCheckboxes();
            }
        };
        
        m_cardLimitCheckBox.addActionListener(listener);
        m_timeLimitCheckBox.addActionListener(listener);
        updateLimiterCheckboxes();
        
        // build panel
        FormLayout layout = new FormLayout(
            "18dlu, d, 9dlu, left:d:grow",                      // columns             //$NON-NLS-1$
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");  // stop condition rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        
        builder.addSeparator(Localization.get(LC.LEARN_SETTINGS_DELIMITERS), cc.xyw( 1,  1, 4));
        builder.add(m_timeLimitCheckBox,                                   cc.xyw( 1,  3, 4));
        builder.addLabel(Localization.get(LC.LEARN_SETTINGS_TIME_LIMIT),   cc.xy ( 2,  5   ));
        builder.add(m_timeLimitSpinner,                                    cc.xy ( 4,  5   ));
        
        builder.add(m_cardLimitCheckBox,                                   cc.xyw( 1,  7, 4));
        builder.addLabel(Localization.get(LC.LEARN_SETTINGS_CARD_LIMIT),   cc.xy ( 2,  9   ));
        builder.add(m_cardLimitSpinner,                                    cc.xy ( 4,  9   ));
        
        builder.add(m_dontRetestCheckBox,                                  cc.xyw( 1, 11, 4));
        
        return builder.getPanel();
    }
    
    public JPanel buildSchedulePanel()
    {
        // prepare widgets
        m_schedulePresetsComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                onPresetScheduleSelected();
            }
        });
        
        // build panel
        FormLayout layout = new FormLayout(
            "p, 20dlu:grow, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, " +      // columns //$NON-NLS-1$
            "p,  3dlu,  p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p",                     //$NON-NLS-1$
            "p, 15dlu,  p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, " + // rows    //$NON-NLS-1$
            "p,  3dlu,  p, 3dlu, p, 3dlu, p, 3dlu");                                         //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.addLabel(Localization.get(LC.LEARN_SETTINGS_PRESETS), cc.xy(1, 1));
        builder.add(m_schedulePresetsComboBox, cc.xyw(3, 1, 11));
        
        for (int i = 0; i < LearnSettings.SCHEDULE_LEVELS; i++)
        {
            addScheduleRow(builder, cc, i);
        }
        
        return builder.getPanel();
    }
    
    public JPanel buildFixedDueTimePanel()
    {
        Date date = new Date();
        SpinnerDateModel model = new SpinnerDateModel(date, null, null, Calendar.HOUR);
        m_fixedExpirationTimeSpinner.setModel(model);
        
        String pattern = "h:mm a";
        if (Localization.SHORT_TIME_FORMATER instanceof SimpleDateFormat)
        {
            SimpleDateFormat formatter = (SimpleDateFormat)Localization.SHORT_TIME_FORMATER;
            pattern = formatter.toPattern();    
        }
        
        JSpinner.DateEditor de = new JSpinner.DateEditor(m_fixedExpirationTimeSpinner, pattern);
        m_fixedExpirationTimeSpinner.setEditor(de);
        
        m_fixedExpirationTimeCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                updateFixedDueTimeSpinner();
            }
        });
        
        // build panel
        FormLayout layout = new FormLayout(
            "18dlu, 70dlu, left:d:grow", // columns //$NON-NLS-1$
            "p, 3dlu, p"); // rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.add(m_fixedExpirationTimeCheckBox,  cc.xyw( 1,  1, 3));
        builder.add(m_fixedExpirationTimeSpinner,   cc.xyw( 2,  3, 1 ));
        
        return builder.getPanel();
    }
    
    private void updateFixedDueTimeSpinner()
    {
        m_fixedExpirationTimeSpinner.setEnabled(m_fixedExpirationTimeCheckBox.isSelected());
    }
    
    private void categoryBoxActionPerformed()
    {
        updateCardButtons();
    }
    
    private void addScheduleRow(PanelBuilder builder, CellConstraints cc, int level)
    {
        ChangeListener listener = new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                spinnerValueChanged();
            }
        };
        
        m_scheduleDays[level].addChangeListener(listener);
        m_scheduleHours[level].addChangeListener(listener);
        m_scheduleMinutes[level].addChangeListener(listener);
        
        String deckLabel = MessageFormat.format(Localization.get(LC.LEARN_SETTINGS_DELAY), level);
        
        builder.addLabel(deckLabel,                                 cc.xy ( 1, 3+2*level));
        builder.add(m_scheduleDays[level],                          cc.xy ( 3, 3+2*level));
        builder.addLabel(Localization.get(LC.LEARN_SETTINGS_DAYS),  cc.xy ( 5, 3+2*level));
        builder.add(m_scheduleHours[level],                         cc.xy ( 7, 3+2*level));
        builder.addLabel(Localization.get(LC.LEARN_SETTINGS_HOURS), cc.xy ( 9, 3+2*level));
        builder.add(m_scheduleMinutes[level],                       cc.xy (11, 3+2*level));
        builder.addLabel(Localization.get(LC.LEARN_SETTINGS_MINUTES), cc.xy (13, 3+2*level));
    }
    
    private void updateLimiterCheckboxes()
    {
        m_timeLimitSpinner.setEnabled(m_timeLimitCheckBox.isSelected());
        m_cardLimitSpinner.setEnabled(m_cardLimitCheckBox.isSelected());
    }
    
    private void updateCategoryOrderButtons()
    {
        m_categoryOrderRandomButton.setEnabled(m_categoryGroupsCheckBox.isSelected());
        m_categoryOrderFixedButton.setEnabled(m_categoryGroupsCheckBox.isSelected());
    }
    
    private void updateCardButtons()
    {
        boolean enableUnlearned = !getSelectedCategory().getUnlearnedCards().isEmpty();
        boolean enableExpired = !getSelectedCategory().getExpiredCards().isEmpty();
        boolean enableAll = enableUnlearned && enableExpired;
        boolean enableSelected =  m_hasSelectedCards;
        
        m_unlearnedCardsButton.setEnabled(enableUnlearned);
        m_expiredCardsButton.setEnabled(enableExpired);
        m_allCardsButton.setEnabled(enableAll);
        m_selectedCardsButton.setEnabled(enableSelected);
        
        boolean reselect = 
            (m_allCardsButton.isSelected() && !enableAll) ||
            (m_unlearnedCardsButton.isSelected() && !enableUnlearned) ||
            (m_expiredCardsButton.isSelected()   && !enableExpired) ||
            (m_selectedCardsButton.isSelected() && !enableSelected);
        
        if (reselect)
        {
            if (enableAll)
                m_allCardsButton.setSelected(true);
            
            else if (enableUnlearned)
                m_unlearnedCardsButton.setSelected(true);
            
            else if (enableExpired)
                m_expiredCardsButton.setSelected(true);
            
            else if (enableSelected)
                m_selectedCardsButton.setSelected(true);
            
            // TODO log else case
        }
    }
    
    private void updateCheckAmountFields()
    {
        m_frontChecksAmountSpinner.setEnabled(m_sidesBothButton.isSelected());
        m_backChecksAmountSpinner.setEnabled(m_sidesBothButton.isSelected());
    }
    
    private void onPresetScheduleSelected()
    {
        int idx = m_schedulePresetsComboBox.getSelectedIndex();
        SchedulePreset preset = SchedulePreset.values()[idx];
        
        if (preset != SchedulePreset.CUSTOM)
        {
            int[] schedule = LearnSettings.getPresetSchedule(preset);
            updateScheduleSpinners(schedule);
        }
    }
    
    private void updateScheduleSpinners(int[] schedule)
    {
        m_updatingSpinners = true;
        
        for (int i = 0; i < LearnSettings.SCHEDULE_LEVELS; i++)
        {
            m_scheduleDays[i].setValue(new Integer(schedule[i] / (60 * 24) ));
            m_scheduleHours[i].setValue(new Integer((schedule[i] % (60 * 24)) / 60 ));
            m_scheduleMinutes[i].setValue(new Integer(schedule[i] % 60));
        }
        
        m_updatingSpinners = false;
    }
    
    private void spinnerValueChanged()
    {
        // just to make sure that the value was changed by a user and not 
        // by choosing from the presets combobox.
        if (!m_updatingSpinners)
        {
            m_schedulePresetsComboBox.setSelectedIndex(
                LearnSettings.SCHEDULE_PRESETS.length - 1);
        }
    }
    
    private int intValue(JSpinner spinner)
    {
        return ((SpinnerNumberModel)spinner.getModel()).getNumber().intValue();
    }
}
