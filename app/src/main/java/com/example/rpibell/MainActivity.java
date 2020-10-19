package com.example.rpibell;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    public Button mButton;
    public boolean off = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView livecam = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = livecam.getSettings();
        webSettings.setJavaScriptEnabled(true);
        livecam.setWebViewClient(new WebViewClient()); // forces all URLs to run only inside this app and doesn't redirect it to chrome

         // upon getting to the live-view part of the app, this will be on
        livecam.loadUrl("http://www.youtube.com"); // tested using google.com
        off = false;

        mButton = findViewById(R.id.LiveViewButton);
        mButton.setOnClickListener(view -> {
            if (!off) {
                livecam.loadUrl("about:blank");
            } else {
                livecam.loadUrl("http://www.youtube.com"); // tested using google.com
            }
            off = !off;
        });

    }




}