package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * This is the GuestNotificationPage Class where the guest will have the ability to view all the past
 * notifications that they have received.
 */
public class GuestNotificationPage extends AppCompatActivity {
    // Global variables
    public String userName;                 // current user
    public String IP;                       // IP address of the user's Raspberry Pi device
    public String token;                    // user's current token
    public String email;                    // user's email
    public String password;                 // user's password

    public FloatingActionButton backButton; // back button to go back to the home page
    public LinearLayout linearView;         // the linear (scrollable) layout to use to insert pics and text
    public Button refreshButton;            // allow guest to refresh the page and get any new message logs
    public Button clearButton;              // allow the guest to clear selected notifications
    public ArrayList<String> notifsToRemove;// List of notifications to remove

    public final int WAIT = 2000;           // amount of time to pause the app in order to give Raspberry Pi Camera time to warm up


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


        // instantiate the arrayList to store the notifications that the user wants to delete
        notifsToRemove = new ArrayList<>();

        // the button used to clear the selected notifications
        clearButton = findViewById(R.id.GuestDeleteNotificsButton);
        clearButton.setVisibility(View.INVISIBLE);
        clearButton.setOnClickListener(task -> {
            Context context = getApplicationContext();
            File dir = context.getDir(userName, Context.MODE_PRIVATE);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
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
            Intent intent = new Intent(GuestNotificationPage.this, GuestNotificationPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();

        });


        // get any new message logs from the server
        refreshButton = findViewById(R.id.GuestRefreshNotificsButton);
        refreshButton.setOnClickListener(refresh -> {
            // load any new notifications from PiBell
            String[] args = {IP,userName};
            new getNewNotifications().execute(args);
            Toast.makeText(GuestNotificationPage.this,"Loading Message Logs ...", Toast.LENGTH_LONG).show();
            SystemClock.sleep(WAIT);

            // send to the notification page
            Intent intent = new Intent(GuestNotificationPage.this, GuestNotificationPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });


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
            Toast.makeText(getApplicationContext(), "NO SAVED MESSAGE LOGS", Toast.LENGTH_LONG).show();
        }

        // display each message timestamp one by one
        for (File file : files) {
            if (!file.getName().equals("Notification.txt")) {
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
                // set local variables
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

} // ends the class
