package com.example.rpibell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This is the AdminInfoPage that is used to display all the current guests of the admin.
 */
public class AdminInfoPage extends AppCompatActivity {
    // Global variables
    public String userName;                         // current user
    public String IP;                               // IP address of the user's Raspberry Pi device
    public String token;                            // user's current token
    public String email;                            // user's email
    public String password;                         // user's password

    public FloatingActionButton backButton;         // back Button
    public TextView AdminID;                        // AdminID display
    public TextView Hostname;                       // Admin's PiBell Hostname Display

    private FirebaseAuth mAuth;                     // access the Firebase Authentication
    private FirebaseFirestore db;                   // access to the Firebase Firestore Database

    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_info_page);

        // get all the data from the past Activity
        IP = getIntent().getExtras().getString("IP");
        userName = getIntent().getExtras().getString("user");
        token = getIntent().getExtras().getString("token");
        email = getIntent().getExtras().getString("email");
        password = getIntent().getExtras().getString("password");

        // connect to the Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // connect to the Firebase Firestore (database that stores user Info)
        db = FirebaseFirestore.getInstance();

        backButton = findViewById(R.id.AdminInfoPageBackButton);
        backButton.setOnClickListener(back -> {
            Intent intent = new Intent(AdminInfoPage.this, GuestManagement.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });

        // fill in the AdminID and Hostname Fields
        // use the userID to look up user info in the DB
        FirebaseUser user = mAuth.getCurrentUser();
        Log.e("userID",user.getUid());

        AdminID = findViewById(R.id.AdminID);
        AdminID.setText(user.getUid());

        // get user document (info) from list of admin users
        DocumentReference adminUsers = db.collection("admins").document(user.getUid());
        adminUsers.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                DocumentSnapshot document = task1.getResult();
                if (document.exists()) {
                    String hostName = document.getString("hostname");
                    Hostname = findViewById(R.id.PiBellHostname);
                    Hostname.setText(hostName);
                } else {
                    Toast.makeText(this,"PROBLEM OBTAINING USER INFO. TRY AGAIN ...",Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this,"EXPERIENCING CONNECTION ISSUES. TRY AGAIN ...",Toast.LENGTH_LONG).show();
            }
        });
    } // ends onCreate()
} // ends AdminInfoPage class