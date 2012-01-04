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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import jmemorize.core.Lesson;
import jmemorize.core.io.CsvBuilder;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.util.ExtensionFileFilter;

/**
 * Exports to comma-separated-values.
 * 
 * @author djemili
 */
public class ExportToCSVAction extends AbstractExportAction
{
    public ExportToCSVAction()
    {
        setValues();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.actions.file.AbstractExportAction
     */
    protected void doExport(Lesson lesson, File file) throws IOException
    {
        FileOutputStream out = new FileOutputStream(file);
        CsvBuilder.exportLesson(out, lesson, ',', Charset.forName("UTF-8"));
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.actions.file.AbstractExportAction
     */
    protected ExtensionFileFilter getFileFilter()
    {
        return new ExtensionFileFilter("csv", Localization.get(LC.FILE_CSV));
    }
    
    private void setValues()
    {
        setName(Localization.get(LC.FILE_CSV));
        setIcon("/resource/icons/file_saveas.gif"); //$NON-NLS-1$
    }
}
