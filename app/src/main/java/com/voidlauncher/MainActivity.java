package com.voidlauncher;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
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
    private EditText editUsername, editPassword;
    private Spinner authTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authTypeSpinner = findViewById(R.id.authTypeSpinner);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        statusText = findViewById(R.id.statusText);
        Button btnLaunch = findViewById(R.id.btnLaunch);
        Button btnLogin = findViewById(R.id.btnLogin);

        // إعداد القائمة المنسدلة
        String[] types = {"Microsoft", "Offline", "Ely.by", "LittleSkin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        authTypeSpinner.setAdapter(adapter);

        btnLogin.setOnClickListener(v -> handleLogin());
        
        btnLaunch.setOnClickListener(v -> {
            btnLaunch.setText("Working...");
            btnLaunch.setEnabled(false);
            fetchVersionsLogic(btnLaunch);
        });
    }

    private void handleLogin() {
        String type = authTypeSpinner.getSelectedItem().toString();
        String user = editUsername.getText().toString();
        String pass = editPassword.getText().toString();

        if (type.equals("Microsoft")) {
            openLoginDialog();
        } else if (type.equals("Offline")) {
            if(user.isEmpty()) user = "VoidPlayer";
            statusText.setText("Offline Mode: " + user);
            statusText.setTextColor(0xFF00FF00);
        } else {
            // Ely.by or LittleSkin
            String url = type.equals("Ely.by") ? MicrosoftAuth.ELY_BY_URL : MicrosoftAuth.LITTLE_SKIN_URL;
            authManager.loginWithYggdrasil(url, user, pass, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            statusText.setText(type + " Login Success!");
                            statusText.setTextColor(0xFF00FF00);
                        });
                    } else {
                        runOnUiThread(() -> statusText.setText("Login Failed: Check Credentials"));
                    }
                }
                @Override public void onFailure(Call call, IOException e) {}
            });
        }
    }

    // ... (أبقي دوال openLoginDialog و exchangeCodeForToken و fetchVersionsLogic كما هي في كودك السابق)
    
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
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                    String msToken = json.get("access_token").getAsString();
                    authManager.getXboxLiveToken(msToken, new Callback() {
                        @Override
                        public void onResponse(Call c, Response res) throws IOException {
                            if (res.isSuccessful()) {
                                runOnUiThread(() -> {
                                    statusText.setText("Microsoft & Xbox Linked!");
                                    statusText.setTextColor(0xFFA020F0);
                                });
                            }
                        }
                        @Override public void onFailure(Call c, IOException e) {}
                    });
                }
            }
            @Override public void onFailure(Call call, IOException e) {}
        });
    }

    // منطق جلب الإصدارات والتحميل (كما هو في الكود السابق)
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
