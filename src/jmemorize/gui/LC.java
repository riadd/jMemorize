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
package jmemorize.gui;

/**
 * Contains localization constants. Use these constants over using the string
 * keys directly. New keys should be added with JDoc since annotation.
 * 
 * @author djemili
 */
public interface LC
{
    // -- General --------

    public static final String FRONTSIDE                              = "General.FRONTSIDE";
    public static final String FLIPSIDE                               = "General.FLIPSIDE";
    public static final String DECK                                   = "General.DECK";
    public static final String CREATED                                = "General.CREATED";
    /** @since 1.2.0 */
    public static final String MODIFIED                               = "General.MODIFIED";
    public static final String EXPIRES                                = "General.EXPIRES";
    public static final String LAST_TEST                              = "General.LAST_TEST";
    public static final String PASSED                                 = "General.PASSED";
    public static final String CATEGORY                               = "General.CATEGORY";
    public static final String ROOT_CATEGORY                          = "General.ROOT_CATEGORY";
    
    /** @since 1.3.0 */
    public static final String IMAGE                                  = "General.IMAGE";

    /**
     * singular
     * @since 1.2.3
     */
    public static final String UNLEARNED                              = "General.UNLEARNED";
    /**
     * singular
     * @since 1.2.3
     */
    public static final String EXPIRED                                = "General.EXPIRED";

    public static final String GENERAL                                = "General.GENERAL";
    public static final String OKAY                                   = "General.OKAY";
    public static final String APPLY                                  = "General.APPLY";
    public static final String CANCEL                                 = "General.CANCEL";
    
    public static final String EMPTY_SIDES_ALERT                      = "General.EMPTY_SIDES_ALERT";
    public static final String EMPTY_SIDES_ALERT_TITLE                = "General.EMPTY_SIDES_ALERT_TITLE";
    
    // -- Summary --------

    public static final String LEARNED                                = "Summary.LEARNED";
    public static final String FAILED                                 = "Summary.FAILED";
    public static final String RELEARNED                              = "Summary.RELEARNED";
    public static final String SKIPPED                                = "Summary.SKIPPED";
    
    // -- File -------
    
    public static final String FILE_CSV                               = "File.CSV";
    
    // -- NewCard --------
    
    public static final String NEW_CARD_TITLE                         = "NewCard.TITLE";
    
    public static final String NEW_CARD_NEW_WINDOW                    = "NewCard.NEW_WINDOW";
    public static final String NEW_CARD_NEW_WINDOW_DESC               = "NewCard.NEW_WINDOW_DESC";
    
    public static final String NEW_CARD_EDIT_RECENTLY                 = "NewCard.EDIT_RECENTLY";
    public static final String NEW_CARD_EDIT_RECENTLY_DESC            = "NewCard.EDIT_RECENTLY_DESC";
    
    public static final String NEW_CARD_DISMISS_WARN                  = "NewCardManager.DISMISSED_WARN";
    public static final String NEW_CARD_CLOSE_WARN                    = "NewCard.CLOSE_WARN";

    // -- StatusBar --------

    // TODO where is the difference to Summary.LEARNED!?
    /** plural */
    public static final String STATUS_LEARNED                         = "StatusBar.LEARNED";
    // TODO where is the difference to Summary.UNLEARNED!?
    /** plural */
    public static final String STATUS_UNLEARNED                       = "StatusBar.UNLEARNED";
    // TODO where is the difference to Summary.EXPIRED!?
    /** plural */
    public static final String STATUS_EXPIRED                         = "StatusBar.EXPIRED";
    // TODO where is the difference to DeckChart.CARDS.!?
    public static final String STATUS_CARDS                           = "StatusBar.CARDS";
    public static final String STATUS_PARTIAL                         = "StatusBar.PARTIAL";
    public static final String STATUS_LEARNING_CATEGORY               = "StatusBar.LEARNING_CATEGORY";
    public static final String STATUS_CARDS_LEFT                      = "StatusBar.CARDS_LEFT";

    // -- Preferences -------

    public static final String PREFERENCES_RESTART                    = "Preferences.RESTART";
    public static final String PREFERENCES_LANG                       = "Preferences.LANGUAGE";
    public static final String PREFERENCES_PREVIEW                    = "Preferences.PREVIEW";
    public static final String PREFERENCES_FONT_SETTINGS              = "Preferences.FONT_SETTINGS";
    public static final String PREFERENCES_FONT                       = "Preferences.FONT";
    public static final String PREFERENCES_SIZE                       = "Preferences.SIZE";
    public static final String PREFERENCES_USE_GZIP                   = "Preferences.USE_GZIP";
    /** since 1.3.0 */
    public static final String PREFERENCES_VERT_ALIGN                 = "Preferences.VERT_ALIGN";
    /** since 1.3.0 */
    public static final String PREFERENCES_ALIGN                      = "Preferences.ALIGNMENT";
    /** since 1.3.0 */
    public static final String ALIGN_LEFT                             = "Preferences.ALIGN_LEFT";
    /** since 1.3.0 */
    public static final String ALIGN_CENTER                           = "Preferences.ALIGN_CENTER";
    /** since 1.3.0 */
    public static final String ALIGN_RIGHT                            = "Preferences.ALIGN_RIGHT";

