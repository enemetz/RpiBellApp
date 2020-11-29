package com.example.rpibell;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    public String email;
    public String password;

    public Button back;                     // back button on the page
    public Button resetEmail;
    public ToggleButton setNotification;    // toggle button to set notifications on or off
    public ToggleButton setPictureCapture;  // toggle button to turn on/off picture capture

    /**
     * This method will be used in order to set up the Settings Page.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);

        // set/save the IP address of the user's Raspberry Pi device
        IP = getIntent().getExtras().getString("IP");

        // save the username
        userName = getIntent().getExtras().getString("user");

        // save the user's token
        token = getIntent().getExtras().getString("token");

        email = getIntent().getExtras().getString("email");

        password = getIntent().getExtras().getString("password");


        //Gets the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        resetEmail = findViewById(R.id.ChangeEmailButton);
        resetEmail.setOnClickListener(view -> {
            // Creates an alert to get text from the user to change their email
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Change email").setMessage("What would you like to change your email to?");

            // Text field to add to the alert dialog
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            builder.setView(input);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Continue with delete operation
                    user.updateEmail(input.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Email updated", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            });

            // A null listener allows the button to dismiss the dialog and take no further action.
            builder.setNegativeButton(android.R.string.no, null);

            builder.show();
        });



        // once the back button is pressed, request the raspberry pi to end the live stream and then take the user back to the homepage
        back = this.<Button>findViewById(R.id.backFromSettingsToHome);
        back.setOnClickListener(view -> {
            try
            {
                Intent intent = new Intent(SettingsPage.this, UserHomePage.class);
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



        // go through Notification.txt and check if the user wants notifications or not
        String getNotif = null;
        try {
            getNotif = new getNotifStatus().execute(userName).get();
            Log.e("Notification.txt Text",getNotif);
        } catch (Exception e) {
            Log.e("Status","Cannot read from Notification.txt");
            Toast.makeText(this, "ERROR OCCURRED. CANNOT RETRIEVE USER PREFERENCES ... ",Toast.LENGTH_LONG).show();
            getNotif = "YES";
        }
        if (getNotif.equals("NO")) {
            setNotification = findViewById(R.id.notificationToggleButton);
            setNotification.setChecked(false);
        }

        // once the switch is turned on, the RPi Device must try to detect for motion and send notifications (if the user wants that)
        setNotification = findViewById(R.id.notificationToggleButton);
        setNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                try {
                    String[] params = {IP,userName,token};
                    String sentToken = new sendUserAndToken().execute(params).get();
                    if (sentToken.equals("DONE")) {
                        new writeNotifYES().execute(userName);
                        Toast.makeText(getApplicationContext(), "NOTIFICATIONS ON", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Status", "Cannot connect to PiBell.");
                        Toast.makeText(this, " CANNOT CONNECT TO PiBELL. PLEASE REFER TO THE HELP PAGE IN SETTINGS. ",Toast.LENGTH_LONG).show();
                        setNotification.setChecked(false);
                    }
                } catch (Exception e) {
                    Log.e("Status", "Error occurred.");
                    Toast.makeText(this, "ERROR OCCURRED. CANNOT RETRIEVE USER PREFERENCES ... ",Toast.LENGTH_LONG).show();
                    setNotification.setChecked(false);
                }
            }
            else {
                try {
                    String[] params = {IP,userName};
                    String stopNotifsReturn = new stopNotifs().execute(params).get();
                    Toast.makeText(this, stopNotifsReturn,Toast.LENGTH_LONG).show();

                    if (stopNotifsReturn.equals("DONE")) {
                        Toast.makeText(this, "HERE 2",Toast.LENGTH_LONG).show();

                        new writeNotifNO().execute(userName);


                        Toast.makeText(getApplicationContext(), "NOTIFICATIONS OFF", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Status", "Cannot connect to PiBell.");
                        Toast.makeText(this, " CANNOT CONNECT TO PiBELL. PLEASE REFER TO THE HELP PAGE IN SETTINGS. ",Toast.LENGTH_LONG).show();
                        setNotification.setChecked(true);
                    }
                } catch (Exception e) {
                    Log.e("Status", "Error occurred.");
                    //Toast.makeText(this, "ERROR OCCURRED. CANNOT RETRIEVE USER PREFERENCES ... ",Toast.LENGTH_LONG).show();
                    setNotification.setChecked(true);
                }
            }
        });





        String wantPicCap = null;
        try{
            wantPicCap = new wantPicCapture().execute(userName).get();
            Log.e("PictureCapture.txt Text",wantPicCap);
        } catch(Exception e) {
            Log.e("Status","Cannot read from Notification.txt");
            Toast.makeText(this, "ERROR OCCURRED. CANNOT RETRIEVE USER PREFERENCES ... ",Toast.LENGTH_LONG).show();
            wantPicCap = "YES";
        }
        if (wantPicCap.equals("NO")) {
            setPictureCapture = findViewById(R.id.pictureCaptureToggleButton);
            setPictureCapture.setChecked(false);
        }

        setPictureCapture = findViewById(R.id.pictureCaptureToggleButton);
        setPictureCapture.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                try {
                    String picCapOn = new turnPictureCaptureON().execute(IP).get();
                    if (picCapOn.equals("DONE")) {
                        new writePicCapYES().execute(userName);
                        Toast.makeText(getApplicationContext(), "PICTURE CAPTURE ON", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Status", "Cannot connect to PiBell.");
                        Toast.makeText(this, " CANNOT CONNECT TO PiBELL. PLEASE REFER TO THE HELP PAGE IN SETTINGS. ",Toast.LENGTH_LONG).show();
                        setPictureCapture.setChecked(false);
                    }
                } catch (Exception e) {
                    Log.e("Status", "Error occurred.");
                    Toast.makeText(this, "ERROR OCCURRED. CANNOT RETRIEVE USER PREFERENCES ... ",Toast.LENGTH_LONG).show();
                    setPictureCapture.setChecked(false);
                }
            } else {
                try {
                    String picCapOff = new turnPictureCaptureOFF().execute(IP).get();
                    if (picCapOff.equals("DONE")) {
                        new writePicCapNO().execute(userName);
                        Toast.makeText(getApplicationContext(), "PICTURE CAPTURE OFF", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Status", "Cannot connect to PiBell.");
                        Toast.makeText(this, " CANNOT CONNECT TO PiBELL. PLEASE REFER TO THE HELP PAGE IN SETTINGS. ",Toast.LENGTH_LONG).show();
                        setPictureCapture.setChecked(true);
                    }
                } catch (Exception e) {
                    Log.e("Status", "Error occurred.");
                    Toast.makeText(this, "ERROR OCCURRED. CANNOT RETRIEVE USER PREFERENCES ... ",Toast.LENGTH_LONG).show();
                    setPictureCapture.setChecked(true);
                }
            }
        });



    } // ends the onCreate() method



    /**
     * This is the getNotifStatus class that will be used to check the user's Notification.txt to see if
     * they set notifications on or off.
     */
    public class getNotifStatus extends AsyncTask<String, Integer, String> {

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
    } // ends the getNotifStatus class





    /**
     * This is the stopNotifs class that will be used to tell the server that the user doesn't want notifications
     */
    public class stopNotifs extends AsyncTask<String, Integer, String> {
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
                return "DONE";
            } catch (Exception e) {
                return "FAIL";
            }
        } // ends the doInBackground() method
    } // ends the stopNotifs class


    /**
     * This is the writeNotifNO class that will be used to write "NO" into Notification.txt
     * since the user does not want notifications
     */
    public class writeNotifNO extends AsyncTask<String, Integer, String> {

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
    } // ends the writeNotifNO class


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
     * This is the writeNotifYES class that will be used to write "YES" into Notification.txt
     * since the user wants notifications
     */
    public class writeNotifYES extends AsyncTask<String, Integer, String> {

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
    } // ends the writeNotifYES class


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

    /**
     * This is the writePicCapYES class that will be used to write "YES" into PictureCapture.txt
     * since the user wants picture capture.
     */
    public class writePicCapYES extends AsyncTask<String, Integer, String> {

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
            File file = new File(dir, "PictureCapture.txt");

            boolean exists = file.exists();
            // read from file
            if (exists == true) {
                file.delete();
                File newFile = new File(dir, "PictureCapture.txt");
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
    } // ends the writePicCapYES class

    /**
     * This is the writePicCapNO class that will be used to write "NO" into PictureCapture.txt
     * since the user wants picture capture.
     */
    public class writePicCapNO extends AsyncTask<String, Integer, String> {

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
            File file = new File(dir, "PictureCapture.txt");

            boolean exists = file.exists();
            // read from file
            if (exists == true) {
                file.delete();
                File newFile = new File(dir, "PictureCapture.txt");
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
    } // ends the writePicCapNO class

} // ends the UserHomePage class