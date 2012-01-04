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
package jmemorize.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;

import jmemorize.core.io.XmlBuilder;
import jmemorize.core.learn.DefaultLearnSession;
import jmemorize.core.learn.LearnHistory;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSessionObserver;
import jmemorize.core.learn.LearnSessionProvider;
import jmemorize.core.learn.LearnSettings;
import jmemorize.gui.swing.frames.MainFrame;
import jmemorize.util.RecentItems;

/**
 * The main class of the application.
 * 
 * @author djemili
 */
public class Main extends Observable implements LearnSessionProvider, 
    LessonProvider, CategoryObserver
{
    public interface ProgramEndObserver
    {
        /**
         * This method is notified when the program ends. 
         */
        public void onProgramEnd();
    }
    
    public static final Properties      PROPERTIES              = new Properties();
    public static final Preferences     USER_PREFS              = 
        Preferences.userRoot().node("de/riad/jmemorize");          //$NON-NLS-1$

    private static final String         PROPERTIES_PATH         = 
        "/resource/jMemorize.properties"; //$NON-NLS-1$

    public static final File            STATS_FILE               = 
        new File(System.getProperty("user.home")+"/.jmemorize-stats.xml"); //$NON-NLS-1$ //$NON-NLS-2$

    private RecentItems                 m_recentFiles           = 
        new RecentItems(5, USER_PREFS.node("recent.files"));        //$NON-NLS-1$

    private static Main                 m_instance;                                                
    
    private MainFrame                   m_frame;
    private Lesson                      m_lesson;
    private LearnSettings               m_learnSettings;
    private LearnHistory                m_globalLearnHistory;
    private int                         m_runningSessions       = 0;

    // observers
    private List<LessonObserver>        m_lessonObservers       = 
        new LinkedList<LessonObserver>();
    private List<LearnSessionObserver>  m_learnSessionObservers = 
        new LinkedList<LearnSessionObserver>();
    private List<ProgramEndObserver>    m_programEndObservers   = 
        new LinkedList<ProgramEndObserver>();
    
    // simple logging support
    private static final Logger     logger = Logger.getLogger("jmemorize");
    private static Throwable        m_lastLoggedThrowable;
    
    /**
     * @return the singleton instance of Main.
     */
    public static Main getInstance()
    {
        if (m_instance == null)
        {
            m_instance = new Main();
        }
        
        return m_instance;
    }
    
    public static Date getNow()
    {
        return new Date();
    }
    
    public static Date getTomorrow()
    {
        return new Date(new Date().getTime() + Card.ONE_DAY);
    }
    
    /* (non-Javadoc)
     * Declared in jmemorize.core.LessonProvider
     */
    public void createNewLesson()
    {
        ImageRepository.getInstance().clear();
        setLesson(new Lesson(false));
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonProvider
     */
    public void setLesson(Lesson lesson)
    {
        Lesson oldLesson = m_lesson;
        m_lesson = lesson;
        
        if (oldLesson != null)
        {
            fireLessonClosed(oldLesson);
        }
        
        if (m_frame != null) // TODO remove call
        {
            m_frame.setLesson(m_lesson);
        }
        
        fireLessonLoaded(m_lesson);
    }

    /* (non-Javadoc)
     * Declared in jmemorize.core.LessonProvider
     */
    public void loadLesson(File file) throws IOException
    {
        try
        {
            ImageRepository.getInstance().clear();
            
            Lesson lesson = new Lesson(false);
            XmlBuilder.loadFromXMLFile(file, lesson);
            lesson.setFile(file);
            lesson.setCanSave(false);
            m_recentFiles.push(file.getAbsolutePath());
            
            setLesson(lesson);
            //startExpirationTimer(); TODO expiration timer
        } 
        catch (Exception e)
        {
            m_recentFiles.remove(file.getAbsolutePath());
            logThrowable("Error loading lesson", e);
            throw new IOException(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * Declared in jmemorize.core.LessonProvider
     */
    public void saveLesson(Lesson lesson, File file) throws IOException
    {
        try
        {
            File tempFile = new File(file.getAbsolutePath()+"~"); //$NON-NLS-1$
            XmlBuilder.saveAsXMLFile(tempFile, lesson);
            
            file.delete();
            copyFile(tempFile, file);
            
            lesson.setFile(file); // note: sets file only if no exception
            lesson.setCanSave(false);
            m_recentFiles.push(file.getAbsolutePath());
            
            for (LessonObserver observer : m_lessonObservers)
            {
                observer.lessonSaved(lesson);
            }
        }
        catch (Throwable t)
        {
            throw new IOException(t.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * Declared in jmemorize.core.LessonProvider
     */
    public Lesson getLesson()
    {
        return m_lesson;
    }

    /* (non-Javadoc)
     * Declared in jmemorize.core.LessonProvider
     */
    public RecentItems getRecentLessonFiles()
    {
        return m_recentFiles;
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonProvider
     */
    public void addLessonObserver(LessonObserver observer)
    {
        m_lessonObservers.add(observer);
    }

    /* (non-Javadoc)
     * @see jmemorize.core.LessonProvider
     */
    public void removeLessonObserver(LessonObserver observer)
    {
        m_lessonObservers.remove(observer);
    }
    
    /**
     * Adds a ProgramEndObserver that will be fired when this program closes.
     * 
     * @param observer
     */
    public void addProgramEndObserver(ProgramEndObserver observer)
    {
        m_programEndObservers.add(observer);
    }
    
    /**
     * @see #addProgramEndObserver(jmemorize.core.Main.ProgramEndObserver)
     */
    public void removeProgramEndObserver(ProgramEndObserver observer)
    {
        m_programEndObservers.remove(observer);
    }
    
    /**
     * Notifies all program end observers and exists the application.
     */
    public void exit()
    {
        for (ProgramEndObserver observer : m_programEndObservers)
        {
            observer.onProgramEnd();
        }
        
        System.exit(0);
    }

    /* (non-Javadoc)
     * Declared in jmemorize.core.LearnSessionProvider
     */
    public void startLearnSession(LearnSettings settings, List<Card> selectedCards, 
        Category category,boolean learnUnlearned, boolean learnExpired) 
    {
        LearnSession session = new DefaultLearnSession(category, settings, 
            selectedCards, learnUnlearned, learnExpired, this);
        
        m_runningSessions++;
        
        for (LearnSessionObserver observer : m_learnSessionObservers)
        {
            observer.sessionStarted(session);
        }
        
        // this needs to be called after notifying the observers so that they
        // don't miss the first card
        session.startLearning();
    }

    /* (non-Javadoc)
     * Declared in jmemorize.core.LearnSessionProvider
     */
    public void sessionEnded(LearnSession session)
    {
        m_runningSessions--;
        
        if (session.isRelevant())
        {
            LearnHistory history = m_lesson.getLearnHistory();
            history.addSummary(
                session.getStart(), 
                session.getEnd(), 
                session.getPassedCards().size(), 
                session.getFailedCards().size(),
                session.getSkippedCards().size(),
                session.getRelearnedCards().size());
        }
        
        for (LearnSessionObserver observer : m_learnSessionObservers)
        {
            observer.sessionEnded(session);
        }
    }

    /* (non-Javadoc)
     * Declared in jmemorize.core.LearnSessionProvider
     */
    public boolean isSessionRunning()
    {
        return m_runningSessions > 0;
    }

    /* (non-Javadoc)
     * Declared in jmemorize.core.LearnSessionProvider
     */
    public void addLearnSessionObserver(LearnSessionObserver observer)
    {
        m_learnSessionObservers.add(observer);
    }

    /* (non-Javadoc)
     * Declared in jmemorize.core.LearnSessionProvider
     */
    public void removeLearnSessionObserver(LearnSessionObserver observer)
    {
        m_learnSessionObservers.remove(observer);
    }

    /**
     * @return the main frame.
     */
    public MainFrame getFrame() 
    {
        return m_frame;
    }

    /**
     * @return currently loaded learn strategy.
     */
    public LearnSettings getLearnSettings()
    {
        return m_learnSettings;
    }
    
    /**
     * @return the statistics for jMemorize.
     */
    public LearnHistory getGlobalLearnHistory()
    {
        return m_globalLearnHistory;
    }

    /* (non-Javadoc)
     * Declared in jmemorize.core.CategoryObserver
     */
    public void onCardEvent(int type, Card card, Category category, int deck)
    {
        fireLessonModified(m_lesson);
    }

    /* (non-Javadoc)
     * Declared in jmemorize.core.CategoryObserver
     */
    public void onCategoryEvent(int type, Category category)
    {
        fireLessonModified(m_lesson);        
    }

    public Main()
    {
        InputStream propertyStream = null;
        
        try
        {
            // TODO - make this adjustable
            // Note that the limit might not be enough for finer.
            Handler fh = new FileHandler("%t/jmemorize%g.log", 10000, 3);
            fh.setLevel(Level.WARNING);            
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            URL resource = getClass().getResource(PROPERTIES_PATH);
            
//            PROPERTIES.load(resource.openStream());
            
            if (resource != null)
            {
                propertyStream = resource.openStream();
                PROPERTIES.load(propertyStream);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logThrowable("Initialization problem", e);
        }
        finally
        {
            try
            {
                if (propertyStream != null)
                    propertyStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                logThrowable("Initialization problem", e);
            }
        }
    }
    
    /**
     * @return <code>true</code> if this is the devel version running.
     * <code>false</code> if it is the release version. This can be used for
     * new and expiremental features.
     */
    public static boolean isDevel()
    {
        String property = PROPERTIES.getProperty("project.release"); //$NON-NLS-1$
        return !Boolean.valueOf(property).booleanValue();
    }
    
    /*
     * Logging utilities
     */
    public static Logger getLogger() 
    {
        return logger;
    }
    
    // note that we cache the throwable so that we only log it the first time.
    // This allows us to put a catch all call to this function in ErrorDialog.
    // Ideally, exceptions should be logged where they are first caught, because
    // we have more information about the exception there.
    public static void logThrowable(String msg, Throwable t) 
    {
        if (t != null && m_lastLoggedThrowable != t) 
        {
            m_lastLoggedThrowable = t;
            logger.severe(msg);
            
            // TODO, consider writing these to the log file only once?
            String java    = System.getProperty("java.version");
            String os      = System.getProperty("os.name");        
            String version = Main.PROPERTIES.getProperty("project.version");
            String buildId = Main.PROPERTIES.getProperty("buildId");
            String txt = "Ver "+ version +" ("+ buildId +") - Java "+ java +" , OS "+ os;
            logger.severe(txt);

            StringWriter strWriter = new StringWriter();
            PrintWriter prWriter = new PrintWriter(strWriter);
            t.printStackTrace(prWriter);
            logger.severe(strWriter.toString());
        }
    }

    public static void clearLastThrowable() 
    {
        m_lastLoggedThrowable = null;
    }
    
    private static void copyFile(File in, File out) throws IOException 
    {
        FileChannel sourceChannel = null;
        FileChannel destinationChannel = null;
        try
        {
            sourceChannel = new FileInputStream(in).getChannel();
            destinationChannel = new FileOutputStream(out).getChannel();
            
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        }
        finally
        {
            if (sourceChannel != null)
                sourceChannel.close();
            
            if (destinationChannel != null)
                destinationChannel.close();
        }
    }

    private void run(File file)
    {
        createNewLesson();
        startStats();
        
        m_frame = new MainFrame();
        m_learnSettings = Settings.loadStrategy(m_frame);
        m_frame.setVisible(true);
        
        if (file != null)
        {
            m_frame.loadLesson(file);
        }
    }

    private void startStats()
    {
        m_globalLearnHistory = new LearnHistory(STATS_FILE);
    }
    
    private void fireLessonLoaded(Lesson lesson)
    {
        lesson.getRootCategory().addObserver(this);
        
        for (LessonObserver observer : m_lessonObservers)
        {
            observer.lessonLoaded(lesson);
        }
    }
    
    private void fireLessonClosed(Lesson lesson)
    {
        lesson.getRootCategory().removeObserver(this);
        
        for (LessonObserver observer : m_lessonObservers)
        {
            observer.lessonClosed(lesson);
        }
    }

    private void fireLessonModified(Lesson lesson)
    {
        if (lesson.canSave())
        {
            for (LessonObserver observer : m_lessonObservers)
            {
                observer.lessonModified(lesson);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) 
    {
        File file = args.length >= 1 ? new File(args[0]) : null;
        Main.getInstance().run(file);        
    }
}
