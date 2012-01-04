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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import jmemorize.core.Main;
import jmemorize.core.learn.LearnHistory;
import jmemorize.core.learn.LearnSession;
import jmemorize.core.learn.LearnSessionObserver;
import jmemorize.core.learn.LearnHistory.CalendarComparator;
import jmemorize.core.learn.LearnHistory.SessionSummary;
import jmemorize.gui.LC;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.ColorConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;

/**
 * A panel that displays a statistical stacked bar chart which represent learn
 * session.
 * 
 * @author djemili
 */
public class HistoryChartPanel extends JPanel
{
    private static final int MAX_SESSIONS  = 15;

    private static final int SHOW_ALL      = 0;
    private static final int SHOW_BY_DATE  = 1;
    private static final int SHOW_BY_WEEK  = 2;
    private static final int SHOW_BY_MONTH = 3;
    private static final int SHOW_BY_YEAR  = 4;

    private int              m_mode        = SHOW_ALL;
    private LearnHistory     m_history;
    private JFreeChart       m_chart;
    
    
    public HistoryChartPanel(LearnHistory history)
    {
        initComponents();
        m_history = history;
        
        Main.getInstance().addLearnSessionObserver(new LearnSessionObserver() {
            public void sessionEnded(LearnSession session)
            {
                updateDataSet();
            }

            public void sessionStarted(LearnSession session)
            {
            }
        });
        
        updateDataSet();
    }

    private void initComponents()
    {
        m_chart = createChart();
        ChartPanel chartPanel = new ChartPanel(m_chart);
        
        chartPanel.setMinimumDrawHeight(100);
        chartPanel.setMinimumDrawWidth(400);
        
        chartPanel.setMaximumDrawHeight(1600);
        chartPanel.setMaximumDrawWidth(3000);
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        add(buildChartChooser(), BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }
    
    private JPanel buildChartChooser()
    {
        JComboBox comboBox = new JComboBox(new String[] {
            Localization.get(LC.HISTORY_RECENT),
            Localization.get(LC.HISTORY_BY_DATE),
            Localization.get(LC.HISTORY_BY_WEEK),
            Localization.get(LC.HISTORY_BY_MONTH),
            Localization.get(LC.HISTORY_BY_YEAR),
        });
        
        comboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                JComboBox box = (JComboBox)e.getSource();
                m_mode = box.getSelectedIndex();
                updateDataSet();
            }
        });
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(comboBox, BorderLayout.WEST);
        
        return panel;
    }
    
    private void updateDataSet()
    {
        CalendarComparator comp = null;
            
        switch (m_mode)
        {
        case SHOW_ALL:      comp = LearnHistory.SIMPLE_COMP; break;
        case SHOW_BY_DATE:  comp = LearnHistory.DATE_COMP;   break;
        case SHOW_BY_WEEK:  comp = LearnHistory.WEEK_COMP;   break;
        case SHOW_BY_MONTH: comp = LearnHistory.MONTH_COMP;  break;
        case SHOW_BY_YEAR:  comp = LearnHistory.YEAR_COMP;   break;
        }
            
        List<SessionSummary> summaries = m_history.getSummaries(comp, MAX_SESSIONS, true); // TODO make last argument optional 
        DateFormat dateFormat = comp.getFormat();
        
        CategoryPlot plot = (CategoryPlot)m_chart.getPlot();
        plot.setDataset(0, createDataSet(summaries, dateFormat));
        plot.setDataset(1, createMinutesDataSet(summaries, dateFormat));
        
        CategoryLabelPositions pos = comp.showRotated() ?
            CategoryLabelPositions.UP_45 : CategoryLabelPositions.STANDARD;
            
        plot.getDomainAxis().setCategoryLabelPositions(pos);
    }
    
    private CategoryDataset createDataSet(List<SessionSummary> summaries, 
        DateFormat format)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (SessionSummary summary : summaries)
        {
            Date start = summary.getStart();
            String date = format.format(start); 
            
            int passed = (int)summary.getPassed();
            int relearned = (int)summary.getRelearned();
            int failed = (int)summary.getFailed();
            
            dataset.setValue(failed, Localization.get(LC.FAILED), date);
            dataset.setValue(relearned, Localization.get(LC.RELEARNED), date);
            dataset.setValue(passed, Localization.get(LC.PASSED), date);
        }
        
        return dataset;
    }
    
    private CategoryDataset createMinutesDataSet(List<SessionSummary> summaries, 
        DateFormat format)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (SessionSummary summary : summaries)
        {
            String date = format.format(summary.getStart()); 
            
            dataset.setValue(summary.getDuration(), 
                Localization.get(LC.HISTORY_DURATION), date);
        }
        
        return dataset;
    }
    
    private JFreeChart createChart() 
    {
        JFreeChart chart = ChartFactory.createStackedBarChart(
            null,                     // chart title
            null,                     // domain axis label
            Localization.get(LC.CHART_CARDS), // range axis label
            new DefaultCategoryDataset(), // data
            PlotOrientation.VERTICAL, // the plot orientation
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );
        
        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        TickUnitSource tickUnits = NumberAxis.createIntegerTickUnits();
        plot.getRangeAxis().setStandardTickUnits(tickUnits); //CHECK use locale
        
        setupRenderer(plot);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        
        return chart;        
    }

    private void setupRenderer(CategoryPlot plot)
    {
        DecimalFormat format = new DecimalFormat("####");
        format.setNegativePrefix("");
        
        StackedBarRenderer renderer = new StackedBarRenderer();
        renderer.setItemLabelGenerator(
            new StandardCategoryItemLabelGenerator("{2}", format));
        renderer.setItemLabelsVisible(true);
        renderer.setPositiveItemLabelPosition(
            new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.HALF_ASCENT_CENTER)
        );
        
        renderer.setSeriesPaint(0, ColorConstants.EXPIRED_CARDS);
        renderer.setSeriesPaint(1, ColorConstants.RELEARNED_CARDS);
        renderer.setSeriesPaint(2, ColorConstants.LEARNED_CARDS);
        
        renderer.setMaximumBarWidth(0.2);
        
        CategoryItemRenderer renderer2 = new LineAndShapeRenderer(true, false);
        renderer2.setSeriesPaint(0, new Color(75, 150, 200));
        plot.setRenderer(1, renderer2);
        
        plot.setRenderer(renderer);
    }
    
    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.setContentPane(new HistoryChartPanel(new LearnHistory(Main.STATS_FILE)));
        frame.setSize(new Dimension(800, 800));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
