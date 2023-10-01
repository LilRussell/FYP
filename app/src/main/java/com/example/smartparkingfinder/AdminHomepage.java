package com.example.smartparkingfinder;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class AdminHomepage extends AppCompatActivity {
    private Button addlocationBtn;
    private RecyclerView mRecyclerView;
    private adminadapter adapter;
    private Toolbar toolbar;
    private String adminId;
    private FirebaseAuth mAuth;
    private FrameLayout radioGroupContainer;
    private boolean isBottomSheetDialogShown = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_homepage);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        adminId = getIntent().getStringExtra("adminId");

        mAuth = FirebaseAuth.getInstance();
        radioGroupContainer = findViewById(R.id.radioGroupContainer);
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
        DatabaseReference cameraRef = database.getReference("camera");
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
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {

            return true;
        } else if (id == R.id.add_camera) {
            showInputDialog();
            return true;
        }else if (id==R.id.tb_logout){
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showBottomSheetDialog(locationRVModel item) {
        if (isBottomSheetDialogShown) {
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
                Intent intent = new Intent(AdminHomepage.this, TestingTab.class);
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
    // To logout the user
    private void logout() {
        mAuth.signOut();

        // After signing out, you can redirect the user to the login screen or perform any other actions you need.
        // For example, you can start a LoginActivity:
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);

        // Make sure to finish the current activity to prevent the user from navigating back to it.
        finish();
    }
    private void showInputDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.CustomAlertDialogTheme));

        // Set dialog title and message
        builder.setTitle("Register Your Camera");
        builder.setMessage("Please enter your CameraID:");

        // Create an EditText widget for text input
        final EditText inputEditText = new EditText(this);
        builder.setView(inputEditText);

        // Set positive (OK) button action
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle OK button click
                String userInput = inputEditText.getText().toString();

                // Define the path where you want to store the camera name
                DatabaseReference camerasRef = FirebaseDatabase.getInstance().getReference("camera").child(userInput);

                // Set the camera name under the defined path
                camerasRef.child("ownedBy").setValue(adminId);
                camerasRef.child("assignedLocation").setValue("None");
                camerasRef.child("assignedCard").setValue("None");
                camerasRef.child("statusA").setValue("");
                camerasRef.child("statusB").setValue("");
                camerasRef.child("statusC").setValue("");
                // Close the dialog
                dialog.dismiss();
            }
        });

        // Set negative (Cancel) button action
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle Cancel button click or dismiss the dialog
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                // Set text color for positive button
                positiveButton.setTextColor(getResources().getColor(R.color.black));

                // Set text color for negative button
                negativeButton.setTextColor(getResources().getColor(R.color.black));
            }
        });
        dialog.show();
    }


}