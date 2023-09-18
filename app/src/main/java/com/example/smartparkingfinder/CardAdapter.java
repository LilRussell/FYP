package com.example.smartparkingfinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<CardItem> cardItemList;
    private List<String> cameraNames; // Add a field for camera names

    public CardAdapter(List<CardItem> cardItemList, List<String> cameraNames) {
        this.cardItemList = cardItemList;
        this.cameraNames = cameraNames; // Initialize the camera names list
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parking_section, parent, false);

        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardItem cardItem = cardItemList.get(position);
        holder.bind(cardItem);

        // Check if cameraNames is not null and has data
        if (cameraNames != null && !cameraNames.isEmpty()) {
            Spinner spinnerCamera = holder.itemView.findViewById(R.id.spinner_camera);

            // Create an ArrayAdapter using the fetched camera names
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    holder.itemView.getContext(),
                    android.R.layout.simple_spinner_item,
                    cameraNames
            );

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Apply the adapter to the Spinner
            spinnerCamera.setAdapter(adapter);
        }
    }

    @Override
    public int getItemCount() {
        return cardItemList.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public CardViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.txtParking1);
        }

        public void bind(CardItem cardItem) {
            textView.setText(cardItem.getCardText());
        }
    }
}