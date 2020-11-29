package com.example.rpibell;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class guestSignUpPage extends AppCompatActivity {

    // Global variables
    public Button signUpButton;
    public EditText guestName, guestEmail, guestPassword, guestPiBellHostname , AdminID;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_sign_up_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        guestName = findViewById(R.id.guestSignUpNameInput);
        guestEmail = findViewById(R.id.guestEmailSignUpPage);
        guestPassword = findViewById(R.id.guestPasswordSignUpPageInput);
        guestPiBellHostname = findViewById(R.id.guestSignUpPiBellName);
        AdminID = findViewById(R.id.guestSignUpAdminIDInput);

        signUpButton = findViewById(R.id.guestSignUpButton);
        signUpButton.setOnClickListener(task -> {
            String name, email, password, hostname, adminID;
            name = guestName.getText().toString();
            email = guestEmail.getText().toString();
            password = guestPassword.getText().toString();
            hostname = guestPiBellHostname.getText().toString();
            adminID = AdminID.getText().toString();

            // Checks for empty fields
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this,"PLEASE ENTER A NAME" , Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this,"PLEASE ENTER AN EMAIL ADDRESS" , Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this,"PLEASE ENTER A PASSWORD" , Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(hostname)) {
                Toast.makeText(this,"PLEASE ENTER THE PiBELL HOSTNAME" , Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(adminID)) {
                Toast.makeText(this,"PLEASE ENTER THE ADMIN ID" , Toast.LENGTH_LONG).show();
                return;
            }

            // If user has valid credentials, the user is registered for a guest account
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, makeNewGuestAccount -> {
                if (makeNewGuestAccount.isSuccessful()) {  // Sign in success

                    Map<String, Object> profile = new HashMap<>();
                    profile.put("email", email);
                    profile.put("hostname", hostname);
                    profile.put("name", name);
                    profile.put("role", "guest");
                    db.collection("admins").document(adminID)
                            .collection("guests")
                            .document(mAuth.getCurrentUser().getUid())
                            .set(profile)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.e("TAG", "DocumentSnapshot successfully written!");
                                    Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                    FirebaseAuth.getInstance().signOut();
                                    // go back to login page following successful sign-up
                                    Intent intent = new Intent(guestSignUpPage.this, LoginPage.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("TAG", "Error writing document", e);
                                    Toast.makeText(getApplicationContext(), "Registration failed. Try again ...", Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    // If sign up fails, display message to the user
                    Log.w("TAG", "createUserWithEmail:failure", makeNewGuestAccount.getException());
                    Toast.makeText(this,"Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                }
            });

        });

    } // ends onCreate()

} // ends class
