package com.example.rpibell;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class LiveViewPage extends AppCompatActivity {

    public Button OnOffLiveButton;
    public boolean off = false;

    public Button backButton;

    String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // disable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        WebView livecam = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = livecam.getSettings();
        webSettings.setJavaScriptEnabled(true);
        livecam.setWebViewClient(new WebViewClient()); // forces all URLs to run only inside this app and doesn't redirect it to chrome

        livecam.getSettings().setLoadWithOverviewMode(true);
        livecam.getSettings().setUseWideViewPort(true);

         // upon getting to the live-view part of the app, this will be on
        livecam.loadUrl("http://192.168.86.227:5000/"); // tested using google.com
        off = false;

        OnOffLiveButton = this.<Button>findViewById(R.id.LiveViewButton);
        OnOffLiveButton.setOnClickListener(view -> {
            if (!off) {
                livecam.loadUrl("about:blank");
            } else {
                livecam.loadUrl("http://192.168.86.227:5000/"); // tested using google.com
            }
            off = !off;
        });

        // get username to get back to user page
        userName = getIntent().getExtras().getString("user");

        backButton = this.<Button>findViewById(R.id.GoBackButtonLiveToHomePage);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(LiveViewPage.this, UserHomePage.class);
            intent.putExtra("user", userName);
            startActivity(intent);
            finish();
        });


    }




}