package com.example.rpibell;

import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the Sign Up Page class, the page that the user is greeted with when
 * registering for their admin account.
 */
public class SignUpPage extends AppCompatActivity {

    // Global variables
    private Button signUp;                                          // sign-up button
    private EditText nameInput, emailInput, pwInput, ipInput;       // Sign-up fields

    private FirebaseAuth mAuth;                                     // access to Firebase Authentication
    private FirebaseFirestore db;                                   // access to Firebase Firestore

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeUI();

        signUp.setOnClickListener(v -> registerNewUser());
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
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {  // Sign in success
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("email", email);
                        profile.put("hostname", hostname);
                        profile.put("name", name);
                        profile.put("password",password);
                        profile.put("role","admin");
                        db.collection("admins").document(mAuth.getCurrentUser().getUid())
                                .set(profile)
                                .addOnSuccessListener(aVoid ->  {
                                    Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                    // go back to login page following successful sign-up
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(SignUpPage.this, LoginPage.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show());

                    } else {
                        // If sign up fails, display message to the user
                        Log.w("TAG", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpPage.this,"Registration failed! Please try again later",Toast.LENGTH_LONG).show();
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

} // ends the SignUpPage class