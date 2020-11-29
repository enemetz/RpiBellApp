package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuestManagement extends AppCompatActivity {
    // Global variables
    public String userName;                 // current user
    public String IP;                       // IP address of the user's Raspberry Pi device
    public String token;                    // user's current token
    public String email;
    public String password;

    public Button addGuest;
    public Button getAdminInfo;
    public FloatingActionButton backButton;

    public FirebaseAuth mAuth;
    public FirebaseFirestore db;
    public FirebaseUser user;

    public List<Guest> guests;
    public LinearLayout linearView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_management);

        // Initialize Firebase user and Firestore database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        // set/save the IP address of the user's Raspberry Pi device
        IP = getIntent().getExtras().getString("IP");

        // save the username
        userName = getIntent().getExtras().getString("user");

        // save the user's token
        token = getIntent().getExtras().getString("token");

        email = getIntent().getExtras().getString("email");

        password = getIntent().getExtras().getString("password");

        // get list of the all the guests of the current user and display them
        guests = new ArrayList<>();
        if (user != null) {
            db.collection("admins").document(user.getUid()).collection("guests").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        guests.add(new Guest(document.get("name").toString(),
                                document.get("email").toString()));
                        Log.d("during", document.getId() + " => " + document.getData());
                    }
                    Log.d("AFTER", guests.toString());

                    linearView = findViewById(R.id.GuestList);
                    if (guests.size() < 1) {
                        Toast.makeText(getApplicationContext(), "NO ADDED GUESTS", Toast.LENGTH_LONG).show();
                    } else {
                        for (Guest guest : guests) {
                            String label = guest.name + " , " + guest.email;
                            TextView text = new TextView(this);
                            text.setTextSize(18);
                            text.setPadding(0,50,0,50);
                            text.setText(label);
                            linearView.addView(text);
                        }
                    }

                } else {
                    Log.e("TAG", "Error getting documents: ", task.getException());
                    Toast.makeText(this, "ERROR OBTAINING USER INFO. PLEASE TRY AGAIN...", Toast.LENGTH_LONG).show();
                }
            });
        }

        // Adding a guest
        addGuest = findViewById(R.id.addGuest);
        addGuest.setOnClickListener(view -> {
            Intent intent = new Intent(GuestManagement.this, AddingGuest.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });

        getAdminInfo = findViewById(R.id.adminInfoButton);
        getAdminInfo.setOnClickListener(get -> {
            Intent intent = new Intent(GuestManagement.this, AdminInfoPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });

        // add back button
        backButton = findViewById(R.id.backButtonManageGuestPage);
        backButton.setOnClickListener(task -> {
            Intent intent = new Intent(GuestManagement.this, UserHomePage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });
    }
}
