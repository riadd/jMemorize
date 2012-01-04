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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import jmemorize.core.FormattedText;
import jmemorize.core.Main;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.CardFont;
import jmemorize.gui.swing.ColorConstants;

/**
 * @author djemili
 */
public class CardSidePanel extends JPanel
{
    public interface CardImageObserver
    {
        public void onImageChanged();
    }
    
    private class ScaledImagePanel extends JPanel
    {
        private Image     m_image;
        private int       m_padding = 2;

        public void setImageToDisplay(Image imageToDisplay)
        {
            m_image = imageToDisplay;
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            
            if (m_image == null)
                return;
            
            int imgWidth = m_image.getWidth(null);
            int imgHeight = m_image.getHeight(null);
            
            Dimension dimension = getSize();
            int w = dimension.width;
            int h = dimension.height;
            int padding = 0;
            
            if (imgWidth > w || imgHeight > h)
            {
                float ratio = imgWidth / (float)w;
                h = (int)(imgHeight / ratio);
                
                if (h > dimension.height)
                {
                    h = dimension.height;
                    ratio = imgHeight / (float)h;
                    w = (int)(imgWidth/ ratio);
                }
                
                padding = m_padding;
            }
            else
            {
                w = imgWidth;
                h = imgHeight;
            }
            
            int left = padding + (dimension.width  - w) / 2;
            int top  = padding + (dimension.height - h) / 2;
            
            if (g instanceof Graphics2D)
            {
                Graphics2D g2d = (Graphics2D)g;
                
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
            
            g.drawImage(m_image, 
                left, top, left + w - 2*padding, top + h - 2*padding, 
                0, 0, imgWidth, imgHeight, null);
        }
    }
    
    private class MyEditorKit extends StyledEditorKit
    {
        public ViewFactory getViewFactory()
        {
            return new StyledViewFactory();
        }

        // TODO make static
        class StyledViewFactory implements ViewFactory
        {
            /* (non-Javadoc)
             * @see javax.swing.text.ViewFactory
             */
            public View create(Element elem)
            {
                String kind = elem.getName();

                if (kind != null)
                {
                    if (kind.equals(AbstractDocument.ContentElementName))
                    {
                        return new LabelView(elem);
                    }
                    else if (kind.equals(AbstractDocument.ParagraphElementName))
                    {
                        return new ParagraphView(elem);
                    }
                    else if (kind.equals(AbstractDocument.SectionElementName))
                    {
                        return new CenteredBoxView(elem, View.Y_AXIS);
                    }
                    else if (kind.equals(StyleConstants.ComponentElementName))
                    {
                        return new ComponentView(elem);
                    }
                    else if (kind.equals(StyleConstants.IconElementName))
                    {
                        return new IconView(elem);
                    }
                }

                return new LabelView(elem); // default to text display
            }
        }
    }
 
    private class CenteredBoxView extends BoxView
    {
        public CenteredBoxView(Element elem, int axis)
        {
            super(elem, axis);
        }
 
        /* (non-Javadoc)
         * @see javax.swing.text.BoxView
         */
        protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans)
        {
            super.layoutMajorAxis(targetSpan, axis, offsets, spans);
 
            int textBlockHeight = 0;
            int offset = 0;
 
            for (int i = 0; i < spans.length; i++)
            {
                textBlockHeight += spans[ i ];
            }
 
            offset = (targetSpan - textBlockHeight) / 2;
 
            for (int i = 0; i < offsets.length; i++)
            {
                offsets[ i ] += offset;
            }
        }
    }
    
    private class SetImageModeAction implements ActionListener
    {
        private Mode m_mode;
        
        public SetImageModeAction(Mode mode)
        {
            m_mode = mode;
        }

        public void actionPerformed(ActionEvent e)
        {
            setImageMode(m_mode);
        }
    }
    
    private enum Mode {TEXT, IMAGE, TEXT_AND_IMAGE};
    
    private JPanel                  m_contentPanel;
    private JToolBar                m_imageBar;
    private JLabel                  m_imageLabel;

