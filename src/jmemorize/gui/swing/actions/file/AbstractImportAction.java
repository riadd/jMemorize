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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import jmemorize.core.Lesson;
import jmemorize.core.Main;
import jmemorize.core.Settings;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.actions.AbstractSessionDisabledAction;
import jmemorize.gui.swing.dialogs.ErrorDialog;

/**
 * An abstract action for importing data.
 * 
 * @author djemili
 */
public abstract class AbstractImportAction extends AbstractSessionDisabledAction
{
    /**
     * Displays a Open dialog.
     * 
     * @return the file path or <code>null</code> if the dialog was cancelled.
     */
    public static File showOpenDialog(JFrame frame, FileFilter fileFilter)
    {
        JFileChooser chooser = new JFileChooser();
        
        try 
        {
            chooser.setCurrentDirectory(Settings.loadLastDirectory());
        }
        catch (Exception ioe)
        {
            Main.logThrowable("Could not load last directory", ioe);
            chooser.setCurrentDirectory(null);
        }                
        
        if (fileFilter != null)
            chooser.setFileFilter(fileFilter);
        
        int choice = chooser.showOpenDialog(frame);
        if (choice != JFileChooser.APPROVE_OPTION)
            return null;

        File file = chooser.getSelectedFile();
        Settings.storeLastDirectory(file);
        return file;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(ActionEvent event)
    {
        Main main = Main.getInstance();
        
        File file = null;
        try
        {
            file = showOpenDialog(main.getFrame(), getFileFilter());
            if (file != null)
                doImport(file, main.getLesson());
        } 
        catch (Exception e)
        {
            Object[] args = {file != null ? file.getName() : "?"};
            MessageFormat form = new MessageFormat(Localization.get(LC.ERROR_LOAD));
            String msg = form.format(args);
            Main.logThrowable(msg, e);

            new ErrorDialog(main.getFrame(), msg, e).setVisible(true);
        }
    }
    
    /**
     * Imports given file contents into given lesson.
     */
    protected abstract void doImport(File file, Lesson lesson) throws IOException;
    
    protected abstract FileFilter getFileFilter();
}
