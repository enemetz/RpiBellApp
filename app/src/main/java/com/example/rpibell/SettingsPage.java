package com.example.rpibell;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This is the SettingsPage class.
 * This is the settings page where the user will have the ability to change the notification settings or settings on the raspberry pi.
 */
public class SettingsPage extends AppCompatActivity {

    // Global variables
    public String userName;             // current user
    public String IP;                   // IP address of the user's Raspberry Pi device
    public Button back;                 // back button on the page

    /**
     * This method will be used in order to set up the Settings Page.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);

        // disable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // set/save the IP address of the user's Raspberry Pi device
        IP = getIntent().getExtras().getString("IP");

        // save the username
        userName = getIntent().getExtras().getString("user");

        // once the back button is pressed, request the raspberry pi to end the live stream and then take the user back to the homepage
        back = this.<Button>findViewById(R.id.GoBackButtonLiveToHomePage);
        back.setOnClickListener(view -> {
            try
            {
                Intent intent = new Intent(SettingsPage.this, UserHomePage.class);
                intent.putExtra("user", userName);
                intent.putExtra("IP",IP);
                startActivity(intent);
                finish();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    } // ends the onCreate() method
} // ends the UserHomePage class