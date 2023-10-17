package com.example.smartparkingfinder;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

public class AdminGraphAdapter extends RecyclerView.Adapter<AdminGraphAdapter.ViewHolder> {
    private List<AdminGraphItem> items;
    private Context context;

    public AdminGraphAdapter(Context context, List<AdminGraphItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.graph_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminGraphItem item = items.get(position);

        // Set data for the CardView
        holder.titleTextView.setText(item.getTitle());
        holder.xAxisLabel.setText(item.getXAxisLabel());
        holder.yAxisLabel.setText(item.getYAxisLabel());
        setupLineChart(holder.lineChart, item.getChartData());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        LineChart lineChart;
        TextView xAxisLabel;
        TextView yAxisLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            lineChart = itemView.findViewById(R.id.lineChart);
            xAxisLabel = itemView.findViewById(R.id.xAxisLabel);
            yAxisLabel = itemView.findViewById(R.id.yAxisLabel);
        }
    }
    private void setupLineChart(LineChart lineChart, List<Entry> chartData) {
        // Create a LineDataSet with the chart data
        LineDataSet dataSet = new LineDataSet(chartData, "Data Set Name");

        // Customize the dataSet (e.g., set colors, labels, etc.)
        dataSet.setColor(Color.BLUE); // Set the line color
        dataSet.setCircleRadius(5f); // Set the radius of the data points
        dataSet.setCircleColor(Color.BLUE); // Set the color of the data points
        dataSet.setLineWidth(2f); // Set the line width

        // Set the mode to LINEAR for connected points
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        // Create a LineData object with the dataSet
        LineData lineData = new LineData(dataSet);
        lineChart.setDrawBorders(true);
        lineChart.setBorderWidth(1f);
        lineChart.getAxisLeft().setAxisMinimum(0f); // Set the minimum value on the left Y-axis to 0
        lineChart.getAxisRight().setEnabled(false); // Disable the right Y-axis
        lineChart.getXAxis().setDrawAxisLine(true);
        lineChart.getXAxis().setDrawGridLines(false);

        // Set the LineData to the LineChart
        lineChart.setData(lineData);

        // Refresh the chart to display the data
        lineChart.invalidate();
    }
}