    private JTextPane               m_textPane       = new JTextPane();
    private JScrollPane             m_textScrollPane = new JScrollPane(m_textPane);
    private ScaledImagePanel        m_imagePanel     = new ScaledImagePanel();

    private List<ImageIcon>         m_images         = new LinkedList<ImageIcon>();
    private int                     m_currentImage   = 0;
    private Mode                    m_mode;
    private List<CardImageObserver> m_imageObservers = new LinkedList<CardImageObserver>();
    private CardFont                m_cardFont;

    private JButton                 m_prevImageButton;
    private JButton                 m_nextImageButton;

    private JButton                 m_textModeButton;
    private JButton                 m_imageModeButton;
    private JButton                 m_imageTexModeButton;
    
    
    public CardSidePanel()
    {
        initComponents();
        setupTabBehavior();
        setupShiftBavior();
        updateImage();
        
        setImageMode(Mode.TEXT);
    }
    
    /**
     * @return The text inside of the Frontside textpane.
     */
    public FormattedText getText()
    {
        return FormattedText.formatted(m_textPane.getStyledDocument()); 
    }
    
    public void setEditable(boolean editable)
    {
        m_textPane.setEditable(editable);
    }
    
    public void requestFocus()
    {
        m_textPane.requestFocus();
    }
    
    public void setCardFont(CardFont cardFont)
    {
        m_cardFont = cardFont;
        m_textPane.setFont(cardFont.getFont());
        
        FormattedText fText = getText();
        m_textPane.setEditorKit(cardFont.isVerticallyCentered() ?
            new MyEditorKit() : new StyledEditorKit()); // HACK
        setText(fText);
        
        StyledDocument doc = (StyledDocument)m_textPane.getDocument();
        setDocAlignment(doc, cardFont);
    }
    
    /**
     * Sets the text of one EditorPane. Using EditorPane#setText caused some
     * weird rendering artifacts. This methods fixes this by completly replacing
     * the document by a new one.
     */
    public Document setText(FormattedText text)
    {
        StyledDocument doc = text.getDocument();
        m_textPane.setDocument(doc);
        
        setDocAlignment(doc, m_cardFont);
        
        clearInputAttributes(m_textPane);
        
        // scroll to top
        m_textPane.scrollRectToVisible(new Rectangle());
        
        return doc;
    }
    
    public void setImages(List<ImageIcon> images)
    {
        m_images.clear();
        
        for (ImageIcon image : images)
        {
            m_images.add(image);
        }
        
        m_currentImage = 0;
        updateImage();
        
        if (images.size() > 0) // HACK
        {
            if (m_mode != Mode.TEXT_AND_IMAGE && m_mode != Mode.IMAGE)
                setImageMode(Mode.TEXT_AND_IMAGE);
        }
        else
        {
            setImageMode(Mode.TEXT);
        }
    }
    
    public void addImage(ImageIcon image)
    {
        m_images.add(image);
        m_currentImage = m_images.size() - 1;
        
        updateImage();
        
        if (m_images.size() == 1)
            setImageMode(Mode.TEXT_AND_IMAGE);
        
        notifyImageObservers();
    }
    
    /**
     * Removes the currently visible image.
     */
    public void removeImage()
    {
        if (m_images.size() == 0)
            return;
        
        m_images.remove(m_currentImage);
        
        if (m_currentImage > 0)
            m_currentImage--;
        
        updateImage();
        notifyImageObservers();
    }
    
    /**
     * @return a unmodifiable list of the images added to this card side.
     */
    public List<ImageIcon> getImages()
    {
        return Collections.unmodifiableList(m_images);
    }
    
