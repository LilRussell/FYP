package com.example.smartparkingfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
    private Context context;
    private List<locationRVModel> data;
    private RecentAdapter.OnItemClickListener listener;
    public RecentAdapter(Context context, List<locationRVModel> data) {
        this.context = context;
        this.data = data;
    }
    public interface OnItemClickListener {
        void onItemClick(locationRVModel item);
    }
    public void setOnItemClickListener(RecentAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_location, parent, false);
        return new ViewHolder(view);
    }
    @NonNull
    @Override
    public void onBindViewHolder(@NonNull RecentAdapter.ViewHolder holder, int position) {
        locationRVModel locationRVModel = data.get(position);
        holder.locationTextView.setText(locationRVModel.getName());
        holder.parkingAvailabilityTextView.setText("Parking Available: " + locationRVModel.getParkingAvailability());

        // Load and display the image using Glide or a similar image loading library
        Glide.with(context)
                .load(locationRVModel.getImageURL())
                .into(holder.locationImageView);

        // Set an item click listener for the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(locationRVModel);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return data.size();
    }
    public void setData(List<locationRVModel> locationDataList) {
        this.data = locationDataList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView locationImageView;
        TextView locationTextView;
        TextView parkingAvailabilityTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationImageView = itemView.findViewById(R.id.idImgLocation);
            locationTextView = itemView.findViewById(R.id.idLocation);
            parkingAvailabilityTextView = itemView.findViewById(R.id.idParkingAvailable);
        }
    }
}
