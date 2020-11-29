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
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
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
import java.net.InetSocketAddress;
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
    public Button notificationPage;        // button used to bring user to the notifications page to view past notifications
    public Switch armDeviceSwitch;         // switch that is used to arm/disarm the RPi device
    public Button manageGuests;            // Go to Guest Management Profile

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
        setContentView(R.layout.user_page);

        // set the welcome bar at the top of the page to greet the user with their username
        welcomeBanner = findViewById(R.id.UserPageWelcomeTitle);
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


        // go through Camera.txt and check if RPi Doorbell was armed or not
        String beenArmed = null;
        try {
            beenArmed = new checkIfArmed().execute(userName).get();
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
                new turnOnLiveStream().execute(IP);
                SystemClock.sleep(WAIT);    // give the camera at least 2 seconds to warm up

                Toast.makeText(UserHomePage.this,"LOADING STREAM..." , Toast.LENGTH_LONG).show();
                Intent intent = new Intent(UserHomePage.this, LiveViewPage.class);
                intent.putExtra("user", userName);
                intent.putExtra("IP",IP);
                intent.putExtra("token", token);
                intent.putExtra("email",email);
                intent.putExtra("password",password);
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
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
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
            intent.putExtra("email",email);
            intent.putExtra("password",password);
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
                try {
                    String wantPicCap = new wantPicCapture().execute(userName).get();
                    if (wantPicCap.equals("YES")) {
                        String turnPicCapOn = new turnPictureCaptureON().execute(IP).get();
                        if (turnPicCapOn.equals("DONE")) {
                            String armed = new armCamera().execute(IP).get();
                            if (armed.equals("DONE")) {
                                new writeArmed().execute(userName);
                                Toast.makeText(getApplicationContext(), "DOORBELL ARMED", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("Status","Can't connect to PiBell");
                                Toast.makeText(this,"PROBLEM CONNECTING TO PiBELL, PLEASE GO TO HELP PAGE LOCATED IN THE SETTINGS",Toast.LENGTH_LONG).show();
                                armDeviceSwitch.setChecked(false);
                            }
                        } else {
                            Log.e("Status","Can't connect to PiBell");
                            Toast.makeText(this,"PROBLEM CONNECTING TO PiBELL, PLEASE GO TO HELP PAGE LOCATED IN THE SETTINGS",Toast.LENGTH_LONG).show();
                            armDeviceSwitch.setChecked(false);
                        }
                    } else {
                        String turnPicCapOff = new turnPictureCaptureOFF().execute(IP).get();
                        if (turnPicCapOff.equals("DONE")) {
                            String armed = new armCamera().execute(IP).get();
                            if (armed.equals("DONE")) {
                                new writeArmed().execute(userName);
                                Toast.makeText(getApplicationContext(), "DOORBELL ARMED", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("Status","Can't connect to PiBell");
                                Toast.makeText(this,"PROBLEM CONNECTING TO PiBELL, PLEASE GO TO HELP PAGE LOCATED IN THE SETTINGS",Toast.LENGTH_LONG).show();
                                armDeviceSwitch.setChecked(false);
                            }
                        } else {
                            Log.e("Status","Can't connect to PiBell");
                            Toast.makeText(this,"PROBLEM CONNECTING TO PiBELL, PLEASE GO TO HELP PAGE LOCATED IN THE SETTINGS",Toast.LENGTH_LONG).show();
                            armDeviceSwitch.setChecked(false);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Status","Error reading from PictureCapture.txt file");
                    Toast.makeText(this,"ERROR OCCURRED. PLEASE TRY AGAIN ...",Toast.LENGTH_LONG).show();
                    armDeviceSwitch.setChecked(false);
                }
            } else {
                try {
                    String wantPicCap = new wantPicCapture().execute(userName).get();
                    if (wantPicCap.equals("YES")) {
                        String turnPicCapOn = new turnPictureCaptureON().execute(IP).get();
                        if (turnPicCapOn.equals("DONE")) {
                            String disarmed = new disarmCamera().execute(IP).get();
                            if (disarmed.equals("DONE")) {
                                new writeDisarmed().execute(userName);
                                Toast.makeText(getApplicationContext(), "DOORBELL DISARMED", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("Status","Can't connect to PiBell");
                                Toast.makeText(this,"PROBLEM CONNECTING TO PiBELL, PLEASE GO TO HELP PAGE LOCATED IN THE SETTINGS",Toast.LENGTH_LONG).show();
                                armDeviceSwitch.setChecked(true);
                            }
                        } else {
                            Log.e("Status","Can't connect to PiBell");
                            Toast.makeText(this,"PROBLEM CONNECTING TO PiBELL, PLEASE GO TO HELP PAGE LOCATED IN THE SETTINGS",Toast.LENGTH_LONG).show();
                            armDeviceSwitch.setChecked(true);
                        }
                    } else {
                        String turnPicCapOff = new turnPictureCaptureOFF().execute(IP).get();
                        if (turnPicCapOff.equals("DONE")) {
                            String disarmed = new disarmCamera().execute(IP).get();
                            if (disarmed.equals("DONE")) {
                                new writeDisarmed().execute(userName);
                                Toast.makeText(getApplicationContext(), "DOORBELL DISARMED", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("Status","Can't connect to PiBell");
                                Toast.makeText(this,"PROBLEM CONNECTING TO PiBELL, PLEASE GO TO HELP PAGE LOCATED IN THE SETTINGS",Toast.LENGTH_LONG).show();
                                armDeviceSwitch.setChecked(true);
                            }
                        } else {
                            Log.e("Status","Can't connect to PiBell");
                            Toast.makeText(this,"PROBLEM CONNECTING TO PiBELL, PLEASE GO TO HELP PAGE LOCATED IN THE SETTINGS",Toast.LENGTH_LONG).show();
                            armDeviceSwitch.setChecked(true);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Status","Error reading from PictureCapture.txt file");
                    Toast.makeText(this,"ERROR OCCURRED. PLEASE TRY AGAIN ...",Toast.LENGTH_LONG).show();
                    armDeviceSwitch.setChecked(true);
                }
            }
        });




        mediaPage = findViewById(R.id.storedMediaButton);
        mediaPage.setOnClickListener(view -> {
            // load any new pictures from the PiBell
            String[] args = {IP,userName};
            new getNewMedia().execute(args);
            Toast.makeText(this,"Loading Media ...", Toast.LENGTH_LONG).show();
            SystemClock.sleep(WAIT);

            // send to the media page
            Intent intent = new Intent(UserHomePage.this, MediaPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });




        notificationPage = findViewById(R.id.notificationsButton);
        notificationPage.setOnClickListener(task -> {
            // load any new notifications from PiBell
            String[] args = {IP,userName};
            new getNewNotifications().execute(args);
            Toast.makeText(this,"Loading Past Notifications ...", Toast.LENGTH_LONG).show();
            SystemClock.sleep(WAIT);

            // send to the notification page
            Intent intent = new Intent(UserHomePage.this, NotificationPage.class);
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
     * This is the turnOnLiveStream class that will be used to request the raspberry pi device to turn on the live stream.
     * This will be executed in the background.
     */
    public class turnOnLiveStream extends AsyncTask<String, Integer, String> {
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
    } // ends the turnOnLiveStream class





    /**
     * This is the armCamera class that will be used to request the raspberry pi device to arm the camera.
     */
    public class armCamera extends AsyncTask<String, Integer, String> {
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
                return "DONE";
            } catch (Exception e) {
                return "FAIL";
            }
        } // ends the doInBackground() method
    } // ends the armCamera class



    /**
     * This is the disarmCamera class that will be used to request the raspberry pi device to disarm the camera.
     */
    public class disarmCamera extends AsyncTask<String, Integer, String> {
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
                return "DONE";
            } catch (Exception e) {
                return "FAIL";
            }
        } // ends the doInBackground() method
    } // ends the disarmCamera class





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




    /**
     * This is the checkIfArmed class that will be used to check the user's Camera.txt to see if the device has been armed already or not.
     */
    public class checkIfArmed extends AsyncTask<String, Integer, String> {

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
    } // ends the checkIfArmed class





    /**
     * This is the writeArmed class that will be used to write into the user's Camera.txt, "armed".
     */
    public class writeArmed extends AsyncTask<String, Integer, String> {

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
            } else {
                // user doesn't have file in there, need to create new one and mark it unarmed
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
    } // ends the writeArmed class




    /**
     * This is the writeDisarmed class that will be used to check the user's Camera.txt to see if the device has been armed already or not.
     */
    public class writeDisarmed extends AsyncTask<String, Integer, String> {

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
    } // ends the writeDisarmed class




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
                Socket socket=new Socket(params[0],RPiDeviceMainServerPort);
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
     * This is the wantPicCapture class that will be used to check the user's PictureCapture.txt to see if
     * they set Picture Capture on or off.
     */
    public class wantPicCapture extends AsyncTask<String, Integer, String> {

        /**
         * This method will check the PictureCapture.txt.
         * @param params username
         * @return armed or disarmed
         */
        @Override
        protected String doInBackground(String[] params) {
            Context context = getApplicationContext();

            // get to the settings page
            File dir = context.getDir(params[0], Context.MODE_PRIVATE);
            File file = new File(dir, "PictureCapture.txt");

            boolean exists = file.exists();
            // read from file
            if (exists == true) {
                Log.e("PictureCapture.txt status","exist");
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
                Log.e("PictureCapture.txt status","doesn't exist");
                FileWriter writer = null;
                try {
                    writer = new FileWriter(file);
                    writer.append("YES");   // default; want Picture Capture
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "YES";
            }
        } // ends the doInBackground() method
    } // ends the wantPicCapture class




    /**
     * This is the turnPictureCaptureON class that will be used to tell the PiBell that picture
     * capture should be on.
     */
    public class turnPictureCaptureON extends AsyncTask<String, Integer, String> {
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
                dout.writeUTF("Pic Capture ON");
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
    } // ends the turnPictureCaptureON class




    /**
     * This is the turnPictureCaptureOFF class that will be used to tell the PiBell that picture
     * capture should be off.
     */
    public class turnPictureCaptureOFF extends AsyncTask<String, Integer, String> {
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
                dout.writeUTF("Pic Capture OFF");
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
    } // ends the turnPictureCaptureOFF class



} // ends the UserHomePage class