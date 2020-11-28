package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * This is the UserHomePage class.
 * This is the main page that the user is greeted with once they log in.
 */
public class UserHomePage extends AppCompatActivity {

    // Global variables
    public TextView welcomeBanner;         // Text at top that will be used in order to greet the user
    public String userName;                // current user

    public Button liveView;                // live view button on the page
    public Button settings;                // settings button on the page
    public Button logOut;                  // log out button on the page
    public Button mediaPage;               // button to bring user to media page to view pictures taken
    public Switch armDeviceSwitch;         // switch that is used to arm/disarm the RPi device
    public Button manageGuests;            // Go to Guest Management Profile

    public String IP;                      // IP address of the user's Raspberry Pi device
    public String token;                   // user's current token

    public final int WAIT = 2000;          // amount of time to pause the app in order to give Raspberry Pi Camera time to warm up


    /**
     * This method will be used in order to set up the Home Page once user logs in.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);



        // disable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);



        // set the welcome bar at the top of the page to greet the user with their username
        welcomeBanner = findViewById(R.id.UserPageWelcomeTitle);
        userName = getIntent().getExtras().getString("user");
        welcomeBanner.append("Welcome, " + userName);



        // set/save the IP address of the user's Raspberry Pi device
        IP = getIntent().getExtras().getString("IP");



        // get the token
        token = getIntent().getExtras().getString("token");



        // ask RPi device to send any new pictures (usually the case once detection has occurred)
        String[] args = {IP,userName};
        new NetTask3().execute(args);



        // go through Camera.txt and check if RPi Doorbell was armed or not
        String beenArmed = null;
        try {
            beenArmed = new NetTask4().execute(userName).get();
            Log.e("Camera.txt Text",beenArmed);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (beenArmed.equals("armed")) {
            armDeviceSwitch = findViewById(R.id.armDisarmSwitch);
            armDeviceSwitch.setChecked(true);
        }



        // once the Live View Button is pressed, the server must be notified to turn the live stream on
        liveView = findViewById(R.id.goToLiveViewButton);
        liveView.setOnClickListener(view -> {
            try
            {
                // tell the server to turn on the live stream, then go to the live stream page
                new NetTask().execute(IP);

                SystemClock.sleep(WAIT);    // give the camera at least 2 seconds to warm up

                Toast.makeText(UserHomePage.this,"LOADING STREAM..." , Toast.LENGTH_LONG).show();
                Intent intent = new Intent(UserHomePage.this, LiveViewPage.class);
                intent.putExtra("user", userName);
                intent.putExtra("IP",IP);
                intent.putExtra("token", token);
                startActivity(intent);
                finish();
            } catch (Exception e1) {
                Toast.makeText(UserHomePage.this,"ERROR LOADING STREAM ..." , Toast.LENGTH_LONG).show();
                e1.printStackTrace();
            }
        });


        // once Manage Guests button is pressed, go to Guest Management page
        manageGuests = findViewById(R.id.manageGuests);
        manageGuests.setOnClickListener(view -> {
            Intent intent = new Intent(UserHomePage.this, GuestManagement.class);
            startActivity(intent);
            finish();
        });


        // once the Settings Button is pressed, go to the settings page
        settings = findViewById(R.id.SettingsButton);
        settings.setOnClickListener(view -> {
            Intent intent = new Intent(UserHomePage.this, SettingsPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            startActivity(intent);
            finish();
        });



        // once the Log Out button is pressed, go back to the log in page
        logOut = findViewById(R.id.logOutButton);
        logOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserHomePage.this, LoginPage.class);
            startActivity(intent);
            finish();
        });





        // once the switch is turned on, the RPi Device must try to detect for motion and send notifications (if the user wants that)
        armDeviceSwitch = findViewById(R.id.armDisarmSwitch);
        armDeviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                Toast.makeText(getApplicationContext(), "DOORBELL ARMED", Toast.LENGTH_SHORT).show();
                new NetTask1().execute(IP);
                new NetTask5().execute(userName);
            }
            else {
                Toast.makeText(getApplicationContext(), "DOORBELL DISARMED", Toast.LENGTH_SHORT).show();
                new NetTask2().execute(IP);
                new NetTask6().execute(userName);
            }
        });



        mediaPage = findViewById(R.id.storedMediaButton);
        mediaPage.setOnClickListener(view -> {
            Intent intent = new Intent(UserHomePage.this, MediaPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            startActivity(intent);
            finish();
        });

    } // ends the onCreate() method

    /**
     * This is the NetTask class that will be used to request the raspberry pi device to turn on the live stream.
     * This will be executed in the background.
     */
    public class NetTask extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to request the main server on the device to start the live stream.
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

