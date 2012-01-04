/*
 * jMemorize - Learning made easy (and fun) - A Leitner flashcards tool
 * Copyright(C) 2004-2006 Riad Djemili
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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import jmemorize.gui.LC;
import jmemorize.gui.Localization;

import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * A simple dialog that displays a main panel with a button bar below which
 * contains a single okay-button.
 * 
 * @author djemili
 */
public class OkayButtonDialog extends JDialog
{
    public OkayButtonDialog(Frame father, String title, boolean modal, 
        JComponent component)
    {
        super(father, title, modal);
        
        getContentPane().add(component, BorderLayout.CENTER);
        getContentPane().add(buildButtonBar(), BorderLayout.SOUTH);
        pack();
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
}
