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
package jmemorize.gui.swing.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.CategoryObserver;
import jmemorize.core.Events;
import jmemorize.core.Main;
import jmemorize.core.SearchTool;
import jmemorize.core.Settings;
import jmemorize.core.Main.ProgramEndObserver;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.actions.AbstractAction2;
import jmemorize.gui.swing.panels.StatusBar;
import jmemorize.gui.swing.widgets.CardTable;
import jmemorize.gui.swing.widgets.CategoryComboBox;
import jmemorize.util.EscapableFrame;
import jmemorize.util.RecentItems;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A frame that shows search options and a card table as results view.
 * 
 * @author djemili
 */
public class FindFrame extends EscapableFrame 
    implements CategoryObserver, ProgramEndObserver
{
    private final static String FRAME_ID            = "findframe";

    private CardTable           m_cardTable         = new CardTable(this, 
        Main.USER_PREFS.node("find.table"), //$NON-NLS-1$
            new int[] {
                CardTable.COLUMN_FRONTSIDE, 
                CardTable.COLUMN_BACKSIDE, 
                CardTable.COLUMN_CATEGORY });

    // swing widgets
    private JComboBox           m_searchTextBox     = new JComboBox();
    private RecentItems         m_recentSearchTexts = new RecentItems(10, 
        Main.USER_PREFS.node("recent.search.texts"));               //$NON-NLS-1$

    private JCheckBox           m_matchCaseBox      = new JCheckBox(
        Localization.get(LC.MATCH_CASE));
    private JRadioButton        m_radioBothSides    = new JRadioButton(
        Localization.get(LC.BOTH_SIDES), true);
    private JRadioButton        m_radioFrontSide    = new JRadioButton(
        Localization.get(LC.FRONTSIDE));
    private JRadioButton        m_radioBackSide     = new JRadioButton(
        Localization.get(LC.FLIPSIDE));
    
    private CategoryComboBox    m_categoryBox       = new CategoryComboBox();
    private StatusBar           m_statusBar         = new StatusBar();

    // these vars are stored when search button is clicked
    private String              m_searchText;
    private int                 m_searchSides;
    private boolean             m_matchCase;
    private Category            m_searchCategory;
    
    private static FindFrame    m_instance;

    private class FindAction extends AbstractAction2
    {
        public FindAction()
        {
            setName(Localization.get("FindTool.FIND")); //$NON-NLS-1$
        }

        public void actionPerformed(java.awt.event.ActionEvent e)
        {
            search();
        }
    }

    private class CloseAction extends AbstractAction2
    {
        public CloseAction()
        {
            setName(Localization.get(LC.CANCEL));
        }

        public void actionPerformed(java.awt.event.ActionEvent e)
        {
            close();
        }
    }

    public static FindFrame getInstance()
    {
        if (m_instance == null)
        {
            m_instance = new FindFrame();
        }
        
        return m_instance;
    }
    
    public void show(Category rootCategory, Category selectedCategory)
    {
        if (rootCategory != m_categoryBox.getRootCategory())
        {
            clear();
        }
        
        m_categoryBox.setRootCategory(rootCategory);
        m_categoryBox.setSelectedCategory(selectedCategory);
        m_searchTextBox.requestFocus();
        setVisible(true);
    }
    
    /**
     * Remove all search results.
     */
    public void clear()
    {
        Category selectedCategory = m_categoryBox.getSelectedCategory();
        m_cardTable.getView().setCards(new ArrayList<Card>(0), selectedCategory);
    }

    public void search()
    {
        String searchText = (String)m_searchTextBox.getSelectedItem();

        if (searchText == null || searchText.equals("")) //$NON-NLS-1$
            return;

        m_searchText = searchText;
        m_recentSearchTexts.push(m_searchText);

        if (m_radioBothSides.isSelected())
        {
            m_searchSides = SearchTool.BOTH_SIDES;
        } 
        else
        {
            m_searchSides = m_radioFrontSide.isSelected() ? 
                SearchTool.FRONT_SIDE : SearchTool.FLIP_SIDE;
        }

        if (m_searchCategory != null)
        {
            m_searchCategory.removeObserver(FindFrame.this);
        }
        m_searchCategory = m_categoryBox.getSelectedCategory();
        m_searchCategory.addObserver(FindFrame.this);

        m_matchCase = m_matchCaseBox.isSelected();

        List<Card> results = SearchTool.search(m_searchText, m_searchSides, 
            m_matchCase, m_searchCategory.getCards());
        
        m_cardTable.getView().setCards(results, m_searchCategory);
    }

    public boolean close()
    {
        setVisible(false);
        return true;
    }

    /*
     * @see jmemorize.core.CategoryObserver#onCardEvent
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        // CHECK move into cardtable!?
        List<Card> cards = m_cardTable.getView().getCards();

        // for now we only remove cards but dont add new cards
        if (type == Events.REMOVED_EVENT)
        {
            cards.remove(card);
        }

        m_cardTable.getView().setCards(cards, m_searchCategory);
        updateStatusBar();
    }

    /*
     * @see jmemorize.core.CategoryObserver#onCategoryEvent
     */
    public void onCategoryEvent(int type, Category category)
    {
        // category combo box handles this event by itself
    }

    public CardTable getCardTable()
    {
        return m_cardTable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jmemorize.core.Main.ProgramEndObserver
     */
    public void onProgramEnd()
    {
        Settings.storeFrameState(this, FRAME_ID);
    }

    private void updateStatusBar()
    {
        m_statusBar.setCards(m_cardTable.getView().getCards());
    }

    private JPanel buildSearchPanel()
    {
        setupSearchTextBox();

        JScrollPane scrollPane = new JScrollPane(m_cardTable);
        Color color = UIManager.getColor("Table.background"); //$NON-NLS-1$
        scrollPane.getViewport().setBackground(color);
        scrollPane.setPreferredSize(new Dimension(500, 200));

        FormLayout layout = new FormLayout(
            "right:pref, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow", // columns // //$NON-NLS-1$
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, p, 9dlu, fill:d:grow"); // rows // //$NON-NLS-1$

        CellConstraints cc = new CellConstraints();

        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.addLabel(Localization.get("FindTool.SEARCH_TEXT"), cc.xy(1, 1)); //$NON-NLS-1$
        builder.add(m_searchTextBox, cc.xyw(3, 1, 5));

        builder.addLabel(Localization.get("General.CATEGORY"), cc.xy(1, 3)); //$NON-NLS-1$
        builder.add(m_categoryBox, cc.xyw(3, 3, 5));

        builder.addLabel(Localization.get("FindTool.SETTINGS"), cc.xy(1, 5)); //$NON-NLS-1$
        builder.add(m_radioBothSides, cc.xy(3, 5));
        builder.add(m_radioFrontSide, cc.xy(5, 5));
        builder.add(m_radioBackSide, cc.xy(7, 5));

        builder.add(m_matchCaseBox, cc.xyw(3, 7, 5));

        builder.addSeparator(Localization.get("FindTool.RESULTS"), cc.xyw(1, 9, 7)); //$NON-NLS-1$
        builder.add(scrollPane, cc.xyw(1, 11, 7));

        return builder.getPanel();
    }

    private void setupSearchTextBox()
    {
        m_searchTextBox.setEditable(true);
        m_searchTextBox.setMaximumRowCount(10);

        m_searchTextBox.addPopupMenuListener(new PopupMenuListener()
        {
            public void popupMenuCanceled(PopupMenuEvent arg0)
            {
                // ignore
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0)
            {
                // ignore
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0)
            {
                m_searchTextBox.setModel(new DefaultComboBoxModel(
                    m_recentSearchTexts.getItems().toArray()));
            }
        });

        Component comp = m_searchTextBox.getEditor().getEditorComponent();
        comp.addKeyListener(new KeyListener()
        {
            public void keyPressed(KeyEvent e)
            {
            }

            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    search();
                }
            }

            public void keyTyped(KeyEvent e)
            {
            }
        });

        Component editorComp = m_searchTextBox.getEditor().getEditorComponent();
        editorComp.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                m_searchTextBox.getEditor().selectAll();
            }

            public void focusLost(FocusEvent e)
            {
                // ignore
            }
        });
    }

    private JPanel buildSearchBar()
    {
        JButton closeButton = new JButton(new CloseAction());
        JButton searchButton = new JButton(new FindAction());

        JPanel buttonPanel = ButtonBarFactory.buildRightAlignedBar(
            searchButton, closeButton);
        buttonPanel.setBorder(new EmptyBorder(0, 5, 5, 10));

        getRootPane().setDefaultButton(searchButton);

        return buttonPanel;
    }
    
    private FindFrame()
    {
        initComponents();

        Main.getInstance().addProgramEndObserver(this);
        Settings.loadFrameState(this, FRAME_ID);
    }

    private void initComponents()
    {
        setTitle(Localization.get("FindTool.FIND")); //$NON-NLS-1$

        // build main panel
        ButtonGroup group = new ButtonGroup();
        group.add(m_radioBothSides);
        group.add(m_radioFrontSide);
        group.add(m_radioBackSide);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buildSearchPanel(), BorderLayout.CENTER);
        mainPanel.add(buildSearchBar(), BorderLayout.SOUTH);
        mainPanel.setBorder(new EtchedBorder());

        // set status bar
        m_statusBar = new StatusBar();
        m_cardTable.setStatusBar(m_statusBar);

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(m_statusBar, BorderLayout.SOUTH);

        setupCardTable();

        setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource("/resource/icons/find.gif"))); //$NON-NLS-1$
        pack();
    }

    private void setupCardTable()
    {
        // close window on ESC key
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        m_cardTable.getInputMap().put(keyStroke, "Cancel"); //$NON-NLS-1$
        m_cardTable.getActionMap().put("Cancel", new AbstractAction() { //$NON-NLS-1$
            public void actionPerformed(ActionEvent e)
            {
                close();
            }
        });

        // overwrite moving to next row when pressing ENTER
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        m_cardTable.getInputMap().put(keyStroke, "Edit"); //$NON-NLS-1$
        m_cardTable.getActionMap().put("Edit", new AbstractAction() { //$NON-NLS-1$
            public void actionPerformed(ActionEvent e)
            {
                editCards();
            }
        });

        m_cardTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() == 2)
                {
                    editCards();
                }
            }
        });
    }

    private void editCards()
    {
        Card card = (Card)m_cardTable.getSelectedCards().get(0);
        List<Card> cards = m_cardTable.getView().getCards();
        Category category = m_cardTable.getView().getCategory();

        EditCardFrame.getInstance().showCard(card, cards, category, 
            m_searchText, m_searchSides, m_matchCase);
    }
}
