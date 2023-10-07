package com.example.smartparkingfinder;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

public class AdminGraphItem {
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private List<Entry> chartData;

    // Empty constructor
    public AdminGraphItem() {
        // Default constructor with no arguments
    }

    public AdminGraphItem(String title, String xAxisLabel, String yAxisLabel, List<Entry> chartData) {
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.chartData = chartData;
    }

    // Getter and setter methods for the chart data
    public List<Entry> getChartData() {
        return chartData;
    }

    public void setChartData(List<Entry> chartData) {
        this.chartData = chartData;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getXAxisLabel() {
        return xAxisLabel;
    }

    public void setXAxisLabel(String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
    }

    public String getYAxisLabel() {
        return yAxisLabel;
    }

    public void setYAxisLabel(String yAxisLabel) {
        this.yAxisLabel = yAxisLabel;
    }
}
