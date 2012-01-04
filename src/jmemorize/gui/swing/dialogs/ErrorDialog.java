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
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import jmemorize.core.Main;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A dialog that can be displayed when an exception occurs.
 * 
 * @author djemili
 */
public class ErrorDialog extends JDialog
{
    private JButton     m_okayButton = new JButton();
    private JButton     m_moreButton = new JButton();
    private JButton     m_copyButton = new JButton();

    private Exception   m_exception;
    private JTextArea   m_stacktraceArea;
    private JScrollPane m_scrollPane;
    private boolean     m_extended   = false;

    private String      m_message;
    private String      m_debugText;
    private JPanel      m_placeholderPanel;
    
    /**
     * Creates a new modal error dialog that shows given the message associated
     * with given exception and also the stack trace if extended.
     */
    public ErrorDialog(Frame owner, Exception cause)
    {
        this(owner, cause.getMessage(), cause);
    }
    
    /**
     * Creates a new modal error dialog that shows given message and if extended
     * also the stack trace of given exception.
     * 
     * @param message the message that should appear instead of the message
     * given by the exception.
     */
    public ErrorDialog(Frame owner, String message, Exception cause)
    {
        super(owner, Localization.get("MainFrame.ERROR_TITLE"), true);
        
        m_message = message;
        m_exception = cause;
        
        Main.logThrowable(message, cause);
        Main.clearLastThrowable();
        initComponents();
        pack();
        
        setLocationRelativeTo(owner);
    }
    
    private static String getDebugText(Exception e)
    {
        String java    = System.getProperty("java.version"); //$NON-NLS-1$
        String os      = System.getProperty("os.name"); //$NON-NLS-1$
        
        String version = Main.PROPERTIES.getProperty("project.version"); //$NON-NLS-1$
//        String revision = Main.PROPERTIES.getProperty("project.revision"); //$NON-NLS-1$
        String revision = ""; // HACK
        String buildId = Main.PROPERTIES.getProperty("buildId");//$NON-NLS-1$ 
        
        String txt = String.format("Ver %s %s (%s) - Java %s , OS %s%n", 
            version, revision, buildId, java, os);
        
        txt += getStackTrace(e);
        
        return txt;
    }
    
    private static String getStackTrace(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    
    private void initComponents()
    {
        getContentPane().add(buildMainPanel(), BorderLayout.CENTER);
        getContentPane().add(buildButtonBar(), BorderLayout.SOUTH);        
    }
    
    private JPanel buildMainPanel()
    {
        // build button bar
        FormLayout layout = new FormLayout(
            "3dlu, p:grow, 3dlu", // columns //$NON-NLS-1$
            "p, 3dlu, fill:p:grow, 3dlu");   // rows    //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setBorder(new EmptyBorder(4, 5, 2, 5));
        
        JLabel textLabel = builder.addLabel("<html>"+m_message+"</html>", cc.xy(2,1));
        textLabel.setIcon(new ImageIcon(getClass().getResource(
            "/resource/icons/warn.gif"))); //$NON-NLS-1$
        textLabel.setBorder(new EmptyBorder(20, 10, 20, 10));
        textLabel.setPreferredSize(new Dimension(500, 90));
        textLabel.setFont(textLabel.getFont().deriveFont(16.0f));

        m_debugText = getDebugText(m_exception);
        
        m_stacktraceArea = new JTextArea(m_debugText);
        m_stacktraceArea.setEditable(false);
        m_scrollPane = new JScrollPane(m_stacktraceArea);
        m_scrollPane.setPreferredSize(new Dimension(500, 300));
        
        m_placeholderPanel = new JPanel(new BorderLayout());
        
        builder.add(m_placeholderPanel, cc.xy(2,3));
        builder.setBorder(new EtchedBorder());
        
        return builder.getPanel();
    }
    
    private JPanel buildButtonBar()
    {
        // buttons
        m_okayButton = new JButton(Localization.get(LC.OKAY));
        m_okayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                dispose();
            }
        });
        
        m_moreButton = new JButton("Show debug information");
        m_moreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                extendDialog();
            }
        });
        
        m_copyButton = new JButton("Copy to clipboard");
        m_copyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) 
            {
                copyDebugTextToClipboard();
            }
        });

        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addFixed(m_moreButton);
        builder.addRelatedGap();
        builder.addGridded(m_copyButton);
        builder.addRelatedGap();
        builder.addGlue();
        builder.addUnrelatedGap();
        builder.addGridded(m_okayButton);
        builder.setBorder(new EmptyBorder(3, 3, 3, 3));
        
        return builder.getPanel();
    }

    private void extendDialog()
    {
        if (!m_extended)
            m_placeholderPanel.add(m_scrollPane, BorderLayout.CENTER);
        else
            m_placeholderPanel.remove(m_scrollPane);
        
        m_extended = !m_extended;
        m_moreButton.setSelected(m_extended);
        pack();
    }

    private void copyDebugTextToClipboard()
    {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        
        StringSelection ss = new StringSelection(m_debugText);
        clipboard.setContents(ss, new ClipboardOwner() {
            public void lostOwnership(Clipboard clipboard, Transferable contents)
            {
                // ignore
            }
        });
    }
}
