package com.example.rpibell;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPage  extends AppCompatActivity {

    public Button logIn;

    public TextView userNameInput;
    public TextView passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // for now the username and password can be whatever, not not empty ...
        userNameInput = findViewById(R.id.UserNameInput);
        passwordInput = findViewById(R.id.PasswordInput);
        logIn = findViewById(R.id.LogInButton);
        logIn.setOnClickListener(view -> {
            if (userNameInput.getText().toString().isEmpty()) {
                Toast.makeText(LoginPage.this,"PLEASE ENTER A USERNAME" , Toast.LENGTH_LONG).show();
                return;
            }
            if (passwordInput.getText().toString().isEmpty()) {
                Toast.makeText(LoginPage.this,"PLEASE ENTER A PASSWORD" , Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(LoginPage.this,"WELCOME, " + userNameInput.getText().toString().toUpperCase() , Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LoginPage.this, MainActivity.class);
            intent.putExtra("advice", userNameInput.getText().toString());
            startActivity(intent);
        });

    }

}
