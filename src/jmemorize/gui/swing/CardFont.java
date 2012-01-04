package jmemorize.gui.swing;

import java.awt.Font;

import javax.swing.text.StyleConstants;

public class CardFont
{
    public enum FontAlignment {LEFT, CENTER, RIGHT};
    public enum FontType {CARD_FRONT, CARD_FLIP, TABLE_FRONT, 
        TABLE_FLIP, LEARN_FRONT, LEARN_FLIP};
    
    private Font          m_font;
    private FontAlignment m_alignment;
    private boolean       m_isVerticallyCentered;
    
    public CardFont(Font font, FontAlignment alignment, boolean verticallyCentered)
    {
        m_font = font;
        m_alignment = alignment;
        m_isVerticallyCentered = verticallyCentered;
    }

    public Font getFont()
    {
        return m_font;
    }

    public void setFont(Font font)
    {
        m_font = font;
    }

    public FontAlignment getAlignment()
    {
        return m_alignment;
    }

    public void setAlignment(FontAlignment alignment)
    {
        m_alignment = alignment;
    }

    public boolean isVerticallyCentered()
    {
        return m_isVerticallyCentered;
    }

    public void setVerticallyCentered(boolean isVerticallyCentered)
    {
        m_isVerticallyCentered = isVerticallyCentered;
    }
    
    public int getSwingAlign()
    {
        switch (m_alignment)
        {
            case CENTER: return StyleConstants.ALIGN_CENTER;
            case RIGHT: return StyleConstants.ALIGN_RIGHT;
            case LEFT: 
            default: return StyleConstants.ALIGN_LEFT;
        }
    }
}
