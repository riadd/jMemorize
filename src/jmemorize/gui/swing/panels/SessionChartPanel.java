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

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import jmemorize.core.Main;
import jmemorize.core.learn.LearnHistory;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnHistory.SessionSummary;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.ColorConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

/**
 * A pie chart that displays a summary of a learn sesssion.
 * 
 * @author djemili
 */
public class SessionChartPanel extends JPanel
{
    public SessionChartPanel(LearnSession finishedSession)
    {
        initComponents(finishedSession);
    }

    private void initComponents(LearnSession finishedSession)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        LearnHistory history = Main.getInstance().getLesson().getLearnHistory();
        SessionSummary lastSummary = history.getLastSummary();
        
        add(createChartPanel(Localization.get(LC.CHART_THIS_SESSION), lastSummary));
        add(createChartPanel(Localization.get(LC.CHART_AVERAGE_SESSION), history.getAverage()));
        
        setPreferredSize(new Dimension(500, 460));
    }
    
    private JPanel createChartPanel(String title, SessionSummary summary)
    {
        ChartPanel chartPanel = new ChartPanel(createChart(title, summary));
        
        chartPanel.setMinimumDrawHeight(100);
        chartPanel.setMinimumDrawWidth(200);
        
        chartPanel.setMaximumDrawHeight(1600);
        chartPanel.setMaximumDrawWidth(1200);
        
        return chartPanel;
    }
    
    private JFreeChart createChart(String title, SessionSummary summary) 
    {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue(Localization.get(LC.LEARNED) , (int)summary.getPassed());
        dataset.setValue(Localization.get(LC.FAILED), (int)summary.getFailed());
        dataset.setValue(Localization.get(LC.SKIPPED), (int)summary.getSkipped());
        dataset.setValue(Localization.get(LC.RELEARNED), (int)summary.getRelearned());
        
        JFreeChart chart = ChartFactory.createPieChart3D(
            title, dataset, true, true, false);
        
        PiePlot plot = (PiePlot)chart.getPlot();
        plot.setForegroundAlpha(0.5f);
        plot.setIgnoreZeroValues(true);
        
        plot.setLabelFont(plot.getLabelFont().deriveFont(11f));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{1} ({2})"));
        
        plot.setSectionPaint(Localization.get(LC.LEARNED), ColorConstants.LEARNED_CARDS);
        plot.setSectionPaint(Localization.get(LC.FAILED), ColorConstants.EXPIRED_CARDS);
        plot.setSectionPaint(Localization.get(LC.SKIPPED), ColorConstants.UNLEARNED_CARDS);
        plot.setSectionPaint(Localization.get(LC.RELEARNED), ColorConstants.RELEARNED_CARDS);
        
        return chart;        
    }
}
