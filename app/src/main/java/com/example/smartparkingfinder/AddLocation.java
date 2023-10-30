package com.example.smartparkingfinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import java.util.Locale;


public class AddLocation extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ImageView imageView;
    private Uri filePath; // To store the selected image URI
    private static final int PICK_IMAGE_REQUEST = 71; // Request code for image selection
    private StorageReference storageReference;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_location_activity);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add New Location ");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button saveButton = findViewById(R.id.btn_save_location);
        Button chooseImageButton = findViewById(R.id.button_choose_image);
        imageView = findViewById(R.id.idParkingImage);
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the image picker to select an image
                chooseImage();
            }
        });
        storageReference = FirebaseStorage.getInstance().getReference("location_images"); // Initialize the storage reference
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Update the reference to "details"

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the values from the EditText fields
                String locationName = ((EditText) findViewById(R.id.edt_location_name)).getText().toString();
                String locationDescription = ((EditText) findViewById(R.id.edt_location_desc)).getText().toString();


                // Check if an image is selected
                if (filePath != null) {
                    // Upload the image to Firebase Storage
                    uploadImage(locationName, locationDescription);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Handle the back button click
            navigateToAdminHomepage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }private void navigateToAdminHomepage() {
        Intent intent = new Intent(this, AdminHomepage.class);
        startActivity(intent);

        // Finish the current activity to prevent the user from navigating back to it
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddLocation.this, AdminHomepage.class);
        startActivity(intent);
        finish();
    }
    // Function to open the image picker
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    // Function to handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                // Load the selected image into the ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(String locationName, String locationDescription) {
        if (filePath != null) {
            DatabaseReference locationRef = mDatabase.child("location");

            // Query to check if a location with the same name already exists
            locationRef.orderByChild("details/name").equalTo(locationName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // A location with the same name already exists, show an error message
                        Toast.makeText(getApplicationContext(), "Location with the same name already exists. Please use a different name.", Toast.LENGTH_SHORT).show();
                    } else {
                        // No location with the same name found, proceed to upload the new location
                        // Create a unique ID for the location
                        String locationId = mDatabase.child("location").push().getKey();
                        // Reference to the location under "location/id/details"
                        DatabaseReference newLocationRef = mDatabase.child("location").child(locationId).child("details");

                        // Upload the image to Firebase Storage
                        StorageReference imageRef = storageReference.child(locationName + ".jpg");
                        imageRef.putFile(filePath)
                                .addOnSuccessListener(taskSnapshot -> {
                                    // Image uploaded successfully
                                    // Get the download URL of the uploaded image
                                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        // Create a Location object with name, description, parking number, and image URL
                                        locationRVModel location = new locationRVModel(locationId, locationName, locationDescription, 0, uri.toString());

                                        // Save the location to Firebase Realtime Database under "location/id/details"
                                        newLocationRef.setValue(location);

                                        // Clear the EditText fields after saving
                                        ((EditText) findViewById(R.id.edt_location_name)).setText("");
                                        ((EditText) findViewById(R.id.edt_location_desc)).setText("");

                                        Toast.makeText(getApplicationContext(), "Location and Image saved!", Toast.LENGTH_SHORT).show();
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    // Handle upload failure
                                    Toast.makeText(getApplicationContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors here
                    Toast.makeText(getApplicationContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }
}
