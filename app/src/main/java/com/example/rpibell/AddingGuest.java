package com.example.rpibell;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class AddingGuest extends AppCompatActivity {

    // Global variables
    public String userName;                 // current user
    public String IP;                       // IP address of the user's Raspberry Pi device
    public String token;                    // user's current token
    public String email;
    public String password;

    public Button addGuestBtn, goBack;         // add a guest and back button
    public EditText guestName, guestEmail, guestPassword;   // Sign-up fields
    public String hostname;

    public FirebaseAuth mAuth;
    public FirebaseFirestore db;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_guest);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // set/save the IP address of the user's Raspberry Pi device
        IP = getIntent().getExtras().getString("IP");

        // save the username
        userName = getIntent().getExtras().getString("user");

        // save the user's token
        token = getIntent().getExtras().getString("token");

        email = getIntent().getExtras().getString("email");

        password = getIntent().getExtras().getString("password");

        guestName = findViewById(R.id.guestNameInput);
        guestEmail = findViewById(R.id.guestEmailInput);
        guestPassword = findViewById(R.id.guestPasswordInput);
        addGuestBtn = findViewById(R.id.addGuestBtn);
        goBack = findViewById(R.id.goBack);

        addGuestBtn.setOnClickListener(v -> {
            registerNewUser();
        });


        goBack.setOnClickListener(v -> {
            Intent intent = new Intent(AddingGuest.this, GuestManagement.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });
    }


    /**
     * Class that registers and adds user to the Firebase user authentication database.
     */
    public void registerNewUser() {
        String name, guestEmailString, guestPasswordString;
        name = guestName.getText().toString();
        guestEmailString = guestEmail.getText().toString();
        guestPasswordString = guestPassword.getText().toString();

        // Checks for empty fields
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(AddingGuest.this,"PLEASE ENTER A NAME" , Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(guestEmailString)) {
            Toast.makeText(AddingGuest.this,"PLEASE ENTER AN EMAIL ADDRESS" , Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(guestPasswordString)) {
            Toast.makeText(AddingGuest.this,"PLEASE ENTER A PASSWORD" , Toast.LENGTH_LONG).show();
            return;
        }

        String adminUID = mAuth.getCurrentUser().getUid();
        Log.d("adminUID" ,adminUID);

        // Retrieve hostname from currentAdmin
        db.collection("admins").document(adminUID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        hostname = document.get("hostname").toString();
                    } else {
                        Log.d("TAG", "Error getting hostname: ", task.getException());
                    }
                });

        // If user has valid credentials, the user is registered for a guest account
        mAuth.createUserWithEmailAndPassword(guestEmailString, guestPasswordString).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {  // Sign in success
                Map<String, Object> profile = new HashMap<>();
                profile.put("email", guestEmailString);
                profile.put("hostname", hostname);
                profile.put("name", name);
                profile.put("role", "guest");
                db.collection("admins").document(adminUID)
                        .collection("guests")
                        .document(mAuth.getCurrentUser().getUid())
                        .set(profile)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                Log.d("TAG", "DocumentSnapshot successfully written!");

                                FirebaseAuth.getInstance().signOut();

                                // sign into admin account again
                                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(AddingGuest.this, task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Intent intent = new Intent(AddingGuest.this, GuestManagement.class);
                                        intent.putExtra("user", userName);
                                        intent.putExtra("IP",IP);
                                        intent.putExtra("token", token);
                                        intent.putExtra("email",email);
                                        intent.putExtra("password",password);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(AddingGuest.this,"TROUBLE GETTING USER INFO ...",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Error writing document", e);
                                Toast.makeText(getApplicationContext(), "Registration Failed. Try Again ...", Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                // If sign up fails, display message to the user
                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                Toast.makeText(AddingGuest.this,"Registration failed! Please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }
}