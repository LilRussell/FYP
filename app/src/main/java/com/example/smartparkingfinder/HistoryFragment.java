package com.example.smartparkingfinder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageView IVTicket;
    private TextView txt_historyNotice;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItemList;
    private String userID;
    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        userID = UserData.getInstance().getUserID();
        // Initialize RecyclerView and historyItemList
        recyclerView = view.findViewById(R.id.RV_history);
        txt_historyNotice = view.findViewById(R.id.txt_historyNotice);
        IVTicket = view.findViewById(R.id.imageView3);
        historyItemList = new ArrayList<>();

        // Initialize and set up the adapter
        adapter = new HistoryAdapter(requireContext(), historyItemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        // Load your history items from Firebase

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                historyItemList.clear();
                List<HistoryItem> newItems = new ArrayList<>();
                for (DataSnapshot historySnapshot : dataSnapshot.getChildren()) {
                    // Parse data from Firebase into HistoryItem objects

                    String numberplate = historySnapshot.child("carName").getValue(String.class);
                    String cardName = historySnapshot.child("cardName").getValue(String.class);
                    String floor = historySnapshot.child("fragmentName").getValue(String.class);
                    String time = historySnapshot.child("timestamp").getValue(String.class);
                    String location = historySnapshot.child("location").getValue(String.class);
                    String firebaseUserID = historySnapshot.child("userId").getValue(String.class);

                    // Check if the userID from Firebase matches the current userID
                    if (firebaseUserID != null && firebaseUserID.equals(userID)) {
                        // Create a HistoryItem object and add it to the list
                        HistoryItem historyItem = new HistoryItem(userID, numberplate, cardName, floor, location, time);
                        newItems.add(0,historyItem);
                    }
                }



                // Add the new items to the top of the list
                historyItemList.addAll(0, newItems);
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
                if (historyItemList.isEmpty()) {
                    txt_historyNotice.setVisibility(View.VISIBLE);
                    IVTicket.setVisibility(View.VISIBLE);
                } else {
                    txt_historyNotice.setVisibility(View.GONE);
                    IVTicket.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
        return view;
    }

    private void loadHistoryItemsFromFirebase() {
        historyItemList.clear();
        // Replace this with the reference to your Firebase database path
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<HistoryItem> newItems = new ArrayList<>();
                for (DataSnapshot historySnapshot : dataSnapshot.getChildren()) {
                    // Parse data from Firebase into HistoryItem objects

                    String numberplate = historySnapshot.child("carName").getValue(String.class);
                    String cardName = historySnapshot.child("cardName").getValue(String.class);
                    String floor = historySnapshot.child("fragmentName").getValue(String.class);
                    String time = historySnapshot.child("timestamp").getValue(String.class);
                    String location = historySnapshot.child("location").getValue(String.class);
                    String firebaseUserID = historySnapshot.child("userId").getValue(String.class);

                    // Check if the userID from Firebase matches the current userID
                    if (firebaseUserID != null && firebaseUserID.equals(userID)) {
                        // Create a HistoryItem object and add it to the list
                        HistoryItem historyItem = new HistoryItem(userID, numberplate, cardName, floor, location, time);
                        newItems.add(0,historyItem);
                    }
                }



                // Add the new items to the top of the list
                historyItemList.addAll(0, newItems);
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }
}