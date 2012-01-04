package jmemorize.gui.swing;

import java.util.Date;

import javax.swing.ImageIcon;

import jmemorize.core.Main;

/**
 * Manages the card status icons.
 * 
 * @author djemili
 */
public class CardStatusIcons
{
    private final ImageIcon OK_ICON      = new ImageIcon(
        getClass().getResource("/resource/icons/state_ok.gif"));   //$NON-NLS-1$
    private final ImageIcon TODAY_ICON   = new ImageIcon(
        getClass().getResource("/resource/icons/state_soon.gif")); //$NON-NLS-1$
    private final ImageIcon NO_ICON      = new ImageIcon(
        getClass().getResource("/resource/icons/state_no.gif"));   //$NON-NLS-1$
    private final ImageIcon EXPIRED_ICON = new ImageIcon(
        getClass().getResource("/resource/icons/state_forgotten.gif")); //$NON-NLS-1$
    private static CardStatusIcons m_instance;
    
    /**
     * @return the singleton instance.
     */
    public static CardStatusIcons getInstance()
    {
        if (m_instance == null)
        {
            m_instance = new CardStatusIcons();
        }
        
        return m_instance;
    }
    
    public ImageIcon getCardIcon(Date expiration)
    {
        // if not learned
        if (expiration == null)
        {
            return NO_ICON;
        }
        else
        {
            // if tomorrow still valid
            if (expiration.after(Main.getTomorrow()))
            {
                return OK_ICON;
            }
            // if only valid day left is today
            else if (expiration.after(Main.getNow()))
            {
                return TODAY_ICON;
            }
            // if expired
            else
            {
                return EXPIRED_ICON;
            }
        }
    }
    
    private CardStatusIcons()
    {
        // singleton
    }
}
