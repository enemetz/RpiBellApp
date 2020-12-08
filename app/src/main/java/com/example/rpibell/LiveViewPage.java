package com.example.rpibell;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * This is the LiveViewPage class.
 * Here the user will be able to view the live stream from the PiBell.
 */
public class LiveViewPage extends AppCompatActivity {

    // Global variables
    public FloatingActionButton backButton;         // back button for the page
    public Button picButton;                        // button used to take pic during live stream

    public WebView liveCam;                         // web viewer on the page

    public String userName;                         // username that needs to be passed between all pages
    public String IP;                               // IP address of the PiBell device playing live stream
    public String token;                            // user's current token

    public final int liveStreamPort = 5000;         // specific port that the PiBell is using for the live stream

    public String email;                            // user's email used for log in
    public String password;                         // user's password used for log in

    /**
     * This method will be used in order to set up the Live View Page.
     * @param savedInstanceState bundle state used to carry some info needed for the page
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_stream_page);

        // get all the extra info from the passed intent
        userName = getIntent().getExtras().getString("user");
        IP = getIntent().getExtras().getString("IP");
        token = getIntent().getExtras().getString("token");
        email = getIntent().getExtras().getString("email");
        password = getIntent().getExtras().getString("password");

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
        backButton = findViewById(R.id.GoBackButtonLiveToHomePage);
        backButton.setOnClickListener(view -> {
            try
            {
                new turnOffLiveStream().execute(IP); // here tell, the server to turn off the live stream
                Intent intent = new Intent(LiveViewPage.this, UserHomePage.class);
                intent.putExtra("user", userName);
                intent.putExtra("IP",IP);
                intent.putExtra("token", token);
                intent.putExtra("email",email);
                intent.putExtra("password",password);
                startActivity(intent);
                finish();
            } catch (Exception e1) {
                Toast.makeText(this, "ERROR OCCURRED. PLEASE TRY AGAIN ...", Toast.LENGTH_LONG).show();
            }
        });

        // once the 'take pic' button is pressed, the user can take a picture of the video during the live stream
        picButton = this.<Button>findViewById(R.id.takePicButton);
        picButton.setOnClickListener(view -> {
            try
            {
                new takeLivePic().execute(IP); // here tell, the server to turn off the live stream
                Toast.makeText(LiveViewPage.this,"TAKING PICTURE..." , Toast.LENGTH_LONG).show();
                SystemClock.sleep(3000);    // give the camera at least 3 seconds to warm up
                liveCam.loadUrl(url);

                verifyStoragePermissions(this);
                String[] args = {IP,userName};
                new sendOverLivePic().execute(args); // here tell, send over the picture taken

            } catch (Exception e1) {
                Toast.makeText(this, "ERROR OCCURRED. PLEASE TRY AGAIN ...", Toast.LENGTH_LONG).show();
            }
        });


    } // ends the onCreate() method




    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }



    /**
     * This is the turnOffLiveStream class that will be used to request the raspberry pi device to turn off the live stream.
     * This will be executed in the background.
     */
    public static class turnOffLiveStream extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;


        /**
         * This method will be used in order to request the main server on the device to turn off the live stream.
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

                // tell the server to end the live
                dout.writeUTF("EndLive");
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
    } // ends the turnOffLiveStream class




    /**
     * This is the takeLivePic class that will be used to request the raspberry pi device to take a picture
     * while the live stream is running.
     */
    public static class takeLivePic extends AsyncTask<String, Integer, String> {
        // Global variables
        public final int RPiDeviceMainServerPort = 9000;

        /**
         * This method will be used in order to request the main server on the device to take a picture during the live stream.
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

                // tell the server to end the live
                dout.writeUTF("Take Pic");
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
    } // ends the takeLivePic class



    /**
     * This is the sendOverLivePic class that will be used to request the raspberry pi device to send over the picture
     * that was taken during the live stream.
     */
    public class sendOverLivePic extends AsyncTask<String, Integer, String> {
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
                    File dir = context.getDir(params[1], Context.MODE_PRIVATE); //Creating an internal dir;
                    File file = new File(dir, picName); //Getting a file within the dir.
                    FileOutputStream filePtr = new FileOutputStream(file);

                    Log.e("fileMade","" + file.getName());
                    Log.e("pathOfFile","" + file.getAbsolutePath());

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
                return "DONE";
            } catch (Exception e) {
                return "FAIL";
            }
        } // ends the doInBackground() method
    } // ends the sendOverLivePic class



} // ends the LiveViewPage class