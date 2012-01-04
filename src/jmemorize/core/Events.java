/*
 * jMemorize - Learning made easy (and fun) - A Leitner flashcards tool
 * Copyright(C) 2004-2006 Riad Djemili
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
 * @see CategoryObserver
 * @author djemili
 */
public interface Events // TODO move to category class
{
    public static final int ADDED_EVENT   = 0;
    public static final int REMOVED_EVENT = 1;
    public static final int MOVED_EVENT   = 2;
    public static final int DECK_EVENT    = 3;
    public static final int EDITED_EVENT  = 4;
    public static final int EXPIRED_EVENT = 5;
}
