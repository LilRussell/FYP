package com.example.smartparkingfinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private Toolbar toolbar;
    private String adminId;
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_homepage);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        adminId = UserData.getInstance().getUserID();
        mAuth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();
        adminId= UserData.getInstance().getUserID();
        AdminHomeFragment adminHomeFragment = new AdminHomeFragment();
        Bundle argsH = new Bundle();
        argsH.putString("adminID", adminId);
        adminHomeFragment.setArguments(argsH);
        loadFragment(adminHomeFragment);
        // Initialize the bottomNavigationView and set up its listener
        bottomNavigationView = findViewById(R.id.bottomNavMain_admin);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        // Handle the home tab
                        AdminHomeFragment adminHomeFragment = new AdminHomeFragment();
                        Bundle argsH = new Bundle();
                        argsH.putString("adminID", adminId);
                        adminHomeFragment.setArguments(argsH);
                        loadFragment(adminHomeFragment);
                        toolbar.setTitle("Smart Parking Finder");
                        return true;
                    case R.id.camera:
                        AdminCameraFragment adminCameraFragment = new AdminCameraFragment();
                        Bundle args = new Bundle();
                        args.putString("adminID", adminId); // Pass the UID to the fragment
                        adminCameraFragment.setArguments(args); // Set the arguments
                        loadFragment(adminCameraFragment);
                        toolbar.setTitle("Camera List");
                        return true;
                    case R.id.statistic:
                        AdminStatisticFragment adminStatisticFragment = new AdminStatisticFragment();
                        Bundle argsHis = new Bundle();
                        argsHis.putString("adminID", adminId);
                        adminStatisticFragment.setArguments(argsHis);
                        loadFragment(adminStatisticFragment);
                        toolbar.setTitle("Statistics");
                        return true;
                }
                return false;
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu); // Inflate the standard toolbar menu

        // Apply the custom style to the popup menu
        Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, findViewById(R.id.add_camera)); // Use the ID of the "Add Camera" item

        // Inflate the menu with the custom style
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, popupMenu.getMenu()); // Replace with your menu resource

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_camera) {
            showInputDialog();
            return true;
        }else if (id==R.id.tb_logout){
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        builder.setMessage("Please enter your Camera ID:");

        // Create an EditText widget for text input
        final EditText inputEditText = new EditText(this);
        inputEditText.setTextColor(getResources().getColor(R.color.black));
        builder.setView(inputEditText);

        // Set positive (OK) button action
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle OK button click
                final String userInput = inputEditText.getText().toString();

                if (userInput.trim().isEmpty()) {
                    // Display an error message if the camera ID is empty
                    Toast.makeText(AdminHomepage.this, "Camera ID cannot be empty.", Toast.LENGTH_LONG).show();
                } else {
                    // Define the path where you want to check if a camera with the same ID exists
                    DatabaseReference camerasRef = FirebaseDatabase.getInstance().getReference("camera");

                    // Query the database to check if a camera with the same ID exists
                    camerasRef.orderByKey().equalTo(userInput).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // A camera with the same ID already exists
                                Toast.makeText(AdminHomepage.this, "A camera with the same ID already exists. Please use a different Camera ID.", Toast.LENGTH_LONG).show();
                            } else {
                                // The Camera ID is unique; proceed with registration
                                DatabaseReference newCameraRef = camerasRef.child(userInput);
                                newCameraRef.child("ownedBy").setValue(adminId);
                                newCameraRef.child("assignedLocation").setValue("None");
                                newCameraRef.child("assignedCard").setValue("None");
                                newCameraRef.child("assignedTab").setValue("None");
                                newCameraRef.child("statusA").setValue("");
                                newCameraRef.child("statusB").setValue("");
                                newCameraRef.child("statusC").setValue("");
                                newCameraRef.child("status").setValue("Offline");
                                // Close the dialog
                                dialog.dismiss();
                                Toast.makeText(AdminHomepage.this, "Camera Added Successfully.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle any errors that occur when reading from the database
                        }
                    });
                }
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
    @Override
    public void onBackPressed() {
        // Check if the current fragment is the HomeFragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container_admin);

        if (currentFragment instanceof AdminHomeFragment) {
            // Handle the back button press in the HomeFragment as needed
            // For example, show a dialog or take some other action
        }else if(currentFragment instanceof AdminCameraFragment){

        }
        else if(currentFragment instanceof AdminStatisticFragment){

        }
        else {
            super.onBackPressed(); // Allow normal back navigation for other fragments
        }
    }
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_admin, fragment);
        transaction.addToBackStack(null); // Optional, to add fragments to the back stack
        transaction.commit();
    }

}