package com.example.smartparkingfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Import TextView
import androidx.fragment.app.Fragment;

public class PageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.parking_box, container, false);

        // Programmatically create and customize a TextView
        TextView textView = new TextView(getContext());
        textView.setText("Hello, World!"); // Set the text

        // Add the TextView to the fragment's layout
        ((ViewGroup) view).addView(textView);

        return view;
    }
}