    // -- Learn --------

    public static final String LEARN_SHOW                             = "Learn.SHOW_ANSWER";
    public static final String LEARN_SKIP                             = "Learn.SKIP_CARD";
    public static final String LEARN_YES                              = "Learn.YES";
    public static final String LEARN_NO                               = "Learn.NO";
    public static final String LEARN_STOP                             = "Learn.STOP";
    public static final String LEARN_SHOW_CATEGORY                    = "Learn.SHOW_CATEGORY";
    public static final String LEARN_FLIPPED                          = "Learn.FLIPPED_CARD_SIDES";
    public static final String LEARN_REMEMBER                         = "Learn.REMEMBER_THE_ANSWER";
    public static final String LEARN_DID_YOU_KNOW                     = "Learn.DID_YOU_KNOW_ANSWER";
    public static final String LEARN_TIMER                            = "Learn.TIMER";
    public static final String LEARN_CARD_COUNTER                     = "Learn.CARD_COUNTER";
    /** @since 1.2.0 */
    public static final String LEARN_CARD                             = "Learn.CARD_PROGRESS";
    /** @since 1.2.0 */
    public static final String LEARN_SESSION                          = "Learn.SESSION_PROGRESS";

    // -- Learn Settings --------

    public static final String LEARN_SETTINGS_TITLE                   = "LearnSettings.TITLE";
    public static final String LEARN_SETTINGS_START                   = "LearnSettings.START";
    public static final String LEARN_SETTINGS_ADVANCED                = "LearnSettings.ADVANCED";
    public static final String LEARN_SETTINGS_SCHEDULING              = "LearnSettings.SCHEDULING";

    public static final String LEARN_SETTINGS_LEARN_UNLEARNED_EXPIRED = "LearnSettings.LEARN_UNLEARNED_EXPIRED";
    public static final String LEARN_SETTINGS_LEARN_UNLEARNED         = "LearnSettings.LEARN_UNLEARNED";
    public static final String LEARN_SETTINGS_LEARN_EXPIRED           = "LearnSettings.LEARN_EXPIRED";
    /** @since 1.3.0 */
    public static final String LEARN_SETTINGS_LEARN_SELECTED          = "LearnSettings.LEARN_SELECTED";
    
    public static final String LEARN_SETTINGS_DELAY                   = "LearnSettings.DELAY_AFTER_LEVEL";
    public static final String LEARN_SETTINGS_DAYS                    = "LearnSettings.DAYS";
    public static final String LEARN_SETTINGS_HOURS                   = "LearnSettings.HOURS";
    public static final String LEARN_SETTINGS_MINUTES                 = "LearnSettings.MINUTES";
    public static final String LEARN_SETTINGS_PRESETS                 = "LearnSettings.SCHEDULE_PRESETS";
    
    public static final String LEARN_SETTINGS_DELIMITERS              = "LearnSettings.DELIMITERS";
    public static final String LEARN_SETTINGS_TIME_LIMIT              = "LearnSettings.TIME_LIMIT";
    public static final String LEARN_SETTINGS_CARD_LIMIT              = "LearnSettings.CARD_LIMIT";
    
    public static final String LEARN_SETTINGS_TIME_LIMIT_TEXT         = "LearnSettings.TIME_LIMIT_TEXT";
    public static final String LEARN_SETTINGS_CARD_LIMIT_TEXT         = "LearnSettings.CARD_LIMIT_TEXT";
    public static final String LEARN_SETTINGS_DONT_RETEST             = "LearnSettings.DONT_RETEST_FAILED";
    
    public static final String LEARN_SETTINGS_MODE_BOTH               = "LearnSettings.BOTH_MODE";
    public static final String LEARN_SETTINGS_MODE_RANDOM             = "LearnSettings.RANDOM_MODE";
    public static final String LEARN_SETTINGS_MODE_FLIP               = "LearnSettings.FLIP_MODE";
    public static final String LEARN_SETTINGS_MODE_NORMAL             = "LearnSettings.NORMAL_MODE";

    public static final String LEARN_SETTINGS_RANDOM_CATEGORY_ORDER   = "LearnSettings.RANDOM_CATEGORY_ORDER";
    public static final String LEARN_SETTINGS_NATURAL_CATEGORY_ORDER  = "LearnSettings.NATURAL_CATEGORY_ORDER";
    public static final String LEARN_SETTINGS_GROUP_CARDS             = "LearnSettings.GROUP_CARDS";
    
    public static final String LEARN_SETTINGS_CARDS_ORDER             = "LearnSettings.CARDS_ORDER";
    /** @since 1.3.0 */
    public static final String LEARN_SETTINGS_SHUFFLE_RATIO           = "LearnSettings.SHUFFLE_RATIO";
    public static final String LEARN_SETTINGS_SHUFFLE                 = "LearnSettings.SHUFFLE_CARDS";
    
