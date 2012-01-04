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

/**
 * A interface for observing the life cycle of an lesson. Register it at the
 * {@link LessonProvider}.
 * 
 * @author djemili
 */
public interface LessonObserver
{
    /**
     * Is fired when a new lesson loaded. This includes opening existing
     * lesson, but also creating new ones.
     * 
     * @param lesson the newly loaded lesson.
     */
    public void lessonLoaded(Lesson lesson);
    
    public void lessonModified(Lesson lesson);
    
    public void lessonSaved(Lesson lesson);
    
    /**
     * Is fired when a lesson is closed (e.g. by creating a new lesson, loading
     * an existing lesson)
     * 
     * @param lesson the closed lesson.
     */
    public void lessonClosed(Lesson lesson);
}
