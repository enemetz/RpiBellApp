package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.*;
import java.io.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.auth.FirebaseAuth;

/**
 * This is the Login Page.
 * Here an email and password must be given in order to access the rest of the application.
 */
public class LoginPage extends AppCompatActivity {
    // Global variables
    public static final String TAG = "MyFirebaseMsgService";

    public Button logIn;// log in button
    public Button resetPass;

    private EditText emailInput;        // user email
    private EditText passwordInput;     // user password

    public String IP;                   // the IP address of the Raspberry Pi device  to connect to
    public String token;                // token for the current connection
    public String username;             // username used to greet the user

    private FirebaseAuth mAuth;         // access the Firebase Authentication
    private FirebaseFirestore db;       // access to the Firebase Firestore Database

    /**
     * This method will be used in order to set up the Login Page once user clicks on the app.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // connect to the Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // connect to the Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // obtain the typed username and password
        emailInput = findViewById(R.id.userEmailInput);
        passwordInput = findViewById(R.id.userPasswordInput);


        // once the login in pressed, check the username and password with the Firebase Authentication
        logIn = findViewById(R.id.LogInButton);
        logIn.setOnClickListener(view -> {
            if (emailInput.getText().toString().isEmpty()) {
                Toast.makeText(LoginPage.this,"PLEASE ENTER AN EMAIL" , Toast.LENGTH_LONG).show();
                return;
            }
            if (passwordInput.getText().toString().isEmpty()) {
                Toast.makeText(LoginPage.this,"PLEASE ENTER A PASSWORD" , Toast.LENGTH_LONG).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {  // sign-in successful
                            Log.d("Login Status", "signInWithEmail:success");
                            // obtain the user's PiBell's Hostname
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.e("userID",user.getUid());

                            // check all the saved admin users for the right account to get the PiBell hostname
                            DocumentReference adminUsers = db.collection("admins").document(user.getUid());
                            adminUsers.get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    DocumentSnapshot document = task1.getResult();
                                    if (document.exists()) {
                                        Log.d("Device Info Status", "SUCCESS");
                                        String hostname = document.getString("hostname");
                                        try
                                        {
                                            IP = new NetTask().execute(hostname).get();
                                            if (IP == null) {
                                                String newHostName = hostname + ".lan";
                                                IP = new NetTask().execute(newHostName).get();
                                            }
                                            if (IP == null) {
                                                Toast.makeText(LoginPage.this,"CANNOT FIND PiBELL. PLEASE CHECK THE HELP PAGE IN SETTINGS TO RESOLVE THE ISSUE." , Toast.LENGTH_LONG).show();
                                                // Gets current token
                                                FirebaseMessaging.getInstance().getToken()
                                                        .addOnCompleteListener(task2 -> {
                                                            if (!task2.isSuccessful()) {
                                                                Log.w(TAG, "Fetching FCM registration token failed", task2.getException());
                                                                token = null;
                                                                Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
                                                                return;
                                                            }
                                                            // Get new FCM registration token
                                                            token = task2.getResult();
                                                            Log.e("Registration Token", token);
                                                            try {
                                                                Log.e("Special Status", "Cannot get IP Address, bring to homepage to resolve");
                                                                username = document.getString("name");
                                                                //Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                                                                Intent intent = new Intent(LoginPage.this, UserHomePage.class);
                                                                intent.putExtra("user", username);
                                                                intent.putExtra("IP",IP);
                                                                intent.putExtra("token", token);
                                                                startActivity(intent);
                                                                finish();
                                                                return;
                                                            } catch (Exception e2) {
                                                                e2.printStackTrace();
                                                            }
                                                        });
                                            } else {
                                                // Gets current token
                                                FirebaseMessaging.getInstance().getToken()
                                                        .addOnCompleteListener(task2 -> {
                                                            if (!task2.isSuccessful()) {
                                                                Log.w(TAG, "Fetching FCM registration token failed", task2.getException());
                                                                token = null;
                                                                Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
                                                                return;
                                                            }
                                                            // Get new FCM registration token
                                                            token = task2.getResult();
                                                            Log.e("Registration Token", token);
                                                            try {
                                                                username = document.getString("name");
                                                                // check the Notification.txt to see if the user wants notifications or not
                                                                String wantNotif = new NetTask3().execute(username).get();
                                                                if (wantNotif.equals("YES")) {
                                                                    // send the RPi device the username and token
                                                                    String[] args = {IP,username,token};
                                                                    new NetTask2().execute(args).get();
                                                                    // go to the next screen (home screen) ; bring hidden variables along to use for later functions
                                                                    Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(LoginPage.this, UserHomePage.class);
                                                                    intent.putExtra("user", username);
                                                                    intent.putExtra("IP",IP);
                                                                    intent.putExtra("token", token);
                                                                    startActivity(intent);
                                                                    finish();
                                                                    return;
                                                                } else {
                                                                    // go to the next screen (home screen) ; bring hidden variables along to use for later functions
                                                                    Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(LoginPage.this, UserHomePage.class);
                                                                    intent.putExtra("user", username);
                                                                    intent.putExtra("IP",IP);
                                                                    intent.putExtra("token", token);
                                                                    startActivity(intent);
                                                                    finish();
                                                                    return;
                                                                }
                                                            } catch (Exception e2) {
                                                                e2.printStackTrace();
                                                            }
                                                        });

                                            }
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    } else {
                                        Log.d("Device Info Status", "ERROR");
                                        Toast.makeText(getApplicationContext(), "ERROR OBTAINING DEVICE INFO", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }
                            });


                            // check all the saved guest accounts for the PiBell's hostname
                            DocumentReference guestUsers = db.collection("guests").document(user.getUid());
                            guestUsers.get().addOnCompleteListener(task3 -> {
                                if (task3.isSuccessful()) {
                                    DocumentSnapshot document = task3.getResult();
                                    if (document.exists()) {
                                        Log.d("Device Info Status", "SUCCESS");
                                        String hostname = document.getString("hostname");
                                        try
                                        {
                                            IP = new NetTask().execute(hostname).get();
                                            if (IP == null) {
                                                String newHostName = hostname + ".lan";
                                                IP = new NetTask().execute(newHostName).get();
                                            }
                                            if (IP == null) {
                                                Toast.makeText(LoginPage.this,"PLEASE MAKE SURE RPi DEVICE IS CONNECTED TO SAME NETWORK ..." , Toast.LENGTH_LONG).show();
                                            } else {
                                                // Gets current token
                                                FirebaseMessaging.getInstance().getToken()
                                                        .addOnCompleteListener(task4 -> {
                                                            if (!task4.isSuccessful()) {
                                                                Log.w(TAG, "Fetching FCM registration token failed", task4.getException());
                                                                token = null;
                                                                Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
                                                                return;
                                                            }
                                                            // Get new FCM registration token
                                                            token = task4.getResult();
                                                            Log.e("Registration Token", token);
                                                            try {
                                                                username = document.getString("name");
                                                                // check the Notification.txt to see if the user wants notifications or not
                                                                String wantNotif = new NetTask3().execute(username).get();
                                                                if (wantNotif.equals("YES")) {
                                                                    // send the RPi device the username and token
                                                                    String[] args = {IP,username,token};
                                                                    new NetTask2().execute(args).get();
                                                                    // go to the next screen (home screen) ; bring hidden variables along to use for later functions
                                                                    Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(LoginPage.this, UserHomePage.class);
                                                                    intent.putExtra("user", username);
                                                                    intent.putExtra("IP",IP);
                                                                    intent.putExtra("token", token);
                                                                    startActivity(intent);
                                                                    finish();
                                                                    return;
                                                                } else {
                                                                    // go to the next screen (home screen) ; bring hidden variables along to use for later functions
                                                                    Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(LoginPage.this, UserHomePage.class);
                                                                    intent.putExtra("user", username);
                                                                    intent.putExtra("IP",IP);
                                                                    intent.putExtra("token", token);
                                                                    startActivity(intent);
                                                                    finish();
                                                                    return;
                                                                }
                                                            } catch (Exception e2) {
                                                                e2.printStackTrace();
                                                            }
                                                        });

                                            }
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "Failed with: ", task3.getException());
                                    //Toast.makeText(getApplicationContext(), "ERROR OBTAINING DEVICE INFO", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            // sign-in failed
                            Log.w("Login Status", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "LOGIN FAILED! TRY AGAIN LATER", Toast.LENGTH_LONG).show();
                        }
                    });
        });

        //Code for resetting password
        resetPass = findViewById(R.id.passwordResetButton);
        resetPass.setOnClickListener(view -> {
            mAuth.sendPasswordResetEmail(emailInput.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Password reset email sent.", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Email sent");
                            }
                        }
            });
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
            if (addr == null) {
                Log.e("Hostname IP","null");
                return null;
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



    /**
     * This is the NetTask class that will be used to check the user's Notification.txt to see if
     * they set notifications on or off.
     */
    public class NetTask3 extends AsyncTask<String, Integer, String> {

        /**
         * This method will check the Notifications.txt.
         * @param params username
         * @return armed or disarmed
         */
        @Override
        protected String doInBackground(String[] params) {
            Context context = getApplicationContext();

            // get to the settings page
            File dir = context.getDir(params[0], Context.MODE_PRIVATE);
            File file = new File(dir, "Notification.txt");

            boolean exists = file.exists();
            // read from file
            if (exists == true) {
                Log.e("Notification.txt status","exist");
                StringBuilder text = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                    }
                    br.close();
                } catch (IOException e) { e.printStackTrace(); }
                return text.toString();
            } else {    // user doesn't have file in there, need to create new one and mark it unarmed
                Log.e("Notification.txt status","doesn't exist");
                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    writer.append("YES");   // default; want notifications
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "YES";
            }
        } // ends the doInBackground() method
    } // ends the NetTask3 class
} // ends the LoginPage class