package com.example.smartparkingfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SuperAdmin extends AppCompatActivity {
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private String UID;
    private BottomNavigationView bottomNavingationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        UID= UserData.getInstance().getUserID();
        SuperAdminGroupFragment superAdminGroupFragment = new SuperAdminGroupFragment();
        loadFragment(superAdminGroupFragment);
        bottomNavingationView = findViewById(R.id.bottomNavMain_superAdmin);
        bottomNavingationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.admin_list:
                        SuperAdminGroupFragment superAdminGroupFragment = new SuperAdminGroupFragment();
                        loadFragment(superAdminGroupFragment);

                        return true;
                    case R.id.pending:
                        SuoerAdminPendingFragment suoerAdminPendingFragment = new SuoerAdminPendingFragment();
                        loadFragment(suoerAdminPendingFragment);

                        return true;
                    case R.id.approved:
                        SuperAdminApprovedFragment superAdminApprovedFragment = new SuperAdminApprovedFragment();
                        loadFragment(superAdminApprovedFragment);

                        return true;
                }
                return false;
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.superadmin_toolbar,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

       if (id == R.id.tb_register_camera) {
            showInputDialog();
            return true;
        }else if (id==R.id.tb_logout){
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void showInputDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set dialog title and message
        builder.setTitle("Camera Registration");
        builder.setMessage("Please enter Camera ID and KEY:");

        // Create a LinearLayout to hold the EditText widgets
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Create EditText widgets for text input
        final EditText inputEditText = new EditText(this);
        inputEditText.setHint("Camera ID");
        final EditText inputEditTextPass = new EditText(this);
        inputEditTextPass.setHint("Camera KEY");

        // Add EditText widgets to the LinearLayout
        layout.addView(inputEditText);
        layout.addView(inputEditTextPass);

        // Set the LinearLayout as the view for the AlertDialog
        builder.setView(layout);
        // Set positive (OK) button action
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle OK button click
                String userInput = inputEditText.getText().toString();
                String userInputKey = inputEditTextPass.getText().toString();
                // Define the path where you want to store the camera name
                DatabaseReference camerasRef = FirebaseDatabase.getInstance().getReference("camera").child(userInput).child("id");
                DatabaseReference camerasKeyRef = FirebaseDatabase.getInstance().getReference("camera").child(userInput).child("key");
                // Set the camera name under the defined path
                camerasRef.setValue(userInput);
                camerasKeyRef.setValue(userInputKey);
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
        dialog.show();
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
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_superAdmin, fragment);
        transaction.addToBackStack(null); // Optional, to add fragments to the back stack
        transaction.commit();
    }

}