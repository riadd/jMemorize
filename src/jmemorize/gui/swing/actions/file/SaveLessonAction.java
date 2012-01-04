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

import java.awt.event.KeyEvent;
import java.io.File;

import jmemorize.core.Lesson;
import jmemorize.core.LessonObserver;
import jmemorize.core.Main;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.actions.AbstractSessionDisabledAction;

/**
 * An action that saves the currently opened lesson.
 * 
 * @author djemili
 */
public class SaveLessonAction extends AbstractSessionDisabledAction 
    implements LessonObserver
{
    public SaveLessonAction()
    {
        setValues();
        
        Main.getInstance().addLessonObserver(this);
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        Main main = Main.getInstance();
        
        File file = main.getLesson().getFile();
        main.getFrame().saveLesson(main.getLesson(), file);
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonLoaded(Lesson newLesson)
    {
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonModified(Lesson lesson)
    {
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonSaved(Lesson lesson)
    {
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonObserver
     */
    public void lessonClosed(Lesson lesson)
    {
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.actions.AbstractSessionDisabledAction
     */
    protected void updateEnablement()
    {
        Main main = Main.getInstance();
        Lesson lesson = main.getLesson();
        
        setEnabled(!main.isSessionRunning() && lesson.canSave());
    }

    private void setValues()
    {
        setName(Localization.get("MainFrame.SAVE")); //$NON-NLS-1$
        setDescription(Localization.get("MainFrame.SAVE_DESC")); //$NON-NLS-1$
        setIcon("/resource/icons/file_save.gif"); //$NON-NLS-1$
        setAccelerator(KeyEvent.VK_S, SHORTCUT_KEY);
        setMnemonic(1);
    }
}
