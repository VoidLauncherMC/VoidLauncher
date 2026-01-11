package com.voidlauncher;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btnLaunch);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Connecting to the Void...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

