package com.example.rpibell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

import java.util.ArrayList;
import java.util.List;

public class GuestManagement extends AppCompatActivity {

    public Button addGuest;

    public FirebaseAuth mAuth;
    public FirebaseFirestore db;
    public FirebaseUser user;
    public List<Guest> guests;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_management);

        // Initialize Firebase user and Firestore database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        guests = retrieve_guests();

        Log.d("TAG", guests.toString());

        //guests.add(new Guest("test1", "test@email.com"));

        // Set up list of guests
        RecyclerView guestMgmtRecyclerView = (RecyclerView) findViewById(R.id.guestMgmtRecyclerView);
        RecyclerView_Adapter adapter = new RecyclerView_Adapter(guests, getApplication());
        guestMgmtRecyclerView.setAdapter(adapter);
        guestMgmtRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adding a guest
        addGuest = findViewById(R.id.addGuest);
        addGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GuestManagement.this, AddingGuest.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Retrieve guests associated with currently signed-in Admin account.
     * @return List of guests
     */
    public List<Guest> retrieve_guests() {
        guests = new ArrayList<>();

        if (user != null) {    // if admin is logged in
            // Retrieve elements of sub-collection
            db.collection("admins")
                    .document(user.getUid())
                    .collection("guests")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    guests.add(new Guest(document.get("name").toString(),
                                            document.get("email").toString()));
                                    Log.d("TAG", document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }
                            //Log.d("TAG", guests.toString());
                        }
                    });
            Log.d("TAG", guests.toString());
       }
        //Log.d("TAG", guests.toString());
        return guests;
    }

}
