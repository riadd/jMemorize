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
package jmemorize.gui.swing.frames;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;
import jmemorize.core.Lesson;
import jmemorize.core.Main;
import jmemorize.core.Settings;
import jmemorize.core.Main.ProgramEndObserver;
import jmemorize.core.learn.LearnHistory;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSessionObserver;
import jmemorize.core.learn.LearnHistory.SessionSummary;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.GeneralTransferHandler;
import jmemorize.gui.swing.MainMenu;
import jmemorize.gui.swing.NewCardFramesManager;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.SelectionProvider.SelectionObserver;
import jmemorize.gui.swing.actions.LearnAction;
import jmemorize.gui.swing.actions.ShowCategoryTreeAction;
import jmemorize.gui.swing.actions.SplitMainFrameAction;
import jmemorize.gui.swing.actions.edit.AddCardAction;
import jmemorize.gui.swing.actions.edit.AddCategoryAction;
import jmemorize.gui.swing.actions.edit.EditCardAction;
import jmemorize.gui.swing.actions.edit.FindAction;
import jmemorize.gui.swing.actions.edit.RemoveAction;
import jmemorize.gui.swing.actions.edit.ResetCardAction;
import jmemorize.gui.swing.actions.file.AbstractExportAction;
import jmemorize.gui.swing.actions.file.ExitAction;
import jmemorize.gui.swing.actions.file.NewLessonAction;
import jmemorize.gui.swing.actions.file.OpenLessonAction;
import jmemorize.gui.swing.actions.file.SaveLessonAction;
import jmemorize.gui.swing.dialogs.ErrorDialog;
import jmemorize.gui.swing.dialogs.OkayButtonDialog;
import jmemorize.gui.swing.panels.DeckChartPanel;
import jmemorize.gui.swing.panels.DeckTablePanel;
import jmemorize.gui.swing.panels.LearnPanel;
import jmemorize.gui.swing.panels.SessionChartPanel;
import jmemorize.gui.swing.panels.StatusBar;
import jmemorize.gui.swing.widgets.CategoryComboBox;
import jmemorize.gui.swing.widgets.CategoryTree;
import jmemorize.util.ExtensionFileFilter;

/**
 * The main window of jMemorize. It has a stats panel in the upper part and a
 * card table/learn panel in the bottom. Optionaly there is also a category tree
 * at the left side.
 * 
 * @author djemili
 */
