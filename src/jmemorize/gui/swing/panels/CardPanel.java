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
package jmemorize.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.StyledEditorKit.StyledTextAction;

import jmemorize.core.Settings;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.GeneralTransferHandler;
import jmemorize.gui.swing.ColorConstants;
import jmemorize.gui.swing.actions.file.AbstractImportAction;
import jmemorize.gui.swing.panels.CardSidePanel.CardImageObserver;
import jmemorize.gui.swing.widgets.CategoryComboBox;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

/**
 * A panel that displays the front and flip side of a card.
 * 
 * @author djemili
 */
public class CardPanel extends JPanel 
{
    /**
     * A interface that allows to listen for textchanges to the card side text
     * panes. Use {@link CardPanel#addTextObserver} method to hook it to
     * the CardPanel.
     */
    public interface CardPanelObserver
    {
        public void onTextChanged();
        public void onImageChanged();
    }
    
    private class InsertImageAction extends StyledTextAction
    {
        public InsertImageAction()
        {
            super("img");
        }

        public void actionPerformed(java.awt.event.ActionEvent e) 
        {
            JEditorPane editor = getEditor(e);
            if (editor != null && editor instanceof JTextPane)
            {
                for (CardSidePanel cardSidePanel : m_cardSides)
                {
                    if (cardSidePanel.getTextPane() != editor)
                        continue;
                    
                    JFileChooser chooser = new JFileChooser();
                    chooser.setCurrentDirectory(Settings.loadLastDirectory());
                    File file = AbstractImportAction.showOpenDialog(null, null);

                    if (file == null)
                        return;
                    
                    ImageIcon icon = new ImageIcon(file.toString());
                    icon.setDescription(file.toString());
                    
                    cardSidePanel.addImage(icon);
                    editor.requestFocus();
                }
            }
        }        
    }
    
    private class RemoveImageAction extends StyledTextAction
    {
        public RemoveImageAction()
        {
            super("img-remove");
        }

        public void actionPerformed(ActionEvent e)
        {
            JEditorPane editor = getEditor(e);
            if (editor != null && editor instanceof JTextPane)
            {
                for (CardSidePanel cardSidePanel : m_cardSides)
                {
                    if (cardSidePanel.getTextPane() != editor)
                        continue;
                    
                    cardSidePanel.removeImage();
                    editor.requestFocus();
                }
            }
        }
    }
    
    private abstract class AbstractStyledTextAction extends StyledTextAction
    {
        private AbstractButton  m_button;
        private List<KeyStroke> m_shortcuts = new ArrayList<KeyStroke>();
        private CaretListener   m_caretListener;
        
        public AbstractStyledTextAction(String nm)
        {
            super(nm);
            
            m_textActions.add(this);
            
            m_caretListener = new CaretListener(){
                public void caretUpdate(CaretEvent e)
                {
                    if (!(e.getSource() instanceof JTextPane))
                        return;

                    JTextPane editor = (JTextPane)e.getSource();
                    updateButton(editor);
                }
            };
        }
        
        public void addShortcut(KeyStroke shortcut)
        {
            m_shortcuts.add(shortcut);
        }
        
        public void actionPerformed(ActionEvent e)
        {
            JEditorPane editor = getEditor(e);
            if (editor != null)
            {
                editor.requestFocus();
                
                StyledEditorKit kit = getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                
                SimpleAttributeSet sas = new SimpleAttributeSet();
                setStyle(sas, !hasStyle(attr));
                
                setCharacterAttributes(editor, sas, false);
                notifyTextObservers();
                
                updateButton(editor);
            }
        }
        
        public void setButton(AbstractButton button)
        {
            m_button = button;
        }
        
        public void attachTextPane(CardSidePanel cardSide)
        {
            String name = (String)getValue(Action.NAME);
            JTextPane textPane = cardSide.getTextPane();
            
            for (KeyStroke shortcut : m_shortcuts)
            {
                textPane.getInputMap().put(shortcut, name);
            }
            
            textPane.getActionMap().put(name, this);
            
            cardSide.addCaretListener(m_caretListener);
        }
        
