package com.example.smartparkingfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.smartparkingfinder.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
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
    private String UID;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        UID= UserData.getInstance().getUserID();
        HomeFragment homeFragment = new HomeFragment();
        Bundle argsH = new Bundle();
        argsH.putString("userID", UID);
        homeFragment.setArguments(argsH);
        loadFragment(homeFragment);

        // Initialize your bottomNavigationView and set up its listener
        bottomNavigationView = findViewById(R.id.bottomNavMain);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        // Handle the home tab
                        HomeFragment homeFragment = new HomeFragment();
                        Bundle argsH = new Bundle();
                        argsH.putString("userID", UID);
                        homeFragment.setArguments(argsH);
                        loadFragment(homeFragment);


                        toolbar.setTitle("Smart Parking Finder");
                        // Replace the fragment container with the HomeFragment if needed
                        return true;
                    case R.id.car:
                        // Handle the location tab
                        CarRegisterFragment carfragment = new CarRegisterFragment();
                        Bundle args = new Bundle();
                        args.putString("userID", UID); // Pass the UID to the fragment
                        carfragment.setArguments(args); // Set the arguments
                        loadFragment(carfragment);

                        toolbar.setTitle("Car Registration");
                        return true;

                    case R.id.history:
                        // Handle the profile tab

                        HistoryFragment historyFragment = new HistoryFragment();
                        Bundle argsHis = new Bundle();
                        argsHis.putString("userID", UID);
                        historyFragment.setArguments(argsHis);
                        loadFragment(historyFragment);

                        toolbar.setTitle("History Log");
                        // Replace the fragment container with the ProfileFragment if needed

                        return true;
                }
                return false;
            }
        });



    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.user_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {

            return true;
        }else if (id==R.id.tb_logout){
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        // Check if the current fragment is the HomeFragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof HomeFragment) {
            // Handle the back button press in the HomeFragment as needed
            // For example, show a dialog or take some other action
        }else if(currentFragment instanceof CarRegisterFragment){

        }
        else if(currentFragment instanceof HistoryFragment){

        }
        else {
            super.onBackPressed(); // Allow normal back navigation for other fragments
        }
    }
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Optional, to add fragments to the back stack
        transaction.commit();
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
}