public class MainFrame extends JFrame implements CategoryObserver, 
    SelectionProvider, SelectionObserver, LearnSessionObserver, ProgramEndObserver
{
    static public final TransferHandler     TRANSFER_HANDLER = new GeneralTransferHandler();
    public static final ExtensionFileFilter FILE_FILTER      = new ExtensionFileFilter(
        "jml", Localization.get(LC.FILE_FILTER_DESC));
    
    private static final String             FRAME_ID             = "main";
    private static final String             REPEAT_CARD          = "repeatCard";
    private static final String             DECK_CARD            = "deckCard";

    // jmemorize swing elements
    private CategoryComboBox                m_categoryBox;
    private CategoryTree                    m_categoryTree;
    private DeckTablePanel                  m_deckTablePanel;
    private DeckChartPanel                  m_deckChartPanel;
    private LearnPanel                      m_learnPanel;
    private StatusBar                       m_statusBar          = new StatusBar();
    private NewCardFramesManager            m_newCardManager     = new NewCardFramesManager();

    // native swing elements
    private JPanel                          m_bottomPanel;
    private JButton                         m_showTreeButton;
    private JSplitPane                      m_horizontalSplitPane;
    private JSplitPane                      m_verticalSplitPane;
    private JScrollPane                     m_treeScrollPane;

    private Main                            m_main;
    private Category                        m_category;
    private int                             m_deck;
    private List<SelectionObserver>         m_selectionObservers = new LinkedList<SelectionObserver>();

    // category tree
    private boolean                         m_showCategoryTree;
    private boolean                         m_showCategoryTreeOld;
    
    private int                             m_categoryTreeWidth  = Settings.loadCategoryTreeWidth();
    
    // either cards or categories can be focused, not both at the same time
    // UGLYHACK remove
    private List<Category>                  m_focusedCategories;

    // set look and feel before we load any frames
    static
    {
        try
        {
//            UIManager.setLookAndFeel(new MetalLookAndFeel());
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            Main.logThrowable("could not set look and feel", e);
        }
    }

    /**
     * Creates a new MainFrame.
     */
    public MainFrame()
    {
        m_main = Main.getInstance();
        
        initComponents();
        loadSettings();
        
        m_deckTablePanel.getCardTable().setStatusBar(m_statusBar);
        m_learnPanel.setStatusBar(m_statusBar);
        
        setLesson(m_main.getLesson()); // GUI is first loaded with empty lesson
        gotoBrowseMode();
        
        m_main.addLearnSessionObserver(this);
        m_main.addProgramEndObserver(this);
    }

    /*
     * Simply listen for card selection events in learn and decktable panel
     * and forward them.
     */
    public void selectionChanged(SelectionProvider selectionProvider)
    {
        m_focusedCategories = null;
        
        updateSelectionObservers();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Card> getRelatedCards()
    {
        return m_focusedCategories == null ? 
            getCurrentSelectionProvider().getRelatedCards() : null;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Card> getSelectedCards()
    {
        return m_focusedCategories == null ? 
            getCurrentSelectionProvider().getSelectedCards() : null;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Category> getSelectedCategories()
    {
        return m_focusedCategories; // can be null
    }

    /**
     * @return Returns the currently displayed category.
     */
    public Category getCategory() 
    {
        return m_category;
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
    
    public void setLesson(Lesson lesson)
    {
        Category rootCategory = lesson.getRootCategory();

        m_categoryBox.setRootCategory(rootCategory);
        m_categoryTree.setRootCategory(rootCategory);
        setCategory(rootCategory);

        EditCardFrame.getInstance().setVisible(false);
        FindFrame.getInstance().setVisible(false);

        updateFrameTitle();
    }
    
    public void setCategory(Category category)
    {
        if (category == null) // HACK
            return;

        if (m_category != null)
        {
            m_category.removeObserver(this);
        }
        m_category = category;
        m_category.addObserver(this);

        m_deckChartPanel.setCategory(category);
        m_deckTablePanel.setCategory(category); // TODO refactor. give only list of cards
        
        m_categoryBox.setSelectedCategory(category);
        m_categoryTree.setSelectedCategory(category);

        // in learn mode the focused item should always our currently learned card
        if (!Main.getInstance().isSessionRunning()) // HACK
        {
            m_focusedCategories = new ArrayList<Category>(1); // HACK
            m_focusedCategories.add(category);
        }
        
        updateSelectionObservers();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public JComponent getDefaultFocusOwner()
    {
        return m_categoryTree.isFocusOwner() ?
            (JComponent)m_categoryTree : (JComponent)m_deckTablePanel.getCardTable();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public JFrame getFrame() 
    {
        return this;
    }

    /**
     * Set the currently displayed deck.
     * 
     * @param level the level of the deck that is to be shown. The deck with
     * unlearned cards has level 0.
     */
    public void setDeck(int level)
    {
        m_deck = level;
        
        m_deckTablePanel.setDeck(level);
        m_deckChartPanel.setDeck(level);
    }
    
    /**
     * @return the level of the currently shown deck. The deck with unlearned
     * cards has level 0.
     */
    public int getDeck()
    {
        return m_deck;
    }

    /**
     * @param show <code>true</code> if the category tree is supposed to be
     * shown. <code>false</code> otherwise.
     */
    public void showCategoryTree(boolean show)
    {
        if (!show)
        {
            if (m_showCategoryTree)
            {
                m_categoryTreeWidth = m_horizontalSplitPane.getDividerLocation();
            }
            
            m_horizontalSplitPane.setDividerSize(0);
            m_showTreeButton.setSelected(false);
            m_treeScrollPane.setVisible(false);
        }
        else
        {
            if (!m_showCategoryTree)
            {
                m_horizontalSplitPane.setDividerLocation(m_categoryTreeWidth);
            }
            
            m_showTreeButton.setSelected(true);
            m_treeScrollPane.setVisible(true);
            m_horizontalSplitPane.setDividerSize(5);
        }
             
        m_showCategoryTree = show;
    }
    
    /**
     * @return <code>true</code> if the category tree is currently visible.
     */
    public boolean isShowCategoryTree()
    {
        return m_showCategoryTree;
    }
    
    public void startLearning(Category category, List<Card> selectedCards, 
        boolean learnUnlearned, boolean learnExpired)
    {
        m_showCategoryTreeOld = m_showCategoryTree;
        showCategoryTree(false);
        
        m_main.startLearnSession(m_main.getLearnSettings(), selectedCards, 
            category, learnUnlearned, learnExpired);
    }
    
    public NewCardFramesManager getNewCardManager() // TODO pull up to a new common singleton
    {
        return m_newCardManager;
    }

    public LearnPanel getLearnPanel()
    {
        return m_learnPanel;
    }
    
    public JSplitPane getVerticalSplitPane()
    {
        return m_verticalSplitPane;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        if (type == REMOVED_EVENT)
        {
            setCategory(m_main.getLesson().getRootCategory()); // HACK
        }
    }

    /* (non-Javadoc)
     * @see jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        // ignore
    }

    /**
     * Loads the lesson and sets it as currently loaded lesson. If there is
     * already a opened lesson, the user might be prompted to save that lesson
     * before opening the new lesson.
     * 
     * @param file The path to the lesson. If <code>null</code> a file chooser
     * is shown that allows the user to select the file.
     */
    public void loadLesson(File file)
    {
        try
        {
            if (!confirmCloseLesson())
                return;
            
            if (file == null)
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
                
                chooser.setFileFilter(MainFrame.FILE_FILTER);
    
                int returnVal = chooser.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    file = chooser.getSelectedFile();
                }
                else
                {
                    return;
                }
            }
        
            m_main.loadLesson(file);
            Settings.storeLastDirectory(file);
            
            LearnHistory history = m_main.getLesson().getLearnHistory();
            if (!history.isLoaded())
                importGlobalLearnHistory(history);
            
        }
        catch (Exception e)
        {
            Object[] args = {file != null ? file.getName() : "?"};
            MessageFormat form = new MessageFormat(Localization.get(LC.ERROR_LOAD));
            String msg = form.format(args);
            Main.logThrowable(msg, e);
            
            new ErrorDialog(this, msg, e).setVisible(true);
        }
    }

    /**
     * Saves the lesson or displays an error message if the operation failed.
     * 
     * @param file The path to the lesson. If <code>null</code> a file chooser
     * is shown that allows the user to select the file.
     */
    public void saveLesson(Lesson lesson, File file)
    {
        try
        {
            if (file == null)
            {
                file = AbstractExportAction.showSaveDialog(
                    this, MainFrame.FILE_FILTER);
                
                if (file == null)
                    return;
            }
            
            m_main.saveLesson(lesson, file);
            updateFrameTitle();
        }
        catch (Exception e)
        {
            Object[] args = {file != null ? file.getName() : "?"};
            MessageFormat form = new MessageFormat(Localization.get(LC.ERROR_SAVE));
            String msg = form.format(args);
            Main.logThrowable(msg, e);
           
            new ErrorDialog(this, msg, e).setVisible(true);
        }
    }

    /**
     * If lesson was modified this shows a dialog that asks if the user wants to
     * save the lesson before closing it.
     * 
     * @return <code>true</code> if user chose not to cancel the lesson close
     * operation. If this method return <code>false</code> the closing of
     * jMemorize was canceled.
     */
    public boolean confirmCloseLesson()
    {
        // first check the editCardFrame for unsaved changes
        EditCardFrame editFrame = EditCardFrame.getInstance();
        if (editFrame.isVisible() && !editFrame.close())
        {
            return false; // user canceled closing of edit card frame
        }

        if (!m_newCardManager.closeAllFrames()) // close all addCard frames
        {
            return false;
        }

        // then see if lesson should to be saved
        Lesson lesson = m_main.getLesson();
        if (lesson.canSave())
        {
            int n = JOptionPane.showConfirmDialog(MainFrame.this,
                Localization.get("MainFrame.SAVE_MODIFIED"), "Warning", //$NON-NLS-1$ //$NON-NLS-2$
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

            if (n == JOptionPane.OK_OPTION)
            {
                saveLesson(lesson, lesson.getFile());
                
                // if lesson was saved return true, false otherwise
                return !lesson.canSave();
            }

            // if NO chosen continue, otherwise CANCEL was chosen
            return n == JOptionPane.NO_OPTION;
        }

        return true;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.Main.ProgramEndObserver
     */
    public void onProgramEnd()
    {
        int hSize = m_showTreeButton.isSelected() ? 
            m_horizontalSplitPane.getDividerLocation() : 
            m_categoryTreeWidth;
        Settings.storeCategoryTreeWidth(hSize);
        
        int vSize = m_verticalSplitPane.getDividerLocation() > 0 ?
            m_verticalSplitPane.getDividerLocation() : 
            m_verticalSplitPane.getLastDividerLocation();
        Settings.storeMainDividerLocation(vSize);
        
        Settings.storeCategoryTreeVisible(m_showTreeButton.isSelected());
        Settings.storeFrameState(this, FRAME_ID);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.Main.LearnSessionStartObserver
     */
    public void sessionStarted(LearnSession session)
    {
        gotoLearnMode();
    }

    /* (non-Javadoc)
     * @see jmemorize.core.Main.LearnSessionStartObserver
     */
    public void sessionEnded(LearnSession session)
    {
        showSessionChart(session);
        gotoBrowseMode();
    }

    /**
     * Displays a dialog which summarizes the given session outcome.
     */
    private void showSessionChart(LearnSession session)
    {
        if (!session.isRelevant())
            return;
        
        JDialog dialog = new OkayButtonDialog(this, 
            Localization.get("Learn.SESSION_RESULTS"), //$NON-NLS-1$ 
            true, new SessionChartPanel(session));
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void gotoBrowseMode()
    {
        m_learnPanel.removeSelectionObserver(this);
        m_deckTablePanel.getCardTable().addSelectionObserver(this);
        
        ((CardLayout)m_bottomPanel.getLayout()).show(m_bottomPanel, DECK_CARD);
    
        m_focusedCategories = null;
    
        showCategoryTree(m_showCategoryTreeOld);
        
        updateSelectionObservers();
    }

    private void gotoLearnMode()
    {
        m_deckTablePanel.getCardTable().removeSelectionObserver(this);
        m_learnPanel.addSelectionObserver(this);
        
        ((CardLayout)m_bottomPanel.getLayout()).show(m_bottomPanel, REPEAT_CARD);
    
        m_focusedCategories = null;
    
        setDeck(-1); //needed to get right values in status bar while learning

        updateSelectionObservers();
    }

    private void updateSelectionObservers()
    {
        for (SelectionObserver listener : m_selectionObservers)
            listener.selectionChanged(this);
    }

    /**
     * Update the frame title. This should be called when a new lesson was
     * loaded or changed.
     */
    private void updateFrameTitle()
    {
        String name    = Main.PROPERTIES.getProperty("project.name");    //$NON-NLS-1$
        String version = Main.PROPERTIES.getProperty("project.version"); //$NON-NLS-1$
        String suffix  = " - " + name + " " + version; //$NON-NLS-1$ //$NON-NLS-2$
    
        File file = m_main.getLesson().getFile();
        if (file != null && !getTitle().equals(file.getName()))
        {
            setTitle(file.getName() + suffix); 
        }
        else if (file == null)
        {
            setTitle(Localization.get("MainFrame.UNNAMED_LESSON") + suffix); //$NON-NLS-1$
        }
    }

    private SelectionProvider getCurrentSelectionProvider() 
    {
        if (m_main.isSessionRunning())
        {
            return m_learnPanel;
        }
        else
        {
            return m_deckTablePanel.getCardTable();
        }
    }
    
    private void initComponents()
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        m_deckChartPanel = new DeckChartPanel(this);
        m_deckChartPanel.setMinimumSize(new Dimension(100, 150));
        
        m_learnPanel = new LearnPanel();
        m_deckTablePanel = new DeckTablePanel(this);

        // north panel
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(buildOperationsBar());
        northPanel.add(buildCategoryBar());

        m_categoryTree = new CategoryTree();
        m_categoryTree.addSelectionObserver(new SelectionObserver() {

            public void selectionChanged(SelectionProvider source)
            {
                treeSelectionChanged(source);
            }
            
        });
        m_treeScrollPane = new JScrollPane(m_categoryTree);
        
        // bottom panel
        m_bottomPanel = new JPanel(new CardLayout());
        m_bottomPanel.add(m_deckTablePanel, DECK_CARD);
        m_bottomPanel.add(m_learnPanel, REPEAT_CARD);

        // vertical split pane
        m_verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        m_verticalSplitPane.setPreferredSize(new Dimension(16, 500));
        m_verticalSplitPane.setBorder(null);
        BasicSplitPaneUI ui = (BasicSplitPaneUI)m_verticalSplitPane.getUI();
        ui.getDivider().setBorder(new EmptyBorder(5, 2, 5, 2));
        
        m_verticalSplitPane.setTopComponent(m_deckChartPanel);
        m_verticalSplitPane.setBottomComponent(m_bottomPanel);
        
        mainPanel.setPreferredSize(new Dimension(800, 500));
        mainPanel.add(m_verticalSplitPane, BorderLayout.CENTER);

        // horizontal split pane
        m_horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        m_horizontalSplitPane.setDividerLocation(m_categoryTreeWidth);
        m_horizontalSplitPane.setDividerSize(4);
        m_horizontalSplitPane.setBorder(null);

        m_horizontalSplitPane.setLeftComponent(m_treeScrollPane);
        m_horizontalSplitPane.setRightComponent(mainPanel);
        
        // frame content pane
        getContentPane().add(northPanel, BorderLayout.NORTH);
        getContentPane().add(m_horizontalSplitPane, BorderLayout.CENTER);
        getContentPane().add(m_statusBar, BorderLayout.SOUTH);
        setJMenuBar(new MainMenu(this, m_main.getRecentLessonFiles()));

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt)
            {
                ExitAction.exit();
            }
        });
        
        setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/resource/icons/main.png"))); //$NON-NLS-1$
        pack();
    }

    private JPanel buildCategoryBar()
    {
        JToolBar categoryToolbar = new JToolBar();
        categoryToolbar.setFloatable(false);
        categoryToolbar.setMargin(new Insets(2, 2, 2, 2));

        m_showTreeButton = new JButton(new ShowCategoryTreeAction());
        m_showTreeButton.setPreferredSize(new Dimension(120, 21));
        categoryToolbar.add(m_showTreeButton);

        JLabel categoryLabel = new JLabel(
            Localization.get(LC.CATEGORY), SwingConstants.CENTER);
        categoryLabel.setPreferredSize(new Dimension(60, 15));
        categoryToolbar.add(categoryLabel);

        m_categoryBox = new CategoryComboBox();
        m_categoryBox.setPreferredSize(new Dimension(24, 24));
        m_categoryBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            {
                categoryBoxActionPerformed();
            }
        });
        categoryToolbar.add(m_categoryBox);
        categoryToolbar.add(new JButton(new SplitMainFrameAction(this)));

        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setBorder(new EtchedBorder());
        categoryPanel.add(categoryToolbar, BorderLayout.NORTH);

        return categoryPanel;
    }
    
    private JPanel buildOperationsBar()
    {
        JToolBar operationsToolbar = new JToolBar();
        operationsToolbar.setFloatable(false);

        operationsToolbar.add(new JButton(new NewLessonAction()));
        operationsToolbar.add(new JButton(new OpenLessonAction()));
        operationsToolbar.add(new JButton(new SaveLessonAction()));
        
        operationsToolbar.add(new JButton(new AddCardAction(this)));
        operationsToolbar.add(new JButton(new EditCardAction(this)));
        operationsToolbar.add(new JButton(new ResetCardAction(this)));
        operationsToolbar.add(new JButton(new RemoveAction(this)));
        
        operationsToolbar.add(new JButton(new AddCategoryAction(this)));
        operationsToolbar.add(new JButton(new FindAction()));
        operationsToolbar.add(new JButton(new LearnAction(this)));
        
        JPanel operationsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
        operationsPanel.add(operationsToolbar);

        return operationsPanel;
    }

    private void categoryBoxActionPerformed()
    {
        setCategory(m_categoryBox.getSelectedCategory());
    }

    private void treeSelectionChanged(SelectionProvider source)
    {
        assert source == m_categoryTree;
        
        if (!m_categoryTree.isPendingSelection())
        {
            Category category = m_categoryTree.getSelectedCategory();
            if (category != m_category)
                setCategory(category);
        }
    }
    
    private void loadSettings()
    {
        showCategoryTree(Settings.loadCategoryTreeVisible());
        m_verticalSplitPane.setDividerLocation(Settings.loadMainDividerLocation());
        Settings.loadFrameState(this, FRAME_ID);
    }

    private void importGlobalLearnHistory(LearnHistory history)
    {
        LearnHistory globalHistory = m_main.getGlobalLearnHistory();
        for (SessionSummary summary : globalHistory.getSummaries())
        {
            history.addSummary(summary.getStart(), summary.getEnd(), 
                (int)summary.getPassed(), (int)summary.getFailed(), 
                (int)summary.getSkipped(), (int)summary.getRelearned());
        }
    }
}
