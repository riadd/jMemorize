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
import javax.swing.JOptionPane;

import jmemorize.core.Lesson;
import jmemorize.core.Main;
import jmemorize.core.Settings;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.actions.AbstractSessionDisabledAction;
import jmemorize.gui.swing.dialogs.ErrorDialog;
import jmemorize.util.ExtensionFileFilter;

public abstract class AbstractExportAction extends AbstractSessionDisabledAction
{
    /**
     * Displays a Save As or Export dialog, and to confirm overwrites,
     * and to attach specified file extension.
     * 
     * @return the file path or <code>null</code> if the dialog was cancelled.
     * 
     * @author Perry (elsapo)
     * @author djemili
     */
    public static File showSaveDialog(JFrame frame, ExtensionFileFilter fileFilter)
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
        
        chooser.setFileFilter(fileFilter);
        
        // Loop so we can prompt again if they choose not to overwrite
        while (true)
        {
            // Do the actual Save As prompt
            int choice = chooser.showSaveDialog(frame);
            if (choice != JFileChooser.APPROVE_OPTION)
                return null;

            File file = chooser.getSelectedFile();

            // Attach desired extension, if supplied
            String extension = fileFilter.getExtension();
            if (extension.length() > 0 && !file.getName().endsWith(extension))
            {
                file = new File(file.getAbsolutePath() + '.' + extension);
                chooser.setSelectedFile(file);
            }
            
            if (file.exists())
            {
                // Prompt to confirm they actually want to overwrite existing file
                String text = Localization.get("MainFrame.CONFIRM_OVERWRITE");
                String title = Localization.get("MainFrame.CONFIRM_OVERWRITE_TITLE");
                
                int act = JOptionPane.showConfirmDialog(frame, 
                    text + " " + file.toString(), 
                    title,
                    JOptionPane.YES_NO_OPTION);
                
                if (act == JOptionPane.NO_OPTION)
                    continue;
            }
            
            Settings.storeLastDirectory(file);
            return file;
        }
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
            file = showSaveDialog(main.getFrame(), getFileFilter());
            if (file != null)
                doExport(main.getLesson(), file);
            
        }
        catch (IOException e)
        {
            Object[] args = {file != null ? file.getName() : "?"};
            MessageFormat form = new MessageFormat(
                Localization.get(LC.ERROR_SAVE));
            String msg = form.format(args);
            Main.logThrowable(msg, e);

            new ErrorDialog(main.getFrame(), msg, e).setVisible(true);
        }
    }
    
    abstract protected void doExport(Lesson lesson, File file) throws IOException;
    abstract protected ExtensionFileFilter getFileFilter();
}
