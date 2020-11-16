package com.example.rpibell;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * This is the LoginPage class.
 * Here a username and password must be given in order to access the rest of the application.
 */
public class LoginPage extends AppCompatActivity {
    // Global variables
    private Button logIn;                // log in button
    private EditText userNameInput, passwordInput;      // input fields

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance();

        initializeUI();

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });

    }  // ends the onCreate() method

    private void loginUserAccount() {
        String username, password;
        username = userNameInput.getText().toString();
        password = passwordInput.getText().toString();

        // Checks for empty fields
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(LoginPage.this,"PLEASE ENTER A USERNAME" , Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(LoginPage.this,"PLEASE ENTER A PASSWORD" , Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {  // sign-in successful
                            Log.d("TAG", "signInWithEmail:success");
                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(LoginPage.this, UserHomePage.class);
                            intent.putExtra("user", username);
                            startActivity(intent);
                        } else {    // sign-in failed
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Login failed! Please try again later", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void initializeUI() {
        userNameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);

        logIn = findViewById(R.id.LogInButton);
    }

} // ends the LoginPage class
