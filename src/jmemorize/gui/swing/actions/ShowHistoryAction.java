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
package jmemorize.gui.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import jmemorize.core.Main;
import jmemorize.core.learn.LearnHistory;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.dialogs.OkayButtonDialog;
import jmemorize.gui.swing.frames.MainFrame;
import jmemorize.gui.swing.panels.HistoryChartPanel;

/**
 * @author djemili
 */
public class ShowHistoryAction extends AbstractAction2
{
    public ShowHistoryAction()
    {
        setValues();
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(ActionEvent e)
    {
        MainFrame parent = Main.getInstance().getFrame();
        
        LearnHistory history = Main.getInstance().getLesson().getLearnHistory();
        HistoryChartPanel chartPanel = new HistoryChartPanel(history);
        
        String title = Localization.get(LC.HISTORY_TITLE);
        JDialog dialog = new OkayButtonDialog(parent, title, true, chartPanel);
        dialog.setSize((int)(parent.getWidth() * 0.70), 500);
        dialog.setLocationRelativeTo(parent);
        
        dialog.setVisible(true);
    }
    
    private void setValues()
    {
        setName(Localization.get(LC.HISTORY_ACTION));
        setIcon("/resource/icons/chart_curve.png");
        setMnemonic(1);
    }
}
