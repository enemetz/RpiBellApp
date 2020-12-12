package com.example.rpibell;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * This is the ChooseNewAccountTypePage Class that will be used to guide the user to the correct page
 * to make a new admin or guest account.
 */
public class ChooseNewAccountTypePage extends AppCompatActivity {
    // Global variables
    Button newAdminButton;          // make new Admin account
    Button newGuestButton;          // make new Guest account

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_new_user_type_page);
        // go to create new admin page
        newAdminButton = findViewById(R.id.chooseAdminButton);
        newAdminButton.setOnClickListener(click -> {
            Intent intent = new Intent(ChooseNewAccountTypePage.this, SignUpPage.class);
            startActivity(intent);
        });
        // go to create new guest page
        newGuestButton = findViewById(R.id.chooseGuestButton);
        newGuestButton.setOnClickListener(click -> {
            Intent intent = new Intent(ChooseNewAccountTypePage.this, guestSignUpPage.class);
            startActivity(intent);
        });
    } // ends onCreate()
} // ends class
