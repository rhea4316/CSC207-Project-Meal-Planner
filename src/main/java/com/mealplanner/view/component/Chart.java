package com.mealplanner.view.component;

import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;

/**
 * A simple Chart component wrapper.
 * Corresponds to chart.tsx (ChartContainer part).
 * For this example, we wrap a PieChart as it's used in the Dashboard.
 */
public class Chart extends StackPane {

    private final PieChart chart;

    public Chart() {
        chart = new PieChart();
        chart.setLabelsVisible(false); // Clean look
        chart.setLegendVisible(false); // Custom legend often used
        chart.getStyleClass().add("chart-pie");
        
        getChildren().add(chart);
        getStyleClass().add("chart-container");
    }

    public void setData(ObservableList<PieChart.Data> data) {
        chart.setData(data);
    }
    
    public PieChart getChart() {
        return chart;
    }
}

