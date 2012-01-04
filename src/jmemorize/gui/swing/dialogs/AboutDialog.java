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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import jmemorize.core.Main;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;

import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * A about dialog that shows some basic info, the license, the Java properties
 * and the program preferences.
 * 
 * @author djemili
 */
public class AboutDialog extends JDialog
{
    private JEditorPane m_licenseTextPane;
    private JTabbedPane m_tabbedPane;

    public AboutDialog(JFrame owner)
    {
        super(owner, Localization.get(LC.MAINFRAME_ABOUT), true);
        initComponents();
        
        setLocationRelativeTo(owner);
        setVisible(true);
    }   
    
    private void initComponents()
    {
        m_tabbedPane = new JTabbedPane();
        m_tabbedPane.setBorder(new EtchedBorder());
        m_tabbedPane.addTab("Info", buildInfoPanel());
        m_tabbedPane.addTab("License", buildLicensePanel());
        m_tabbedPane.addTab("Java Properties", buildPropertiesPanel());
        m_tabbedPane.addTab("Program Preferences", buildPreferencesPanel());
        m_tabbedPane.setPreferredSize(new java.awt.Dimension(500, 300));
        
        m_tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                if (m_tabbedPane.getSelectedIndex() == 1) // HACK
                    loadLicense(); 
            }
        });
        
        getContentPane().add(m_tabbedPane, BorderLayout.CENTER);
        getContentPane().add(buildButtonBar(), BorderLayout.SOUTH);
        
        pack();
    }
    
    private JPanel buildInfoPanel()
    {
        String text = 
            "<html><p><b>Version: " + //$NON-NLS-1$
            Main.PROPERTIES.getProperty("project.version") + //$NON-NLS-1$ 
            "</b></p>"+ //$NON-NLS-1$
            "<p>Build: " + 
            Main.PROPERTIES.getProperty("buildId") + //$NON-NLS-1$
            "</p>" + //$NON-NLS-1$
            "<p><br>Homepage: http://jmemorize.org</p>" + //$NON-NLS-1$
            "<p>2004-2008 Riad Djemili and contributors</p></html>"; //$NON-NLS-1$
        
        JLabel titleLabel = new JLabel();
        ImageIcon icon = new ImageIcon(getClass().getResource("/resource/about.png"));
        titleLabel.setIcon(icon);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(new EtchedBorder());
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(titleLabel.getFont().deriveFont(14.0f));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        textPanel.add(textLabel, BorderLayout.NORTH);
            
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel buildLicensePanel()
    {
        m_licenseTextPane = new JEditorPane();
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(m_licenseTextPane), BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadLicense()
    {
        if (m_licenseTextPane.getText().length() > 0)
            return;
        
        try
        {
            m_licenseTextPane.setPage(getClass().getResource("/LICENSE"));
        } 
        catch (IOException e)
        {
            Main.logThrowable("Failed to Load LICENSE", e);
            
            m_licenseTextPane.setText(
                "Failed to load LICENSE text. See the LICENSE file that " +
                "was delivered with this program.");
        }
    }
    
    private JPanel buildButtonBar()
    {
        JButton okayButton = new JButton(Localization.get(LC.OKAY));
        okayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });
        
        JPanel buttonPanel = ButtonBarFactory.buildOKBar(okayButton);
        buttonPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        
        getRootPane().setDefaultButton(okayButton);
        
        return buttonPanel;
    }

    private JPanel buildPreferencesPanel()
    {
        JTable table = new JTable();
        table.setModel(createPreferencesTableModel());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        return panel;
    }
    
    private TableModel createPreferencesTableModel()
    {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Preference", "Value"}, 0);
        Preferences prefs = Main.USER_PREFS;
        
        addPreferenceNode(prefs, model);
        return model;
    }
    
    private void addPreferenceNode(Preferences node, DefaultTableModel model)
    {
        try
        {
            String[] keys = node.keys();
            for (int i = 0; i < keys.length; i++)
            {
                String val = node.get(keys[i], "");
                model.addRow(new Object[]{node.name() + '.' + keys[i], val});
            }
            
            String[] childs = node.childrenNames();
            for (int i = 0; i < childs.length; i++)
            {
                addPreferenceNode(node.node(childs[i]), model);
            }
        } 
        catch (BackingStoreException e)
        {
            Main.logThrowable("failed to create preference node", e);
        }
    }

    private JPanel buildPropertiesPanel()
    {
        JTable table = new JTable();
        table.setModel(createPropertiesTableModel());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        return panel;
    }

    private TableModel createPropertiesTableModel()
    {
        Properties properties = System.getProperties();
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Property", "Value"}, 0);
        
        for (Iterator<?> it = properties.keySet().iterator(); it.hasNext();)
        {
            String key = (String)it.next();
            Object val = properties.get(key);
            
            model.addRow(new Object[]{key, val});
        }
        
        return model;
    }
}
