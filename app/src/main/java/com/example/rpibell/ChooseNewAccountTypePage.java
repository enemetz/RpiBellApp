package com.example.rpibell;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseNewAccountTypePage extends AppCompatActivity {
    // Global variables
    Button newAdminButton;          // make new Admin account
    Button newGuestButton;          // make new Guest account

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_new_user_type_page);

        newAdminButton = findViewById(R.id.chooseAdminButton);
        newAdminButton.setOnClickListener(click -> {
            Intent intent = new Intent(ChooseNewAccountTypePage.this, SignUpPage.class);
            startActivity(intent);
        });

        newGuestButton = findViewById(R.id.chooseGuestButton);
        newGuestButton.setOnClickListener(click -> {
            Intent intent = new Intent(ChooseNewAccountTypePage.this, guestSignUpPage.class);
            startActivity(intent);
        });

    } // ends onCreate()

} // ends class
