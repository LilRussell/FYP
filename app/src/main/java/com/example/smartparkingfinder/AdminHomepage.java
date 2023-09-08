package com.example.smartparkingfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.security.identity.MessageDecryptionException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminHomepage extends AppCompatActivity {
    private  Button addlocationBtn;
    private RecyclerView mRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_homepage);

        addlocationBtn=findViewById(R.id.add_btn);
        addlocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomepage.this,AddLocation.class);
                startActivity(intent);
                finish();
            }
        });
        mRecyclerView = findViewById(R.id.admin_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this); // Replace 'this' with your context
        mRecyclerView.setLayoutManager(layoutManager);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("location");
        adminadapter adapter= new adminadapter(this, new ArrayList<>());

// Set the adapter to your RecyclerView
        mRecyclerView.setAdapter(adapter);

// Retrieve data from Firebase
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<locationRVModel> locationDataList = new ArrayList<>();

                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    String id = locationSnapshot.getKey(); // Get the "id" as the key of each location node
                    String name = locationSnapshot.child("name").getValue(String.class); // Get the "name" field
                    String description = locationSnapshot.child("description").getValue(String.class); // Get the "descp" field

                    if (id != null && name != null && description != null) {
                        Log.d("FirebaseData","Got Data");
                        locationRVModel locationData = new locationRVModel(id, name, description);
                        locationDataList.add(locationData);
                    }
                    else {
                        // Log a message if any of the data is null
                        Log.d("FirebaseData", "Skipping data with null values: id=" + id + ", name=" + name + ", description=" + description);
                    }
                }

                // Update your RecyclerView adapter with the retrieved data
                adapter.setData(locationDataList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });


    }
}