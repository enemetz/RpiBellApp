package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MediaPage extends AppCompatActivity {

    // Global variables
    public String userName;                 // current user
    public String IP;                       // IP address of the user's Raspberry Pi device
    public String token;                    // user's current token
    public String email;                    // user's email
    public String password;                 // user's password

    public ArrayList<String> picsToRemove;  // List of pics to remove
    public Button clearButton;              // button used to clear pics from storage
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

        // get all info from last Activity
        IP = getIntent().getExtras().getString("IP");
        userName = getIntent().getExtras().getString("user");
        token = getIntent().getExtras().getString("token");
        email = getIntent().getExtras().getString("email");
        password = getIntent().getExtras().getString("password");

        // instantiate the arrayList to store the names of files the user wants to delete
        picsToRemove = new ArrayList<>();

        // the button used to clear the selected pics
        clearButton = findViewById(R.id.DeletePicsButton);
        clearButton.setVisibility(View.INVISIBLE);
        clearButton.setOnClickListener(task -> {
            Context context = getApplicationContext();
            File dir = context.getDir(userName, Context.MODE_PRIVATE);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg"));
            // delete the selected pics
            for (File file : files) {
              if (picsToRemove.contains(file.getName().substring(0,file.getName().length()-4))) {
                  file.delete();
              }
            }
            picsToRemove.clear();
            Toast.makeText(this,"Updating...",Toast.LENGTH_LONG).show();
            // refresh the activity
            Intent intent = new Intent(MediaPage.this, MediaPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });

        // allows user to go back to the home page
        backButton = findViewById(R.id.PicsPageBackButton);
        backButton.setOnClickListener(view -> {
            try
            {
                Intent intent = new Intent(MediaPage.this, UserHomePage.class);
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
        linearView = findViewById(R.id.PicsLinearView);

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
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setImageBitmap(myBitmap);
            imageView.setPadding(0,60,0,-120);
            linearView.addView(imageView);

            CheckBox checkBox = new CheckBox(getApplicationContext());
            checkBox.setText(file.getName().substring(0,file.getName().length()-4));
            checkBox.setTextColor(Color.WHITE);
            checkBox.setPadding(200,0,0,0);
            checkBox.setHighlightColor(Color.WHITE);

            checkBox.setOnClickListener(task -> {
                if (checkBox.isChecked()) {
                    picsToRemove.add(checkBox.getText().toString());
                    if (!picsToRemove.isEmpty()) {
                        clearButton.setVisibility(View.VISIBLE);
                    } else {
                        clearButton.setVisibility(View.INVISIBLE);
                    }
                } else {
                    picsToRemove.remove(checkBox.getText().toString());
                    if (!picsToRemove.isEmpty()) {
                        clearButton.setVisibility(View.VISIBLE);
                    } else {
                        clearButton.setVisibility(View.INVISIBLE);
                    }
                }
            });
            linearView.addView(checkBox);
        }

    } // ends the onCreate() method

} // ends the MediaPage
