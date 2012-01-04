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

import javax.swing.JPanel;

import jmemorize.core.CardSide;

public interface Quiz
{
//    /**
//     * @return <code>true</code> if the visual representation of the quiz
//     * panel should be shown during the question-part. False otherwise.
//     */
    public void showQuestion(CardSide answerCardSide);
    
    /**
     * @return a number denoting if the user passed the card or not.
     */
    public float showAnswer();
    
    /**
     * @return the panel reponsible for showing answer/question panel.
     */
    public JPanel getVisual();
    
    public void setQuestionFont(CardFont font);
    public void setAnswerFont(CardFont font);
    
    /**
     * @return the text that is shown while learning a card of this type.
     */
    public String getHelpText();
}
