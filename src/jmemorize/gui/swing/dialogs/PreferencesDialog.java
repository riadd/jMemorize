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
package jmemorize.gui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jmemorize.core.Settings;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.CardFont;
import jmemorize.gui.swing.CardFont.FontAlignment;
import jmemorize.gui.swing.CardFont.FontType;
import jmemorize.gui.swing.frames.MainFrame;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A modal dialog that is used to set different user preferences like font and
 * translation.
 * 
 * @author djemili
 */
public class PreferencesDialog extends JDialog
{
    private static final String[] FONT_SIZES  = {"8", "9", "10","11","12", "14", 
        "16", "18","20", "22", "24","26","28", "32", "36","72", "92", "128"};
    
    // font selection
    private JComboBox m_fontSideBox;
    
    private JCheckBox m_verticalAlignBox = new JCheckBox(
        Localization.get(LC.PREFERENCES_VERT_ALIGN));
    
    private JList     m_sizeList        = new JList(FONT_SIZES);
    private JList     m_fontList        = new JList();
    private JList     m_alignList       = new JList(); // TODO translate
    
    private JLabel    m_previewLabel    = new JLabel("jMemorize", JLabel.CENTER); //$NON-NLS-1$
    
    private List<CardFont> m_fonts          = new ArrayList<CardFont>();
    private boolean        m_changingFont   = false;

    // other preferences
    private JComboBox m_langComboBox    = new JComboBox();
    private JCheckBox m_zippedLessonBox = new JCheckBox(
        Localization.get(LC.PREFERENCES_USE_GZIP));
    
    private JButton   m_applyButton     = new JButton(Localization.get(LC.APPLY));
    
    // form parts
    private JPanel    m_settingsPanel;
    
