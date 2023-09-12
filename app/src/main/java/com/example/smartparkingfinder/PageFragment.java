package com.example.smartparkingfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView; // Import TextView
import androidx.fragment.app.Fragment;

public class PageFragment extends Fragment {
    private LinearLayout cardContainer; // Reference to the card container

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container, container, false);

        // Find the container where you want to add the card views
        cardContainer = view.findViewById(R.id.cardContainer);

        return view;
    }

    // Method to programmatically add a card view to the container
    public void addCardViewToContainer(String cardText) {
        // Inflate the card view layout (parking_section.xml)
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.parking_section, null);

        // Customize the card view here (e.g., set text, listeners, etc.)
        TextView textView = cardView.findViewById(R.id.txtParking1);
        textView.setText(cardText);

        // Add the card view to the container
        cardContainer.addView(cardView);
    }
}
