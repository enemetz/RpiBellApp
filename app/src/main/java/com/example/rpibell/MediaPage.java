package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * This is the MediaPage class that will show the admin user all the media that has been received
 * and saved.
 */
public class MediaPage extends AppCompatActivity {

    // Global variables
    public String userName;                 // current user
    public String IP;                       // IP address of the user's Raspberry Pi device
    public String token;                    // user's current token
    public String email;                    // user's email
    public String password;                 // user's password

    public ArrayList<String> picsToRemove;  // List of pics to remove
    public Button clearButton;              // button used to clear pics from storage
    public Button refreshButton;            // button allows user to refresh the screen and get any new media
    public FloatingActionButton backButton; // back button to go back to the home page
    public LinearLayout linearView;         // the linear (scrollable) layout to use to insert pics and text

    public final int WAIT = 2000;           // amount of time to pause the app in order to give Raspberry Pi Camera time to warm up

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

        // obtain any new pictures that were taken by the PiBell
        refreshButton = findViewById(R.id.RefreshPicsButton);
        refreshButton.setOnClickListener(refresh -> {
            // load any new pictures from the PiBell
            String[] args = {IP,userName};
            new getNewMedia().execute(args);
            Toast.makeText(MediaPage.this,"Loading Media ...", Toast.LENGTH_LONG).show();
            SystemClock.sleep(WAIT);

            // send to the media page
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

        // display pictures one by one
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



    /**
     * This is the getNewMedia class that will be used to request the raspberry pi device to send over any new pictures
     * that were taken.
     */
    public class getNewMedia extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to request the main server on the device to send over any new pictures.
         * @param params the IP address of the raspberry pi device
         * @return null since nothing else is needed
         */
        @Override
        protected String doInBackground(String[] params) {

            Context context = getApplicationContext();

            try {
                // set local variables
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(params[0],RPiDeviceMainServerPort),2000);
                DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
                DataInputStream din=new DataInputStream(socket.getInputStream());

                // tell the server to end the live
                dout.writeUTF("Send Pics");
                dout.flush();

                // server responds : number of pics
                String numberOfPics = din.readUTF();
                Log.e("NUM PICS",numberOfPics);

                // say OK
                dout.writeUTF("OK");
                dout.flush();

                // now server will send those pics here
                int num = Integer.parseInt(numberOfPics);
                for (int i = 1 ; i <= num ; ++i) {
                    // get the name of the pic and create file
                    String picName = din.readUTF();
                    Log.e("PIC NAME",picName);

                    // say OK
                    dout.writeUTF("OK");
                    dout.flush();

                    // get the size of the file
                    String picSize = din.readUTF();
                    Log.e("PIC SIZE",picSize);

                    // say OK
                    dout.writeUTF("OK");
                    dout.flush();

                    // get new pic file ready
                    File dir = context.getDir(params[1], Context.MODE_PRIVATE);         //Creating an internal dir;
                    File file = new File(dir, picName);                                 //Getting a file within the dir.
                    FileOutputStream filePtr = new FileOutputStream(file);

                    Log.e("fileMade","" + file.getName());

                    // copy the bytes to buffer
                    int count;
                    int fileSize = Integer.parseInt(picSize);
                    byte[] buffer = new byte[4096]; // or 4096, or more
                    while ((count = din.read(buffer)) > 0)
                    {
                        fileSize -= count;
                        if (fileSize <= 0) {
                            break;
                        }
                        filePtr.write(buffer, 0, count);
                    } // ends the while-loop
                    Log.e("status","File received");

                    // write the bytes into the file and the CLOSE it
                    filePtr.close();

                    // say OK
                    dout.writeUTF("OK");
                    dout.flush();
                } // ends the for-loop

                // server sends last OK
                din.readUTF();

                // close all
                dout.close();
                din.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        } // ends the doInBackground() method
    } // ends the getNewMedia class

} // ends the MediaPage
