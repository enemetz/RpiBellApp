package com.example.rpibell;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This is the Main Activity class, which is the first screen of the application.
 * This is where the user will either sign up for an account or login.
 */
public class MainActivity extends AppCompatActivity {

    Button loginBtn, signupBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        // Goes to sign-up page if "new user" button pressed
        signupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpPage.class);
            startActivity(intent);
        });


        // Goes to login page if "login" button pressed
        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginPage.class);
            startActivity(intent);
        });

    }

    /**
     * Initializes views for sign-up and login buttons.
     */
    private void initializeViews() {
        signupBtn = findViewById(R.id.btnSignUp);
        loginBtn = findViewById(R.id.btnLogin);

    }
}