    public void addCaretListener(CaretListener listener)
    {
        m_textPane.addCaretListener(listener);
        
        /*
         * Our problem is that the TextPane inserts new CaretListeners at the
         * first position. Because we add our text actions after the editor kit
         * has already been implictly set, they will get fired before the
         * AttributeTracker in StyledEditorKit (which also listens as caret
         * listener) has the chance to update the input attributes which our
         * text actions rely on. By resetting the editor kit, the editor kit
         * will be removed from the caret listeners and reinserted at the first
         * position, so that our text actions can correctly access the current
         * input attributes.
         */
        m_textPane.setEditorKit(m_textPane.getEditorKit());
    }
    
    public void addImageListener(CardImageObserver listener)
    {
        if (!m_imageObservers.contains(listener))
            m_imageObservers.add(listener);
    }
    
    public JTextPane getTextPane()
    {
        return m_textPane;
    }
    
    private void notifyImageObservers()
    {
        for (CardImageObserver observer : m_imageObservers)
            observer.onImageChanged();
    }
    
    private void setImageMode(Mode mode)
    {
        m_mode = mode;
        
        m_textModeButton.setSelected(mode == Mode.TEXT);
        m_imageModeButton.setSelected(mode == Mode.IMAGE);
        m_imageTexModeButton.setSelected(mode == Mode.TEXT_AND_IMAGE);
        
        m_contentPanel.removeAll();
        
//        JScrollPane textScrollPane = new JScrollPane(m_textPane);
//        textScrollPane.setBorder(null);
        
        switch (mode)
        {
        case TEXT:
            m_contentPanel.setLayout(new BorderLayout());
            m_contentPanel.add(m_textScrollPane, BorderLayout.CENTER);
            m_textPane.requestFocus();
            break;
            
        case IMAGE:
            m_contentPanel.setLayout(new BorderLayout());
            m_contentPanel.add(m_imagePanel, BorderLayout.CENTER);
            break;
            
        case TEXT_AND_IMAGE:
            m_contentPanel.setLayout(new GridLayout(1, 2));
            m_contentPanel.add(m_textScrollPane, BorderLayout.CENTER);
            m_contentPanel.add(m_imagePanel, BorderLayout.EAST);
            m_textPane.requestFocus();
            break;
        }
        
//        Document doc = m_textPane.getDocument();
//        m_textPane.setDocument(new DefaultStyledDocument());
//        m_textPane.setDocument(doc);
//        
        m_imagePanel.validate();
        m_textPane.validate();
        
        m_contentPanel.validate();
        m_contentPanel.repaint();
    }

    private void clearInputAttributes(JEditorPane editorPane)
    {
        StyledEditorKit kit = (StyledEditorKit)editorPane.getEditorKit();
        MutableAttributeSet attr = kit.getInputAttributes();
        attr.removeAttributes(attr.getAttributeNames());
    }
    
    private void setDocAlignment(StyledDocument doc, CardFont cardFont)
    {
        int swingAlign = StyleConstants.ALIGN_LEFT;
        if (cardFont != null)
            swingAlign = cardFont.getSwingAlign();
        
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setAlignment(sas, swingAlign);
        doc.setParagraphAttributes(0, doc.getLength() + 1, sas, false);
    }

