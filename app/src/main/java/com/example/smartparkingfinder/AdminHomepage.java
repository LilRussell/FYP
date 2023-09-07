package com.example.smartparkingfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AdminHomepage extends AppCompatActivity {
    private  Button addlocationBtn;
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
    }
}