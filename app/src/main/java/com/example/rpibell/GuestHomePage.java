package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class GuestHomePage extends AppCompatActivity {
    public TextView welcomeBanner;         // Text at top that will be used in order to greet the user
    public String userName;                // current user
    public Button settings;                // settings button on the page
    public Button logOut;                  // log out button on the page
    public Button mediaPage;               // button to bring user to media page to view pictures taken

    public String IP;                      // IP address of the user's Raspberry Pi device
    public String token;                   // user's current token

    public final int WAIT = 2000;          // amount of time to pause the app in order to give Raspberry Pi Camera time to warm up


    /**
     * This method will be used in order to set up the Home Page once user logs in.
     *
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_page);


        // disable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Guest Home Page");


        // set the welcome bar at the top of the page to greet the user with their username
        welcomeBanner = findViewById(R.id.UserPageWelcomeTitle);
        //userName = getIntent().getExtras().getString("user");
        //welcomeBanner.append("Welcome, " + userName);


        // set/save the IP address of the user's Raspberry Pi device
//        IP = getIntent().getExtras().getString("IP");
//
//
//
//        // get the token
//        token = getIntent().getExtras().getString("token");
//
//
//
//        // ask RPi device to send any new pictures (usually the case once detection has occurred)
//        String[] args = {IP,userName};


        // once the Settings Button is pressed, go to the settings page
//        settings = findViewById(R.id.SettingsButton);
//        settings.setOnClickListener(view -> {
//            Intent intent = new Intent(GuestHomePage.this, SettingsPage.class);
//            intent.putExtra("user", userName);
//            intent.putExtra("IP", IP);
//            intent.putExtra("token", token);
//            startActivity(intent);
//            finish();
//        });


        // once the Log Out button is pressed, go back to the log in page
//        logOut = findViewById(R.id.logOutButton);
//        logOut.setOnClickListener(view -> {
//            FirebaseAuth.getInstance().signOut();
//            Intent intent = new Intent(GuestHomePage.this, LoginPage.class);
//            startActivity(intent);
//            finish();
//        });

//        mediaPage = findViewById(R.id.storedMediaButton);
//        mediaPage.setOnClickListener(view -> {
//            Intent intent = new Intent(GuestHomePage.this, MediaPage.class);
//            intent.putExtra("user", userName);
//            intent.putExtra("IP", IP);
//            intent.putExtra("token", token);
//            startActivity(intent);
//            finish();
//        });

    } // ends the onCreate() method
}




//    /**
//     * This is the NetTask class that will be used to check the user's Camera.txt to see if the device has been armed already or not.
//     */
//    public class NetTask4 extends AsyncTask<String, Integer, String> {
//
//        /**
//         * This method will check the setting.txt.
//         * @param params username
//         * @return armed or disarmed
//         */
//        @Override
//        protected String doInBackground(String[] params) {
//            Context context = getApplicationContext();
//
//            // get to the settings page
//            File dir = context.getDir(params[0], Context.MODE_PRIVATE);
//            File file = new File(dir, "Camera.txt");
//
//            boolean exists = file.exists();
//            // read from file
//            if (exists == true) {
//                Log.e("Camera.txt status","exist");
//                StringBuilder text = new StringBuilder();
//                try {
//                    BufferedReader br = new BufferedReader(new FileReader(file));
//                    String line;
//                    while ((line = br.readLine()) != null) {
//                        text.append(line);
//                    }
//                    br.close();
//                } catch (IOException e) { e.printStackTrace(); }
//                return text.toString();
//            } else {    // user doesn't have file in there, need to create new one and mark it unarmed
//                Log.e("Camera.txt status","doesn't exist");
//                FileWriter writer = null;
//                try {
//                    writer = new FileWriter(file);
//                    writer.append("disarmed");
//                    writer.flush();
//                    writer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return "disarmed";
//            }
//        } // ends the doInBackground() method
//    } // ends the NetTask4 class
//
//
//
//
//
//    /**
//     * This is the NetTask class that will be used to check the user's Camera.txt to see if the device has been armed already or not.
//     */
//    public class NetTask5 extends AsyncTask<String, Integer, String> {
//
//        /**
//         * This method will check the setting.txt.
//         * @param params username
//         * @return armed or disarmed
//         */
//        @Override
//        protected String doInBackground(String[] params) {
//            Context context = getApplicationContext();
//
//            // get to the settings page
//            File dir = context.getDir(params[0], Context.MODE_PRIVATE);
//            File file = new File(dir, "Camera.txt");
//
//            boolean exists = file.exists();
//            // read from file
//            if (exists == true) {
//                file.delete();
//                File newFile = new File(dir, "Camera.txt");
//                FileWriter writer = null;
//                try {
//                    writer = new FileWriter(newFile);
//                    writer.append("armed");
//                    writer.flush();
//                    writer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return "armed";
//            } else {    // user doesn't have file in there, need to create new one and mark it unarmed
//                FileWriter writer = null;
//                try {
//                    writer = new FileWriter(file);
//                    writer.append("armed");
//                    writer.flush();
//                    writer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return "armed";
//            }
//        } // ends the doInBackground() method
//    } // ends the NetTask5 class
//
//
//
//
//    /**
//     * This is the NetTask class that will be used to check the user's Camera.txt to see if the device has been armed already or not.
//     */
//    public class NetTask6 extends AsyncTask<String, Integer, String> {
//
//        /**
//         * This method will check the setting.txt.
//         * @param params username
//         * @return armed or disarmed
//         */
//        @Override
//        protected String doInBackground(String[] params) {
//            Context context = getApplicationContext();
//
//            // get to the settings page
//            File dir = context.getDir(params[0], Context.MODE_PRIVATE);
//            File file = new File(dir, "Camera.txt");
//
//            boolean exists = file.exists();
//            // read from file
//            if (exists == true) {
//                file.delete();
//                File newFile = new File(dir, "Camera.txt");
//                FileWriter writer = null;
//                try {
//                    writer = new FileWriter(newFile);
//                    writer.append("disarmed");
//                    writer.flush();
//                    writer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return "disarmed";
//            } else {    // user doesn't have file in there, need to create new one and mark it unarmed
//                FileWriter writer = null;
//                try {
//                    writer = new FileWriter(file);
//                    writer.append("disarmed");
//                    writer.flush();
//                    writer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return "disarmed";
//            }
//        } // ends the doInBackground() method
//    } // ends the NetTask6 class
//
//
//} // ends the UserHomePage class
