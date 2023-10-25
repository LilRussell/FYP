package com.example.smartparkingfinder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Register extends AppCompatActivity {

    private TextInputEditText txtEditEmail, txtEditPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtEditEmail = findViewById(R.id.txtEdit_email);
        txtEditPassword = findViewById(R.id.txtEdit_password);
        btnRegister = findViewById(R.id.btn_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String email = txtEditEmail.getText().toString().trim();
        final String password = txtEditPassword.getText().toString().trim();

        // Email format validation
        if (!isValidEmail(email)) {
            Toast.makeText(Register.this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return; // Stop the registration process
        }

        // Password length validation
        if (password.length() < 6) { // Change 7 to your desired minimum length
            Toast.makeText(Register.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return; // Stop the registration process
        }
        String hashedPassword = hashPassword(password);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                // Store user data in the Realtime Database
                                mDatabase.child(uid).child("email").setValue(email);
                                mDatabase.child(uid).child("password").setValue(hashedPassword);
                                mDatabase.child(uid).child("role").setValue("user");

                                // Registration success
                                showSuccessDialog();
                            }
                        } else {
                            // Registration failed
                            Toast.makeText(Register.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    // Password hashing using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            // Convert the byte array to a hexadecimal string
            StringBuilder builder = new StringBuilder();
            for (byte b : hashedBytes) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registration Successful!")
                .setMessage("You have successfully registered.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Navigate back to the login screen (you can start LoginActivity here)
                        Intent loginIntent = new Intent(Register.this, Login.class);
                        startActivity(loginIntent);
                        finish(); // Close the Register activity
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    // Email format validation function
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}

