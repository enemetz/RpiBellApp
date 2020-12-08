package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.auth.FirebaseAuth;

/**
 * This is the Login Page.
 * Here an email and password must be given in order to access the rest of the application.
 */
public class LoginPage extends AppCompatActivity {
    // Global variables
    public static final String TAG = "MyFirebaseMsgService";

    public Button logIn;                // log in button
    public TextView resetPass;          // pop-up area for password reset

    private EditText emailInput;        // user email
    private EditText passwordInput;     // user password

    public String IP;                   // the IP address of the Raspberry Pi device  to connect to
    public String token;                // token for the current connection
    public String username;             // username used to greet the user
    public String email;                // email used in order to log the user in
    public String password;             // password used in order to log the user in

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

        // connect to the Firebase Firestore (database that stores user Info)
        db = FirebaseFirestore.getInstance();

        // obtain the typed username and password
        emailInput = findViewById(R.id.userEmailInput);
        passwordInput = findViewById(R.id.userPasswordInput);


        // remember me feature
//        if (mAuth.getCurrentUser() != null) {
//            // get user document (info) from list of admin users
//            DocumentReference adminUsers = db.collection("admins").document(mAuth.getCurrentUser().getUid());
//            adminUsers.get().addOnCompleteListener(task1 -> {
//                if (task1.isSuccessful()) { // successful in getting "list" from admin collection
//                    DocumentSnapshot document = task1.getResult();
//                    if (document.exists()) { // found user info in the admin's collection
//                        Log.e("Status", "User data obtained");
//                        String hostname = document.getString("hostname");
//                        email = document.getString("email");
//                        password = document.getString("password");
//
//                        try
//                        {
//                            // get the IP (try both a direct and lan connection)
//                            IP = new getIP().execute(hostname).get();
//                            if (IP == null) {
//                                String newHostName = hostname + ".lan";         // sometimes, the .lan connection works better than the direct access
//                                IP = new getIP().execute(newHostName).get();
//                            }
//
//                            // cannot locate PiBEll, ler user know and allow them to go to the homepage>settings to resolve
//                            if (IP == null) {
//                                Toast.makeText(LoginPage.this,"CANNOT FIND PiBELL. PLEASE CHECK THE HELP PAGE IN SETTINGS TO RESOLVE THE ISSUE." , Toast.LENGTH_LONG).show();
//                                // Gets current token
//                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task2 -> {
//                                    if (!task2.isSuccessful()) {
//                                        Log.w(TAG, "Fetching FCM registration token failed", task2.getException());
//                                        token = null;
//                                        Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
//                                        return;
//                                    }
//                                    // Get new FCM registration token
//                                    token = task2.getResult();
//                                    Log.e("Registration Token", token);
//                                    Log.e("Status", "Cannot get IP Address");
//                                    username = document.getString("name");
//                                    Toast.makeText(getApplicationContext(), "FAILED TO CONNECT TO PiBELL. PLEASE RESOLVE IN SETTINGS.", Toast.LENGTH_LONG).show();
//                                    Intent intent = new Intent(LoginPage.this, UserHomePage.class);
//                                    intent.putExtra("user", username);
//                                    intent.putExtra("IP",IP);
//                                    intent.putExtra("token", token);
//                                    intent.putExtra("email",email);
//                                    intent.putExtra("password",password);
//                                    startActivity(intent);
//                                    finish();
//                                });
//                            } else {
//                                // Gets current token
//                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task2 -> {
//                                    if (!task2.isSuccessful()) {
//                                        Log.w(TAG, "Fetching FCM registration token failed", task2.getException());
//                                        token = null;
//                                        Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
//                                        return;
//                                    }
//                                    // Get new FCM registration token and move to the next activity
//                                    token = task2.getResult();
//                                    username = document.getString("name");
//                                    Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
//                                    Intent intent = new Intent(LoginPage.this, UserHomePage.class);
//                                    intent.putExtra("user", username);
//                                    intent.putExtra("IP",IP);
//                                    intent.putExtra("token", token);
//                                    intent.putExtra("email",email);
//                                    intent.putExtra("password",password);
//                                    startActivity(intent);
//                                    finish();
//                                });
//                            }
//                        } catch (Exception e1) {
//                            Log.e("Status", "Exception in moving to the next Activity.");
//                            Toast.makeText(getApplicationContext(), "ERROR OCCURRED IN CURRENT ACTIVITY. PLEASE TRY AGAIN LATER...", Toast.LENGTH_LONG).show();
//                        }
//                    } else {
//                        // they might be a guest
//                        DocumentReference guestUsers = db.collection("guests").document(mAuth.getCurrentUser().getUid());
//                        guestUsers.get().addOnCompleteListener(task2 -> {
//                            DocumentSnapshot guestDoc = task2.getResult();
//                            if (guestDoc.exists()) {
//                                Log.e("Status", "User data obtained");
//                                String hostname = guestDoc.getString("hostname");
//                                email = guestDoc.getString("email");
//                                password = guestDoc.getString("password");
//
//                                try
//                                {
//                                    // get the IP (try both a direct and lan connection)
//                                    IP = new getIP().execute(hostname).get();
//                                    if (IP == null) {
//                                        String newHostName = hostname + ".lan";         // sometimes, the .lan connection works better than the direct access
//                                        IP = new getIP().execute(newHostName).get();
//                                    }
//
//                                    // cannot locate PiBEll, ler user know and allow them to go to the homepage>settings to resolve
//                                    if (IP == null) {
//                                        Toast.makeText(LoginPage.this,"CANNOT FIND PiBELL. PLEASE CHECK THE HELP PAGE IN SETTINGS TO RESOLVE THE ISSUE." , Toast.LENGTH_LONG).show();
//                                        // Gets current token
//                                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task3 -> {
//                                            if (!task3.isSuccessful()) {
//                                                Log.w(TAG, "Fetching FCM registration token failed", task3.getException());
//                                                token = null;
//                                                Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
//                                                return;
//                                            }
//                                            // Get new FCM registration token
//                                            token = task3.getResult();
//                                            Log.e("Registration Token", token);
//                                            Log.e("Status", "Cannot get IP Address");
//                                            username = guestDoc.getString("name");
//                                            Toast.makeText(getApplicationContext(), "FAILED TO CONNECT TO PiBELL. PLEASE RESOLVE IN SETTINGS.", Toast.LENGTH_LONG).show();
//                                            Intent intent = new Intent(LoginPage.this, GuestHomePage.class);
//                                            intent.putExtra("user", username);
//                                            intent.putExtra("IP",IP);
//                                            intent.putExtra("token", token);
//                                            intent.putExtra("email",email);
//                                            intent.putExtra("password",password);
//                                            startActivity(intent);
//                                            finish();
//                                        });
//                                    } else {
//                                        // Gets current token
//                                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task4 -> {
//                                            if (!task4.isSuccessful()) {
//                                                Log.w(TAG, "Fetching FCM registration token failed", task4.getException());
//                                                token = null;
//                                                Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
//                                                return;
//                                            }
//                                            // Get new FCM registration token and move to the next activity
//                                            token = task4.getResult();
//                                            username = guestDoc.getString("name");
//                                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
//                                            Intent intent = new Intent(LoginPage.this, GuestHomePage.class);
//                                            intent.putExtra("user", username);
//                                            intent.putExtra("IP",IP);
//                                            intent.putExtra("token", token);
//                                            intent.putExtra("email",email);
//                                            intent.putExtra("password",password);
//                                            startActivity(intent);
//                                            finish();
//                                        });
//                                    }
//                                } catch (Exception e1) {
//                                    Log.e("Status", "Exception in moving to the next Activity.");
//                                    Toast.makeText(getApplicationContext(), "ERROR OCCURRED IN CURRENT ACTIVITY. PLEASE TRY AGAIN LATER...", Toast.LENGTH_LONG).show();
//                                }
//                            } else {
//                                Toast.makeText(getApplicationContext(), "ERROR OBTAINING USER DATA. PLEASE TRY AGAIN.", Toast.LENGTH_LONG).show();
//                                Log.e("Status","Error occurred when trying to get user info from database.");
//                            }
//                        });
//                    }
//                } else {
//                    Toast.makeText(getApplicationContext(), "ERROR OBTAINING USER DATA. PLEASE TRY AGAIN.", Toast.LENGTH_LONG).show();
//                    Log.e("Status","Error occurred when trying to get user info from database.");
//                }
//            });
//        } // ends the remember me feature


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

