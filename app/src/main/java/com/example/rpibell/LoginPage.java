package com.example.rpibell;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.*;
import java.io.*;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This is the LoginPage class. This is the first screen the user is greeted with.
 * Here a username and password must be given in order to access the rest of the application.
 */
public class LoginPage extends AppCompatActivity {

    // Global variables
    public Button logIn;                // log in button
    public TextView userNameInput;      // username input field
    public TextView passwordInput;      // password input field
    public String IP;                   // the IP address of the Raspberry Pi device  to connect to

    /**
     * This method will be used in order to set up the Login Page once user clicks on the app.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // obtain the username and password
        // for now, it can be anything ; will need to be connected to the DB in the future
        userNameInput = findViewById(R.id.UserNameInput);
        passwordInput = findViewById(R.id.PasswordInput);
        logIn = findViewById(R.id.LogInButton);

        // once the Login button is pressed, make sure that both the username and password were filled out
        logIn.setOnClickListener(view -> {
            if (userNameInput.getText().toString().isEmpty()) {
                Toast.makeText(LoginPage.this,"PLEASE ENTER A USERNAME" , Toast.LENGTH_LONG).show();
                return;
            }
            if (passwordInput.getText().toString().isEmpty()) {
                Toast.makeText(LoginPage.this,"PLEASE ENTER A PASSWORD" , Toast.LENGTH_LONG).show();
                return;
            }

            try
            {
                // here the username and password from the user will be checked by the database
                // the hostname of the RPI will be returned, which will be used in order to get the IP address
                // for now, I am using my RPi's name directly (possible since the device is on the same wifi network)
                IP = new NetTask().execute("raspberrypi").get();

                // go to the next screen (home screen) ; bring hidden variables along to use for later functions
                Intent intent = new Intent(LoginPage.this, UserHomePage.class);
                intent.putExtra("user", userNameInput.getText().toString());
                intent.putExtra("IP",IP);
                startActivity(intent);
                finish();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    } // ends the onCreate() method

    /**
     * This is the NetTask class that will be used to obtain the IP address of the raspberry pi device.
     * This will be executed in the background.
     */
    public static class NetTask extends AsyncTask<String, Integer, String> {

        /**
         * This method will be used in order to obtain the IP address.
         * This works in the background of the app.
         * @param params the name of the raspberry pi device
         * @return the IP address of the raspberry pi device as a String
         */
        @Override
        protected String doInBackground(String[] params) {
            InetAddress addr = null;
            try {
                addr = InetAddress.getByName(params[0]);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return addr.getHostAddress();
        } // ends the doInBackground() method
    } // ends the NetTask class
} // ends the LoginPage class
