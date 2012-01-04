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
package jmemorize.gui.swing.panels;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import jmemorize.core.CardSide;
import jmemorize.core.ImageRepository;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.CardFont;
import jmemorize.gui.swing.Quiz;

public class ThinkQuiz implements Quiz
{
    private CardSidePanel     m_answerPanel = new CardSidePanel();
    
    private CardSide          m_answerCardSide;

    public ThinkQuiz()
    {
        m_answerPanel.setEditable(false);
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.Quiz
     */
    public void showQuestion(CardSide answerCardSide)
    {
        m_answerCardSide = answerCardSide;
        
        m_answerPanel.setText(m_answerCardSide.getText());
        
        ImageRepository repo = ImageRepository.getInstance();
        List<ImageIcon> images = repo.toImageIcons(m_answerCardSide.getImages());
        m_answerPanel.setImages(images);
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.Quiz
     */
    public float showAnswer()
    {
        return -1f;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.Quiz
     */
    public JPanel getVisual()
    {
        return m_answerPanel;
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.Quiz
     */
    public void setQuestionFont(CardFont questionFont)
    {
    }
    
    /* (non-Javadoc)
     * @see jmemorize.gui.swing.Quiz
     */
    public void setAnswerFont(CardFont answerFont)
    {
        m_answerPanel.setCardFont(answerFont);
    }

    /* (non-Javadoc)
     * @see jmemorize.gui.swing.Quiz
     */
    public String getHelpText()
    {
        return Localization.get(LC.LEARN_REMEMBER);
    }
}
