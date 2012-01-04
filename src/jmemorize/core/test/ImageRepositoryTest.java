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
package jmemorize.core.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jmemorize.core.ImageRepository;
import jmemorize.core.Main;
import jmemorize.core.io.XmlBuilder;
import junit.framework.TestCase;

public class ImageRepositoryTest extends TestCase
{
    private final static File TEST_DIR = new File("./images-test");
    
    private ImageRepository m_ir = ImageRepository.getInstance();
    
    @Override
    protected void setUp() throws Exception
    {
        TEST_DIR.mkdir();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        m_ir.clear();
        delDir(TEST_DIR);
    }
    
    public void testAddSingleImageReturnsSameId() throws IOException
    {
        String id1 = addImage("test.gif");
        assertNotNull(id1);
        
        String id2 = addImage("test.gif");
        assertNotNull(id2);
        
//        assertEquals(id1, id2); TODO!
    }
    
    public void testIdOfSingleImage() throws IOException
    {
        String id = addImage("test.gif");
        assertEquals("test.gif", id);
    }
    
    public void testIdOfTwoImagesWithSameName() throws IOException
    {
        addImage("test.gif");
        String id2 = addImage("more/test.gif");
        
        assertEquals("test_0.gif", id2);
    }
    
    public void testIdOfThreeImagesWithSameName() throws IOException
    {
        addImage("test.gif");
        addImage("more/test.gif");
        String id3 = addImage("more2/test.gif");
        
        assertEquals("test_1.gif", id3);
    }
    
    public void testThreeImageWithSameFilenameHaveDifferentIds() throws IOException
    {
        String id1 = addImage("test.gif");
        String id2 = addImage("more/test.gif");
        String id3 = addImage("test.png");
        
        assertFalse(id1.equals(id2));
        assertFalse(id2.equals(id3));
        assertFalse(id1.equals(id3));
    }
    
    public void testEmptyRepositoryAfterClear()
    {
        m_ir.clear();
        assertEquals(0, m_ir.getImageItems().size());
    }
    
    public void testRepositoryClearedWhenCreatingNewProject() throws IOException
    {
        addImage("test.gif");
        addImage("more/test.gif");
        assertEquals(2, m_ir.getImageItems().size());
        
        Main.getInstance().createNewLesson();
        assertEquals(0, m_ir.getImageItems().size());
    }
    
    public void testRepositoryClearedWhenLoadingProject() throws IOException
    {
        assertEquals(0, m_ir.getImageItems().size());
        
        Main.getInstance().loadLesson(new File("test/fixtures/simple_de.jml"));
        assertEquals(0, m_ir.getImageItems().size());
        
        addImage("test.gif");
        addImage("more/test.gif");
        assertEquals(2, m_ir.getImageItems().size());
        
        Main.getInstance().loadLesson(new File("test/fixtures/simple_de.jml"));
        assertEquals(0, m_ir.getImageItems().size());
    }
    
    public void testRetainImages() throws IOException
    {
        addImage("test.gif");
        addImage("more/test.gif");
        assertEquals(2, m_ir.getImageItems().size());
        
        Set<String> ids = new HashSet<String>();
        ids.add("test.gif");
        m_ir.retain(ids);
        
        assertEquals(1, m_ir.getImageItems().size());
        assertNotNull(m_ir.getImage("test.gif"));
        assertNull(m_ir.getImage("more/test.gif"));
    }
    
    public void testSaveImagesToDisk() throws IOException
    {
        addImage("test.gif");
        addImage("more/test.gif");
        
        File imgDir = XmlBuilder.writeImageRepositoryToDisk(TEST_DIR);
        
        File[] files = imgDir.listFiles();
        assertEquals(2, files.length);
    }
    
    public void testRemoveFilesAlsoWhenSavingToDisk() throws IOException
    {
        addImage("test.gif");
        addImage("test.png");
        File imgDir = XmlBuilder.writeImageRepositoryToDisk(TEST_DIR);
        
        m_ir.clear();
        addImage("test.png");
        imgDir = XmlBuilder.writeImageRepositoryToDisk(TEST_DIR);
        
        File[] files = imgDir.listFiles();
        assertEquals(1, files.length);
        assertTrue(files[0].toString().endsWith(".png"));
    }
    
    private void delDir(File dir)
    {
        String[] entries = dir.list();
        
        if (entries != null)
        {
            for (int i = 0; i < entries.length; i++)
            {
                delDir(new File(entries[i]));
            }
        }
        
        dir.delete();
    }
    
    private String addImage(String filename) throws IOException
    {
        File file = new File("test/fixtures/test-images/"+filename);
        return m_ir.addImage(new FileInputStream(file), file.getName());
    }
}
