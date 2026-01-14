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
        setContentView(R.layout.activity_login);

        spinnerLoginType = findViewById(R.id.spinnerLoginType);
        editUsername = findViewById(R.id.editUsername);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            LoginType selected = (LoginType) spinnerLoginType.getSelectedItem();

            if (!selected.name.equals("Offline")) {
                Toast.makeText(this,
                    "will be supported " + selected.name + " later.",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            String username = editUsername.getText().toString().trim();
            if (username.isEmpty()) {
                editUsername.setError("Type the player name");
                return;
            }

            ProfileManager pm = new ProfileManager(this);
            pm.saveProfile("offline_username", username);

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
