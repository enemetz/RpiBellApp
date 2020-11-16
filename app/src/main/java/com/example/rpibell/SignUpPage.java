package com.example.rpibell;

import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the Sign Up Page class, the page that the user is greeted with when
 * registering for their admin account.
 */
public class SignUpPage extends AppCompatActivity {

    // Global variables
    private Button signUp;               // sign-up button
    private EditText nameInput, emailInput, pwInput, ipInput;   // Sign-up fields

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeUI();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
    }

    /**
     * Class that registers and adds user to the Firebase user authentication database.
     */
    private void registerNewUser() {
        String name, email, password, hostname;
        name = nameInput.getText().toString();
        email = emailInput.getText().toString();
        password = pwInput.getText().toString();
        hostname = ipInput.getText().toString();

        // Checks for empty fields
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(SignUpPage.this,"PLEASE ENTER A NAME" , Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(SignUpPage.this,"PLEASE ENTER AN EMAIL ADDRESS" , Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(SignUpPage.this,"PLEASE ENTER A PASSWORD" , Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(hostname)) {
            Toast.makeText(SignUpPage.this,"PLEASE ENTER YOUR HOSTNAME" , Toast.LENGTH_LONG).show();
            return;
        }

        // If user has valid credentials, the user is registered for an admin account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {  // Sign in success
                            Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();

                                Map<String, Object> profile = new HashMap<>();
                                profile.put("email", email);
                                profile.put("hostname", hostname);
                                profile.put("name", name);
                                db.collection("admins").document(mAuth.getCurrentUser().getUid())
                                        .set(profile)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("TAG", "DocumentSnapshot successfully written!");
                                            }
                                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("TAG", "Error writing document", e);
                                    }
                                });

                            /*IP = new LoginPage.NetTask().execute("czpi1").get();*/

                            // go back to login page following successful sign-up
                            Intent intent = new Intent(SignUpPage.this, LoginPage.class);
                            startActivity(intent);

                        } else {
                            // If sign up fails, display message to the user
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpPage.this,"Registration failed! Please try again later",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Initializes the UI to find the corresponding text and button views.
     */
    private void initializeUI() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        pwInput = findViewById(R.id.pwInput);
        ipInput = findViewById(R.id.ipInput);
        signUp = findViewById(R.id.SignUpButton);
    }

    /**
     * This is the NetTask class that will be used to obtain the IP address of the raspberry pi device.
     * This will be executed in the background.
     */
    /*
    public static class NetTask extends AsyncTask<String, Integer, String> {

        /**
         * This method will be used in order to obtain the IP address.
         * This works in the background of the app.
         * @param params the name of the raspberry pi device
         * @return the IP address of the raspberry pi device as a String
         */
    /*
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
    } // ends the NetTask class */
}
