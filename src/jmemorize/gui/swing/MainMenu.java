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
package jmemorize.gui.swing;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import jmemorize.core.Main;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.actions.AboutAction;
import jmemorize.gui.swing.actions.LearnAction;
import jmemorize.gui.swing.actions.OpenURLAction;
import jmemorize.gui.swing.actions.RenameCategoryAction;
import jmemorize.gui.swing.actions.ShowHistoryAction;
import jmemorize.gui.swing.actions.edit.AddCardAction;
import jmemorize.gui.swing.actions.edit.AddCategoryAction;
import jmemorize.gui.swing.actions.edit.CopyAction;
import jmemorize.gui.swing.actions.edit.CutAction;
import jmemorize.gui.swing.actions.edit.EditCardAction;
import jmemorize.gui.swing.actions.edit.FindAction;
import jmemorize.gui.swing.actions.edit.PasteAction;
import jmemorize.gui.swing.actions.edit.RemoveAction;
import jmemorize.gui.swing.actions.edit.ResetCardAction;
import jmemorize.gui.swing.actions.file.ExitAction;
import jmemorize.gui.swing.actions.file.ExportToCSVAction;
import jmemorize.gui.swing.actions.file.ExportToCleanLessonAction;
import jmemorize.gui.swing.actions.file.ExportToPDFAction;
import jmemorize.gui.swing.actions.file.ExportToRTFAction;
import jmemorize.gui.swing.actions.file.ImportCSVAction;
import jmemorize.gui.swing.actions.file.ImportJMLAction;
import jmemorize.gui.swing.actions.file.ImportTSVAction;
import jmemorize.gui.swing.actions.file.NewLessonAction;
import jmemorize.gui.swing.actions.file.OpenLessonAction;
import jmemorize.gui.swing.actions.file.OpenRecentLessonAction;
import jmemorize.gui.swing.actions.file.PreferencesAction;
import jmemorize.gui.swing.actions.file.SaveLessonAction;
import jmemorize.gui.swing.actions.file.SaveLessonAsAction;
import jmemorize.util.RecentItems;
import jmemorize.util.RecentItems.RecentItemsObserver;

/**
 * The main menu of jMemorize.
 * 
 * @author djemili
 */
public class MainMenu extends JMenuBar implements RecentItemsObserver
{
    private JMenu                       m_fileMenu;
    
    public MainMenu(SelectionProvider selectionProvider, RecentItems recentFiles)
    {
        buildMenu(selectionProvider);
        recentFiles.addObserver(this);
    }
    
    /* (non-Javadoc)
     * @see jmemorize.util.RecentItems.RecentItemsObserver
     */
    public void onRecentItemChange(RecentItems src)
    {
        buildFileMenu();
    }
    
    private void buildMenu(SelectionProvider provider)
    {
        m_fileMenu = new JMenu(Localization.get("MainFrame.MENU_FILE")); //$NON-NLS-1$
        add(m_fileMenu);
        
        buildFileMenu();
        add(buildEditMenu(provider));
        add(buildLearnMenu(provider));
        add(buildHelpMenu());
    }

    private JMenu buildLearnMenu(SelectionProvider provider)
    {
        JMenu learnMenu = new JMenu(Localization.get("MainFrame.MENU_LESSON")); //$NON-NLS-1$
        learnMenu.add(new JMenuItem(new AddCardAction(provider)));
        learnMenu.add(new JMenuItem(new AddCategoryAction(provider)));
        learnMenu.addSeparator();
        learnMenu.add(new JMenuItem(new LearnAction(provider)));
        learnMenu.add(new JMenuItem(new ShowHistoryAction()));
        return learnMenu;
    }

    private JMenu buildEditMenu(SelectionProvider provider)
    {
        JMenu editMenu = new JMenu(Localization.get("MainFrame.MENU_EDIT")); //$NON-NLS-1$
        editMenu.add(new JMenuItem(new EditCardAction(provider)));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(new CutAction(provider)));
        editMenu.add(new JMenuItem(new CopyAction(provider)));
        editMenu.add(new JMenuItem(new PasteAction(provider)));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(new RemoveAction(provider)));
        
        if (Main.isDevel())
            editMenu.add(new JMenuItem(new RenameCategoryAction(provider)));
        
        editMenu.add(new JMenuItem(new ResetCardAction(provider)));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(new FindAction()));
        return editMenu;
    }

    private JMenu buildHelpMenu()
    {
        JMenu menu = new JMenu(Localization.get("MainFrame.MENU_HELP")); //$NON-NLS-1$
        menu.add(new JMenuItem(new AboutAction()));
        menu.addSeparator();
        
        menu.add(new JMenuItem(new OpenURLAction(
            Localization.get("URL.HOMEPAGE"), //$NON-NLS-1$
            "http://jmemorize.org" //$NON-NLS-1$
        )));
        
        menu.add(new JMenuItem(new OpenURLAction(
            Localization.get("URL.MANUAL"), //$NON-NLS-1$
            "http://wiki.jmemorize.org/User_Manual" //$NON-NLS-1$
        )));
        
        menu.add(new JMenuItem(new OpenURLAction(
            Localization.get("URL.FEATURE_TRACKER"), //$NON-NLS-1$
            "http://sourceforge.net/tracker/?group_id=121967&atid=691941" //$NON-NLS-1$
        )));
        
        menu.add(new JMenuItem(new OpenURLAction(
            Localization.get("URL.BUG_TRACKER"), //$NON-NLS-1$
            "http://sourceforge.net/tracker/?group_id=121967&atid=691938" //$NON-NLS-1$
        )));
        
        return menu;
    }

    private void buildFileMenu()
    {
        m_fileMenu.removeAll();
        m_fileMenu.add(new NewLessonAction());
        m_fileMenu.add(new OpenLessonAction());
        m_fileMenu.add(new SaveLessonAction());
        m_fileMenu.add(new SaveLessonAsAction());
        
        // sub menu for import menu items
        JMenu importMenu = new JMenu(Localization.get("MainFrame.IMPORT")); //$NON-NLS-1$
        importMenu.setIcon(new ImageIcon(
            getClass().getResource("/resource/icons/blank.gif"))); //$NON-NLS-1$
        importMenu.add(new ImportCSVAction());
        importMenu.add(new ImportTSVAction());
        importMenu.add(new ImportJMLAction());
        
        
        // Sub menu for export menu items 
        JMenu exportMenu = new JMenu(Localization.get("MainFrame.EXPORT")); //$NON-NLS-1$
        exportMenu.setIcon(new ImageIcon(
            getClass().getResource("/resource/icons/blank.gif"))); //$NON-NLS-1$
        exportMenu.add(new ExportToPDFAction());
        exportMenu.add(new ExportToRTFAction());
        exportMenu.add(new ExportToCSVAction());
        exportMenu.add(new ExportToCleanLessonAction());
        
        m_fileMenu.addSeparator();
        m_fileMenu.add(importMenu);
        m_fileMenu.add(exportMenu);
        
        // add recent files menu items
        int recentFiles = Main.getInstance().getRecentLessonFiles().size();

        if (recentFiles > 0)
        {
            m_fileMenu.addSeparator();
        }

        for (int i = 0; i < recentFiles; i++)
        {
            JMenuItem menuItem = new JMenuItem(new OpenRecentLessonAction(i));
            m_fileMenu.add(menuItem);
        }

        m_fileMenu.addSeparator();
        m_fileMenu.add(new JMenuItem(new PreferencesAction()));
        m_fileMenu.addSeparator();
        m_fileMenu.add(new JMenuItem(new ExitAction()));
    }
}
