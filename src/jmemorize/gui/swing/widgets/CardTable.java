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

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.Main;
import jmemorize.core.Settings;
import jmemorize.core.Main.ProgramEndObserver;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.CardFont;
import jmemorize.gui.swing.CardStatusIcons;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.CardFont.FontAlignment;
import jmemorize.gui.swing.CardFont.FontType;
import jmemorize.gui.swing.actions.LearnAction;
import jmemorize.gui.swing.actions.edit.AddCardAction;
import jmemorize.gui.swing.actions.edit.CopyAction;
import jmemorize.gui.swing.actions.edit.CutAction;
import jmemorize.gui.swing.actions.edit.EditCardAction;
import jmemorize.gui.swing.actions.edit.PasteAction;
import jmemorize.gui.swing.actions.edit.RemoveAction;
import jmemorize.gui.swing.actions.edit.ResetCardAction;
import jmemorize.gui.swing.frames.MainFrame;
import jmemorize.gui.swing.panels.StatusBar;
import jmemorize.util.Arrow;
import jmemorize.util.PreferencesTool;
import jmemorize.util.ReverseOrder;
import jmemorize.util.TimeSpan;

/**
 * @author djemili
 */
public class CardTable extends JTable implements Settings.CardFontObserver, 
    SelectionProvider, ProgramEndObserver
{
    /**
     * A TableCellRenderer that displays card sides. It uses the specified fonts
     * for front/flip sides.
     */
    private class SideRenderer extends DefaultTableCellRenderer
    {
        private CardFont   m_font;
        
        public SideRenderer(CardFont font)
        {
            m_font = font;
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component component = super.getTableCellRendererComponent(table, 
                value, isSelected, hasFocus, row, column);
            
            if (m_font != null)
            {
                component.setFont(m_font.getFont());
                
                if (component instanceof JLabel)
                {
                    JLabel label = (JLabel)component;

                    if (m_font.getAlignment() == FontAlignment.LEFT)
                        label.setHorizontalAlignment(SwingConstants.LEADING);
                    
                    else if (m_font.getAlignment() == FontAlignment.CENTER)
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                    
                    else if (m_font.getAlignment() == FontAlignment.RIGHT)
                        label.setHorizontalAlignment(SwingConstants.TRAILING);
                }
            }
            
            return component;
        }
    }
    
    /**
     * A TableCellRenderer that displays cells with Strings and a specific
     * string suffix.
     */
    private class TextRenderer extends DefaultTableCellRenderer
    {
        private String m_suffix;
        
        public TextRenderer(String suffix)
        {
            m_suffix = suffix;
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column)
        {
            return super.getTableCellRendererComponent(table, value + m_suffix, 
                isSelected, hasFocus, row, column);
        }
    }
    
    /**
     * A TableCellRenderer that renders normal cells with dates.
     */
    private class DateRenderer extends DefaultTableCellRenderer 
    {   
        private String m_nullString;
        
        public DateRenderer(String nullString)
        {
            m_nullString = nullString;
        }
        
        protected void setValue(Object value) 
        {
            setText(value == null ? 
                m_nullString : Localization.SHORT_DATE_FORMATER.format(value));            
        }
    }
    
    /**
     * A TableCellRenderer that renders cells which show expiration dates. This
     * renderer displays a little icon with every date. The icon is chosen
     * according to the expiration state (e.g. expired, unlearned, etc.).
     */
    private class DateExpiredRenderer extends DefaultTableCellRenderer 
    {    
        public void setValue(Object value) 
        {
            // if not learned
            if (value == null)
            {
                setText(Localization.get("CardTable.NOT_LEARNED")); //$NON-NLS-1$
            }
            else
            {
                setText(Localization.SHORT_DATE_FORMATER.format(value));
            }
            
            Date expiration = (Date)value;
            setIcon(CardStatusIcons.getInstance().getCardIcon(expiration));
        }
    }
    
    /**
     * A Comparator that orders cards according to the specified model index.
     */
    private class CardComparator implements Comparator<Card>
    {
        private int m_modelIndex;
        
        public CardComparator(int modelIndex)
        {
            m_modelIndex = modelIndex;
        }

        /**
         * @see java.util.Comparator
         */
        public int compare(Card arg0, Card arg1)
        {
            Comparable col0 = (Comparable)getValue(arg0, m_modelIndex);
            Comparable col1 = (Comparable)getValue(arg1, m_modelIndex);
            
            if (col0 == null)
            {
                return col1 == null ? 0 : -1;
            } 
            else
            {
                return col1 == null ? 1 : col0.compareTo(col1);
            } 
        }
    }
    
    /**
     * Copied from Java Sample.
     */
    private class SortableHeaderRenderer implements TableCellRenderer
    {
        private TableCellRenderer tableCellRenderer;

        public SortableHeaderRenderer(TableCellRenderer tableCellRenderer)
        {
            this.tableCellRenderer = tableCellRenderer;
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component c = tableCellRenderer.getTableCellRendererComponent(table, 
                value, isSelected, hasFocus, row, column);
            
            if (c instanceof JLabel)
            {
                JLabel l = (JLabel)c;
                l.setHorizontalTextPosition(SwingConstants.LEFT);
                int modelColumn = table.convertColumnIndexToModel(column);
                
                if (modelColumn == m_tableModel.getSortingColumn())
                {
                    l.setIcon(m_tableModel.getSortingDir() == ViewModel.ASCENDING ? 
                        m_ascendingArrow : m_descendingArrow);
                }
                else
                {
                    l.setIcon(null);
                }
            }
            
            return c;
        }
    }
    
    public class ViewModel extends AbstractTableModel
    {
        public static final int ASCENDING  = 0;
        public static final int DESCENDING = 1;

        private List<Card>      m_cards    = new ArrayList<Card>();
        // HACK currently this is only needed for transferhandlers
        private Category        m_category;

        private int             m_orderModelIndex;
        private int             m_orderDir;
        
        public Category getCategory()
        {
            return m_category;
        }
        
        public void setCards(List<Card> cards, Category category)
        {
            m_cards = cards;
            m_category = category;
            resort();
            
            updateCardCountStatusBar();
        }
        
        /**
         * Set the model column index and direction by which this table should
         * be sorted.
         * 
         * @param modelIndex The model index by which the sorting should happen.
         * @param direction Can be in ASCENDING or DESCENDING order.
         */
        public void setSorting(int modelIndex, int direction)
        {
            m_orderModelIndex = modelIndex;
            m_orderDir = direction;
            
            resort();
        }
        
        public List<Card> getCards()
        {
            return m_cards;
        }

        /**
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount()
        {
            return m_cards != null ? m_cards.size() : 0;
        }

        /**
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount()
        {
            return COLUMN_NAMES.length;
        }
        
        /**
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        public String getColumnName(int column)
        {
            return COLUMN_NAMES[column];
        }
        
        /**
         * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
         */
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }

        /**
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Card card = (Card)m_cards.get(rowIndex);
            return getValue(card, columnIndex);
        }

        /**
         * @return Returns the orderColumn.
         */
        public int getSortingColumn()
        {
            return m_orderModelIndex;
        }
        
        /**
         * @return Returns the orderDir.
         */
        public int getSortingDir()
        {
            return m_orderDir;
        }
        
        private void resort()
        {
            if (m_cards != null) //CHECK
            {
                Comparator<Card> comparator = new CardComparator(m_orderModelIndex);
                Collections.sort(m_cards, m_orderDir == ASCENDING ? 
                    comparator : new ReverseOrder<Card>(comparator));
                
                fireTableDataChanged();
            }
        }
    }
    
    // column enum
    public static final int       COLUMN_FRONTSIDE    = 0;
    public static final int       COLUMN_BACKSIDE     = 1;
    public static final int       COLUMN_DECK         = 2;
    public static final int       COLUMN_CATEGORY     = 3;
    public static final int       COLUMN_PATH         = 4;
    public static final int       COLUMN_CREATED      = 5;
    public static final int       COLUMN_TESTED       = 6;
    public static final int       COLUMN_EXPIRES      = 7;
    public static final int       COLUMN_RATIO        = 8;
    public static final int       COLUMN_MODIFIED     = 9;

    // preferences keys
    private static final String   PREF_SORT           = "sort-by";  //$NON-NLS-1$
    private static final String   PREF_SORT_DIR       = "sort-dir"; //$NON-NLS-1$
    private static final String   PREF_WIDTHS         = "widths";   //$NON-NLS-1$
    private static final String   PREF_COLUMNS        = "columns";  //$NON-NLS-1$
    
    private static final String[] COLUMN_NAMES = { 
        Localization.get(LC.FRONTSIDE),
        Localization.get(LC.FLIPSIDE),
        Localization.get(LC.DECK),
        Localization.get(LC.CATEGORY),
        Localization.get(LC.CARDTABLE_PATH),
        Localization.get(LC.CREATED),
        Localization.get(LC.LAST_TEST),
        Localization.get(LC.EXPIRES),
        Localization.get(LC.PASSED),
        Localization.get(LC.MODIFIED)};
    
    private static final int      DEFAULT_COLUMN_WIDTH = 170;
    
    // widgets
    private JCheckBoxMenuItem[]   m_checkBoxItems   = new JCheckBoxMenuItem[COLUMN_NAMES.length];
    private JPopupMenu            m_headerMenu      = new JPopupMenu(
        Localization.get("CardTable.COLUMNS")); //$NON-NLS-1$
    private JPopupMenu            m_cardMenu;
    private StatusBar             m_statusBar;

    // icons
    private Icon                  m_ascendingArrow  = new Arrow(false, 17);
    private Icon                  m_descendingArrow = new Arrow(true, 17);
    
    /** background color for odd rows in table */
    private Color                 m_highlight       = new Color(0xF4, 0xF4, 0xF3);
    
    private ViewModel             m_tableModel      = new ViewModel();
    private int[]                 m_columns;
    private int[]                 m_defaultColumns;
    private int[]                 m_columnWidths    = new int[COLUMN_NAMES.length];
    private CardFont              m_frontSideFont;
    private CardFont              m_flipSideFont;
    private Preferences           m_prefs;
    private int                   m_minTableRowHeight;
    
    private JFrame                m_frame;
    private List<SelectionObserver> m_selectionListeners = new LinkedList<SelectionObserver>();
    
    /**
     * Creates a new CardTable which can be used to show a list of cards.
     * 
     * @param the frame that should be used to display modal dialogs.
     * @param prefs the Preferences object which should be used to load/store
     * preferences of this card table.
     * @param defaultColumns if columns can't be loaded from preferences,
     * display these columns.
     */
    public CardTable(JFrame frame, Preferences prefs, int[] defaultColumns) 
    {
        m_frame = frame;
        m_cardMenu = buildCardContextMenu();
        
        m_prefs = prefs;
        m_defaultColumns = defaultColumns;
        
        m_minTableRowHeight = getRowHeight();
        
        setModel(m_tableModel);
        
        buildHeaderMenu();
        getTableHeader().addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e)
            {
                headerClicked(e);
            }
        });
        
        hookCardContextMenu(this);
        
        loadFromPreferences();
        
        setTransferHandler(MainFrame.TRANSFER_HANDLER);
        setDragEnabled(true);
        setShowGrid(false);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // we add the listener here. removal is done from outside if suitable
        Settings.addCardFontObserver(this);
        Settings.setCardFont(this, FontType.TABLE_FRONT, FontType.TABLE_FLIP);
        
        Main.getInstance().addProgramEndObserver(this);
    }

    public void setColumns(int[] columns)
    {
        m_columns = columns;
        
        // create and set tablecolumn model
        TableColumnModel columnModel = new DefaultTableColumnModel();
        for (int i = 0; i < columns.length; i++)
        {
            if (columns[i] < 0 || columns[i] > COLUMN_NAMES.length)
            {
                continue;
            }
            
            TableColumn column = new TableColumn(columns[i]);
            column.setHeaderValue(COLUMN_NAMES[columns[i]]);
            
            TableCellRenderer defaultRenderer = getTableHeader().getDefaultRenderer();
            column.setHeaderRenderer(new SortableHeaderRenderer(defaultRenderer));
            
            if (m_columnWidths[columns[i]] > 0)
            {
                column.setPreferredWidth(m_columnWidths[columns[i]]);
            }
            else
            {
                column.setPreferredWidth(DEFAULT_COLUMN_WIDTH);
            }
                
            switch (columns[i])
            {
                case COLUMN_FRONTSIDE:
                    column.setCellRenderer(new SideRenderer(m_frontSideFont));
                    break;
                  
                case COLUMN_BACKSIDE:
                    column.setCellRenderer(new SideRenderer(m_flipSideFont));
                    break;
                
                case COLUMN_CREATED: // fall through
                case COLUMN_MODIFIED:
                    column.setCellRenderer(new DateRenderer("-")); //$NON-NLS-1$
                    break;
                    
                case COLUMN_EXPIRES:
                    column.setCellRenderer(new DateExpiredRenderer());
                    break;
                    
                case COLUMN_TESTED:
                    column.setCellRenderer(new DateRenderer(
                        Localization.get("CardTable.NOT_LEARNED"))); //$NON-NLS-1$
                    break;
                    
                case COLUMN_RATIO:
                    column.setCellRenderer(new TextRenderer("%"));
                    break;
                    
                default:
                    column.setCellRenderer(new DefaultTableCellRenderer());
            }
            
            columnModel.addColumn(column);
        }
        
        setColumnModel(columnModel);
    }
    
    public void setStatusBar(StatusBar statusBar)
    {
        m_statusBar = statusBar;
        
        if (m_statusBar != null)
        {
            updateCardCountStatusBar();
            updateSelectedCardCountStatusBar();
        }
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Card> getRelatedCards()
    {
        return m_tableModel.getCards();
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public Category getCategory()
    {
        return m_tableModel.getCategory();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Category> getSelectedCategories()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public JComponent getDefaultFocusOwner()
    {
        return this;
    }
    
    public JFrame getFrame()
    {
        return m_frame;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public void addSelectionObserver(SelectionObserver observer)
    {
        m_selectionListeners.add(observer);
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public void removeSelectionObserver(SelectionObserver observer)
    {
        m_selectionListeners.remove(observer);
    }
    
    /**
     * @return the ViewModel for this table.
     */
    public ViewModel getView() // TODO replace by cardprovider
    {
        return m_tableModel;
    }
    
    /**
     * @return all selected cards or <code>null</code> no cards are selected.
     * 
     * @see jmemorize.gui.swing.SelectionProvider
     */
    public List<Card> getSelectedCards()
    {
        if (m_tableModel == null)
        {
            return null;
        }
        
        List<Card> cards = m_tableModel.getCards();
        List<Card> selection = new ArrayList<Card>(cards.size());
        
        int rows[] = getSelectedRows();
        
        for (int i = 0; i < rows.length; i++)
        {
            selection.add(cards.get(rows[i]));
        }
        
        return selection.size() > 0 ? selection : null;
    }
    
    /*
     * Is called when a listSelection occured.
     *
     * @see javax.swing.JTable
     */
    public void valueChanged(ListSelectionEvent e)
    {
        super.valueChanged(e);
        
        updateSelectedCardCountStatusBar();
        
        // this can be called while constructing super class. in that case our 
        // variables wouldn't yet be set. 
        if (!e.getValueIsAdjusting() && m_selectionListeners != null)
        {
            for (SelectionObserver listener : m_selectionListeners)
            {
                listener.selectionChanged(this);
            }
        }
    }
    
    /*
     * @see javax.swing.JTable
     */
    public String getToolTipText(MouseEvent event)
    {
        /* this can be called while constructing super class. in that case our 
         * variables wouldn't yet be set. */
        if (m_columns == null || m_tableModel == null)
        {
            return "";
        }
        
        int row = rowAtPoint(event.getPoint());
        int col = columnAtPoint(event.getPoint());
        
        Card card = (Card)m_tableModel.getCards().get(row);
        int modelIndex = m_columns[col];
        
        switch (modelIndex)
        {
            case COLUMN_CATEGORY:
                return card.getCategory().getPath();
                
            case COLUMN_CREATED: // fall-through
            case COLUMN_MODIFIED:
                Date date = modelIndex == COLUMN_CREATED ? 
                    card.getDateCreated() : card.getDateModified();
                return ((date != null) ? TimeSpan.format(new Date(), date) : null);
                
            case COLUMN_TESTED: // fall-through
            case COLUMN_EXPIRES:
                date = modelIndex == COLUMN_TESTED ? 
                    card.getDateTested() : card.getDateExpired();
                return ((date != null) ? TimeSpan.format(new Date(), date) : 
                    Localization.get("CardTable.NO_DATE_DESC")); //$NON-NLS-1$
            
            case COLUMN_RATIO:
                return card.getTestsPassed() + " / " + card.getTestsTotal();
                
            default:
                return null;
        }
    }
    
    /*
     * @see javax.swing.JTable
     */
    public void columnMarginChanged(ChangeEvent e) 
    {
        // CHECK replace by application exit behaviour
        super.columnMarginChanged(e);
        
        /* this can be called while constructing super class. in that case our 
         * variables wouldn't yet be set. */
        if (m_columns == null || m_columnWidths == null)
        {
            return;
        }
        
        for (int i = 0; i < m_columns.length; i++)
        {
            TableColumn column = getColumnModel().getColumn(i);
            m_columnWidths[column.getModelIndex()] = column.getWidth();
        }
    }
    
    /*
     * @see javax.swing.JTable
     */
    public void columnMoved(TableColumnModelEvent evt)
    {
        super.columnMoved(evt);
        
        /* this can be called while constructing super class. in that case our 
         * variables wouldn't yet be set. */
        if (m_columns == null)
        {
            return;
        }
        
        int[] columns = new int[m_columns.length];
        for (int i = 0; i < columns.length; i++)
        {
            TableColumn column = getColumnModel().getColumn(i);
            columns[i] = column.getModelIndex();
        }
        
        m_columns = columns;
    }
    
    /*
     * @see jmemorize.core.Settings.CardFontObserver
     */
    public void fontChanged(FontType type, CardFont font)
    {
        if (type == FontType.TABLE_FRONT)
            m_frontSideFont = font;
        
        else if (type == FontType.TABLE_FLIP)
            m_flipSideFont = font;
        
        else
            return;
        
        if (m_frontSideFont == null || m_flipSideFont == null)
            return;
        
        FontMetrics metrics = getFontMetrics(m_frontSideFont.getFont());
        int h = metrics.getHeight();
        
        metrics = getFontMetrics(m_flipSideFont.getFont());
        if (metrics.getHeight() > h)
            h = metrics.getHeight();
        
        setRowHeight(h > m_minTableRowHeight ? h : m_minTableRowHeight);
        
        setColumns(m_columns); //HACK
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.Main.ProgramEndObserver
     */
    public void onProgramEnd()
    {
        m_prefs.putInt(PREF_SORT, m_tableModel.getSortingColumn());                            
        m_prefs.putInt(PREF_SORT_DIR, m_tableModel.getSortingDir());
        PreferencesTool.putIntArray(m_prefs, PREF_WIDTHS, m_columnWidths);
        PreferencesTool.putIntArray(m_prefs, PREF_COLUMNS, m_columns);        
    }

    /**
     * Hook the card context menu to given JComponent. Attaches a MouseListener
     * to the given component which checks for right mouse button clicks and
     * shows the context menu.
     */
    public void hookCardContextMenu(JComponent comp)
    {
        comp.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    int row = rowAtPoint(e.getPoint());
                    if (!selectionModel.isSelectedIndex(row))
                    {
                        selectionModel.setSelectionInterval(row,row);
                    }
                    
                    m_cardMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JTable#prepareRenderer
     */
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
    {
        Component cell = super.prepareRenderer(renderer, row, column);
        
        Color background = (row % 2 == 0) ? m_highlight : getBackground();
        if (isCellSelected(row, column)) background = getSelectionBackground();
        cell.setBackground(background);
        
        return cell;
    }
    
    /**
     * Return the value for given model index column in given card model.
     * 
     * @param card the card from which one column value is returned.
     * @param modelIndex the index in model space (is'nt affected by the current
     * visual shown card columns or the way they are ordered in the table).
     * @return the value at given model index for given card.
     */
    private static Object getValue(Card card, int modelIndex)
    {
        switch (modelIndex)
        {
            case COLUMN_FRONTSIDE: 
                return card.getFrontSide().getText().getUnformatted().replace('\n', ' ');
                
            case COLUMN_BACKSIDE:
                return card.getBackSide().getText().getUnformatted().replace('\n', ' ');
                
            case COLUMN_DECK:
                return new Integer(card.getLevel());
                
            case COLUMN_CATEGORY: 
                return card.getCategory().getName();
                
            case COLUMN_PATH: 
                return card.getCategory().getPath();
                
            case COLUMN_CREATED: 
                return card.getDateCreated();
                
            case COLUMN_MODIFIED:
                return card.getDateModified();
                
            case COLUMN_TESTED:
                return card.getDateTested();
                
            case COLUMN_EXPIRES:
                return card.getDateExpired();
                
            case COLUMN_RATIO:
                return new Integer(card.getPassRatio());
                
            default:
                return "-"; // this should never be reached //$NON-NLS-1$
        }
    }
    
    private void columnSelectionChanged()
    {
        ArrayList<Integer> columns = new ArrayList<Integer>();
        for (int i = 0; i < m_columns.length; i++)
        {
            columns.add(m_columns[i]);
        }
        
        for (int i = 0; i < COLUMN_NAMES.length; i++)
        {
            if (m_checkBoxItems[i].isSelected() && !columns.contains(i))
            {
                columns.add(Math.min(i, columns.size()), i);
            }
            else if (!m_checkBoxItems[i].isSelected() && columns.contains(i))
            {
                columns.remove(new Integer(i));
            }
        }
        
        int[] columnArray = new int[columns.size()];
        int idx = 0;
        for (int i : columns)
        {
            columnArray[idx++] = i;
        }
        
        setColumns(columnArray);
    }
    
    private void headerClicked(MouseEvent e)
    {
        if (SwingUtilities.isLeftMouseButton(e))
        {
            JTableHeader h = (JTableHeader)e.getSource();            
            TableColumnModel columnModel = h.getColumnModel();            
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());            
            int column = columnModel.getColumn(viewColumn).getModelIndex();
            
            if (column != -1)
            {
                // if column already sorting invert sort direction
                if (column == m_tableModel.getSortingColumn())
                {
                    int dir = m_tableModel.getSortingDir();
                    m_tableModel.setSorting(column, dir == ViewModel.ASCENDING ? 
                        ViewModel.DESCENDING : ViewModel.ASCENDING);
                }
                // if new sorting column selected
                else
                {
                    m_tableModel.setSorting(column, ViewModel.ASCENDING);
                }
                
                h.repaint();
            }
        }
        else if (SwingUtilities.isRightMouseButton(e))
        {
            // first set all checkbox menu items to not selected..
            for (int i = 0; i < m_checkBoxItems.length; i++)
            {
                m_checkBoxItems[i].setSelected(false);
            }
            // ..now we only select those given as new columns
            for (int i = 0; i < m_columns.length; i++)
            {
                m_checkBoxItems[m_columns[i]].setSelected(true);
            }
            
            m_headerMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    /**
     * Load table state from disk.
     */
    private void loadFromPreferences()
    {
        // we need to check the result because there might be stored values
        // from older project versions that might have become invalid.
        
        m_columnWidths = PreferencesTool.getIntArray(m_prefs, PREF_WIDTHS);
        if (m_columnWidths == null || m_columnWidths.length != COLUMN_NAMES.length)
        {
            m_columnWidths = new int[COLUMN_NAMES.length];
        }
        
        // set columns
        int[] columns = PreferencesTool.getIntArray(m_prefs, PREF_COLUMNS);
        if (columns == null || columns.length <= 0 || columns.length >= COLUMN_NAMES.length)
        {
            setColumns(m_defaultColumns);
        }
        else
        {
            setColumns(columns);
        }
        
        // set sorting
        int sortBy  = m_prefs.getInt(PREF_SORT, COLUMN_EXPIRES);
        int sortDir = m_prefs.getInt(PREF_SORT_DIR, ViewModel.ASCENDING);
        if (sortBy < 0 || sortBy > COLUMN_NAMES.length - 1)
        {
            m_tableModel.setSorting(COLUMN_EXPIRES, ViewModel.ASCENDING);
        }
        else
        {
            m_tableModel.setSorting(sortBy, sortDir);
        }
    }
    
    private void updateSelectedCardCountStatusBar()
    {
        if (m_statusBar != null)
        {
            if (getSelectedRowCount() >= 2)
            {
                m_statusBar.setLeftText(
                    Localization.get("CardTable.SELECTED_CARDS")+ ": " + //$NON-NLS-1$ //$NON-NLS-2$ 
                    getSelectedRowCount());
            }
            else
            {
                m_statusBar.setLeftText(""); //$NON-NLS-1$
            }
        }
    }
    
    private void updateCardCountStatusBar()
    {
        if (m_statusBar != null)
        {
            m_statusBar.setCards(m_tableModel.getCards());
        }
    }
    
    private void buildHeaderMenu()
    {
        for (int i = 0; i < COLUMN_NAMES.length; i++)
        {
            m_checkBoxItems[i] = new JCheckBoxMenuItem(COLUMN_NAMES[i]);
            m_checkBoxItems[i].addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e)
                {
                    columnSelectionChanged();
                }
            });
            m_headerMenu.add(m_checkBoxItems[i]);
        }
    }

    private JPopupMenu buildCardContextMenu()
    {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new LearnAction(this));
        menu.addSeparator();
        
        menu.add(new AddCardAction(this));
        menu.add(new EditCardAction(this));
        menu.add(new ResetCardAction(this));
        menu.add(new RemoveAction(this));       
        menu.addSeparator();
        
        menu.add(new CopyAction(this));
        menu.add(new CutAction(this));
        menu.add(new PasteAction(this));
        
        return menu;
    }
}
