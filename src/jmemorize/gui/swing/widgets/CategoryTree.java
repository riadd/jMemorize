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
package jmemorize.gui.swing.widgets;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;
import jmemorize.core.Main;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.actions.LearnAction;
import jmemorize.gui.swing.actions.edit.AddCardAction;
import jmemorize.gui.swing.actions.edit.AddCategoryAction;
import jmemorize.gui.swing.actions.edit.CopyAction;
import jmemorize.gui.swing.actions.edit.CutAction;
import jmemorize.gui.swing.actions.edit.PasteAction;
import jmemorize.gui.swing.actions.edit.RemoveAction;
import jmemorize.gui.swing.frames.MainFrame;


/**
 * A category tree that shows a visual representation of the categories. It also
 * has support for drag'n'drop actions and renaming categories.
 * 
 * @author djemili
 */
public class CategoryTree extends JTree implements CategoryObserver, SelectionProvider
{
    private class CellRenderer extends DefaultTreeCellRenderer
    {
        public CellRenderer()
        {
            setLeafIcon(FOLDER_ICON);
            setOpenIcon(FOLDER_ICON);
            setClosedIcon(FOLDER_ICON);
        }
        
        /**
         * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value, 
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
        {
            JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, 
                sel, expanded, leaf, row, hasFocus);
            Object nodeValue = ((DefaultMutableTreeNode)value).getUserObject();
            
            if (nodeValue instanceof Category)
            {
                Category category = (Category)nodeValue;
                label.setText(category.getName());
            }
            
            return label;
        }
    }
    
    private class CategoryTreeModel extends DefaultTreeModel
    {
        /**
         * @param root
         */
        public CategoryTreeModel(TreeNode root)
        {
            super(root);
        }
        
        /**
         * Instead of overwriting userObject like the super method does, the
         * input is set as the new name of the category.
         * 
         * @see javax.swing.tree.DefaultTreeModel#valueForPathChanged
         */
        public void valueForPathChanged(TreePath path, Object newValue)
        {
            DefaultMutableTreeNode aNode = (DefaultMutableTreeNode)path.getLastPathComponent();
            Category category = (Category)aNode.getUserObject();
            category.setName((String)newValue);
            
            nodeChanged(aNode);
            updateSelectionObservers();
        }
        
    }
    
    private class CellEditor extends DefaultTreeCellEditor
    {
        private Category m_editedCategory;
        private DefaultMutableTreeNode  m_editedNode;     //HACK
        
        public CellEditor(JTree tree, DefaultTreeCellRenderer renderer)
        {
            super(tree, renderer);
        }
        
        public DefaultMutableTreeNode getEditedNode()
        {
            return m_editedNode; 
        }
        
        /**
         * @return Returns the nodeCategory.
         */
        public Category getNodeCategory()
        {
            return m_editedCategory;
        }
        
        /**
         * @see javax.swing.tree.DefaultTreeCellEditor#isCellEditable
         */
        public boolean isCellEditable(EventObject event)
        {
            // event is null if edit is started by click-pause-click
            if (event != null)
            {
                MouseEvent mEvent = (MouseEvent)event;
                TreePath path     = getPathForLocation(mEvent.getX(), mEvent.getY());
                
                if (path != null)
                {
                    m_editedNode      = (DefaultMutableTreeNode)path.getLastPathComponent();
                    m_editedCategory  = (Category)m_editedNode.getUserObject();
                }
            }
            
            // make root not editable
            return super.isCellEditable(event) && m_editedCategory != m_rootCategory;
        }
        
