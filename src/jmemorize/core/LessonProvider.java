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
import java.io.IOException;

import jmemorize.util.RecentItems;

/**
 * A lesson provider is responsible for handling loading and saving of lessons.
 * You should always the methods of this class to load and save lessons, instead
 * of using methods directly on the lesson objects.
 * 
 * Note that there can always only be one lesson that is loaded. Loading another
 * lesson will close the previous lesson, before loading the new one.
 * 
 * Currently {@link Main} implements this interface.
 * 
 * @author djemili
 */
public interface LessonProvider
{
    /**
     * Creates a new empty lesson.
     * 
     * This method fires a {@link LessonObserver#lessonClosed(Lesson)} if there
     * was an lesson loaded. This methods also fire
     * {@link LessonObserver#lessonLoaded(Lesson)} after having created the new
     * lesson.
     */
    public abstract void createNewLesson();
    
    /**
     * Sets a new lesson.
     * 
     * @param lesson the lesson that should be used from now on.
     */
    public abstract void setLesson(Lesson lesson);

    /**
     * Loads a lesson.
     * 
     * This method fires a {@link LessonObserver#lessonClosed(Lesson)} if there
     * was an lesson loaded. This methods also fire
     * {@link LessonObserver#lessonLoaded(Lesson)} after having loaded the new
     * lesson.
     * 
     * @param file Can't be null. Use setLesson(new Lesson()) if you want to set
     * a empty lesson.
     */
    public abstract void loadLesson(File file) throws IOException;

    /**
     * Saves the lesson.
     * 
     * This method fires a {@link LessonObserver#lessonSaved(Lesson)} after
     * having saved the lesson.
     * 
     * @param lesson the lesson that is to be saved. To save the currently
     * opened lesson, use
     * <code>saveLesson(Main.getInstance().getLesson(), file)</code>.
     * @param file the file in which the lesson should be saved.
     * @throws IOException exception that is thrown when lesson couldn't be
     * saved.
     */
    public abstract void saveLesson(Lesson lesson, File file) throws IOException;

    /**
     * @return currently loaded lesson. There can always only be at most one
     * lesson that is loaded. Returns <code>null</code> if there is no
     * currently loaded lesson.
     */
    public abstract Lesson getLesson();

    /**
     * @return a list of all recently saved/loaded lesson files.
     */
    public abstract RecentItems getRecentLessonFiles();

    /**
     * Adds the lesson observer.
     * 
     * @param observer the new observer.
     */
    public abstract void addLessonObserver(LessonObserver observer);

    /**
     * Removes the lesson observer.
     * 
     * @param observer the observer that is to be removed.
     */
    public abstract void removeLessonObserver(LessonObserver observer);

}