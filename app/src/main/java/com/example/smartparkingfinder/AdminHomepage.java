package com.example.smartparkingfinder;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminHomepage extends AppCompatActivity {
    private Button addlocationBtn;
    private RecyclerView mRecyclerView;
    private adminadapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_homepage);

        addlocationBtn = findViewById(R.id.add_btn);
        addlocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomepage.this, AddLocation.class);
                startActivity(intent);
                finish();
            }
        });

        mRecyclerView = findViewById(R.id.admin_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("location");

        // Initialize the adapter
        adapter = new adminadapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(adapter);

        // Set an item click listener for the adapter
        adapter.setOnItemClickListener(new adminadapter.OnItemClickListener() {
            @Override
            public void onItemClick(locationRVModel item) {
                showBottomSheetDialog(item);
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

    private void showBottomSheetDialog(locationRVModel item) {
        // Check if the activity is still alive
        if (isFinishing()) {
            return;
        }
        // Create and show the BottomSheetDialog here
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        // Initialize views in the bottom sheet
        TextView locationTextView = view.findViewById(R.id.idTVLocation);
        TextView descriptionTextView = view.findViewById(R.id.idTVDescription);
        TextView parkingTextView = view.findViewById(R.id.idTVParkingAvailable);
        ImageView imageView = view.findViewById(R.id.idIVLocation); // Assuming this is the ID of your ImageView
        ImageView imageViewDel = view.findViewById(R.id.idIVdeleteButton);
        Button editLocationButton = view.findViewById(R.id.idBtnEditLocationDetails); // Add your Edit Location Button ID
        Button editParkingLayoutButton = view.findViewById(R.id.idBtnEditParkingLayout); // Add your Edit Parking Layout Button ID


        locationTextView.setText(item.getName());
        descriptionTextView.setText(item.getDescription());
        parkingTextView.setText("Parking Availability: " + item.getParkingAvailability());

        // Load and display the image using Glide or a similar image loading library
        Glide.with(this)
                .load(item.getImageURL()) // Use the URL from your locationRVModel
                .into(imageView);
        // Set click listeners for the two buttons
        editLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Edit Location button click here
                // You can launch an edit location activity or perform any other action
            }
        });

        editParkingLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Edit Parking Layout button click here
                String selectedLocationId = item.getId();
                // You can launch an edit parking layout activity or perform any other action
                Intent intent = new Intent(AdminHomepage.this, ParkingLayout.class);
                intent.putExtra("locationId", selectedLocationId);
                startActivity(intent);

            }
        });
        imageViewDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the location ID of the item you want to delete
                String locationIdToDelete = item.getId();

                // Initialize Firebase
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference locationRef = database.getReference("location");

                // Reference to the location you want to delete
                DatabaseReference locationToDeleteRef = locationRef.child(locationIdToDelete);

                // Remove the location data
                locationToDeleteRef.removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Location deleted successfully
                                // You can perform any additional actions or show a toast message
                                Toast.makeText(getApplicationContext(), "Location deleted!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle any errors that occur during the delete operation
                                Toast.makeText(getApplicationContext(), "Error deleting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        bottomSheetDialog.setContentView(view);
        // Check again if the activity is still alive before showing the dialog
        if (!isFinishing()) {
            bottomSheetDialog.show();
        }
    }

}