    private void setupTabBehavior()
    {
        // focus next pane with TAB instead of CTRL+TAB
        Set<KeyStroke> key = new HashSet<KeyStroke>();
        key.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));

        int forwardTraversal = KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS;
        m_textPane.setFocusTraversalKeys(forwardTraversal, key);

        // focus previous pane with SHIFT+TAB instead of SHIFT+CTRL+TAB
        key = new HashSet<KeyStroke>();
        key.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK));

        int backwardTraversal = KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS;
        m_textPane.setFocusTraversalKeys(backwardTraversal, key);

        int shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        KeyStroke ctrlTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, shortcutKey);
        // insert tab with CTRL+TAB instead of TAB
        m_textPane.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlTab,
            DefaultEditorKit.insertTabAction);
    }
    
    private void setupShiftBavior()
    {
        int shift = InputEvent.SHIFT_DOWN_MASK;
        
        InputMap inputMap = m_textPane.getInputMap(JComponent.WHEN_FOCUSED);

        KeyStroke shiftDel = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, shift);
        inputMap.put(shiftDel, DefaultEditorKit.deleteNextCharAction);
        
        KeyStroke shiftBS = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, shift);
        inputMap.put(shiftBS, DefaultEditorKit.deletePrevCharAction);
    }
    
    private void updateImage()
    {
        int imgCount = m_images.size();
        
        m_imageBar.setVisible(imgCount > 0);
        
        if (imgCount == 0)
        {
            setImageMode(Mode.TEXT);
        }
        else
        {
            String text = String.format("   %s %d/%d ", //$NON-NLS-1$
                Localization.get(LC.IMAGE), m_currentImage + 1, imgCount);
            
            m_imageLabel.setText(text);
            m_imagePanel.setImageToDisplay(m_images.get(m_currentImage).getImage());
            m_imagePanel.repaint();
        }
    }
    
    private void initComponents()
    {
        buildImageBar();
        
        m_textPane.setBackground(ColorConstants.CARD_PANEL_COLOR);
        
        m_textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        m_textScrollPane.setBorder(null);
        
        m_contentPanel = new JPanel(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(m_contentPanel, BorderLayout.CENTER);
        mainPanel.add(m_imageBar, BorderLayout.SOUTH);
        
        m_imagePanel.setBackground(m_textPane.getBackground());
        m_imagePanel.setForeground(m_textPane.getForeground());
        
        m_imagePanel.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                    m_nextImageButton.doClick();
            }
        });
        
        // we want to use the default scrollpane border
        Color color = UIManager.getColor("InternalFrame.borderShadow"); //$NON-NLS-1$
        
        if (color == null)
        {
            color = new Color(167, 166, 170);
            Main.getLogger().warning("UI key for card side border not found!"); //$NON-NLS-1$
        }
        
        Border border = new LineBorder(color);
        mainPanel.setBorder(border);
        
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private void buildImageBar()
    {
        m_imageBar = new JToolBar();
        m_imageBar.setBackground(ColorConstants.SIDEBAR_COLOR);
        m_imageBar.setFloatable(false);
        
        m_imageLabel = new JLabel();
        m_imageLabel.setHorizontalAlignment(StyleConstants.ALIGN_LEFT);
        m_imageBar.add(m_imageLabel);
        
        m_prevImageButton = new JButton(loadIcon("arrow_left.png")); //$NON-NLS-1$
        m_prevImageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (m_currentImage > 0)
                    m_currentImage--;
                else
                    m_currentImage = m_images.size() - 1;
                
                updateImage();
                m_textPane.requestFocus();
            }
        });
        m_imageBar.add(m_prevImageButton);
        
        m_nextImageButton = new JButton(loadIcon("arrow_right.png")); //$NON-NLS-1$
        m_nextImageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (m_currentImage < m_images.size() - 1)
                    m_currentImage++;
                else
                    m_currentImage = 0;
                    
                updateImage();
                m_textPane.requestFocus();
            }
        });
        m_imageBar.add(m_nextImageButton);
        
        m_imageBar.addSeparator();
        
        m_imageTexModeButton = new JButton(loadIcon("picture_and_text.png")); //$NON-NLS-1$
        m_imageTexModeButton.addActionListener(new SetImageModeAction(Mode.TEXT_AND_IMAGE));
        m_imageBar.add(m_imageTexModeButton);
        
        m_textModeButton = new JButton(loadIcon("text.png")); //$NON-NLS-1$
        m_imageBar.add(m_textModeButton);
        m_textModeButton.addActionListener(new SetImageModeAction(Mode.TEXT));
        
        m_imageModeButton = new JButton(loadIcon("picture.png")); //$NON-NLS-1$
        m_imageBar.add(m_imageModeButton);
        m_imageModeButton.addActionListener(new SetImageModeAction(Mode.IMAGE));
    }
    
    private ImageIcon loadIcon(String imgName)
    {
        String path = "/resource/icons/"+imgName; //$NON-NLS-1$
        return new ImageIcon(getClass().getResource(path));
    }
}
