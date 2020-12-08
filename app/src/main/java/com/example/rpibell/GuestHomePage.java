package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class GuestHomePage extends AppCompatActivity {
    // Global variables
    public TextView welcomeBanner;         // Text at top that will be used in order to greet the user
    public String userName;                // current user

    public Button settings;                // settings button on the page
    public Button logOut;                  // log out button on the page
    public Button notificationPage;        // button used to bring user to the notifications page to view past notifications

    public String IP;                      // IP address of the user's Raspberry Pi device
    public String token;                   // user's current token

    public String email;
    public String password;

    public final int WAIT = 2000;          // amount of time to pause the app in order to give Raspberry Pi Camera time to warm up

    /**
     * This method will be used in order to set up the Home Page once user logs in.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_home_page);

        // set the welcome bar at the top of the page to greet the user with their username
        welcomeBanner = findViewById(R.id.guestWelcomePageBanner);
        userName = getIntent().getExtras().getString("user");
        welcomeBanner.append("Welcome, " + userName);

        // set/save the IP address of the user's Raspberry Pi device
        IP = getIntent().getExtras().getString("IP");

        // get the token
        token = getIntent().getExtras().getString("token");

        email = getIntent().getExtras().getString("email");

        password = getIntent().getExtras().getString("password");


        // check if the IP is null
        if (IP == null) {
            Toast.makeText(this,"CANNOT RESOLVE IP, PLEASE GO TO HELP PAGE LOCATED IN SETTINGS",Toast.LENGTH_LONG).show();
        } else {
            // check Notification.txt to see if token should be sent to the PiBell
            try {
                String wantNotifs = new wantNotifs().execute(userName).get();
                if (wantNotifs.equals("YES")) {
                    // send PiBell the username and token
                    String[] args = {IP,userName,token};
                    String sentUserAndToken = new sendUserAndToken().execute(args).get();
                    if (sentUserAndToken.equals("FAIL")) {
                        Toast.makeText(this,"PROBLEM CONNECTING TO PiBELL, PLEASE GO TO HELP PAGE LOCATED IN THE SETTINGS",Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e("Status","Error reading from Notification.txt");
            }
        }




        // once the Settings Button is pressed, go to the settings page
        settings = findViewById(R.id.guestSettingsButton);
        settings.setOnClickListener(view -> {
            Intent intent = new Intent(this, GuestSettingsPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });




        // once the Log Out button is pressed, go back to the log in page
        logOut = findViewById(R.id.guestLogOutButton);
        logOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginPage.class);
            startActivity(intent);
            finish();
        });



        notificationPage = findViewById(R.id.notificationLogButton);
        notificationPage.setOnClickListener(task -> {
            // load any new notifications from PiBell
            String[] args = {IP,userName};
            new getNewNotifications().execute(args);
            Toast.makeText(this,"Loading Past Notifications ...", Toast.LENGTH_LONG).show();
            SystemClock.sleep(WAIT);

            // send to the notification page
            Intent intent = new Intent(this, GuestNotificationPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });


    } // ends the onCreate() method



    /**
     * This is the wantNotifs class that will be used to check the user's Notification.txt to see if
     * they set notifications on or off.
     */
    public class wantNotifs extends AsyncTask<String, Integer, String> {

        /**
         * This method will check the Notifications.txt.
         * @param params username
         * @return armed or disarmed
         */
        @Override
        protected String doInBackground(String[] params) {
            Context context = getApplicationContext();

            // get to the settings page
            File dir = context.getDir(params[0], Context.MODE_PRIVATE);
            File file = new File(dir, "Notification.txt");

            boolean exists = file.exists();
            // read from file
            if (exists == true) {
                Log.e("Notification.txt status","exist");
                StringBuilder text = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                    }
                    br.close();
                } catch (IOException e) { e.printStackTrace(); }
                return text.toString();
            } else {    // user doesn't have file in there, need to create new one and mark it unarmed
                Log.e("Notification.txt status","doesn't exist");
                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    writer.append("YES");   // default; want notifications
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "YES";
            }
        } // ends the doInBackground() method
    } // ends the wantNotifs class



    /**
     * This is the sendUserAndToken class that will be used to send the RPi device the current username and token.
     */
    public class sendUserAndToken extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to update the main server's username and token
         * @param params the IP address of the raspberry pi device, username and token (respectively)
         * @return DONE if success, FAIL otherwise
         */
        @Override
        protected String doInBackground(String[] params) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(params[0],RPiDeviceMainServerPort),2000);
                DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
                DataInputStream din=new DataInputStream(socket.getInputStream());

                // tell the server to start the live
                dout.writeUTF("Log In");
                dout.flush();

                // server responds : "OK"
                din.readUTF();

                // send username
                dout.writeUTF(params[1]);
                dout.flush();

                // server responds : "OK"
                din.readUTF();

                // send token
                dout.writeUTF(params[2]);
                dout.flush();

                // server responds : "OK"
                din.readUTF();

                // close all
                dout.close();
                din.close();
                socket.close();

                return "DONE";
            } catch (Exception e) {
                Log.e("Connecting to PiBell","Couldn't connect");
                return "FAIL";
            }
        } // ends the doInBackground() method
    } // ends the sendUserAndToken class


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
