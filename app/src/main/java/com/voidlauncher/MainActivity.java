package com.voidlauncher;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Button;
import android.util.Log;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class MainActivity extends Activity {
    private VersionManager versionManager = new VersionManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btnLaunch);
        btn.setOnClickListener(v -> {
            btn.setText("Fetching Versions...");
            btn.setEnabled(false);

            versionManager.fetchVersions(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("VoidLauncher", "Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String jsonData = response.body().string();
                        runOnUiThread(() -> {
                            btn.setText("Data Received!");
                            btn.setEnabled(true);
                        });
                    }
                }
            });
        });
    }
}
