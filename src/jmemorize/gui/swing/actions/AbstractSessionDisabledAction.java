package jmemorize.gui.swing.actions;

import jmemorize.core.Main;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSessionObserver;

/**
 * An abstract action which observes the system for session starts and ends and
 * calls {@link #updateEnablement()} if this happens. See
 * {@link #updateEnablement()} to see what this implementation does. Overwrite
 * to redefine.
 * 
 * @author djemili
 */
public abstract class AbstractSessionDisabledAction extends AbstractAction2 
    implements LearnSessionObserver
{
    public AbstractSessionDisabledAction()
    {
        Main.getInstance().addLearnSessionObserver(this);
        updateEnablement();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LearnSessionObserver#sessionStarted(jmemorize.core.LearnSession)
     */
    public void sessionStarted(LearnSession session)
    {
        updateEnablement();
    }
    
    /* (non-Javadoc)
     * @see jmemorize.core.LearnSessionObserver#sessionEnded(jmemorize.core.LearnSession)
     */
    public void sessionEnded(LearnSession session)
    {
        updateEnablement();
    }

    /**
     * This method is called when a session starts or ends. The implementation
     * only enables the action when there is currently no session running.
     * Overwrite if you want to define your own behavior.
     */
    protected void updateEnablement()
    {
        setEnabled(!Main.getInstance().isSessionRunning());
    }
}
