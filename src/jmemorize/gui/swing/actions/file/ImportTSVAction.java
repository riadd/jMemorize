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

import javax.swing.filechooser.FileFilter;

import jmemorize.core.Lesson;
import jmemorize.core.Main;
import jmemorize.core.io.CsvBuilder;
import jmemorize.core.io.CsvBuilder.BadHeaderException;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.frames.MainFrame;
import jmemorize.util.ExtensionFileFilter;

/**
 * An action for importing Tab-Separated-Values (TSV).
 * 
 * @author djemili
 */
public class ImportTSVAction extends AbstractImportAction
{
    public ImportTSVAction()
    {
        setValues();
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
            MainFrame frame = Main.getInstance().getFrame();
            Charset charset = ImportCSVAction.showCharsetChooser(frame);
            
            if (charset == null)
                return;
            
            CsvBuilder.importLesson(in, '\t', charset, lesson);
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
        return new ExtensionFileFilter("tsv", Localization.get("File.TSV"));
    }

    private void setValues()
    {
        setName(Localization.get("File.TSV")); //$NON-NLS-1$
        setIcon("/resource/icons/file_saveas.gif"); //$NON-NLS-1$
        setMnemonic(1);
    }
}
