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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import jmemorize.core.learn.LearnSettings;
import jmemorize.core.learn.LearnSettings.SchedulePreset;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.CardFont;
import jmemorize.gui.swing.CardFont.FontAlignment;
import jmemorize.gui.swing.CardFont.FontType;
import jmemorize.gui.swing.frames.MainFrame;
import jmemorize.util.PreferencesTool;

/**
 * Stores all jMemorize settings by using a preferences node. Use storeXXX to
 * save settings and loadXXX to load them again.
 * 
 * @author djemili
 */
public class Settings
{
    /**
     * Listens for changes to the currently set card font which can be set by
     * using the preferences window.
     */
    public interface CardFontObserver
    {
        public void fontChanged(FontType type, CardFont font);
    }
    
    // TODO move all user prefs here
    public final static Preferences PREFS = Main.USER_PREFS;
    
    // observers
    private static List<CardFontObserver> m_cardFontObservers = 
        new LinkedList<CardFontObserver>();
    
    // locale keys
    private final static String LOCALE_LANG = "locale-lang";       //$NON-NLS-1$
    private final static String LOCALE_COUNTRY = "locale-country"; //$NON-NLS-1$

    // font keys
    private final static String FONT_FRONT_KEY = "front"; //$NON-NLS-1$
    private final static String FONT_FLIP_KEY = "flip"; //$NON-NLS-1$
    private final static String FONT_LEARN_FRONT_KEY = "learn-front"; //$NON-NLS-1$
    private final static String FONT_LEARN_FLIP_KEY = "learn-flip"; //$NON-NLS-1$
    private final static String FONT_TABLE_FRONT_KEY = "table-front"; //$NON-NLS-1$
    private final static String FONT_TABLE_FLIP_KEY = "table-flip"; //$NON-NLS-1$

    // strategy keys
    private final static String LIMIT_CARDS_ENABLED = "limit.cards.enabled"; //$NON-NLS-1$
    private final static String LIMIT_TIME_ENABLED = "limit.time.enabled"; //$NON-NLS-1$
    private final static String LIMIT_CARDS = "limit.cards"; //$NON-NLS-1$
    private final static String LIMIT_TIME = "limit.time"; //$NON-NLS-1$
    private final static String RETEST_FAILED_CARDS = "retest-failed-cards";   //$NON-NLS-1$
    
    private final static String SCHEDULE_PRESET = "schedule.preset"; //$NON-NLS-1$
    private final static String SCHEDULE = "schedule.values"; //$NON-NLS-1$
    
    private final static String SCHEDULE_FIXED_EXPIRATION_ENABLED = "schedule.fixed-expiration.enabled"; //$NON-NLS-1$
    private final static String SCHEDULE_FIXED_EXPIRATION_HOUR = "schedule.fixed-expiration.hour"; //$NON-NLS-1$
    private final static String SCHEDULE_FIXED_EXPIRATION_MINUTE = "schedule.fixed-expiration.minute";  //$NON-NLS-1$
    
    private final static String SIDES = "sides";                 //$NON-NLS-1$
    private final static String SIDES_FRONT_AMOUNT = "sides-front-amount"; //$NON-NLS-1$
    private final static String SIDES_FLIP_AMOUNT = "sides-flip-amount"; //$NON-NLS-1$
    private final static String GROUP_BY_CATEGORY = "card-order.group-by-category"; //$NON-NLS-1$
    private final static String CATEGORY_ORDER = "card-order.group-by-category.order"; //$NON-NLS-1$
    private final static String SHUFFLE_CARDS = "card-order.shuffle"; //$NON-NLS-1$
    
    // gui
    private final static String FRAME_MAXIMIZED = "frame.maximized"; //$NON-NLS-1$
    private final static String FRAME_POSITION = "frame.position"; //$NON-NLS-1$
    private final static String FRAME_SIZE = "frame.size"; //$NON-NLS-1$

