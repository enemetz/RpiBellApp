package com.example.rpibell;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * This is the LiveViewPage class.
 * Here the user will be able to view the live stream from the raspberry pi.
 */
public class LiveViewPage extends AppCompatActivity {

    // Global variables
    public Button backButton;                       // back button on the page
    public Button picButton;                        // button used to take pic during live stream
    public WebView liveCam;                         // web viewer on the page
    public String userName;                         // username that needs to be passed between all pages
    public String IP;                               // IP address of the raspberry pi device playing live stream
    public final int liveStreamPort = 5000;         // specific port that the raspberry pi is using for the live stream

    /**
     * This method will be used in order to set up the Live View Page.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_stream_page);

        // disable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // get username to get back to user page
        userName = getIntent().getExtras().getString("user");
        IP = getIntent().getExtras().getString("IP");

        // set up the webView
        liveCam = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = liveCam.getSettings();
        webSettings.setJavaScriptEnabled(true);
        liveCam.setWebViewClient(new WebViewClient()); // forces all URLs to run only inside this app and doesn't redirect it to chrome
        liveCam.getSettings().setLoadWithOverviewMode(true);
        liveCam.getSettings().setUseWideViewPort(true);

        // load the live view from the raspberry pi using the IP address and live stream port
        String url = "http://" + IP + ":" + liveStreamPort + "/";
        liveCam.loadUrl(url);

        // once the back button is pressed, request the raspberry pi to end the live stream and then take the user back to the homepage
        backButton = this.<Button>findViewById(R.id.GoBackButtonLiveToHomePage);
        backButton.setOnClickListener(view -> {
            try
            {
                new NetTask1().execute(IP); // here tell, the server to turn off the live stream
                Intent intent = new Intent(LiveViewPage.this, UserHomePage.class);
                intent.putExtra("user", userName);
                intent.putExtra("IP",IP);
                startActivity(intent);
                finish();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // once the 'take pic' button is pressed, the user can take a picture of the video during the live stream
        picButton = this.<Button>findViewById(R.id.takePicButton);
        picButton.setOnClickListener(view -> {
            try
            {
                new NetTask2().execute(IP); // here tell, the server to turn off the live stream
                Toast.makeText(LiveViewPage.this,"TAKING PICTURE..." , Toast.LENGTH_LONG).show();
                SystemClock.sleep(2000);    // give the camera at least 2 seconds to warm up
                liveCam.loadUrl(url);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

    } // ends the onCreate() method

    /**
     * This is the NetTask class that will be used to request the raspberry pi device to turn off the live stream.
     * This will be executed in the background.
     */
    public static class NetTask1 extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;


        /**
         * This method will be used in order to request the main server on the device to turn off the live stream.
         * @param params the IP address of the raspberry pi device
         * @return null since nothing else is needed
         */
        @Override
        protected String doInBackground(String[] params) {
            Log.e("HERE1", params[0]);
            try {
                // set local variables
                Socket socket=new Socket(params[0],RPiDeviceMainServerPort);
                DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
                DataInputStream din=new DataInputStream(socket.getInputStream());

                // tell the server to end the live
                dout.writeUTF("EndLive");
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
        } // ends the doInBackground(0 method
    } // ends the NetTask class


    /**
     * This is the NetTask class that will be used to request the raspberry pi device to turn off the live stream.
     * This will be executed in the background.
     */
    public static class NetTask2 extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to request the main server on the device to turn off the live stream.
         * @param params the IP address of the raspberry pi device
         * @return null since nothing else is needed
         */
        @Override
        protected String doInBackground(String[] params) {
            Log.e("HERE2", params[0]);
            try {
                // set local variables
                Socket socket=new Socket(params[0],RPiDeviceMainServerPort);
                DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
                DataInputStream din=new DataInputStream(socket.getInputStream());

                // tell the server to end the live
                dout.writeUTF("Take Pic");
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
        } // ends the doInBackground(0 method
    } // ends the NetTask class
} // ends the LiveViewPage class