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
package jmemorize.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A sime file filter for file choosers.
 * 
 * @author djemili
 */
public class ExtensionFileFilter extends FileFilter
{
    private String m_extension;
    private String m_description;

    public ExtensionFileFilter(String extension, String description)
    {
        m_extension = extension;
        m_description = description;
    }

    /*
     * @see javax.swing.filechooser.FileFilter
     */
    public boolean accept(File f)
    {
        return f.isDirectory() || f.getName().endsWith(m_extension);
    }

    /*
     * @see javax.swing.filechooser.FileFilter
     */
    public String getDescription()
    {
        return m_description;
    }

    public String getExtension()
    {
        return m_extension;
    }
}
