package com.example.smartparkingfinder;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminHomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Button addlocationBtn;
    private RecyclerView mRecyclerView;
    private adminadapter adapter;
    private String adminId;
    private boolean isBottomSheetDialogShown = false;
    public AdminHomeFragment() {
        // Required empty public constructor
    }

    public static AdminHomeFragment newInstance(String param1, String param2) {
        AdminHomeFragment fragment = new AdminHomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_admin_home, container, false);

        addlocationBtn = rootView.findViewById(R.id.adminHome_add_btn);
        addlocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), AddLocation.class);
                startActivity(intent);
            }
        });

        mRecyclerView = rootView.findViewById(R.id.adminHome_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        mRecyclerView.setLayoutManager(layoutManager);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("location");
        DatabaseReference cameraRef = database.getReference("camera");
        // Initialize the adapter
        adapter = new adminadapter(requireContext(), new ArrayList<>());
        mRecyclerView.setAdapter(adapter);

        // Set an item click listener for the adapter
        adapter.setOnItemClickListener(new adminadapter.OnItemClickListener() {
            @Override
            public void onItemClick(locationRVModel item) {
                showBottomSheetDialog(item);
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
    private void showBottomSheetDialog(locationRVModel item) {
        if (isBottomSheetDialogShown) {
            return;
        }
        // Create and show the BottomSheetDialog here
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireActivity());
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
        parkingTextView.setText("Parking Available: " + item.getParkingAvailability());

        // Load and display the image using Glide or a similar image loading library
        Glide.with(this)
                .load(item.getImageURL()) // Use the URL from your locationRVModel
                .into(imageView);
        // Set click listeners for the two buttons
        editLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedLocationId = item.getId();
                Intent intent = new Intent(requireActivity(), EditLocation.class);
                intent.putExtra("locationId", selectedLocationId);
                startActivity(intent);
            }
        });

        editParkingLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Edit Parking Layout button click here
                String selectedLocationId = item.getId();
                // You can launch an edit parking layout activity or perform any other action
                Intent intent = new Intent(requireActivity(), TestingTab.class);
                intent.putExtra("locationId", selectedLocationId);
                intent.putExtra("adminId",adminId);
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
                                Toast.makeText(requireContext(), "Location deleted!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle any errors that occur during the delete operation
                                Toast.makeText(requireContext(), "Error deleting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        // Set click listener for closing the BottomSheetDialog
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                // Update the flag when the BottomSheetDialog is dismissed
                isBottomSheetDialogShown = false;
            }
        });

        // Show the BottomSheetDialog
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
        isBottomSheetDialogShown = true; // Update the flag

    }
}