                // tell the server to start the live
                dout.writeUTF("StartLive");
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
    } // ends the NetTask class


    /**
     * This is the NetTask class that will be used to request the raspberry pi device to arm the camera.
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
                dout.writeUTF("Arm Doorbell");
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
     * This is the NetTask class that will be used to request the raspberry pi device to disarm the camera.
     */
    public class NetTask2 extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to request the main server on the device to disarm the camera.
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
                dout.writeUTF("Disarm Doorbell");
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
    } // ends the NetTask2 class





    /**
     * This is the NetTask class that will be used to request the raspberry pi device to send over the picture
     * that was taken during the live stream.
     */
    public class NetTask3 extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to request the main server on the device to send over the picture
         * taken during the live stream.
         * @param params the IP address of the raspberry pi device
         * @return null since nothing else is needed
         */
        @Override
        protected String doInBackground(String[] params) {

            Context context = getApplicationContext();

            try {
                // set local variables
                Socket socket=new Socket(params[0],RPiDeviceMainServerPort);
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

                    File dir = context.getDir(params[1], Context.MODE_PRIVATE); //Creating an internal dir;
                    File file = new File(dir, picName); //Getting a file within the dir.
                    FileOutputStream filePtr = new FileOutputStream(file);


                    //File file = new File(context.getFilesDir(), picName);
                    Log.e("fileMade","" + file.getName());
                    Log.e("pathOfFile","" + file.getAbsolutePath());
                    //FileOutputStream filePtr = openFileOutput(file.getName() , Context.MODE_PRIVATE);

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
    } // ends the NetTask3 class




    /**
     * This is the NetTask class that will be used to check the user's Camera.txt to see if the device has been armed already or not.
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
            File file = new File(dir, "Camera.txt");

            boolean exists = file.exists();
            // read from file
            if (exists == true) {
                Log.e("Camera.txt status","exist");
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
                Log.e("Camera.txt status","doesn't exist");
                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    writer.append("disarmed");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "disarmed";
            }
        } // ends the doInBackground() method
    } // ends the NetTask4 class





    /**
     * This is the NetTask class that will be used to check the user's Camera.txt to see if the device has been armed already or not.
     */
    public class NetTask5 extends AsyncTask<String, Integer, String> {

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
            File file = new File(dir, "Camera.txt");

            boolean exists = file.exists();
            // read from file
            if (exists == true) {
                file.delete();
                File newFile = new File(dir, "Camera.txt");
                FileWriter writer = null;
                try {
                    writer = new FileWriter(newFile);
                    writer.append("armed");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "armed";
            } else {    // user doesn't have file in there, need to create new one and mark it unarmed
                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    writer.append("armed");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "armed";
            }
        } // ends the doInBackground() method
    } // ends the NetTask5 class




    /**
     * This is the NetTask class that will be used to check the user's Camera.txt to see if the device has been armed already or not.
     */
    public class NetTask6 extends AsyncTask<String, Integer, String> {

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
            File file = new File(dir, "Camera.txt");

            boolean exists = file.exists();
            // read from file
            if (exists == true) {
                file.delete();
                File newFile = new File(dir, "Camera.txt");
                FileWriter writer = null;
                try {
                    writer = new FileWriter(newFile);
                    writer.append("disarmed");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "disarmed";
            } else {    // user doesn't have file in there, need to create new one and mark it unarmed
                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    writer.append("disarmed");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "disarmed";
            }
        } // ends the doInBackground() method
    } // ends the NetTask6 class


} // ends the UserHomePage class