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
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class NotificationPage extends AppCompatActivity {
    // Global variables
    public String userName;                 // current user
    public String IP;                       // IP address of the user's Raspberry Pi device
    public String token;                    // user's current token
    public String email;                    // user's email
    public String password;                 // user's password

    public ArrayList<String> notifsToRemove;// List of notifications to remove
    public Button clearButton;              // button used to clear pics from storage
    public Button refreshButton;            // button allows user to refresh the page for new message logs
    public FloatingActionButton backButton; // back button to go back to the home page
    public LinearLayout linearView;         // the linear (scrollable) layout to use to insert pics and text

    public final int WAIT = 2000;           // amount of time to pause the app in order to give Raspberry Pi Camera time to warm up

    /**
     * This method will be used in order to set up the Notifications Page.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_page);

        // get all the extra info from the past Activity
        IP = getIntent().getExtras().getString("IP");
        userName = getIntent().getExtras().getString("user");
        token = getIntent().getExtras().getString("token");
        email = getIntent().getExtras().getString("email");
        password = getIntent().getExtras().getString("password");

        // instantiate the arrayList to store the notifications that the user wants to delete
        notifsToRemove = new ArrayList<>();

        // the button used to clear the selected notifications
        clearButton = findViewById(R.id.DeleteNotificsButton);
        clearButton.setVisibility(View.INVISIBLE);
        clearButton.setOnClickListener(task -> {
            Context context = getApplicationContext();
            File dir = context.getDir(userName, Context.MODE_PRIVATE);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));

            // create an arg list
            String[] args = new String[notifsToRemove.size()+1];
            args[0] = IP;
            int index = 0;
            for (int i = 1 ; i < args.length ; ++i) {
                args[i] = notifsToRemove.get(index);
                ++index;
            }

            // send this list to the PiBell to delete on the server side as well
            try {
                String clearing = new deleteNotifsOnServer().execute(args).get();
                if (clearing.equals("DONE")) {

                    // delete the selected pics
                    for (File file : files) {
                        if (notifsToRemove.contains(file.getName().substring(0,file.getName().length()-4))) {
                            file.delete();
                        }
                    }

                    Toast.makeText(this, "CLEARING ...",Toast.LENGTH_LONG).show();
                    SystemClock.sleep(2000); // wait 2 seconds for asynchronous process to delete all the notif logs on server side
                    notifsToRemove.clear();

                    // refresh the activity
                    Intent intent = new Intent(NotificationPage.this, NotificationPage.class);
                    intent.putExtra("user", userName);
                    intent.putExtra("IP",IP);
                    intent.putExtra("token", token);
                    intent.putExtra("email",email);
                    intent.putExtra("password",password);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "ERROR CONNECTING TO PiBELL. PLEASE TRY AGAIN ...",Toast.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                Toast.makeText(this, "ERROR OCCURRED, PLEASE TRY AGAIN ...",Toast.LENGTH_LONG).show();
            }
        });


        refreshButton = findViewById(R.id.RefreshNotificsButton);
        refreshButton.setOnClickListener(refresh -> {
            // load any new notifications from PiBell
            String[] args = {IP,userName};
            new getNewNotifications().execute(args);
            Toast.makeText(NotificationPage.this,"Loading Message Logs ...", Toast.LENGTH_LONG).show();
            SystemClock.sleep(WAIT);

            // send to the notification page
            Intent intent = new Intent(NotificationPage.this, NotificationPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });


        // allows user to go back to the home page
        backButton = findViewById(R.id.notifsPageBackButton);
        backButton.setOnClickListener(view -> {
            try
            {
                Intent intent = new Intent(NotificationPage.this, UserHomePage.class);
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
        linearView = findViewById(R.id.notifsLinearView);

        Context context = getApplicationContext();
        File dir = context.getDir(userName, Context.MODE_PRIVATE); //Creating an internal dir;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        Arrays.sort(files);
        Collections.reverse(Arrays.asList(files));
        if (files.length < 4) {
            Toast.makeText(getApplicationContext(), "NO SAVED MESSAGE LOGS", Toast.LENGTH_LONG).show();
        }

        for (File file : files) {
            if (!file.getName().equals("Camera.txt") && !file.getName().equals("Notification.txt") && !file.getName().equals("PictureCapture.txt")) {
                Log.e("file",file.getName());
                CheckBox checkBox = new CheckBox(getApplicationContext());
                checkBox.setText(file.getName().substring(0,file.getName().length()-4));
                checkBox.setTextColor(Color.WHITE);
                checkBox.setPadding(200,50,0,50);
                checkBox.setScaleX(1f);
                checkBox.setScaleY(1.5f);
                checkBox.setOnClickListener(task -> {
                    if (checkBox.isChecked()) {
                        notifsToRemove.add(checkBox.getText().toString());
                        if (!notifsToRemove.isEmpty()) {
                            clearButton.setVisibility(View.VISIBLE);
                        } else {
                            clearButton.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        notifsToRemove.remove(checkBox.getText().toString());
                        if (!notifsToRemove.isEmpty()) {
                            clearButton.setVisibility(View.VISIBLE);
                        } else {
                            clearButton.setVisibility(View.INVISIBLE);
                        }
                    }
                });
                linearView.addView(checkBox);
            }
        }

    } // ends the onCreate() method



    /**
     * This deleteNotifsOnServer class is used in order to delete any notifications that are stored on the PiBell device
     */
    public class deleteNotifsOnServer extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;    // port that the PiBell accepts commands from

        /**
         * This method will be used in order to request the main server on the device to send over any saved notifications.
         * @param params the IP address of the raspberry pi device
         * @return null since nothing else is needed
         */
        @Override
        protected String doInBackground(String[] params) {

            try {
                // set local variables
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(params[0],RPiDeviceMainServerPort),2000);
                DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
                DataInputStream din=new DataInputStream(socket.getInputStream());

                // tell the server to delete notifs
                dout.writeUTF("Delete Notifs");
                dout.flush();

                // server sends OK
                din.readUTF();

                // send server the number of notifs to delete
                int numberOfNotifsToDelete = params.length - 1;
                String stringNumNotifsToDelete = Integer.toString(numberOfNotifsToDelete);
                dout.writeUTF(stringNumNotifsToDelete);
                dout.flush();

                // server sends OK
                din.readUTF();

                // now send server all the names of notifications to delete
                for (int i = 1 ; i <= numberOfNotifsToDelete ; ++i) {
                    dout.writeUTF(params[i] + ".txt");
                    dout.flush();

                    // server sends OK
                    din.readUTF();

                } // ends the for-loop

                // close all
                dout.close();
                din.close();
                socket.close();
                return "DONE";
            } catch (Exception e) {
                Log.e("Status","Cannot connect to PiBell to get Notifications");
                return "FAIL";
            }
        } // ends method
    } // ends the deleteNotifsOnServer class



    /**
     * This getNewNotifications class is used in order to get any new notifications from the PiBell
     * to be stored on this device.
     */
    public class getNewNotifications extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;    // port that the PiBell accepts commands from

        /**
         * This method will be used in order to request the main server on the device to send over any saved notifications.
         * @param params the IP address of the raspberry pi device
         * @return null since nothing else is needed
         */
        @Override
        protected String doInBackground(String[] params) {

            Context context = getApplicationContext();          // used in order to get internal data

            try {
                // set local variables;
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(params[0],RPiDeviceMainServerPort),2000);
                DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
                DataInputStream din=new DataInputStream(socket.getInputStream());

                // tell the server to send notifs
                dout.writeUTF("Send Notifs");
                dout.flush();

                // server responds : number of notifications
                String numberOfNotifs = din.readUTF();
                Log.e("NUM NOTIFS",numberOfNotifs);

                // say OK
                dout.writeUTF("OK");
                dout.flush();

                // now server will send those pics here
                int num = Integer.parseInt(numberOfNotifs);
                for (int i = 1 ; i <= num ; ++i) {
                    // get the notif timestamp
                    String picName = din.readUTF();
                    Log.e("NOTIFICATION TIMESTAMP",picName);

                    // create new file
                    File dir = context.getDir(params[1], Context.MODE_PRIVATE);
                    File file = new File(dir, picName);

                    // write into file in order to create it (simple work-around after testing)
                    FileWriter writer = new FileWriter(file);
                    writer.append("notification");
                    writer.flush();
                    writer.close();

                    Log.e("NotifFileMade","" + file.getName());

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
                Log.e("Status","Cannot connect to PiBell to get Notifications");
            }
            return null;
        } // ends method
    } // ends the getNewNotifications class


}


