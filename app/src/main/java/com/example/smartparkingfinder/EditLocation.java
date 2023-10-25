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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class EditLocation extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ImageView imageView;
    private Uri filePath; // To store the selected image URI
    private static final int PICK_IMAGE_REQUEST = 71; // Request code for image selection
    private StorageReference storageReference;
    private String locationId;
    private String originalImageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);
        Button saveButton = findViewById(R.id.btn_save_location);
        Button chooseImageButton = findViewById(R.id.button_choose_image);
        locationId = getIntent().getStringExtra("locationId");
        imageView = findViewById(R.id.idParkingImage);
        getLocationDetails(locationId);
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
                    uploadImage(locationId,locationName, locationDescription);
                } else {
                    if (originalImageUrl != null) {
                        // Use the original imageUrl for the existing location
                        updateLocationDetails(locationId,locationName, locationDescription, originalImageUrl);
                    } else{
                        Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EditLocation.this, AdminHomepage.class);
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

    private void uploadImage(String locationId,String locationName, String locationDescription) {
        if (filePath != null) {
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
                            locationRVModel location = new locationRVModel(locationId, locationName, locationDescription, 0, uri.toString());

                            // Save the location to Firebase Realtime Database under "location/id/details"
                            locationRef.setValue(location);

                            // Clear the EditText fields after saving
                            ((EditText) findViewById(R.id.edt_location_name)).setText("");
                            ((EditText) findViewById(R.id.edt_location_desc)).setText("");
                            imageView.setImageDrawable(null);
                            Toast.makeText(getApplicationContext(), "Location and Image saved!", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle upload failure
                        Toast.makeText(getApplicationContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void getLocationDetails(String locationId) {
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child("location").child(locationId).child("details");

        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    locationRVModel location = dataSnapshot.getValue(locationRVModel.class);

                    if (location != null) {
                        // Set the name and description
                        ((EditText) findViewById(R.id.edt_location_name)).setText(location.getName());
                        ((EditText) findViewById(R.id.edt_location_desc)).setText(location.getDescription());
                        originalImageUrl = location.getImageURL();
                        // Load the image into the ImageView (you may use a library like Picasso or Glide)
                        String imageUrl = location.getImageURL();
                        if (imageUrl != null) {
                            // Load the image into the ImageView using Picasso
                            Picasso.get().load(imageUrl).into(imageView);
                        }
                        // Load the image using your preferred image loading library
                        // For example, if you use Picasso:
                        // Picasso.get().load(imageUrl).into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors here
                Toast.makeText(getApplicationContext(), "Failed to retrieve location details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateLocationDetails(String locationId,String locationName, String locationDescription, String imageUrl) {
        // Reference to the location under "location/id/details"
        DatabaseReference locationRef = mDatabase.child("location").child(locationId).child("details");

        // Create a Location object with name, description, parking number, and image URL
        locationRVModel location = new locationRVModel(locationId, locationName, locationDescription, 0, imageUrl);

        // Save the location to Firebase Realtime Database under "location/id/details"
        locationRef.setValue(location);

        // Clear the EditText fields after saving
        ((EditText) findViewById(R.id.edt_location_name)).setText("");
        ((EditText) findViewById(R.id.edt_location_desc)).setText("");
        imageView.setImageDrawable(null);
        Toast.makeText(getApplicationContext(), "Location and Image saved!", Toast.LENGTH_SHORT).show();
    }
}
