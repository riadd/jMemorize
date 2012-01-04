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
package jmemorize.gui.swing.actions.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import jmemorize.core.Lesson;
import jmemorize.core.Main;
import jmemorize.core.io.CsvBuilder;
import jmemorize.core.io.CsvBuilder.BadHeaderException;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.util.ExtensionFileFilter;

/**
 * An action for importing Comma-Separated-Values (CSV).
 * 
 * @author djemili
 */
public class ImportCSVAction extends AbstractImportAction
{
    public ImportCSVAction()
    {
        setValues();
    }
    
    public static Charset showCharsetChooser(JFrame owner)
    {
        Object[] charsets = Charset.availableCharsets().keySet().toArray();
        
        String selection = (String)JOptionPane.showInputDialog(
            owner, 
            Localization.get("MainFrame.SELECT_CHARACTER_SET"), 
            Localization.get("MainFrame.CHARACTER_SET"), 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            charsets, 
            "ISO-8859-1");
        
        /*
         * someone reported a bug (with stack tracke) where altough selection
         * was tested for null, before calling the method, a
         * illegal-null-argument exception was thrown - don't ask how this can
         * be possible
         */
        return selection == null ? null : Charset.forName(selection);
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.actions.file.AbstractImportAction
     */
    protected void doImport(File file, Lesson lesson) throws IOException
    {
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(file);
            Charset charset = showCharsetChooser(Main.getInstance().getFrame());
            
            if (charset == null)
                return;
             
            CsvBuilder.importLesson(in, ',', charset, lesson);
        } 
        catch (BadHeaderException e)
        {
            if (in != null)
                in.close();
            
            throw new IOException(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.actions.file.AbstractImportAction
     */
    protected FileFilter getFileFilter()
    {
        return new ExtensionFileFilter("csv", Localization.get(LC.FILE_CSV));
    }

    private void setValues()
    {
        setName(Localization.get(LC.FILE_CSV));
        setMnemonic(1);
        setIcon("/resource/icons/file_saveas.gif"); //$NON-NLS-1$
    }
}
