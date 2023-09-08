package com.example.smartparkingfinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class adminadapter extends RecyclerView.Adapter<adminadapter.ViewHolder>{

        private List<locationRVModel> data;
        private Context context;

        public adminadapter(Context context, List<locationRVModel> data) {
            this.context = context;
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.admin_location_rv, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            locationRVModel locationRVModel = data.get(position);
            holder.locationTextView.setText(locationRVModel.getName());
            holder.descriptionTextView.setText(locationRVModel.getDescription());
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
            TextView locationTextView;
            TextView descriptionTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                locationTextView = itemView.findViewById(R.id.idAdminLocation);
                descriptionTextView = itemView.findViewById(R.id.idAdminDescription);
            }
        }
    }

