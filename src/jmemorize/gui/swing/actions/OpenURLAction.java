package jmemorize.gui.swing.actions;

import java.awt.event.ActionEvent;

import jmemorize.util.BareBonesBrowserLaunch;

/**
 * An action that opens given URL.
 * 
 * @author djemili
 */
public class OpenURLAction extends AbstractAction2
{
    private final String m_name;
    private final String m_url;

    public OpenURLAction(String name, String url)
    {
        m_name = name;
        m_url = url;
        
        setValues();
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(ActionEvent e)
    {
        BareBonesBrowserLaunch.openURL(m_url);
    }

    private void setValues()
    {
        setName(m_name);
        setDescription(m_url);
    }
}
