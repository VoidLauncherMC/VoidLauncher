package com.voidlauncher;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import java.io.File;
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
    private GameDownloader downloader = new GameDownloader();
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btnLaunch);

        btn.setOnClickListener(v -> {
            btn.setText("Connecting...");
            btn.setEnabled(false);

            versionManager.fetchVersions(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handleError(btn, "Check Internet");
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
                                fetchGameDetails(versions.get(0), btn);
                            }
                        } catch (Exception e) {
                            handleError(btn, "JSON Error");
                        }
                    }
                }
            });
        });
    }

    private void fetchGameDetails(MCVersion version, Button btn) {
        runOnUiThread(() -> btn.setText("Getting Link..."));

        versionManager.fetchVersionDetails(version.url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleError(btn, "Details Failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject details = gson.fromJson(response.body().string(), JsonObject.class);
                        String downloadUrl = details.get("downloads").getAsJsonObject()
                                                   .get("client").getAsJsonObject()
                                                   .get("url").getAsString();

                        startDownload(downloadUrl, version.id, btn);
                    } catch (Exception e) {
                        handleError(btn, "Link Error");
                    }
                }
            }
        });
    }

    private void startDownload(String url, String versionName, Button btn) {
        runOnUiThread(() -> btn.setText("Downloading " + versionName + "..."));
        
        File versionDir = new File(getExternalFilesDir(null), "mc_versions");
        if (!versionDir.exists()) versionDir.mkdirs();
        
        File outputFile = new File(versionDir, versionName + ".jar");

        downloader.downloadFile(url, outputFile, new GameDownloader.DownloadCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    btn.setText("Download Complete!");
                    btn.setEnabled(true);
                });
                Log.d("VoidLauncher", "File saved at: " + outputFile.getAbsolutePath());
            }

            @Override
            public void onError(String error) {
                handleError(btn, "Download Failed");
            }
        });
    }

    private void handleError(Button btn, String message) {
        runOnUiThread(() -> {
            btn.setText(message);
            btn.setEnabled(true);
        });
    }
}
