package com.example.smartparkingfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;



public class AddLocation extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ImageView imageView;
    private Uri filePath; // To store the selected image URI
    private static final int PICK_IMAGE_REQUEST = 71; // Request code for image selection
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_location_activity);

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
                String parkingSpace = ((EditText) findViewById(R.id.edt_parking_space)).getText().toString();

                // Check if an image is selected
                if (filePath != null) {
                    // Upload the image to Firebase Storage
                    uploadImage(locationName, locationDescription, parkingSpace);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

    private void uploadImage(String locationName, String locationDescription, String parkingSpace) {
        if (filePath != null) {
            // Create a unique ID for the location
            String locationId = mDatabase.child("location").push().getKey();

            // Reference to the location under "location/id/details"
            DatabaseReference locationRef = mDatabase.child("location").child(locationId).child("details");

            StorageReference imageRef = storageReference.child(locationName + ".jpg");

            // Upload the image to Firebase Storage
            imageRef.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully
                        // Get the download URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                            // Create a Location object with name, description, parking number, and image URL
                            locationRVModel location = new locationRVModel(locationId, locationName, locationDescription, Integer.parseInt(parkingSpace), uri.toString());

                            // Save the location to Firebase Realtime Database under "location/id/details"
                            locationRef.setValue(location);

                            // Clear the EditText fields after saving
                            ((EditText) findViewById(R.id.edt_location_name)).setText("");
                            ((EditText) findViewById(R.id.edt_location_desc)).setText("");
                            ((EditText) findViewById(R.id.edt_parking_space)).setText("");

                            Toast.makeText(getApplicationContext(), "Location and Image saved!", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle upload failure
                        Toast.makeText(getApplicationContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