        /**
         * @see javax.swing.tree.DefaultTreeCellEditor#getTreeCellEditorComponent
         */
        public Component getTreeCellEditorComponent(JTree tree, Object value, 
            boolean isSelected, boolean expanded, boolean leaf, int row)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Category category = (Category)node.getUserObject();
            return super.getTreeCellEditorComponent(tree, category.getName(), 
                isSelected, expanded, leaf, row);
        }
    }
    
    /**
     * Is responsible for setting the previous category after a action has happened.
     */
    private class ActionWrapper implements Action
    {
        private Action m_wrapped;
        
        public ActionWrapper(Action original)
        {
            m_wrapped = original;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener)
        {
            m_wrapped.addPropertyChangeListener(listener);
        }

        public Object getValue(String key)
        {
            return m_wrapped.getValue(key);
        }

        public boolean isEnabled()
        {
            return m_wrapped.isEnabled();
        }

        public void putValue(String key, Object value)
        {
            m_wrapped.putValue(key, value);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener)
        {
            m_wrapped.removePropertyChangeListener(listener);
        }

        public void setEnabled(boolean b)
        {
            m_wrapped.setEnabled(b);
        }

        public void actionPerformed(ActionEvent e)
        {
            m_wrapped.actionPerformed(e);
            setSelectedCategory(m_beforeMenuCategory);
        }
    }
    
    private final ImageIcon FOLDER_ICON = new ImageIcon(
        getClass().getResource("/resource/icons/folder.gif")); //$NON-NLS-1$
    
    
    private Category                m_rootCategory;
    
    /** The category that was shown before the popup menu was opened. */
    private Category                m_beforeMenuCategory;
    
    private List<SelectionObserver> m_selectionObservers = new ArrayList<SelectionObserver>();
    private JPopupMenu              m_categoryMenu;
    private boolean                 m_reopeningCategoryMenu = false;
    
    public CategoryTree()
    {
        CellRenderer renderer = new CellRenderer();
        setCellRenderer(renderer);
        
        m_categoryMenu = buildCategoryContextMenu();
        hookCategoryContextMenu();
        
        setCellEditor(new CellEditor(this, renderer));
        setTransferHandler(MainFrame.TRANSFER_HANDLER);
        
//        addTreeSelectionListener(new TreeSelectionListener()
//        {
//            public void valueChanged(TreeSelectionEvent evt)
//            {
//                updateSelectionObservers();
//            }
//        });
        
        setDragEnabled(true);
        setEditable(true);
    }
    
    public void setRootCategory(Category category)
    {
        if (m_rootCategory != null)
        {
            m_rootCategory.removeObserver(this);
        }
        
        m_rootCategory = category;
        m_rootCategory.addObserver(this);
        
        MutableTreeNode root = createCategoryNode(category);
        setModel(new CategoryTreeModel(root));
        this.repaint();
        
        setSelectedCategory(m_rootCategory);
    }
    
    public void setSelectedCategory(Category category)
    {
        if (category == null || m_rootCategory == null) //HACK
        {
            return;
        }
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
        Enumeration enumer = root.depthFirstEnumeration();
        
        while (enumer.hasMoreElements())
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)enumer.nextElement();
            Object userValue = node.getUserObject();
            
            if (userValue == category)
            {
                setSelectionPath(new TreePath(node.getPath()));
            }
        }
        
        updateSelectionObservers();
    }
    
    public Category getSelectedCategory() // TODO replace by getCategory
    {
        return getCategory();        
    }
    
    
    /**
     * @return <code>true</code> if the selected category is pending. This is 
     * the case if the category was selected by right-clicking on the node.
     */
    public boolean isPendingSelection()
    {
        return m_beforeMenuCategory != null;
    }
    
    /*
     * @see jmemorize.core.CategoryObserver#onCategoryEvent
     */
    public void onCategoryEvent(int type, Category category)
    {
        MutableTreeNode parent = null;
        
        switch (type)
        {
            case ADDED_EVENT: 
                parent = getNode(category.getParent());
                MutableTreeNode newChild = createCategoryNode(category);
                
                int idx = category.getParent().getChildCategories().indexOf(category);
                parent.insert(newChild, idx);                
                break;
                
            case REMOVED_EVENT:
                parent = getNode(category.getParent());
                MutableTreeNode child  = getNode(category);
                parent.remove(child);
                break;
                
            case EDITED_EVENT:
                parent = getNode(category);
                break;
        }
        
        DefaultTreeModel model = (DefaultTreeModel)getModel();
        model.reload(parent);
    }

    /*
     * @see jmemorize.core.CategoryObserver#onCardEvent
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        // ignore
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public void addSelectionObserver(SelectionObserver observer)
    {
        m_selectionObservers.add(observer);
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public void removeSelectionObserver(SelectionObserver observer)
    {
        m_selectionObservers.remove(observer);
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public Category getCategory()
    {
        TreePath path = getSelectionPath();
        
        if (path != null)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            return (Category)node.getUserObject();
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public JComponent getDefaultFocusOwner()
    {
        return this;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public JFrame getFrame()
    {
        return Main.getInstance().getFrame();
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Card> getRelatedCards()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Card> getSelectedCards()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Category> getSelectedCategories()
    {
        List<Category> categories = new ArrayList<Category>();
        categories.add(getCategory());
        return categories;        
    }
    
    private void updateSelectionObservers()
    {
        for (SelectionObserver observer : m_selectionObservers)
        {
            observer.selectionChanged(this);
        }
    }
    
    private void hookCategoryContextMenu()
    {
        addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e)
            {
                updateSelectionObservers();
            }
        });
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    int row = getRowForLocation(e.getX(), e.getY());
                    if (row > -1)
                    {
                        m_beforeMenuCategory = null;
                    }
                    
                    m_reopeningCategoryMenu = false;
                }
                
                if (SwingUtilities.isRightMouseButton(e))
                {
                    int row = getRowForLocation(e.getX(), e.getY());
                    if (row > -1)
                    {
                        m_reopeningCategoryMenu = true;
                    }
                }
            }
            
            public  void mouseClicked(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    int row = getRowForLocation(e.getX(), e.getY());
                    if (row > -1)
                    {
                        if (!selectionModel.isRowSelected(row))
                        {
                            if (m_beforeMenuCategory == null)
                                m_beforeMenuCategory = getCategory();
                            
                            setSelectionRow(row);
                        }
                        
                        m_categoryMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }
    
    private JPopupMenu buildCategoryContextMenu()
    {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new ActionWrapper(new LearnAction(this)));
        menu.add(new ActionWrapper(new AddCardAction(this)));
        menu.add(new ActionWrapper(new AddCategoryAction(this)));
        menu.add(new ActionWrapper(new RemoveAction(this)));
        menu.addSeparator();
        menu.add(new ActionWrapper(new CopyAction(this)));
        menu.add(new ActionWrapper(new CutAction(this)));
        menu.add(new ActionWrapper(new PasteAction(this)));
        
        menu.addPopupMenuListener(new PopupMenuListener(){
            public void popupMenuCanceled(PopupMenuEvent e)
            {
//                if (!m_reopeningCategoryMenu)
                {
                    setSelectedCategory(m_beforeMenuCategory);
                }
                
                m_reopeningCategoryMenu = false;
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
            {               
                // ignore
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
                // ignore
            }
        });
        
        return menu;
    }
    
    private DefaultMutableTreeNode getNode(Object userValue)
    {
        DefaultTreeModel model = (DefaultTreeModel)getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        
        for (Enumeration enumer = root.depthFirstEnumeration(); enumer.hasMoreElements();)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)enumer.nextElement();
            
            if (node.getUserObject() == userValue)
            {
                return node;
            }
        }
        
        return null;
    }
    
    private MutableTreeNode createCategoryNode(Category category)
    {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(category);
        
        // for all child categories
        for (Category cat : category.getChildCategories())
        {
            node.add(createCategoryNode(cat));
        }
        
        return node;
    }
}