        /**
         * @return <code>true</code> if the style associated with this text
         * action is enabled. <code>false</code> otherwise.
         */
        public abstract boolean hasStyle(AttributeSet attr);
        
        /**
         * Enables/disables the style associated with this text action in the
         * given attributes.
         */
        public abstract void setStyle(MutableAttributeSet attr, boolean enabled);
        
        private void updateButton(JEditorPane editor)
        {
            StyledEditorKit kit = (StyledEditorKit)editor.getEditorKit();
            MutableAttributeSet attr = kit.getInputAttributes();
            
            m_button.setSelected(hasStyle(attr));
        }
    }
    
    private class BoldAction extends AbstractStyledTextAction
    {
        public BoldAction()
        {
            super("font-bold");
            addShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_B, CTRL_MASK));
        }

        public boolean hasStyle(AttributeSet attr)
        {
            return StyleConstants.isBold(attr);
        }

        public void setStyle(MutableAttributeSet attr, boolean enabled)
        {
            StyleConstants.setBold(attr, enabled);
        }
    }
    
    private class ItalicAction extends AbstractStyledTextAction
    {
        public ItalicAction()
        {
            super("font-italic");
            addShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_I, CTRL_MASK));
        }

        public boolean hasStyle(AttributeSet attr)
        {
            return StyleConstants.isItalic(attr);
        }

        public void setStyle(MutableAttributeSet attr, boolean enabled)
        {
            StyleConstants.setItalic(attr, enabled);
        }
    }
    
    private class UnderlineAction extends AbstractStyledTextAction
    {
        public UnderlineAction()
        {
            super("font-underline");
            addShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_U, CTRL_MASK));
        }

        public boolean hasStyle(AttributeSet attr)
        {
            return StyleConstants.isUnderline(attr);
        }

        public void setStyle(MutableAttributeSet attr, boolean enabled)
        {
            StyleConstants.setUnderline(attr, enabled);
        }
    }
    
    private class SupAction extends AbstractStyledTextAction
    {
        public SupAction()
        {
            super("sup");
            addShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 
                CTRL_MASK | InputEvent.SHIFT_DOWN_MASK));
            addShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 
                CTRL_MASK | InputEvent.SHIFT_DOWN_MASK));
        }

        public boolean hasStyle(AttributeSet attr)
        {
            return StyleConstants.isSuperscript(attr);
        }

        public void setStyle(MutableAttributeSet attr, boolean enabled)
        {
            StyleConstants.setSuperscript(attr, enabled);
            StyleConstants.setSubscript(attr, false);
        }
    }
    
    private class SubAction extends AbstractStyledTextAction
    {
        public SubAction()
        {
            super("sub");
            addShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, CTRL_MASK));
        }

        public boolean hasStyle(AttributeSet attr)
        {
            return StyleConstants.isSubscript(attr);
        }

        public void setStyle(MutableAttributeSet attr, boolean enabled)
        {
            StyleConstants.setSubscript(attr, enabled);
            StyleConstants.setSuperscript(attr, false);
        }
    }
    
    private class ShowCardSideButton extends JButton implements ActionListener
    {
        private String m_text;
        private int[]  m_sides;
        
        public ShowCardSideButton(String text, int ... sides)
        {
            m_text = text;
            m_sides = sides;
            m_showSideButtons.add(this);
            
            setBackground(ColorConstants.CARD_SIDE_BAR_COLOR);
            addActionListener(this);
            
//            Character character = new Character(Integer.toString(index).charAt(0));
//            String actionName = "show-card-side-action-"+index;
//            getInputMap().put(KeyStroke.getKeyStroke(character, InputEvent.CTRL_MASK), actionName);
//            getActionMap().put(actionName, this);
        }
        
        public void actionPerformed(ActionEvent e)
        {
            for (int i = 0; i < m_cardSidesPanel.getComponentCount(); i++)
                setCardSideVisible(i, hasSide(i));
            
            updateCardSideButtons();
        }

        public boolean hasSide(int index)
        {
            for (int i = 0; i < m_sides.length; i++)
            {
                if (m_sides[i] == index)
                    return true;
            }
            
            return false;
        }
        
        private void updateText()
        {
            boolean highlight = true;
            for (int i = 0; i < m_cardSidesPanel.getComponentCount(); i++)
                highlight &= hasSide(i) == isCardSideVisible(i);
            
            String name = highlight ? "["+m_text+"]" : m_text;
            setText("  " + name + "  ");
            
//            setFont(getFont().deriveFont(highlight ? Font.ITALIC : Font.PLAIN));
////            setBackground(highlight ? new Color(255, 180, 0) : ColorConstants.CARD_SIDE_BAR_COLOR);
        }
    }
    
    private static final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    protected boolean                      m_flippedCardSides = false;
    private boolean                        m_verticalLayout   = true;
    
    private CategoryComboBox               m_categoryBox      = new CategoryComboBox();
    
    private List<CardPanelObserver>        m_observers        = new LinkedList<CardPanelObserver>();
    private List<CardSidePanel>            m_cardSides        = new LinkedList<CardSidePanel>();
    private List<AbstractStyledTextAction> m_textActions      = new LinkedList<AbstractStyledTextAction>();
    private List<ShowCardSideButton>       m_showSideButtons  = new LinkedList<ShowCardSideButton>();
    
    private JPanel                         m_cardSidesPanel;
    private JPopupMenu                     m_popupMenu;
    private MouseAdapter                   m_menuAdapter;
    
    private CardImageObserver              m_imageObserver;

    /**
     * Creates new form EditCardPanel
     */
    public CardPanel(boolean allowEdits)
    {
        initComponent(allowEdits);
        updateCardSideButtons();
        
        m_popupMenu = buildPopupMenu(allowEdits);
        
        m_menuAdapter = new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    JTextPane textPane = (JTextPane)e.getSource();
                    textPane.requestFocus();
                    
                    m_popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        
        m_imageObserver = new CardImageObserver()
        {
            public void onImageChanged()
            {
                notifyImageObservers();
            }
        };
    }
    
    public void addCardSide(String title, JComponent component)
    {
        JPanel cardSideWithTitle = wrapCardSide(title, component);
        
        if (component instanceof CardSidePanel)
        {
            CardSidePanel cardSide = (CardSidePanel)component;
            
            m_cardSides.add(cardSide);
            
            for (AbstractStyledTextAction textAction : m_textActions)
            {
                textAction.attachTextPane(cardSide);
            }
            
            cardSide.getTextPane().addMouseListener(m_menuAdapter);
            GeneralTransferHandler handler = new GeneralTransferHandler(cardSide);
            cardSide.getTextPane().setTransferHandler(handler);
            
            cardSide.addImageListener(m_imageObserver);
        }
        
        m_cardSidesPanel.add(cardSideWithTitle);
        
        updateCardSideButtons();
        updateCardSideBorders();
    }
    
    public void removeCardSide(int index)
    {
        m_cardSidesPanel.getComponent(index);
        
        m_cardSidesPanel.remove(index);
        
        // TODO
    }
    
    public void setCardSideVisible(int index, boolean visible)
    {
        m_cardSidesPanel.getComponent(index).setVisible(visible);
        
        updateCardSideBorders();
        updateCardSideButtons();
    }
    
    public void setCardSideEnabled(int index, boolean enabled)
    {
        for (ShowCardSideButton button : m_showSideButtons)
        {
            if (button.hasSide(index))
                button.setEnabled(enabled);
        }
        
//        m_showSideButtons.get(index).setEnabled(enabled);
    }
    
    public boolean isCardSideVisible(int index)
    {
        if (index >= m_cardSidesPanel.getComponentCount())
            return false;
        
        return m_cardSidesPanel.getComponent(index).isVisible();
    }

    /**
     * @param editable <code>true</code> if front/back side textpanes should
     * be editable. <code>false</code> otherwise.
     */
    public void setEditable(boolean editable)
    {
        for (CardSidePanel cardSide : m_cardSides)
        {
            cardSide.setEditable(editable);
        }
    }

    public List<CardSidePanel> getCardSides()
    {
        return Collections.unmodifiableList(m_cardSides);
    }

    public CategoryComboBox getCategoryComboBox()
    {
        return m_categoryBox;
    }

    /**
     * Adds a text observer that will be triggered when the text of the
     * frontside textpane or backside textpane is changed by the users key
     * input.
     * 
     * @param observer The text observer that is to be added as observer.
     */
    public void addObserver(CardPanelObserver observer)
    {
        m_observers.add(observer);
    }
    
    /**
     * Notify all observers that the text of the frontside textpane or backside
     * textpane has been changed by the users keyinput.
     */
    protected void notifyTextObservers()
    {
        for (CardPanelObserver observer : m_observers)
        {
            observer.onTextChanged();
        }
    }
    
    private void notifyImageObservers()
    {
        for (CardPanelObserver observer : m_observers)
        {
            observer.onImageChanged();
        }
    }
    
    private void updateCardSideButtons()
    {
        for (ShowCardSideButton action : m_showSideButtons)
            action.updateText();
    }

    private void updateCardSideBorders()
    {
        int margin = Sizes.dialogUnitYAsPixel(3, this);
        
        int mx = 0;
        int my = 0;
        
        if (m_verticalLayout)
            my = margin;
        else
            mx = margin;
        
        boolean addBorder = false;
        for (int i = 0; i < m_cardSidesPanel.getComponentCount(); i++)
        {
            Component comp = m_cardSidesPanel.getComponent(i);
            
            if (!(comp instanceof JPanel))
                continue;
            
            JPanel sidePanel = (JPanel)comp;
            
            if (addBorder)
                sidePanel.setBorder(new EmptyBorder(my, mx, 0, 0));
            else
                sidePanel.setBorder(null);
            
            if (sidePanel.isVisible())
                addBorder = true;
        }
        
//        if (m_cardSides.size() > 0)
//        {
//            int px = Sizes.dialogUnitYAsPixel(3, this); 
//            cardSideWithTitle.setBorder(new EmptyBorder(px, 0, 0, 0));
//        }
    }

    private JPanel wrapCardSide(String title, JComponent cardSide)
    {
        FormLayout layout = new FormLayout(
//            "38dlu, 3dlu, d:grow", // columns //$NON-NLS-1$
            "d:grow", // columns //$NON-NLS-1$
            "fill:20dlu:grow"); // rows //$NON-NLS-1$
    
        CellConstraints cc = new CellConstraints();
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        
//        builder.addLabel(title, cc.xy(1, 1, "left, top")); //$NON-NLS-1$
//        builder.add(cardSide, cc.xy(3, 1 ));
        builder.add(cardSide, cc.xy(1, 1 ));
        
        return builder.getPanel();
    }
    
    private void initComponent(boolean allowEdits)
    {
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        if (allowEdits)
            topPanel.add(buildCategoryPanel());
        
        topPanel.add(buildInnerPanel(buildSetSidesToolbar()));
        
        if (allowEdits)
            topPanel.add(buildInnerPanel(buildEditToolbar()));
        
        add(topPanel, BorderLayout.NORTH);
        
        m_cardSidesPanel = new JPanel();
        m_cardSidesPanel.setLayout(new BoxLayout(m_cardSidesPanel, BoxLayout.Y_AXIS));
        add(m_cardSidesPanel, BorderLayout.CENTER);
    }
    
    private JToolBar buildSetSidesToolbar()
    {
        JToolBar toolBar = new JToolBar();
        
        toolBar.add(new ShowCardSideButton("Frontside/Flipside", 0, 1));
        toolBar.add(new ShowCardSideButton("Frontside", 0));
        toolBar.add(new ShowCardSideButton("Flipside", 1));
        
        toolBar.setBorder(new EtchedBorder());
        toolBar.setBackground(ColorConstants.CARD_SIDE_BAR_COLOR);
        
        toolBar.setFloatable(false);
        return toolBar;
    }

    private JToolBar buildEditToolbar()
    {
        JToolBar toolBar = new JToolBar();
        
        toolBar.add(createButton(new DefaultEditorKit.CopyAction(), "edit_copy.gif"));
        toolBar.add(createButton(new DefaultEditorKit.CutAction(), "edit_cut.gif"));
        toolBar.add(createButton(new DefaultEditorKit.PasteAction(), "edit_paste.gif"));
        toolBar.addSeparator();
        
        toolBar.add(createButton(new BoldAction(), "text_bold.png"));
        toolBar.add(createButton(new ItalicAction(), "text_italic.png"));
        toolBar.add(createButton(new UnderlineAction(), "text_underline.png"));
        toolBar.add(createButton(new SupAction(), "text_superscript.png"));
        toolBar.add(createButton(new SubAction(), "text_subscript.png"));
        
        toolBar.addSeparator();
        toolBar.add(createButton(new InsertImageAction(), "picture_add.png"));
        toolBar.add(createButton(new RemoveImageAction(), "picture_delete.png"));
        
        toolBar.setFloatable(false);
        return toolBar;
    }
    
    private JPopupMenu buildPopupMenu(boolean editable)
    {
        JPopupMenu menu = new JPopupMenu();
        menu.add(createMenuItem(new DefaultEditorKit.CopyAction(), 
            Localization.get(LC.COPY), "edit_copy.gif"));
        
        if (editable)
        {
            menu.add(createMenuItem(new DefaultEditorKit.CutAction(), 
                Localization.get(LC.CUT), "edit_cut.gif"));
            
            menu.add(createMenuItem(new DefaultEditorKit.PasteAction(), 
                Localization.get(LC.PASTE), "edit_paste.gif"));
            
            menu.addSeparator();
            
            // TODO add localization
            menu.add(createMenuItem(new BoldAction(), "Bold", "text_bold.png"));
            menu.add(createMenuItem(new ItalicAction(), "Italic", "text_italic.png"));
            menu.add(createMenuItem(new UnderlineAction(), "Underline", "text_underline.png"));
            menu.add(createMenuItem(new SupAction(), "Superscript", "text_superscript.png"));
            menu.add(createMenuItem(new SubAction(), "Subscript", "text_subscript.png"));
        }
        
        return menu;
    }
    
    private JButton createButton(AbstractStyledTextAction action, String icon)
    {
        JButton button = new JButton(action);
        action.setButton(button);
        
        button.setText("");
        button.setIcon(new ImageIcon(getClass().getResource("/resource/icons/"+icon)));
        return button;
    }
    
    private JMenuItem createMenuItem(Action action, String text, String icon)
    {
        JMenuItem item = new JMenuItem(action);
        
        if (action instanceof AbstractStyledTextAction)
            ((AbstractStyledTextAction)action).setButton(item);
        
        item.setIcon(new ImageIcon(getClass().getResource("/resource/icons/"+icon)));
        item.setText(text);
        return item;
    }
    
    private JPanel buildCategoryPanel()
    {
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder;
        
        FormLayout layout = new FormLayout(
//            "38dlu, 3dlu, d:grow", // columns //$NON-NLS-1$
            "d:grow", // columns //$NON-NLS-1$
            "p, 3dlu"); // rows //$NON-NLS-1$

        builder = new DefaultFormBuilder(layout);
//        builder.addLabel(Localization.get(LC.CATEGORY), cc.xy ( 1, 1));
//        builder.add(m_categoryBox, cc.xy(3, 1));
        builder.add(m_categoryBox, cc.xy(1, 1));
        
        return builder.getPanel();
    }
    
    private JPanel buildInnerPanel(Component comp)
    {
        CellConstraints cc = new CellConstraints();
        
        DefaultFormBuilder builder;
        FormLayout layout = new FormLayout(
//            "38dlu, 3dlu, d:grow", // columns //$NON-NLS-1$
            "d:grow", // columns //$NON-NLS-1$
            "p, 3dlu"); // rows //$NON-NLS-1$

        builder = new DefaultFormBuilder(layout);
//        builder.add(comp, cc.xy (3, 1));
        builder.add(comp, cc.xy (1, 1));
        
        return builder.getPanel();
    }
    
    private JButton createButton(Action action, String icon)
    {
        JButton button = new JButton(action);
        button.setText("");
        button.setIcon(new ImageIcon(getClass().getResource("/resource/icons/"+icon)));
        return button;
    }
}