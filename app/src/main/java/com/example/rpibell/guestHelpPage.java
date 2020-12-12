package com.example.rpibell;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class guestHelpPage extends AppCompatActivity {
    // Global variables
    public String userName;                     // current user
    public String IP;                           // IP address of the user's Raspberry Pi device
    public String token;                        // user's current token
    public String email;                        // user's email
    public String password;                     // user's password

    public TextView currentStatus;              // will let the user know the current connection status between the app and the PiBell
    public Button retryButton;                  // allow the user to retry the connection
    public FloatingActionButton backButton;     // go back to the settings page

    /**
     * This method will be used in order to set up the Admin Help Page.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_help_page);

        // get all the data from the last Activity
        IP = getIntent().getExtras().getString("IP");
        userName = getIntent().getExtras().getString("user");
        token = getIntent().getExtras().getString("token");
        email = getIntent().getExtras().getString("email");
        password = getIntent().getExtras().getString("password");


        // update the connection status
        currentStatus = findViewById(R.id.GuesthelpPageCurrentConnectionStatus);
        try {
            String getConStatus = new isConnected().execute(IP).get();
            currentStatus.setText(getConStatus);
        } catch (Exception ex) {
            // assume bad connection ...
            String bad = "BAD";
            currentStatus.setText(bad);
        }


        // retry connection and update the connection status
        retryButton = findViewById(R.id.GuesthelpPageRetryButton);
        retryButton.setOnClickListener(task -> {
            try {
                String getConStatus = new isConnected().execute(IP).get();
                currentStatus.setText(getConStatus);
            } catch (Exception ex) {
                // assume bad connection ...
                String bad = "BAD";
                currentStatus.setText(bad);
            }
        });


        // get back to the guest's help page
        backButton = findViewById(R.id.guestHelpPageBackButton);
        backButton.setOnClickListener(task -> {
            Intent intent = new Intent(this, GuestSettingsPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });

    } // ends the onCreate

    /**
     * This is the isConnected class that will be used to check if the app is correctly
     * connected to the PiBell.
     */
    public class isConnected extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will check the connection.
         * @param params IP
         * @return "GOOD" or "BAD"
         */
        @Override
        protected String doInBackground(String[] params) {
            try {
                // set local variables
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(params[0],RPiDeviceMainServerPort),2000);
                DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
                DataInputStream din=new DataInputStream(socket.getInputStream());

                // tell the server to arm the doorbell
                dout.writeUTF("Testing");
                dout.flush();

                // server responds : "OK"
                din.readUTF();

                // close all
                dout.close();
                din.close();
                socket.close();
                return "GOOD";
            } catch (Exception e) {
                return "BAD";
            }
        } // ends the doInBackground() method
    } // ends the isConnected class

} // ends the class
