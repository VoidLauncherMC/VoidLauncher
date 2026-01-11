package com.voidlauncher;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
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
    private MicrosoftAuth authManager = new MicrosoftAuth();
    private Gson gson = new Gson();
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLaunch = findViewById(R.id.btnLaunch);
        Button btnLogin = findViewById(R.id.btnLogin);
        statusText = findViewById(R.id.statusText);

        btnLogin.setOnClickListener(v -> openLoginDialog());
        btnLaunch.setOnClickListener(v -> {
            btnLaunch.setText("Connecting...");
            btnLaunch.setEnabled(false);
            fetchVersionsLogic(btnLaunch);
        });
    }

    private void openLoginDialog() {
        Dialog loginDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        WebView webView = new WebView(this);
        loginDialog.setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("?code=")) {
                    String code = url.split("code=")[1].split("&")[0];
                    loginDialog.dismiss();
                    exchangeCodeForToken(code);
                    return true;
                }
                return false;
            }
        });
        webView.loadUrl(MicrosoftAuth.getLoginUrl());
        loginDialog.show();
    }

    private void exchangeCodeForToken(String code) {
        runOnUiThread(() -> statusText.setText("Linking Microsoft..."));
        
        authManager.getMicrosoftToken(code, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> statusText.setText("MS Auth Failed"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                    String msToken = json.get("access_token").getAsString();

                    runOnUiThread(() -> statusText.setText("Authenticating with Xbox..."));
                    
                    authManager.getXboxLiveToken(msToken, new Callback() {
                        @Override
                        public void onResponse(Call c, Response resXbox) throws IOException {
                            if (resXbox.isSuccessful()) {
                                runOnUiThread(() -> {
                                    statusText.setText("Xbox & Microsoft Linked!");
                                    statusText.setTextColor(0xFFA020F0);
                                });
                                Log.d("VoidLauncher", "Xbox Auth Success");
                            }
                        }
                        @Override
                        public void onFailure(Call c, IOException e) {
                            runOnUiThread(() -> statusText.setText("Xbox Auth Failed"));
                        }
                    });
                }
            }
        });
    }

    private void fetchVersionsLogic(Button btn) {
        versionManager.fetchVersions(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { handleError(btn, "Check Internet"); }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
                        List<MCVersion> versions = gson.fromJson(jsonObject.get("versions"), new TypeToken<List<MCVersion>>(){}.getType());
                        if (versions != null && !versions.isEmpty()) {
                            fetchGameDetails(versions.get(0), btn);
                        }
                    } catch (Exception e) { handleError(btn, "JSON Error"); }
                }
            }
        });
    }

    private void fetchGameDetails(MCVersion version, Button btn) {
        runOnUiThread(() -> btn.setText("Getting Link..."));
        versionManager.fetchVersionDetails(version.url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { handleError(btn, "Details Failed"); }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject details = gson.fromJson(response.body().string(), JsonObject.class);
                        String downloadUrl = details.get("downloads").getAsJsonObject().get("client").getAsJsonObject().get("url").getAsString();
                        startDownload(downloadUrl, version.id, btn);
                    } catch (Exception e) { handleError(btn, "Link Error"); }
                }
            }
        });
    }

    private void startDownload(String url, String versionName, Button btn) {
        runOnUiThread(() -> btn.setText("Downloading..."));
        File versionDir = new File(getExternalFilesDir(null), "mc_versions");
        if (!versionDir.exists()) versionDir.mkdirs();
        File outputFile = new File(versionDir, versionName + ".jar");
        downloader.downloadFile(url, outputFile, new GameDownloader.DownloadCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    btn.setText("Ready!");
                    btn.setEnabled(true);
                    statusText.setText("File: " + versionName + ".jar");
                });
            }
            @Override
            public void onError(String error) { handleError(btn, "Download Failed"); }
        });
    }

    private void handleError(Button btn, String message) {
        runOnUiThread(() -> {
            btn.setText(message);
            btn.setEnabled(true);
        });
    }
}