    // etc keys
    private final static String LAST_DIRECTORY = "last-directory"; //$NON-NLS-1$
    private final static String SAVE_COMPRESSED = "gzip";          //$NON-NLS-1$
    private final static String CATEGORY_TREE_WIDTH = "category-tree.width"; //$NON-NLS-1$
    private final static String CATEGORY_TREE_VISIBLE = "category-tree.visible"; //$NON-NLS-1$
    private final static String MAIN_DIVIDER_LOCATION = "main-divider.location"; //$NON-NLS-1$
    
    
    public static void storeLocale(Locale locale)
    {
        PREFS.put(LOCALE_LANG, locale.getLanguage());
        PREFS.put(LOCALE_COUNTRY, locale.getCountry());
    }
    
    /**
     * @return The locale stored in preferences or the default locale if no
     * locale was stored.
     */
    public static Locale loadLocale()
    {
        String lang = PREFS.get(LOCALE_LANG, null);
        if (lang == null)
        {
            return Localization.getDefaultLocale();
        }
        
        String country = PREFS.get(LOCALE_COUNTRY, null);
        return country != null ? new Locale(lang, country) : new Locale(lang);
    }
    
    public static void storeFont(FontType type, CardFont font)
    {
        String key = toFontString(type);
        
        PREFS.put(toNameKey(key), font.getFont().getFamily());
        PREFS.putInt(toSizeKey(key), font.getFont().getSize());
        PREFS.put(toAlignmentKey(key), font.getAlignment().toString());
        PREFS.putBoolean(toVerticalAlignmentKey(key), font.isVerticallyCentered());
        
        for (CardFontObserver observer : m_cardFontObservers)
        {
            observer.fontChanged(type, font);
        }
    }

    public static CardFont loadFont(FontType type)
    {
        String key = toFontString(type);
        
        String name = PREFS.get(toNameKey(key), null);
        int size = PREFS.getInt(toSizeKey(key), 12);
        String align = PREFS.get(toAlignmentKey(key), null);
        boolean valign = PREFS.getBoolean(toVerticalAlignmentKey(key), false);
        
        Font font = new Font(name, Font.PLAIN, size);
        FontAlignment alignment = FontAlignment.LEFT; 
        try
        {
            alignment = FontAlignment.valueOf(align);
        }
        catch (Exception e) 
        {
//            Main.logThrowable("failed to load font alignment.", e);
        }
        
        return new CardFont(font, alignment, valign);
    }
    
    public static void setCardFont(CardFontObserver observer, boolean flipped, 
        FontType type1, FontType type2)
    {
        CardFont frontFont = loadFont(type1);
        CardFont flipFont = loadFont(type2);
        
        observer.fontChanged(type1, !flipped ? frontFont : flipFont);
        observer.fontChanged(type2, !flipped ? flipFont : frontFont);
    }
    
    public static void addCardFontObserver(CardFontObserver observer)
    {
        m_cardFontObservers.add(observer);
    }
    
    public static void setCardFont(CardFontObserver observer, 
        FontType type1, FontType type2)
    {
        setCardFont(observer, false, type1, type2);
    }
    
    // TODO rename
    public static void removedCardFontObserver(CardFontObserver observer)
    {
        m_cardFontObservers.remove(observer);
    }
    
    public static void storeStrategy(LearnSettings strategy)
    {
        PREFS.putBoolean(LIMIT_CARDS_ENABLED, strategy.isCardLimitEnabled());
        PREFS.putInt(LIMIT_CARDS, strategy.getCardLimit());
        
        PREFS.putBoolean(LIMIT_TIME_ENABLED, strategy.isTimeLimitEnabled());
        PREFS.putInt(LIMIT_TIME, strategy.getTimeLimit());
        
        PREFS.putBoolean(RETEST_FAILED_CARDS, strategy.isRetestFailedCards());
        PREFS.putFloat(SHUFFLE_CARDS, strategy.getShuffleRatio());
        
        PREFS.putInt(SIDES, strategy.getSidesMode());
        PREFS.putInt(SIDES_FRONT_AMOUNT, strategy.getAmountToTest(true));
        PREFS.putInt(SIDES_FLIP_AMOUNT, strategy.getAmountToTest(false));
        
        PREFS.putInt(SCHEDULE_PRESET, strategy.getSchedulePreset().ordinal());
        PreferencesTool.putIntArray(PREFS, SCHEDULE, strategy.getSchedule());
        
        PREFS.putBoolean(SCHEDULE_FIXED_EXPIRATION_ENABLED, strategy.isFixedExpirationTimeEnabled());
        PREFS.putInt(SCHEDULE_FIXED_EXPIRATION_HOUR, strategy.getFixedExpirationHour());
        PREFS.putInt(SCHEDULE_FIXED_EXPIRATION_MINUTE, strategy.getFixedExpirationMinute());
        
        PREFS.putBoolean(GROUP_BY_CATEGORY, strategy.isGroupByCategory());
        PREFS.putInt(CATEGORY_ORDER, strategy.getCategoryOrder());
    }
    
