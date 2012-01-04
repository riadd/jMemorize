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
package jmemorize.gui.swing.widgets;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;

/**
 * A combobox that shows categories. The categories are indented in a way that
 * shows their hierarchy.
 * 
 * @author djemili
 */
public class CategoryComboBox extends JComboBox implements CategoryObserver
{
    private class CatergoryRenderer extends BasicComboBoxRenderer
    {
        /* (non-Javadoc)
         * @see javax.swing.plaf.basic.BasicComboBoxRenderer
         */
        public Component getListCellRendererComponent(JList list, Object value, 
            int index, boolean isSelected, boolean cellHasFocus)
        {
            Category cat = (Category)value;
            JLabel label = (JLabel)super.getListCellRendererComponent(list, 
                cat.getName(), index, isSelected, cellHasFocus);
            label.setIcon(FOLDER_ICON);
            
            // show items in combo list indented.
            int leftSpace = index >= 0 ? 20 * cat.getDepth() : 0;
            label.setBorder(new EmptyBorder(2, leftSpace, 2, 2));
            
            if (index < 0)
                label.setText(cat.getPath());

            return label;
        }
    }
    
    private final ImageIcon     FOLDER_ICON = new ImageIcon(getClass().
        getResource("/resource/icons/folder.gif")); //$NON-NLS-1$
    
    private Category            m_rootCategory;
    
    public CategoryComboBox()
    {
        setRenderer(new CatergoryRenderer());
        setMaximumRowCount(12);
    }
    
    public void setRootCategory(Category category)
    {
        if (m_rootCategory != null)
        {
            m_rootCategory.removeObserver(this);
        }
        m_rootCategory = category;
        m_rootCategory.addObserver(this);
        
        updateModel();
    }
    
    public Category getRootCategory()
    {
        return m_rootCategory;
    }
    
    public void setSelectedCategory(Category category)
    {
        setSelectedItem(category);
    }
    
    public Category getSelectedCategory()
    {
        return (Category)getModel().getSelectedItem();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        updateModel();
    }

    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        // ignore
    }
    
    private void updateModel()
    {
        Object selected = getModel().getSelectedItem();
        List<Category> categoryList = m_rootCategory.getSubtreeList();
        DefaultComboBoxModel model = new DefaultComboBoxModel(categoryList.toArray());
        
        // if former selected object still there, select it again
        if (categoryList.contains(selected))
        {
            model.setSelectedItem(selected);
        }

        setModel(model);
    }
}
