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
package jmemorize.gui.swing;

import java.awt.Color;

/**
 * A simple container for all color constants used.
 * 
 * @author djemili
 */
public interface ColorConstants
{
    public final static Color UNLEARNED_CARDS       = Color.GRAY;
    public final static Color EXPIRED_CARDS         = Color.RED;
    public final static Color LEARNED_CARDS         = new Color(0, 235, 0);
    public final static Color RELEARNED_CARDS       = Color.ORANGE;
    public final static Color PARTIAL_LEARNED_CARDS = LEARNED_CARDS.brighter();

    public final static Color SELECTION_COLOR       = new Color(0, 80, 107);
    public static final Color SIDEBAR_COLOR         = new Color(225, 230, 235);

    public static final Color CARD_SIDE_BAR_COLOR   = new Color(255, 240, 200);
    static final Color        CARD_PANEL_COLOR      = Color.WHITE;
}
