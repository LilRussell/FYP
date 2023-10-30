package com.example.smartparkingfinder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    TextView textView,txtForgotPassword;
    private EditText txtEditEmail,txtEditPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEditEmail = findViewById(R.id.txtEdit_Email);
        txtEditPassword = findViewById(R.id.txtEdit_Password);
        btnLogin = findViewById(R.id.btn_login);
        textView = findViewById(R.id.signup);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),Register.class);
                startActivity(intent);
                finish();
            }
        });
        txtForgotPassword = findViewById(R.id.txt_ForgotPass);
        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPasswordDialog();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = txtEditEmail.getText().toString().trim();
        String password = txtEditPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                // Check the user's role in the database
                                checkUserRole(uid);
                            }
                        } else {
                            // Login failed
                            Toast.makeText(Login.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void checkUserRole(final String uid) {
        mDatabase.child(uid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String role = dataSnapshot.getValue(String.class);
                if (role != null) {
                    if (role.equals("admin")) {
                        // User is an admin, go to AdminHomepage
                        Intent intent = new Intent(Login.this, AdminHomepage.class);
                        intent.putExtra("adminId", uid); // Pass the uid as an extra
                        UserData.getInstance().setUserID(uid);
                        startActivity(intent);
                    }
                    else if (role.equals("superadmin")) {
                        // User is an admin, go to AdminHomepage
                        Intent intent = new Intent(Login.this, SuperAdmin.class);

                        startActivity(intent);
                    }
                    else {
                        // User is a regular user, go to MainActivity
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.putExtra("userID",uid);
                        UserData.getInstance().setUserID(uid);
                        startActivity(intent);
                    }
                    finish(); // Close the login activity
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if needed
                Toast.makeText(Login.this, "Failed to check user role.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.CustomAlertDialogTheme));
        builder.setTitle("Reset Password");

        // Create an EditText for the email address
        final EditText emailEditText = new EditText(this);
        emailEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEditText.setTextColor(getResources().getColor(R.color.black));
        emailEditText.setHint("Enter your email");

        builder.setView(emailEditText);

        // Add a positive button (Send) to the dialog
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEditText.getText().toString().trim();
                sendPasswordResetEmail(email);
            }
        });

        // Add a negative button (Cancel) to the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

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
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Password reset email sent. Check your inbox.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Login.this, "Failed to send reset email. Please check your email address.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}