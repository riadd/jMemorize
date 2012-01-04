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
package jmemorize.gui.swing.actions.file;

import java.io.File;

import jmemorize.core.Main;
import jmemorize.gui.swing.actions.AbstractSessionDisabledAction;
import jmemorize.gui.swing.frames.MainFrame;

/**
 * An action that opens a recently used lesson. Before dismissing the current
 * lesson, the user is asked for confirmation.
 * 
 * @see MainFrame#confirmCloseLesson()
 * 
 * @author djemili
 * 
 */
public class OpenRecentLessonAction extends AbstractSessionDisabledAction
{
    private int m_id;

    public OpenRecentLessonAction(int id)
    {
        m_id = id;
        File file = new File(Main.getInstance().getRecentLessonFiles().get(id));

        setName((id + 1) + ". " + file.getName()); //$NON-NLS-1$
        setDescription(file.toString()); //$NON-NLS-1$
        setIcon("/resource/icons/blank.gif"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        Main main = Main.getInstance();
        
        File file = new File(main.getRecentLessonFiles().get(m_id));
        main.getFrame().loadLesson(file);
    }
}
