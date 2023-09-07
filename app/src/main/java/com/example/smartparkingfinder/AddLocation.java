package com.example.smartparkingfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class AddLocation extends AppCompatActivity {

    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_location_activity);

        Button saveButton = findViewById(R.id.btn_save_location);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("location");
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the values from the EditText fields
                String locationName = ((EditText) findViewById(R.id.edt_location_name)).getText().toString();
                String locationDescription = ((EditText) findViewById(R.id.edt_location_desc)).getText().toString();

                // Generate a unique ID for the location
                String locationId = databaseReference.push().getKey();

                // Create a Location object
                locationRVModel location = new locationRVModel(locationId, locationName, locationDescription);

                // Save the location to Firebase under the unique ID
                databaseReference.child(locationId).setValue(location);

                // Clear the EditText fields after saving
                ((EditText) findViewById(R.id.edt_location_name)).setText("");
                ((EditText) findViewById(R.id.edt_location_desc)).setText("");

                Toast.makeText(getApplicationContext(), "Location saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}