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
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btnLaunch);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn.setText("Connecting to Mojang...");
                btn.setEnabled(false);

                versionManager.fetchVersions(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handleError(btn, "Connection Failed");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String jsonData = response.body().string();
                                JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
                                List<MCVersion> versions = gson.fromJson(
                                    jsonObject.get("versions"), 
                                    new TypeToken<List<MCVersion>>(){}.getType()
                                );

                                if (versions != null && !versions.isEmpty()) {
                                    MCVersion latest = versions.get(0);
                                    
                                    fetchGameDetails(latest, btn);
                                }
                            } catch (Exception e) {
                                handleError(btn, "Parse Error");
                            }
                        }
                    }
                });
            }
        });
    }

    private void fetchGameDetails(MCVersion version, Button btn) {
        runOnUiThread(() -> btn.setText("Getting Download Link..."));

        versionManager.fetchVersionDetails(version.url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleError(btn, "Details Failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String detailJson = response.body().string();
                        JsonObject details = gson.fromJson(detailJson, JsonObject.class);
                        
                        String downloadUrl = details.get("downloads")
                                                   .getAsJsonObject()
                                                   .get("client")
                                                   .getAsJsonObject()
                                                   .get("url").getAsString();

                        Log.d("VoidLauncher", "Direct URL for " + version.id + ": " + downloadUrl);

                        runOnUiThread(() -> {
                            btn.setText("Ready: " + version.id);
                            btn.setEnabled(true);
                        });
                    } catch (Exception e) {
                        handleError(btn, "Detail Parse Error");
                    }
                }
            }
        });
    }

    private void handleError(Button btn, String message) {
        Log.e("VoidLauncher", message);
        runOnUiThread(() -> {
            btn.setText(message);
            btn.setEnabled(true);
        });
    }
}