    /**
     * @return A newly created strategy that got instantiated with all the
     * stored strategy data or with default values.
     */
    public static LearnSettings loadStrategy(MainFrame frame)
    {
        LearnSettings settings = new LearnSettings();
        
        settings.setCardLimitEnabled(PREFS.getBoolean(LIMIT_CARDS_ENABLED, false));
        settings.setCardLimit(PREFS.getInt(LIMIT_CARDS, 20));
        
        settings.setTimeLimitEnabled(PREFS.getBoolean(LIMIT_TIME_ENABLED, true));
        settings.setTimeLimit(PREFS.getInt(LIMIT_TIME, 20));
        
        settings.setRetestFailedCards(PREFS.getBoolean(RETEST_FAILED_CARDS, true));
        settings.setSidesMode(PREFS.getInt(SIDES, LearnSettings.SIDES_NORMAL));
        settings.setAmountToTest(true, PREFS.getInt(SIDES_FRONT_AMOUNT, 1));
        settings.setAmountToTest(false, PREFS.getInt(SIDES_FLIP_AMOUNT, 1));
        
        int preset = PREFS.getInt(SCHEDULE_PRESET, 1); //linear as default
        
        // if preconfigured schedule
        if (preset > -1 && preset < SchedulePreset.values().length - 1)
        {
            settings.setSchedulePreset(SchedulePreset.values()[preset]);
        }
        else // if custom
        {
            int[] schedule = PreferencesTool.getIntArray(PREFS, SCHEDULE);
            if (schedule != null)
                settings.setCustomSchedule(schedule);
        }
        
        settings.setFixedExpirationTimeEnabled(PREFS.getBoolean(
            SCHEDULE_FIXED_EXPIRATION_ENABLED, false));
        
        int hour = PREFS.getInt(SCHEDULE_FIXED_EXPIRATION_HOUR, 3);
        int minute = PREFS.getInt(SCHEDULE_FIXED_EXPIRATION_MINUTE, 0);
        settings.setFixedExpirationTime(hour, minute);
        
        settings.setGroupByCategory(PREFS.getBoolean(GROUP_BY_CATEGORY, true));
        settings.setCategoryOrder(PREFS.getInt(CATEGORY_ORDER, 
            LearnSettings.CATEGORY_ORDER_FIXED));
        settings.setShuffleRatio(PREFS.getFloat(SHUFFLE_CARDS, 0.3f));
        
        return settings;
    }
    
    public static void storeSaveCompressed(boolean saveCompressed)
    {
        PREFS.putBoolean(SAVE_COMPRESSED, saveCompressed);
    }
    
    /**
     * @return <code>true</code> if lesson files should be compressed with
     * GZIP when saving.
     */
    public static boolean loadIsSaveCompressed()
    {
        return PREFS.getBoolean(SAVE_COMPRESSED, true);
    }
    
    // TODO merge storeCategoryTreeWidth and storeCategoryTreeVisible
    public static void storeCategoryTreeWidth(int width)
    {
        PREFS.putInt(CATEGORY_TREE_WIDTH, width);
    }
    
    public static int loadCategoryTreeWidth()
    {
        return PREFS.getInt(CATEGORY_TREE_WIDTH, 150);
    }
    
    public static void storeCategoryTreeVisible(boolean visible)
    {
        PREFS.putBoolean(CATEGORY_TREE_VISIBLE, visible);
    }
    
    public static boolean loadCategoryTreeVisible()
    {
        return PREFS.getBoolean(CATEGORY_TREE_VISIBLE, false);
    }
    
    public static void storeMainDividerLocation(int size)
    {
        PREFS.putInt(MAIN_DIVIDER_LOCATION, size);
    }
    
