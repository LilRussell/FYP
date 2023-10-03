package com.example.smartparkingfinder;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private UserAdapter adapter;
    private String userID;
    private RecyclerView mRecyclerView,HorizontalRV;
    public HomeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("location");
        DatabaseReference cameraRef = database.getReference("camera");
        FirebaseApp.initializeApp(requireContext());

        mRecyclerView = rootView.findViewById(R.id.verticalRecyclerView_Main);
        HorizontalRV = rootView.findViewById(R.id.horizontalRecyclerView_Main);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new UserAdapter(requireContext(), new ArrayList<>());
        mRecyclerView.setAdapter(adapter);
        HorizontalRV.setAdapter(adapter);
        Bundle argsH = getArguments();
        if (argsH != null) {
            userID = argsH.getString("userID");
            Log.d("passID",userID);

        }
        // Set an item click listener for the adapter
        adapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(locationRVModel item) {
                // Handle Edit Parking Layout button click here
                String selectedLocationId = item.getId();
                String selectedLocationTitle = item.getName();
                // You can launch an edit parking layout activity or perform any other action
                Intent intent = new Intent(requireActivity(), User_Parking_Location.class);
                intent.putExtra("locationId", selectedLocationId);
                intent.putExtra("locationName", selectedLocationTitle);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });
        cameraRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot cameraSnapshot) {
                // Initialize a map to store the total empty status count for each location
                Map<String, Integer> locationEmptyCounts = new HashMap<>();

                // Iterate through camera data
                for (DataSnapshot cameraDataSnapshot : cameraSnapshot.getChildren()) {
                    String assignedLocation = cameraDataSnapshot.child("assignedLocation").getValue(String.class);
                    String statusA = cameraDataSnapshot.child("statusA").getValue(String.class);
                    String statusB = cameraDataSnapshot.child("statusB").getValue(String.class);
                    String statusC = cameraDataSnapshot.child("statusC").getValue(String.class);

                    int emptyCount = 0;

                    // Check if statusA is "Empty" and increment the count
                    if ("Empty".equals(statusA)) {
                        emptyCount++;
                    }

                    // Check if statusB is "Empty" and increment the count
                    if ("Empty".equals(statusB)) {
                        emptyCount++;
                    }

                    // Check if statusC is "Empty" and increment the count
                    if ("Empty".equals(statusC)) {
                        emptyCount++;
                    }

                    // Update the total empty status count for the assigned location
                    if (assignedLocation != null&&!assignedLocation.equals("None")) {
                        locationEmptyCounts.put(assignedLocation, locationEmptyCounts.getOrDefault(assignedLocation, 0) + emptyCount);
                    }
                }

                // Now, locationEmptyCounts contains the total count of "Empty" statuses for each assigned location
                // You can iterate through the map and update the parking availability for each location
                for (Map.Entry<String, Integer> entry : locationEmptyCounts.entrySet()) {
                    String locationId = entry.getKey();
                    int totalEmptyCount = entry.getValue();

                    DatabaseReference currentLocationRef = reference.child(locationId);
                    currentLocationRef.child("details").child("parkingAvailability").setValue(totalEmptyCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });


        // Retrieve data from Firebase
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<locationRVModel> locationDataList = new ArrayList<>();

                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    String locationId = locationSnapshot.getKey(); // Get the location ID
                    if (locationId != null) {

                    }
                    // Access the "details" node under the location ID
                    DataSnapshot detailsSnapshot = locationSnapshot.child("details");

                    String name = detailsSnapshot.child("name").getValue(String.class);
                    String description = detailsSnapshot.child("description").getValue(String.class);
                    Integer parkingAvailabilityObj = detailsSnapshot.child("parkingAvailability").getValue(Integer.class);
                    String imageURL = detailsSnapshot.child("imageURL").getValue(String.class);



                    if (locationId != null && name != null && description != null) {
                        Log.d("Get Location", "Got Value");
                        locationRVModel locationData = new locationRVModel(locationId, name, description, parkingAvailabilityObj, imageURL);
                        locationDataList.add(locationData);
                    }
                }

                // Update the RecyclerView adapter with the retrieved data
                adapter.setData(locationDataList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
        return rootView;
    }
}