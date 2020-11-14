package com.example.rpibell;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.*;
import java.io.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.tasks.Task;


/**
 * This is the LoginPage class. This is the first screen the user is greeted with.
 * Here a username and password must be given in order to access the rest of the application.
 */
public class LoginPage extends AppCompatActivity {
    public static final String TAG = "MyFirebaseMsgService";

    // Global variables
    public Button logIn;                // log in button
    public TextView userNameInput;      // username input field
    public TextView passwordInput;      // password input field
    public String IP;                   // the IP address of the Raspberry Pi device  to connect to
    public String token;                // token for the current connection
    String username;

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
                IP = new NetTask().execute("czpi1").get();

                if (IP == null) {
                    Toast.makeText(LoginPage.this,"PLEASE MAKE SURE RPi DEVICE IS CONNECTED TO SAME NETWORK ..." , Toast.LENGTH_LONG).show();
                } else {
                    // Gets current token
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                    token = null;
                                    Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
                                    return;
                                }
                                // Get new FCM registration token
                                token = task.getResult();
                                Log.e("Registration Token", token);
                                try {
                                    // send the RPi device the username and token
                                    String username = userNameInput.getText().toString();
                                    String[] args = {IP,username,token};
                                    new NetTask2().execute(args).get();

                                    // go to the next screen (home screen) ; bring hidden variables along to use for later functions
                                    Intent intent = new Intent(LoginPage.this, UserHomePage.class);
                                    intent.putExtra("user", username);
                                    intent.putExtra("IP",IP);
                                    intent.putExtra("token", token);
                                    startActivity(intent);
                                    finish();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            });

                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });


    } // ends the onCreate() method

    /**
     * This is the NetTask class that will be used to obtain the IP address of the raspberry pi device.
     * This will be executed in the background.
     */
    public class NetTask extends AsyncTask<String, Integer, String> {

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

    /**
     * This is the NetTask class that will be used to send the RPi device the current username and token.
     */
    public class NetTask2 extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to update the main server's username and token
         * @param params the IP address of the raspberry pi device, username and token
         * @return null since nothing else is needed
         */
        @Override
        protected String doInBackground(String[] params) {
            try {
                //Log.e("username", params[1]);
                //Log.e("token", params[2]);
                // set local variables
                Socket socket=new Socket(params[0],RPiDeviceMainServerPort);
                DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
                DataInputStream din=new DataInputStream(socket.getInputStream());

                // tell the server to start the live
                dout.writeUTF("Log In");
                dout.flush();

                // server responds : "OK"
                din.readUTF();

                // send username
                dout.writeUTF(params[1]);
                dout.flush();

                // server responds : "OK"
                din.readUTF();

                // send token
                dout.writeUTF(params[2]);
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
    } // ends the NetTask2 class
} // ends the LoginPage class
