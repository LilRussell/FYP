package com.example.smartparkingfinder;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private UserAdapter adapter;
    private RecentAdapter adapterRecent;
    private String userID;
    private RecyclerView mRecyclerView,HorizontalRV;
    private SearchView searchView;
    private List<locationRVModel> locationDataList = new ArrayList<>();
    private List<locationRVModel> recentLocationList=new ArrayList<>();
    public HomeFragment() {
        // Required empty public constructor
    }

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

        userID =UserData.getInstance().getUserID();

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("location");
        DatabaseReference cameraRef = database.getReference("camera");
        DatabaseReference userRef = database.getReference("users").child(userID).child("recentPlaces");
        FirebaseApp.initializeApp(requireContext());

        mRecyclerView = rootView.findViewById(R.id.verticalRecyclerView_Main);
        HorizontalRV = rootView.findViewById(R.id.recent_rv);
        searchView = rootView.findViewById(R.id.searchView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new UserAdapter(requireContext(), locationDataList);
        mRecyclerView.setAdapter(adapter);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        HorizontalRV.setLayoutManager(horizontalLayoutManager);
        adapterRecent = new RecentAdapter(requireContext(),recentLocationList);
        HorizontalRV.setAdapter(adapterRecent);

        // Set an item click listener for the adapter
        adapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(locationRVModel item) {
                // Handle Edit Parking Layout button click here
                String selectedLocationId = item.getId();
                String selectedLocationTitle = item.getName();
                saveRecentPlace(userID, selectedLocationId);
                // You can launch an edit parking layout activity or perform any other action
                Intent intent = new Intent(requireActivity(), User_Parking_Location.class);
                intent.putExtra("locationId", selectedLocationId);
                intent.putExtra("locationName", selectedLocationTitle);
                intent.putExtra("userID",userID);

                startActivity(intent);
            }
        });
        adapterRecent.setOnItemClickListener(new RecentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(locationRVModel item) {
                // Handle Edit Parking Layout button click here
                String selectedLocationId = item.getId();
                String selectedLocationTitle = item.getName();
                saveRecentPlace(userID, selectedLocationId);
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
               locationDataList.clear();
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

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the list to avoid duplicates when the database changes
                recentLocationList.clear();

                for (DataSnapshot recentPlaceSnapshot : dataSnapshot.getChildren()) {
                    String recentPlaceId = recentPlaceSnapshot.getValue(String.class);
                    Log.d("Recent", recentPlaceId);

                    if (recentPlaceId != null) {
                        DatabaseReference locationRef = database.getReference("location").child(recentPlaceId);

                        // Add a listener for the specific location ID
                        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot locationDataSnapshot) {
                                if (!locationDataSnapshot.exists()) {
                                    // Location data does not exist, remove the ID from recentPlaces
                                    recentPlaceSnapshot.getRef().removeValue();
                                } else {
                                    DataSnapshot detailsSnapshot = locationDataSnapshot.child("details");
                                    String name = detailsSnapshot.child("name").getValue(String.class);
                                    String description = detailsSnapshot.child("description").getValue(String.class);
                                    Integer parkingAvailabilityObj = detailsSnapshot.child("parkingAvailability").getValue(Integer.class);
                                    String imageURL = detailsSnapshot.child("imageURL").getValue(String.class);

                                    if (name != null && description != null) {
                                        Log.d("Get Location", "Got Value");
                                        locationRVModel locationData = new locationRVModel(recentPlaceId, name, description, parkingAvailabilityObj, imageURL);
                                        recentLocationList.add(locationData);
                                        adapterRecent.setData(recentLocationList);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError locationDatabaseError) {
                                // Handle errors here
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission (if needed)
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle search query text change
                filter(newText);
                return true;
            }
        });

        return rootView;


    }

    private void saveRecentPlace(String userId, String locationId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Get the current list of recent places
        userRef.child("recentPlaces").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> recentPlaces = new ArrayList<>();

                // Iterate through the current recent places and add them to the list
                for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                    String placeId = placeSnapshot.getValue(String.class);
                    recentPlaces.add(placeId);
                }

                // Check if the locationId already exists in the list
                if (recentPlaces.contains(locationId)) {
                    // If it exists, remove it from its current position
                    recentPlaces.remove(locationId);
                }

                // Add the new locationId to the front of the list
                recentPlaces.add(0, locationId);

                // Limit the list to the most recent 5 places
                if (recentPlaces.size() > 5) {
                    recentPlaces = recentPlaces.subList(0, 5);
                }

                // Update the "recentPlaces" node with the updated list
                userRef.child("recentPlaces").setValue(recentPlaces);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }
    private void filter(String text) {
        ArrayList<locationRVModel> filteredList = new ArrayList<>();

        // Iterate through your original list and add items that match the search query
        for (locationRVModel item : locationDataList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        // Update the RecyclerView adapter with the filtered list
        adapter.setData(filteredList);
    }
}