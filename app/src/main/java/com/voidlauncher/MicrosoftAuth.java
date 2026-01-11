package com.voidlauncher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;

public class MicrosoftAuth {
    public static final String CLIENT_ID = "00000000402b5328";
    public static final String REDIRECT_URL = "https://login.live.com/oauth20_desktop.srf";
    
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

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

    public void getXboxLiveToken(String msToken, Callback callback) {
        JsonObject properties = new JsonObject();
        properties.addProperty("AuthMethod", "RPS");
        properties.addProperty("SiteName", "user.auth.xboxlive.com");
        properties.addProperty("RpsTicket", "d=" + msToken);

        JsonObject body = new JsonObject();
        body.add("Properties", properties);
        body.addProperty("RelyingParty", "http://auth.xboxlive.com");
        body.addProperty("TokenType", "JWT");

        RequestBody requestBody = RequestBody.create(
                gson.toJson(body), 
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://user.auth.xboxlive.com/user/authenticate")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
