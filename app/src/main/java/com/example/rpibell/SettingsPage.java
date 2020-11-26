package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * This is the SettingsPage class.
 * This is the settings page where the user will have the ability to change the notification settings or settings on the raspberry pi.
 */
public class SettingsPage extends AppCompatActivity {

    // Global variables
    public String userName;                 // current user
    public String IP;                       // IP address of the user's Raspberry Pi device
    public String token;                    // user's current token

    public Button back;                     // back button on the page
    public ToggleButton setNotification;    // toggle button to set notifications on or off

    /**
     * This method will be used in order to set up the Settings Page.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.settings_page);

        // disable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // set/save the IP address of the user's Raspberry Pi device
        IP = getIntent().getExtras().getString("IP");

        // save the username
        userName = getIntent().getExtras().getString("user");

        // save the user's token
        token = getIntent().getExtras().getString("token");

        // once the back button is pressed, request the raspberry pi to end the live stream and then take the user back to the homepage
        back = this.<Button>findViewById(R.id.backFromSettingsToHome);
        back.setOnClickListener(view -> {
            try
            {
                Intent intent = new Intent(SettingsPage.this, UserHomePage.class);
                intent.putExtra("user", userName);
                intent.putExtra("IP",IP);
                intent.putExtra("token", token);
                startActivity(intent);
                finish();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // go through Notification.txt and check if the user wants notifications or not
        String getNotif = null;
        try {
            getNotif = new NetTask().execute(userName).get();
            Log.e("Notification.txt Text",getNotif);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (getNotif.equals("NO")) {
            setNotification = findViewById(R.id.notificationToggleButton);
            setNotification.setChecked(false);
        }


        // once the switch is turned on, the RPi Device must try to detect for motion and send notifications (if the user wants that)
        setNotification = findViewById(R.id.notificationToggleButton);
        setNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                Toast.makeText(getApplicationContext(), "NOTIFICATIONS ON", Toast.LENGTH_SHORT).show();
                String[] params = {IP,userName,token};
                new NetTask3().execute(params);
                new NetTask4().execute(userName);
            }
            else {
                Toast.makeText(getApplicationContext(), "NOTIFICATIONS OFF", Toast.LENGTH_SHORT).show();
                String[] params = {IP,userName};
                new NetTask1().execute(params);
                new NetTask2().execute(userName);
            }
        });

    } // ends the onCreate() method



    /**
     * This is the NetTask class that will be used to check the user's Notification.txt to see if
     * they set notifications on or off.
     */
    public class NetTask extends AsyncTask<String, Integer, String> {

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
    } // ends the NetTask class





    /**
     * This is the NetTask class that will be used to tell the server that the user doesn't want notifications
     */
    public class NetTask1 extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to request the main server on the device to arm the camera.
         * @param params the IP address of the raspberry pi device
         * @return null since nothing else is needed
         */
        @Override
        protected String doInBackground(String[] params) {
            try {
                // set local variables
                Socket socket=new Socket(params[0],RPiDeviceMainServerPort);
                DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
                DataInputStream din=new DataInputStream(socket.getInputStream());

                // tell the server to arm the doorbell
                dout.writeUTF("Stop Notifications");
                dout.flush();

                // server responds : "OK"
                din.readUTF();

                // send username
                dout.writeUTF(params[1]);
                dout.flush();

                // server responds : "OK"
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
    } // ends the NetTask1 class


    /**
     * This is the NetTask class that will be used to write "NO" into Notification.txt
     * since the user does not want notifications
     */
    public class NetTask2 extends AsyncTask<String, Integer, String> {

        /**
         * This method will check the setting.txt.
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
                file.delete();
                File newFile = new File(dir, "Notification.txt");
                FileWriter writer = null;
                try {
                    writer = new FileWriter(newFile);
                    writer.append("NO");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "NO";
            } else {    // user doesn't have file in there, need to create new one and mark it unarmed
                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    writer.append("NO");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "NO";
            }
        } // ends the doInBackground() method
    } // ends the NetTask2 class


    /**
     * This is the NetTask class that will be used to send the RPi device the current username and token. (user wants notifications)
     */
    public class NetTask3 extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to update the main server's username and token
         * @param params the IP address of the raspberry pi device, username and token
         * @return null since nothing else is needed
         */
        @Override
        protected String doInBackground(String[] params) {
            try {
                //Log.e("username", params[1]);
                //Log.e("token", params[2]);
                // set local variables
                Socket socket=new Socket(params[0],RPiDeviceMainServerPort);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        } // ends the doInBackground() method
    } // ends the NetTask3 class


    /**
     * This is the NetTask class that will be used to write "YES" into Notification.txt
     * since the user wants notifications
     */
    public class NetTask4 extends AsyncTask<String, Integer, String> {

        /**
         * This method will check the setting.txt.
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
                file.delete();
                File newFile = new File(dir, "Notification.txt");
                FileWriter writer = null;
                try {
                    writer = new FileWriter(newFile);
                    writer.append("YES");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "YES";
            } else {    // user doesn't have file in there, need to create new one and mark it unarmed
                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    writer.append("YES");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "YES";
            }
        } // ends the doInBackground() method
    } // ends the NetTask4 class


} // ends the UserHomePage class