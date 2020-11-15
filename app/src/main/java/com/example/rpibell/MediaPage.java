package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class MediaPage extends AppCompatActivity {

    // Global variables
    public String userName;                 // current user
    public String IP;                       // IP address of the user's Raspberry Pi device
    public String token;                    // user's current token

    public FloatingActionButton backButton; // back button to go back to the home page
    public LinearLayout linearView;         // the linear (scrollable) layout to use to insert pics and text

    /**
     * This method will be used in order to set up the Settings Page.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_page);

        // disable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // set/save the IP address of the user's Raspberry Pi device
        IP = getIntent().getExtras().getString("IP");

        // save the username
        userName = getIntent().getExtras().getString("user");

        // save the user's token
        token = getIntent().getExtras().getString("token");

        backButton = findViewById(R.id.mediaPageBackButton);
        backButton.setOnClickListener(view -> {
            try
            {
                Intent intent = new Intent(MediaPage.this, UserHomePage.class);
                intent.putExtra("user", userName);
                intent.putExtra("IP",IP);
                intent.putExtra("token", token);
                startActivity(intent);
                finish();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        linearView = findViewById(R.id.mediaLinearView);

        Context context = getApplicationContext();
        File dir = context.getDir(userName, Context.MODE_PRIVATE); //Creating an internal dir;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg"));
        Arrays.sort(files);
        Collections.reverse(Arrays.asList(files));
        if (files.length == 0) {
            Toast.makeText(getApplicationContext(), "NO SAVED MEDIA", Toast.LENGTH_LONG).show();
        }
        for (File file : files) {
            Log.e("file",file.getName());
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ImageView imageView = new ImageView(this);
            TextView textView = new TextView(this);
            imageView.setImageBitmap(myBitmap);
            imageView.setPadding(40,0,40,0);
            textView.setText(file.getName().substring(0,file.getName().length()-4));
            textView.setPadding(250,-10,0,20);
            linearView.addView(imageView);
            linearView.addView(textView);
        }

    } // ends the onCreate() method
}
