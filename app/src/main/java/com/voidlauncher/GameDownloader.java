package com.voidlauncher;

import okhttp3.*;
import java.io.*;

public class GameDownloader {
    private final OkHttpClient client = new OkHttpClient();

    public void downloadFile(String url, File outputFile, DownloadCallback callback) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Server error");
                    return;
                }

                try (InputStream is = response.body().byteStream();
                     FileOutputStream fos = new FileOutputStream(outputFile)) {
                    
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                    callback.onSuccess();
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public interface DownloadCallback {
        void onSuccess();
        void onError(String error);
    }
}
