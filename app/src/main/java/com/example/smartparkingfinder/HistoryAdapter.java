package com.example.smartparkingfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<HistoryItem> historyItemList;

    public HistoryAdapter(Context context, List<HistoryItem> historyItemList) {
        this.context = context;
        this.historyItemList = historyItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem historyItem = historyItemList.get(position);
        holder.carNameTextView.setText(historyItem.getCarName());
        holder.locationTextView.setText(historyItem.getLocation());
        holder.floorTextView.setText(historyItem.getFragmentName());
        holder.sectionTextView.setText(historyItem.getCardName());
        holder.timeTextView.setText(historyItem.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return historyItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView carNameTextView;
        TextView locationTextView,floorTextView,sectionTextView;
        TextView timeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            carNameTextView = itemView.findViewById(R.id.txt_car);
            locationTextView = itemView.findViewById(R.id.txt_parked_at_location);
            timeTextView = itemView.findViewById(R.id.txt_time);
            floorTextView = itemView.findViewById(R.id.txt_parked_at_floor);
            sectionTextView = itemView.findViewById(R.id.txt_parked_at_section);
        }
    }
}