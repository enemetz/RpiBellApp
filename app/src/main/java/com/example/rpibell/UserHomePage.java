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
 * This is the UserHomePage class.
 * This is the main page that the user is greeted with once they log in.
 */
public class UserHomePage extends AppCompatActivity {

    // Global variables
    public TextView welcomeBanner;         // Text at top that will be used in order to greet the user
    public String userName;                // current user
    public Button liveView;                // live view button on the page
    public Button settings;                // settings button on the page
    public Button logOut;                  // log out button on the page
    public String IP;                      // IP address of the user's Raspberry Pi device
    public final int WAIT = 2000;          // amount of time to pause the app in order to give Raspberry Pi Camera time to warm up

    /**
     * This method will be used in order to set up the Home Page once user logs in.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);

        // disable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // set the welcome bar at the top of the page to greet the user with their username
        welcomeBanner = findViewById(R.id.UserPageWelcomeTitle);
        userName = getIntent().getExtras().getString("user");
        welcomeBanner.append("Welcome, " + userName);

        // set/save the IP address of the user's Raspberry Pi device
        IP = getIntent().getExtras().getString("IP");

        // once the Live View Button is pressed, the server must be notified to turn the live stream on
        liveView = findViewById(R.id.goToLiveViewButton);
        liveView.setOnClickListener(view -> {
            try
            {
                // tell the server to turn on the live stream, then go to the live stream page
                new NetTask().execute(IP);

                SystemClock.sleep(WAIT);    // give the camera at least 2 seconds to warm up

                Toast.makeText(UserHomePage.this,"LOADING STREAM..." , Toast.LENGTH_LONG).show();
                Intent intent = new Intent(UserHomePage.this, LiveViewPage.class);
                intent.putExtra("user", userName);
                intent.putExtra("IP",IP);

                startActivity(intent);
                finish();
            } catch (Exception e1) {
                Toast.makeText(UserHomePage.this,"ERROR LOADING STREAM ..." , Toast.LENGTH_LONG).show();
                e1.printStackTrace();
            }
        });


        // once the Settings Button is pressed, go to the settings page
        settings = findViewById(R.id.SettingsButton);
        settings.setOnClickListener(view -> {
            Intent intent = new Intent(UserHomePage.this, LoginPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            startActivity(intent);
            finish();
        });

        // once the Log Out button is pressed, go back to the log in page
        logOut = findViewById(R.id.logOutButton);
        logOut.setOnClickListener(view -> {
            Intent intent = new Intent(UserHomePage.this, LoginPage.class);
            startActivity(intent);
            finish();
        });
    } // ends the onCreate() method

    /**
     * This is the NetTask class that will be used to request the raspberry pi device to turn on the live stream.
     * This will be executed in the background.
     */
    public static class NetTask extends AsyncTask<String, Integer, String> {

        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to request the main server on the device to start the live stream.
         * @param params the IP address of the raspberry pi device
         * @return null since nothing else is needed
         */
        @Override
        protected String doInBackground(String[] params) {
            try {
                // set local variables
                Socket socket=new Socket(params[0],RPiDeviceMainServerPort);
                DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
                DataInputStream din=new DataInputStream(socket.getInputStream());

                // tell the server to start the live
                dout.writeUTF("StartLive");
                dout.flush();

                // server responds : "OK"
                din.readUTF();

                // close all
                dout.close();
                din.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        } // ends the doInBackground() method
    } // ends the NetTask class
} // ends the UserHomePage class