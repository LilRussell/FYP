package com.example.smartparkingfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.smartparkingfinder.databinding.ActivityMainBinding;
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
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize your bottomNavigationView and set up its listener
        bottomNavigationView = findViewById(R.id.bottomNavMain);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        // Handle the home tab
                        loadFragment(new HomeFragment());
                        // Replace the fragment container with the HomeFragment if needed
                        return true;
                    case R.id.car:
                        // Handle the location tab
                        loadFragment(new CarRegisterFragment());
                        // Replace the fragment container with the LocationFragment

                        return true;
                    case R.id.history:
                        // Handle the profile tab
                        loadFragment(new HistoryFragment());
                        // Replace the fragment container with the ProfileFragment if needed
                        return true;
                    case R.id.profiles:
                        // Handle the profile tab
                        loadFragment(new ProfileFragment());
                        // Replace the fragment container with the ProfileFragment if needed
                        return true;
                }
                return false;
            }
        });

        // Load the initial fragment (e.g., HomeFragment)
        loadFragment(new HomeFragment());

    }
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Optional, to add fragments to the back stack
        transaction.commit();
    }
}