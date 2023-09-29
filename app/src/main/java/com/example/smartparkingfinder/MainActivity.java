package com.example.smartparkingfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private UserAdapter adapter;
    private RecyclerView mRecyclerView,HorizontalRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("location");
        FirebaseApp.initializeApp(this);
        mRecyclerView = findViewById(R.id.verticalRecyclerView_Main);
        HorizontalRV = findViewById(R.id.horizontalRecyclerView_Main);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new UserAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(adapter);
        HorizontalRV.setAdapter(adapter);
        // Set an item click listener for the adapter
        adapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(locationRVModel item) {
                // Handle Edit Parking Layout button click here
                String selectedLocationId = item.getId();
                String selectedLocationTitle = item.getName();
                // You can launch an edit parking layout activity or perform any other action
                Intent intent = new Intent(MainActivity.this, User_Parking_Location.class);
                intent.putExtra("locationId", selectedLocationId);
                intent.putExtra("locationName",selectedLocationTitle);
                startActivity(intent);
            }
        });
        // Retrieve data from Firebase
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<locationRVModel> locationDataList = new ArrayList<>();

                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    String locationId = locationSnapshot.getKey(); // Get the location ID

                    // Access the "details" node under the location ID
                    DataSnapshot detailsSnapshot = locationSnapshot.child("details");

                    String name = detailsSnapshot.child("name").getValue(String.class);
                    String description = detailsSnapshot.child("description").getValue(String.class);
                    Integer parkingAvailabilityObj = detailsSnapshot.child("parkingAvailability").getValue(Integer.class);
                    String imageURL = detailsSnapshot.child("imageURL").getValue(String.class);

                    int parkingAvailability = (parkingAvailabilityObj != null) ? parkingAvailabilityObj.intValue() : 0;

                    if (locationId != null && name != null && description != null) {
                        Log.d("Get Location", "Got Value");
                        locationRVModel locationData = new locationRVModel(locationId, name, description, parkingAvailability, imageURL);
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
    }
}