    public static int loadMainDividerLocation()
    {
        return PREFS.getInt(MAIN_DIVIDER_LOCATION, 250);
    }
    
    public static void storeLastDirectory(File directory)
    {
        PREFS.put(LAST_DIRECTORY, directory.getAbsolutePath());
    }
    
    public static File loadLastDirectory()
    {
        String directory = PREFS.get(LAST_DIRECTORY, null);
        if (directory != null)
        {
            File file = new File(directory);
            if (file.exists())
                return file;
        }
        
        return null;
    }
    
    public static void loadFrameState(JFrame frame, String frameId)
    {
        if (Settings.loadIsFrameMaximized(frameId))
        {
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
        else
        {
            Point framePosition = Settings.loadFramePosition(frameId);
            if (framePosition != null)
            {
                frame.setLocation(framePosition);
            }
            else
            {
                frame.setLocationRelativeTo(null);
            }
            
            Dimension frameSize = Settings.loadFrameSize(frameId);
            if (frameSize != null)
                frame.setSize(frameSize);
        }
    }

    public static void storeFrameState(JFrame frame, String frameId)
    {
        Settings.storeIsFrameMaximized(frameId, 
            frame.getExtendedState() == Frame.MAXIMIZED_BOTH);
        
        Settings.storeFramePosition(frameId, frame.getLocation());
        Settings.storeFrameSize(frameId, frame.getSize());
    }

    /**
     * @param frameId an identifier for the frame.
     * 
     * @return <code>true</code> if the main frame should be displayed as
     * maximized at program start.
     */
    private static boolean loadIsFrameMaximized(String frameId)
    {
        return PREFS.getBoolean(frameId + '.' + FRAME_MAXIMIZED, false);
    }
    
    /**
     * @see #loadIsFrameMaximized()
     */
    private static void storeIsFrameMaximized(String frameId, boolean maximized)
    {
        PREFS.putBoolean(frameId + '.' + FRAME_MAXIMIZED, maximized);
    }
    
    /**
     * @param frameId an identifier for the frame.
     * 
     * @return the position of the main frame. <code>null</code> if no
     * position is stored.
     */
    private static Point loadFramePosition(String frameId)
    {
        String str = PREFS.get(frameId + '.' + FRAME_POSITION, null);
        if (str == null)
            return null;
        
        String[] pos = str.split(",");
        return new Point(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
    }
    
    /**
     * @see #loadFramePosition()
     */
    private static void storeFramePosition(String frameId, Point position)
    {
        PREFS.put(frameId + '.' + FRAME_POSITION, position.x + "," + position.y);
    }
    
    /**
     * @param frameId an identifier for the frame.
     * 
     * @return the size of the main frame. <code>null</code> if no size is
     * stored.
     */
    private static Dimension loadFrameSize(String frameId)
    {
        String str = PREFS.get(frameId + '.' + FRAME_SIZE, null);
        if (str == null)
            return null;
        
        String[] pos = str.split(",");
        return new Dimension(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
    }
    
    /**
     * @see #loadFrameSize()
     */
    private static void storeFrameSize(String frameId, Dimension size)
    {
        PREFS.put(frameId + '.' + FRAME_SIZE, 
            (int)size.getWidth() + "," + (int)size.getHeight());
    }
    
    private static String toSizeKey(String key)
    {
        return key + "-font-size";
    }

    private static String toNameKey(String key)
    {
        return key + "-font-name";
    }
    
    private static String toAlignmentKey(String key)
    {
        return key + "-font-alignment";
    }
    
    private static String toVerticalAlignmentKey(String key)
    {
        return key + "-font-valignment";
    }
    
    private static String toFontString(FontType type)
    {
        switch (type)
        {
        case CARD_FRONT: return FONT_FRONT_KEY;
        case CARD_FLIP: return FONT_FLIP_KEY;
        case LEARN_FRONT: return FONT_LEARN_FRONT_KEY;
        case LEARN_FLIP: return FONT_LEARN_FLIP_KEY;
        case TABLE_FRONT: return FONT_TABLE_FRONT_KEY;
        case TABLE_FLIP: return FONT_TABLE_FLIP_KEY;
            
        default:
            throw new IllegalArgumentException("Unknown font identifier");
        }
    }
}