    /**
     * Constructs the preferences dialog.
     */
    public PreferencesDialog(MainFrame frame)
    {
        super(frame, true);
        
        loadFonts();
        
        m_settingsPanel = buildSettingsPanel(); // needs to be built after combo box!
        
        // language combo box
        List<Locale> locales = Localization.getAvailableLocales();
        m_langComboBox.setModel(new DefaultComboBoxModel(formatLocaleStrings(locales)));
        m_langComboBox.setSelectedIndex(locales.indexOf(Settings.loadLocale()));
        
        // font combo box
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        m_fontList.setListData(fontNames);
        
        // alignment combo box
        m_alignList.setListData(new String[] {
            Localization.get(LC.ALIGN_LEFT), 
            Localization.get(LC.ALIGN_CENTER),
            Localization.get(LC.ALIGN_RIGHT)});
        
        // etc
        m_zippedLessonBox.setSelected(Settings.loadIsSaveCompressed());
        
        // prepare lists/combobox
        updateListFromFont();
        updateFontPreview();
        attachListeners();
        
        // add components
        getContentPane().add(buildMainPanel(), BorderLayout.CENTER);
        getContentPane().add(buildButtonBar(), BorderLayout.SOUTH);
        
        // on ESC key close dialog
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");    //$NON-NLS-1$
        getRootPane().getActionMap().put("Cancel", new AbstractAction(){ //$NON-NLS-1$
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });
        
        setTitle(Localization.get(LC.MAINFRAME_PREFERENCES));
        pack();
        
//        setSize(400, getHeight());
        setResizable(false);
        
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    private void loadFonts()
    {
        m_fonts.add(Settings.loadFont(FontType.CARD_FRONT));
        m_fonts.add(Settings.loadFont(FontType.CARD_FLIP));
        m_fonts.add(Settings.loadFont(FontType.LEARN_FRONT));
        m_fonts.add(Settings.loadFont(FontType.LEARN_FLIP));
        m_fonts.add(Settings.loadFont(FontType.TABLE_FRONT));
        m_fonts.add(Settings.loadFont(FontType.TABLE_FLIP));
        
        String frontSide = Localization.get(LC.FRONTSIDE);
        String flipSide = Localization.get(LC.FLIPSIDE);
        String table = Localization.get(LC.CHART_CARDS);
        String learn = Localization.get(LC.LEARN);
        
        m_fontSideBox = new JComboBox(new String[]{
            frontSide, 
            flipSide,
            String.format("%s (%s)", frontSide, learn),
            String.format("%s (%s)", flipSide, learn),
            String.format("%s (%s)", frontSide, table),
            String.format("%s (%s)", flipSide, table)
        });
    }

    /**
     * Attach listeners to font combobox and to font lists.
     */
    private void attachListeners()
    {
        // font side changed
        m_fontSideBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                m_changingFont = true;
                updateListFromFont();
                m_changingFont = false;
                updateFontPreview();
            }
        });
        
        // font in list selected
        ListSelectionListener fontUpdater = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!m_changingFont)
                    updateFontFromList();
                
                updateFontPreview();
            }
        };
        
        m_fontList.addListSelectionListener(fontUpdater);
        m_sizeList.addListSelectionListener(fontUpdater);
        m_alignList.addListSelectionListener(fontUpdater);
        
        m_verticalAlignBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                if (!m_changingFont)
                    updateFontFromList();
                
                updateFontPreview();
            }
        });
    }
    
    private JPanel buildMainPanel()
    {
        // build main panel
        FormLayout layout = new FormLayout(
            "p:grow",       // columns //$NON-NLS-1$
            "fill:p:grow"); // rows    //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.add(m_settingsPanel, cc.xy(1,1));
        
        return builder.getPanel();
    }
    
    private JPanel buildSettingsPanel()
    {
        // build panel
        FormLayout layout = new FormLayout(
            "p:grow",      // columns //$NON-NLS-1$
            "p, 3dlu, p"); // rows    //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setBorder(new EtchedBorder());
        
        builder.add(buildGeneralPanel(), cc.xy (1, 1));
        builder.add(buildFontPanel(),    cc.xy (1, 3));
        
        return builder.getPanel();
    }
    
    private JPanel buildGeneralPanel()
    {
        // build panel
        FormLayout layout = new FormLayout(
            "p, 9dlu, p:grow",      // columns //$NON-NLS-1$
            "p, 3dlu, p, 9dlu, p"); // rows    //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.addSeparator(Localization.get(LC.GENERAL),      cc.xyw(1, 1, 3));
        builder.addLabel(Localization.get(LC.PREFERENCES_LANG), cc.xy (1, 3));
        builder.add(m_langComboBox,                             cc.xy (3, 3));
        builder.add(m_zippedLessonBox,                          cc.xyw(1, 5, 3));
        
        return builder.getPanel();
    }
    
    /**
     * Build the panel that is responsible for customizing the font.
     */
    private JPanel buildFontPanel()
    {
        // prepare components
        m_fontList.setVisibleRowCount(5);
        JScrollPane fontScroll = new JScrollPane(m_fontList);
        
        m_sizeList.setVisibleRowCount(5);
        JScrollPane sizeScroll = new JScrollPane(m_sizeList);
        
        m_alignList.setVisibleRowCount(5);
        JScrollPane alignScroll = new JScrollPane(m_alignList);
        
        m_previewLabel.setPreferredSize(new Dimension(400, 50));
        
        JPanel previewPanel = new JPanel();
        previewPanel.add(m_previewLabel, BorderLayout.CENTER);
        previewPanel.setBorder(new TitledBorder(Localization.get(LC.PREFERENCES_PREVIEW)));
        
        // build panel
        FormLayout layout = new FormLayout(
            "p:grow, 9dlu, 50dlu, 9dlu, 50dlu", // columns              //$NON-NLS-1$
            "p, 9dlu, p, 12dlu, p, 3dlu, p, 3dlu, p, 22dlu, p, 9dlu, p"); // rows //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        
        builder.addSeparator(Localization.get(LC.PREFERENCES_FONT_SETTINGS), cc.xyw( 1, 1, 5));
        builder.add(m_fontSideBox,                                           cc.xyw( 1, 3, 5));
        
        builder.addLabel(Localization.get(LC.PREFERENCES_FONT),              cc.xy ( 1, 5));   
        builder.addLabel(Localization.get(LC.PREFERENCES_SIZE),              cc.xy ( 3, 5));   
        builder.addLabel(Localization.get(LC.PREFERENCES_ALIGN),             cc.xy ( 5, 5));
        
        builder.add(fontScroll,                                              cc.xy ( 1, 7));
        builder.add(sizeScroll,                                              cc.xy ( 3, 7));
        builder.add(alignScroll,                                             cc.xy ( 5, 7));
        
        builder.add(m_verticalAlignBox,                                      cc.xyw( 1, 9, 5));
        
        builder.add(previewPanel,                                            cc.xyw( 1,11, 5));
        
        return builder.getPanel();
    }
    
    private Locale getSelectedLocale()
    {
        return (Locale)Localization.getAvailableLocales().get(m_langComboBox.getSelectedIndex());
    }
    
    /**
     * Set the currently set font family and size in the font lists.
     */
    private void updateListFromFont()
    {
        CardFont cardFont = getSelectedCardFont();
        
        m_fontList.setSelectedValue(cardFont.getFont().getFamily(), true);
        m_sizeList.setSelectedValue(Integer.toString(cardFont.getFont().getSize()), true);
        m_alignList.setSelectedIndex(cardFont.getAlignment().ordinal());
        m_verticalAlignBox.setSelected(cardFont.isVerticallyCentered());
    }
    
    private void updateFontFromList()
    {
        Font font = new Font(
            (String)m_fontList.getSelectedValue(), 
            Font.PLAIN, 
            Integer.parseInt((String)m_sizeList.getSelectedValue()));
        
        FontAlignment hAlign = FontAlignment.values()[m_alignList.getSelectedIndex()];
        boolean vAlign = m_verticalAlignBox.isSelected();
        
        CardFont cardFont = getSelectedCardFont();
        cardFont.setFont(font);
        cardFont.setAlignment(hAlign);
        cardFont.setVerticallyCentered(vAlign);
    }
    
    /**
     * Updates the font preview.
     */
    private void updateFontPreview()
    {
        CardFont cardFont = getSelectedCardFont();
        
        int hAlign = SwingConstants.LEADING;
        switch (cardFont.getAlignment())
        {
        case CENTER: hAlign = SwingConstants.CENTER; break;
        case RIGHT: hAlign = SwingConstants.TRAILING; break;
        }
        
        m_previewLabel.setFont(cardFont.getFont());
        m_previewLabel.setHorizontalAlignment(hAlign);
    }
    
    private CardFont getSelectedCardFont()
    {
        return m_fonts.get(m_fontSideBox.getSelectedIndex());
    }
    
    private JPanel buildButtonBar()
    {
        JButton okayButton = new JButton(Localization.get(LC.OKAY));
        okayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                apply();
                dispose();
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
     * Apply all currently set preferences so that they take effect in jMemorize.
     */
    private void apply()
    {
        Locale locale = getSelectedLocale();
        if (!locale.equals(Settings.loadLocale()))
        {
            Settings.storeLocale(locale);
            JOptionPane.showMessageDialog(this, Localization.get(LC.PREFERENCES_RESTART));
        }
        
        // TODO store card type in CardFont instead
        Settings.storeFont(FontType.CARD_FRONT, m_fonts.get(0));
        Settings.storeFont(FontType.CARD_FLIP, m_fonts.get(1));
        Settings.storeFont(FontType.LEARN_FRONT, m_fonts.get(2));
        Settings.storeFont(FontType.LEARN_FLIP, m_fonts.get(3));
        Settings.storeFont(FontType.TABLE_FRONT, m_fonts.get(4));
        Settings.storeFont(FontType.TABLE_FLIP, m_fonts.get(5));
        
        Settings.storeSaveCompressed(m_zippedLessonBox.isSelected());
    }
    
    /**
     * Return the locale-formatted names of given locales. 
     */
    private Vector<String> formatLocaleStrings(List<Locale> locales)
    {
        Vector<String> localeStrings = new Vector<String>();
        for (Locale locale : locales)
        {
            StringBuffer sb = new StringBuffer(locale.getDisplayLanguage());
            if (locale.getCountry().length() > 0)
            {
                sb.append(" ("+ locale.getDisplayCountry() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            localeStrings.add(sb.toString());
        }
        
        return localeStrings;
    }
}
