package com.voidlauncher;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {
    private VersionManager versionManager = new VersionManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btnLaunch);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn.setText("Fetching Versions...");
                btn.setEnabled(false);

                versionManager.fetchVersions(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("VoidLauncher", "Connection Error: " + e.getMessage());
                        runOnUiThread(() -> {
                            btn.setText("Error! Try Again");
                            btn.setEnabled(true);
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful() && response.body() != null) {
                            String jsonData = response.body().string();
                            
                            try {
                                Gson gson = new Gson();
                                JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
                                
                                List<MCVersion> versions = gson.fromJson(
                                    jsonObject.get("versions"), 
                                    new TypeToken<List<MCVersion>>(){}.getType()
                                );

                                if (versions != null && !versions.isEmpty()) {
                                    MCVersion latest = versions.get(0);

                                    runOnUiThread(() -> {
                                        btn.setText("Latest: " + latest.id);
                                        btn.setEnabled(true);
                                    });
                                }
                            } catch (Exception e) {
                                Log.e("VoidLauncher", "Parsing Error: " + e.getMessage());
                            }
                        }
                    }
                });
            }
        });
    }
}
