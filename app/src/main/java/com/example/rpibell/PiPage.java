package com.example.rpibell;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PiPage extends AppCompatActivity {
    // Global variables
    public TextView welcomeBanner;          // Text at top that will be used in order to greet the user
    public String userName;                 // current user

    public Button liveView;                 // live view button on the page
    public Button settings;                 // settings button on the page
    public Button logOut;                   // log out button on the page
    public Button mediaPage;                // button to bring user to media page to view pictures taken
    public Button notificationPage;         // button used to bring user to the notifications page to view past notifications
    public Switch armDeviceSwitch;          // switch that is used to arm/disarm the RPi device
    public Button manageGuests;             // Go to Guest Management Profile

    public String IP;                       // IP address of the user's Raspberry Pi device
    public String token;                    // user's current token

    public String email;                    // user's email
    public String password;                 // user's password
    public final int WAIT = 2000;           // amount of time to pause the app in order to give Raspberry Pi Camera time to warm up

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.page_1:
                        return true;
                    case R.id.page_2:
                        try {
                            // tell the server to turn on the live stream, then go to the live stream page
                            new UserHomePage.turnOnLiveStream().execute(IP);
                            SystemClock.sleep(WAIT);    // give the camera at least 2 seconds to warm up

                            Toast.makeText(PiPage.this, "LOADING STREAM...", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(PiPage.this, LiveViewPage.class);
                            intent.putExtra("user", userName);
                            intent.putExtra("IP", IP);
                            intent.putExtra("token", token);
                            intent.putExtra("email", email);
                            intent.putExtra("password", password);
                            startActivity(intent);
                        } catch (Exception e1) {
                            Toast.makeText(PiPage.this, "ERROR LOADING STREAM ...", Toast.LENGTH_LONG).show();
                            e1.printStackTrace();
                        }
                        return true;
                    case R.id.page_3:
                        Intent logout = new Intent(PiPage.this, SettingsPage.class);
                        logout.putExtra("user", userName);
                        logout.putExtra("IP", IP);
                        logout.putExtra("token", token);
                        logout.putExtra("email", email);
                        logout.putExtra("password", password);
                        startActivity(logout);
                        return true;
                }
                return false;
            }
        });
    }
}
