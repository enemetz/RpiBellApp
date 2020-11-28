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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddingGuest extends AppCompatActivity {

    private Button addGuestBtn, goBack;         // add a guest and back button
    private EditText guestName, guestEmail, guestPassword;   // Sign-up fields
    public String hostname;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_guest);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeUI();

        addGuestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddingGuest.this, GuestManagement.class);
                startActivity(intent);
            }
        });
    }


    /**
     * Class that registers and adds user to the Firebase user authentication database.
     */
    private void registerNewUser() {
        String name, email, password;
        name = guestName.getText().toString();
        email = guestEmail.getText().toString();
        password = guestPassword.getText().toString();
        //hostname = guestIP.getText().toString();

        // Checks for empty fields
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(AddingGuest.this,"PLEASE ENTER A NAME" , Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(AddingGuest.this,"PLEASE ENTER AN EMAIL ADDRESS" , Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(AddingGuest.this,"PLEASE ENTER A PASSWORD" , Toast.LENGTH_LONG).show();
            return;
        }

        String adminUID = mAuth.getCurrentUser().getUid();

        // Retrieve hostname from currentAdmin
        db.collection("admins").document(adminUID)
        .get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    hostname = document.get("hostname").toString();
                } else {
                    Log.d("TAG", "Error getting hostname: ", task.getException());
                }
            }
        });

        // If user has valid credentials, the user is registered for a guest account
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
                            profile.put("role", "guest");
                            db.collection("admins").document(adminUID)
                                    .collection("guests")
                                    .document(mAuth.getCurrentUser().getUid())
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

                            // go back to login page following successful sign-up
                            Intent intent = new Intent(AddingGuest.this, GuestManagement.class);
                            startActivity(intent);

                        } else {
                            // If sign up fails, display message to the user
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(AddingGuest.this,"Registration failed! Please try again later",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Initializes the UI to find the corresponding text and button views.
     */
    private void initializeUI() {
        guestName = findViewById(R.id.guestNameInput);
        guestEmail = findViewById(R.id.guestEmailInput);
        guestPassword = findViewById(R.id.guestPasswordInput);
        addGuestBtn = findViewById(R.id.addGuestBtn);
        goBack = findViewById(R.id.goBack);
    }
}
