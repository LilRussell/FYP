package com.example.smartparkingfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DatabaseReference databaseReference;
        FirebaseApp.initializeApp(this);

        // Initialize the database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("test").child("Space");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String spaceStatus = dataSnapshot.getValue(String.class);

                // Update the UI based on the value of spaceStatus
                TextView textViewSpaceStatus = findViewById(R.id.txt_Parking);
                if ("Occupied".equals(spaceStatus)) {
                    textViewSpaceStatus.setTextColor(Color.RED);
                } else if ("Empty".equals(spaceStatus)) {
                    textViewSpaceStatus.setTextColor(Color.GREEN); // or any other color
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if necessary
            }
        });
    }
}