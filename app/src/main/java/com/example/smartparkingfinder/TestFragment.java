package com.example.smartparkingfinder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparkingfinder.CardAdapter;
import com.example.smartparkingfinder.CardItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TestFragment extends Fragment {
    private static final String TAG = "TestFragment";
    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<CardItem> cardItemList;
    private List<String> cameraNames;

    public TestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_container, container, false);
        Log.d(TAG, "onCreateView called for Fragment " + getArguments().getString("tabTitle"));

        // Initialize the RecyclerView and cardItemList
        recyclerView = view.findViewById(R.id.RV_parking);
        cardItemList = new ArrayList<>();
        adapter = new CardAdapter(cardItemList, cameraNames); // Pass the context

        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

            // Fetch camera names from Firebase
            DatabaseReference camerasRef = FirebaseDatabase.getInstance().getReference("camera").child("P8keeLX1JLSM8NBa6PiE2O8vtWW2");
            camerasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<String> cameraNames = new ArrayList<>();
                    cameraNames.add("Unassigned");
                    for (DataSnapshot cameraSnapshot : dataSnapshot.getChildren()) {
                        String cameraName = cameraSnapshot.child("name").getValue(String.class);
                        if (cameraName != null) {
                            cameraNames.add(cameraName);
                        }

                    }
                    // Pass the camera names to the adapter when creating an instance of it
                    adapter = new CardAdapter(cardItemList, cameraNames);

                    // Set up the RecyclerView
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    recyclerView.setAdapter(adapter);

                    // Add camera names to the RecyclerView
                    for (String cameraName : cameraNames) {
                        addCardToRecyclerView(cameraName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });


        return view;
    }

    // Method to add a new card item to the RecyclerView
    public void addCardToRecyclerView(String cardText) {
        // Create a new CardItem and add it to the adapter's data list
        CardItem cardItem = new CardItem(cardText);
        cardItemList.add(cardItem);

        // Notify the adapter of the data change
        adapter.notifyDataSetChanged();
    }
}