    /** @since 1.3.0 */
    public static final String LEARN_SETTINGS_FIXED_EXPIRATION_TIME   = "LearnSettings.FIXED_EXPIRATION_TIME";

    // -- FindTool --------

    public static final String MATCH_CASE                             = "FindTool.MATCH_CASE";
    public static final String BOTH_SIDES                             = "FindTool.BOTH_SIDES";

    // -- MainFrame --------

    public static final String MAINFRAME_ABOUT                        = "MainFrame.ABOUT";
    public static final String ERROR_LOAD                             = "MainFrame.ERROR_LOAD";
    public static final String ERROR_SAVE                             = "MainFrame.ERROR_SAVE";

    public static final String ACTION_ADD_CATEGORY                    = "MainFrame.ADD_CATEGORY";
    public static final String ACTION_ADD_CATEGORY_DESC               = "MainFrame.ADD_CATEGORY_DESC";
    public static final String ACTION_ADD_CATEGORY_INPUT              = "MainFrame.ADD_CATEGORY_INPUT";

    public static final String RESET                                  = "MainFrame.RESET";
    public static final String RESET_DESC                             = "MainFrame.RESET_DESC";
    public static final String RESET_WARN                             = "MainFrame.RESET_WARN";

    public static final String LEARN                                  = "MainFrame.LEARN";
    public static final String LEARN_DESC                             = "MainFrame.LEARN_DESC";
    
    public static final String RENAME                                 = "MainFrame.RENAME";
    public static final String RENAME_INPUT                           = "MainFrame.RENAME_INPUT";

    public static final String MAINFRAME_PREFERENCES                  = "MainFrame.PREFERENCES";
    
    public static final String CUT                                    = "MainFrame.CUT";
    public static final String COPY                                   = "MainFrame.COPY";
    public static final String PASTE                                  = "MainFrame.PASTE";
    
    public static final String FILE_FILTER_DESC                       = "MainFrame.FILE_FILTER_DESC";
    
    public static final String EXPORT_CLEAN                           = "MainFrame.EXPORT_CLEAN";
    public static final String EXPORT_CLEAN_DESC                      = "MainFrame.EXPORT_CLEAN_DESC";

    // -- About --------

    public static final String ABOUT_INFO                             = "About.INFO";
    public static final String ABOUT_LICENSE                          = "About.LICENSE";
    public static final String ABOUT_PROPERTIES                       = "About.PROPERTIES";
    public static final String ABOUT_PREFERENCES                      = "About.PREFERENCES";

    // -- CardTable --------

    public static final String CARDTABLE_PATH                         = "CardTable.PATH";

    // -- EditCard --------

    public static final String RATIO                                  = "EditCard.DETAILS_RATIO";
    
    public static final String NEXT_CARD                              = "EditCard.NEXT_CARD";
    public static final String NEXT_CARD_DESC                         = "EditCard.NEXT_CARD_DESC";
    
    public static final String PREV_CARD                              = "EditCard.PREV_CARD";
    public static final String PREV_CARD_DESC                         = "EditCard.PREV_CARD_DESC";
    
    // -- DeckChart --------

    public static final String CHART_CARDS                            = "DeckChart.CARDS";
    
    // -- SessionChart -------
    
    /** @since 1.3.0 */
    public static final String CHART_THIS_SESSION                     = "SessionChart.THIS_SESSION";
    /** @since 1.3.0 */
    public static final String CHART_AVERAGE_SESSION                  = "SessionChart.AVERAGE_SESSION";

    // -- History --------

    /** @since 1.2.3 */
    public static final String HISTORY_TITLE                          = "History.TITLE";
    /** @since 1.2.3 */
    public static final String HISTORY_ACTION                         = "History.ACTION";
    /** @since 1.2.0 */
    public static final String HISTORY_RECENT                         = "History.RECENT";
    /** @since 1.2.0 */
    public static final String HISTORY_BY_DATE                        = "History.BY_DATE";
    /** @since 1.2.0 */
    public static final String HISTORY_BY_WEEK                        = "History.BY_WEEK";
    /** @since 1.2.0 */
    public static final String HISTORY_BY_MONTH                       = "History.BY_MONTH";
    /** @since 1.2.0 */
    public static final String HISTORY_BY_YEAR                        = "History.BY_YEAR";
    /** @since 1.2.0 */
    public static final String HISTORY_DURATION                       = "History.SESSION_DURATION";
    
    // -- Schedule ------
    
    public static final String SCHEDULE_CONST                         = "Strategy.CONSTANT_SCHEDULE";
    public static final String SCHEDULE_CUSTOM                        = "Strategy.CUSTOM_SCHEDULE";
    public static final String SCHEDULE_CRAM                          = "Strategy.CRAM_SCHEDULE";
    public static final String SCHEDULE_EXPONENTIAL                   = "Strategy.EXPONENTIAL_SCHEDULE";
    public static final String SCHEDULE_QUAD                          = "Strategy.QUADRATIC_SCHEDULE";
    public static final String SCHEDULE_LINEAR                        = "Strategy.LINEAR_DEFAULT_SCHEDULE";
}
