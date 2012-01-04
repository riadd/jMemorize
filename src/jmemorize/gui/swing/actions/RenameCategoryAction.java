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
package jmemorize.gui.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import jmemorize.core.Category;
import jmemorize.core.Lesson;
import jmemorize.core.Main;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSessionObserver;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.SelectionProvider.SelectionObserver;

/**
 * Renames a category.
 * 
 * @author djemili
 */
public class RenameCategoryAction extends AbstractAction2 
    implements SelectionObserver, LearnSessionObserver
{
    private SelectionProvider m_selectionProvider;

    public RenameCategoryAction(SelectionProvider selectionProvider)
    {
        Main.getInstance().addLearnSessionObserver(this);
        
        m_selectionProvider = selectionProvider;
        m_selectionProvider.addSelectionObserver(this);
        
        setValues();
        updateEnablement();
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(ActionEvent e)
    {
        String name = JOptionPane.showInputDialog(
            Main.getInstance().getFrame(),
            Localization.get(LC.RENAME_INPUT));

        if (name != null && name.trim().length() > 0)
        {
            m_selectionProvider.getCategory().setName(name.trim());
        }
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider.SelectionObserver
     */
    public void selectionChanged(SelectionProvider source)
    {
        updateEnablement();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.learn.LearnSessionObserver
     */
    public void sessionEnded(LearnSession session)
    {
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see jmemorize.core.learn.LearnSessionObserver
     */
    public void sessionStarted(LearnSession session)
    {
        updateEnablement();
    }

    protected void updateEnablement()
    {
        boolean sessionRunning = Main.getInstance().isSessionRunning();
        
        Lesson lesson = Main.getInstance().getLesson();
        Category rootCategory = lesson.getRootCategory();
        Category category = m_selectionProvider.getCategory();
        
        setEnabled(!sessionRunning && lesson != null && category != rootCategory);
    }

    private void setValues()
    {
        setName(Localization.get(LC.RENAME));
        setIcon("/resource/icons/blank.gif"); //$NON-NLS-1$
    }
}
