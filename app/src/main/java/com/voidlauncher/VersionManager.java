package com.voidlauncher;

import okhttp3.*;
import java.io.IOException;

public class VersionManager {
    private static final String MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private final OkHttpClient client = new OkHttpClient();

    public void fetchVersions(Callback callback) {
        Request request = new Request.Builder().url(MANIFEST_URL).build();
        client.newCall(request).enqueue(callback);
    }
}
