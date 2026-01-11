package com.voidlauncher;

import okhttp3.*;
import java.io.IOException;

public class MicrosoftAuth {
    public static final String CLIENT_ID = "00000000402b5328";
    public static final String SCOPE = "service::user.auth.xboxlive.com::MBI_SSL";
    public static final String REDIRECT_URL = "https://login.live.com/oauth20_desktop.srf";

    private final OkHttpClient client = new OkHttpClient();

    public static String getLoginUrl() {
        return "https://login.live.com/oauth20_authorize.srf" +
                "?client_id=" + CLIENT_ID +
                "&response_type=code" +
                "&scope=service::user.auth.xboxlive.com::MBI_SSL" +
                "&redirect_uri=" + REDIRECT_URL;
    }

    public void getMicrosoftToken(String code, Callback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("code", code)
                .add("grant_type", "authorization_code")
                .add("redirect_uri", REDIRECT_URL)
                .build();

        Request request = new Request.Builder()
                .url("https://login.live.com/oauth20_token.srf")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