            email = emailInput.getText().toString();
            password = passwordInput.getText().toString();

            mAuth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString()).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {  // sign-in successful
                    Log.d("Login Status", "signInWithEmail:success");

                    // use the userID to look up user info in the DB
                    FirebaseUser user = mAuth.getCurrentUser();
                    Log.e("userID",user.getUid());

                    // get user document (info) from list of admin users
                    DocumentReference adminUsers = db.collection("admins").document(user.getUid());
                    adminUsers.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) { // successful in getting "list" from admin collection
                            DocumentSnapshot document = task1.getResult();
                            if (document.exists()) { // found user info in the admin's collection
                                Log.e("Status", "User data obtained");
                                String hostname = document.getString("hostname");

                                // put the email and password into the fields as well (will use later for remember me)
                                adminUsers.update("email",email);
                                adminUsers.update("password",password);

                                try
                                {
                                    // get the IP (try both a direct and lan connection)
                                    IP = new getIP().execute(hostname).get();
                                    if (IP == null) {
                                        String newHostName = hostname + ".lan";         // sometimes, the .lan connection works better than the direct access
                                        IP = new getIP().execute(newHostName).get();
                                    }

                                    // cannot locate PiBEll, ler user know and allow them to go to the homepage>settings to resolve
                                    if (IP == null) {
                                        Toast.makeText(LoginPage.this,"CANNOT FIND PiBELL. PLEASE CHECK THE HELP PAGE IN SETTINGS TO RESOLVE THE ISSUE." , Toast.LENGTH_LONG).show();
                                        // Gets current token
                                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task2 -> {
                                            if (!task2.isSuccessful()) {
                                                Log.w(TAG, "Fetching FCM registration token failed", task2.getException());
                                                token = null;
                                                Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            // Get new FCM registration token
                                            token = task2.getResult();
                                            Log.e("Registration Token", token);
                                            Log.e("Status", "Cannot get IP Address");
                                            username = document.getString("name");
                                            Toast.makeText(getApplicationContext(), "FAILED TO CONNECT TO PiBELL. PLEASE RESOLVE IN SETTINGS.", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(LoginPage.this, UserHomePage.class);
                                            intent.putExtra("user", username);
                                            intent.putExtra("IP",IP);
                                            intent.putExtra("token", token);
                                            intent.putExtra("email",email);
                                            intent.putExtra("password",password);
                                            startActivity(intent);
                                            finish();
                                        });
                                    } else {
                                        // Gets current token
                                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task2 -> {
                                            if (!task2.isSuccessful()) {
                                                Log.w(TAG, "Fetching FCM registration token failed", task2.getException());
                                                token = null;
                                                Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            // Get new FCM registration token and move to the next activity
                                            token = task2.getResult();
                                            username = document.getString("name");
                                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(LoginPage.this, UserHomePage.class);
                                            intent.putExtra("user", username);
                                            intent.putExtra("IP",IP);
                                            intent.putExtra("token", token);
                                            intent.putExtra("email",email);
                                            intent.putExtra("password",password);
                                            startActivity(intent);
                                            finish();
                                        });
                                    }
                                } catch (Exception e1) {
                                    Log.e("Status", "Exception in moving to the next Activity.");
                                    Toast.makeText(getApplicationContext(), "ERROR OCCURRED IN CURRENT ACTIVITY. PLEASE TRY AGAIN LATER...", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                // they might be a guest
                                DocumentReference guestUsers = db.collection("guests").document(user.getUid());
                                guestUsers.get().addOnCompleteListener(task2 -> {
                                    DocumentSnapshot guestDoc = task2.getResult();
                                    if (guestDoc.exists()) {
                                        Log.e("Status", "User data obtained");
                                        String hostname = guestDoc.getString("hostname");

                                        // put the email and password into the fields as well (will use later for remember me)
                                        guestUsers.update("email",email);
                                        guestUsers.update("password",password);

                                        try
                                        {
                                            // get the IP (try both a direct and lan connection)
                                            IP = new getIP().execute(hostname).get();
                                            if (IP == null) {
                                                String newHostName = hostname + ".lan";         // sometimes, the .lan connection works better than the direct access
                                                IP = new getIP().execute(newHostName).get();
                                            }

                                            // cannot locate PiBEll, ler user know and allow them to go to the homepage>settings to resolve
                                            if (IP == null) {
                                                Toast.makeText(LoginPage.this,"CANNOT FIND PiBELL. PLEASE CHECK THE HELP PAGE IN SETTINGS TO RESOLVE THE ISSUE." , Toast.LENGTH_LONG).show();
                                                // Gets current token
                                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task3 -> {
                                                    if (!task3.isSuccessful()) {
                                                        Log.w(TAG, "Fetching FCM registration token failed", task3.getException());
                                                        token = null;
                                                        Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
                                                        return;
                                                    }
                                                    // Get new FCM registration token
                                                    token = task3.getResult();
                                                    Log.e("Registration Token", token);
                                                    Log.e("Status", "Cannot get IP Address");
                                                    username = guestDoc.getString("name");
                                                    Toast.makeText(getApplicationContext(), "FAILED TO CONNECT TO PiBELL. PLEASE RESOLVE IN SETTINGS.", Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent(LoginPage.this, GuestHomePage.class);
                                                    intent.putExtra("user", username);
                                                    intent.putExtra("IP",IP);
                                                    intent.putExtra("token", token);
                                                    intent.putExtra("email",email);
                                                    intent.putExtra("password",password);
                                                    startActivity(intent);
                                                    finish();
                                                });
                                            } else {
                                                // Gets current token
                                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task4 -> {
                                                    if (!task4.isSuccessful()) {
                                                        Log.w(TAG, "Fetching FCM registration token failed", task4.getException());
                                                        token = null;
                                                        Toast.makeText(LoginPage.this,"TROUBLE CONNECTING TO FIREBASE, TRY AGAIN LATER ..." , Toast.LENGTH_LONG).show();
                                                        return;
                                                    }
                                                    // Get new FCM registration token and move to the next activity
                                                    token = task4.getResult();
                                                    username = guestDoc.getString("name");
                                                    Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent(LoginPage.this, GuestHomePage.class);
                                                    intent.putExtra("user", username);
                                                    intent.putExtra("IP",IP);
                                                    intent.putExtra("token", token);
                                                    intent.putExtra("email",email);
                                                    intent.putExtra("password",password);
                                                    startActivity(intent);
                                                    finish();
                                                });
                                            }
                                        } catch (Exception e1) {
                                            Log.e("Status", "Exception in moving to the next Activity.");
                                            Toast.makeText(getApplicationContext(), "ERROR OCCURRED IN CURRENT ACTIVITY. PLEASE TRY AGAIN LATER...", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "ERROR OBTAINING USER DATA. PLEASE TRY AGAIN.", Toast.LENGTH_LONG).show();
                                        Log.e("Status","Error occurred when trying to get user info from database.");
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "ERROR OBTAINING USER DATA. PLEASE TRY AGAIN.", Toast.LENGTH_LONG).show();
                            Log.e("Status","Error occurred when trying to get user info from database.");
                        }
                    });
                } else {
                    // sign-in failed
                    Log.w("Login Status", "signInWithEmail:failure", task.getException());
                    Toast.makeText(getApplicationContext(), "LOGIN FAILED! TRY AGAIN", Toast.LENGTH_LONG).show();
                }
            });
        });


        //Code for resetting password
        resetPass = findViewById(R.id.passwordResetButton);
        resetPass.setOnClickListener(view -> {
            if (emailInput.getText().toString().isEmpty()) {
                Toast.makeText(LoginPage.this,"PLEASE ENTER AN EMAIL" , Toast.LENGTH_LONG).show();
                return;
            }
            mAuth.sendPasswordResetEmail(emailInput.getText().toString()).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Password reset email sent.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "EMAIL NOT FOUND IN RECORDS...", Toast.LENGTH_LONG).show();
                }
            });
        });




    } // ends the onCreate() method





    /**
     * This is the getIP class that will be used to obtain the IP address of the PiBell.
     */
    public class getIP extends AsyncTask<String, Integer, String> {
        /**
         * This method will be used in order to obtain the IP address.
         * This works in the background of the app.
         * @param params the name of the PiBell
         * @return the IP Address of the PiBell as a String
         */
        @Override
        protected String doInBackground(String[] params) {
            InetAddress addr = null;
            try {
                addr = InetAddress.getByName(params[0]);
                if (addr == null) {
                    Log.e("Hostname IP","null");
                    return null;
                }
            } catch (UnknownHostException e) {
                return null;
            }
            return addr.getHostAddress();
        } // ends method
    } // ends the getIP class


} // ends the LoginPage class