package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GuestNotificationPage extends AppCompatActivity {
    // Global variables
    public String userName;                 // current user
    public String IP;                       // IP address of the user's Raspberry Pi device
    public String token;                    // user's current token
    public String email;                    // user's email
    public String password;                 // user's password

    public FloatingActionButton backButton; // back button to go back to the home page
    public LinearLayout linearView;         // the linear (scrollable) layout to use to insert pics and text

    /**
     * This method will be used in order to set up the Notifications Page.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_notification_page);

        // get all the data from the past Activity
        IP = getIntent().getExtras().getString("IP");
        userName = getIntent().getExtras().getString("user");
        token = getIntent().getExtras().getString("token");
        email = getIntent().getExtras().getString("email");
        password = getIntent().getExtras().getString("password");


        // allows user to go back to the home page
        backButton = findViewById(R.id.GuestNotifsPageBackButton);
        backButton.setOnClickListener(view -> {
            try
            {
                Intent intent = new Intent(this, GuestHomePage.class);
                intent.putExtra("user", userName);
                intent.putExtra("IP",IP);
                intent.putExtra("token", token);
                intent.putExtra("email",email);
                intent.putExtra("password",password);
                startActivity(intent);
                finish();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // place on the screen where the pics, checkbox and text are being displayed
        linearView = findViewById(R.id.GuestNotifsLinearView);
        linearView.setGravity(Gravity.CENTER);
        Context context = getApplicationContext();
        File dir = context.getDir(userName, Context.MODE_PRIVATE); //Creating an internal dir;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        Arrays.sort(files);
        Collections.reverse(Arrays.asList(files));
        if (files.length < 2) {
            Toast.makeText(getApplicationContext(), "NO SAVED NOTIFICATIONS", Toast.LENGTH_LONG).show();
        }

        for (File file : files) {
            if (!file.getName().equals("Notification.txt")) {
                Log.e("file",file.getName());
                TextView text = new TextView(this);
                text.setTextSize(18);
                text.setPadding(30,50,0,50);
                text.setTextColor(Color.WHITE);
                text.setText(file.getName().substring(0,file.getName().length()-4));
                linearView.addView(text);
            }
        }

    } // ends the onCreate() method
}
