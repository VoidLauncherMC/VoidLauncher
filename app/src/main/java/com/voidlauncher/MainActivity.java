package com.voidlauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class LoginActivity extends Activity {

    private Spinner spinnerLoginType;
    private EditText editUsername;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        ProfileManager pm = new ProfileManager(this);
        String username = pm.getProfile("offline_username");

        if (username != null) {
            statusText.setText("Welcome, " + username);
        } else {
            statusText.setText("Not logged in");
        }
    }
}
