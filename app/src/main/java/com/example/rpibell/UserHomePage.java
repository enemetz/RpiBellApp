package com.example.rpibell;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserHomePage extends AppCompatActivity {

    TextView welcomeBanner;
    String userName;
    Button liveView;
    Button logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);

        // disable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        welcomeBanner = findViewById(R.id.UserPageWelcomeTitle);

        userName = getIntent().getExtras().getString("user");
        welcomeBanner.append("Welcome, " + userName);

        liveView = findViewById(R.id.goToLiveViewButton);
        liveView.setOnClickListener(view -> {
            Intent intent = new Intent(UserHomePage.this, LiveViewPage.class);
            intent.putExtra("user", userName);
            startActivity(intent);
        });

        logOut = findViewById(R.id.logOutButton);
        logOut.setOnClickListener(view -> {
            Intent intent = new Intent(UserHomePage.this, LoginPage.class);
            intent.putExtra("user", userName);
            startActivity(intent);
            finish();
        });

    }

}
