package com.voidlauncher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;

public class MicrosoftAuth {
    public static final String CLIENT_ID = "00000000402b5328";
    public static final String REDIRECT_URL = "https://login.live.com/oauth20_desktop.srf";
    
    public static final String ELY_BY_URL = "https://authserver.ely.by/auth/authenticate";
    public static final String LITTLE_SKIN_URL = "https://littleskin.cn/api/yggdrasil/authserver/authenticate";

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
                .add("redirect_uri", REDIRECT_URL).build();
        Request request = new Request.Builder().url("https://login.live.com/oauth20_token.srf").post(formBody).build();
        client.newCall(request).enqueue(callback);
    }

    public void getXboxLiveToken(String msToken, Callback callback) {
        JsonObject props = new JsonObject();
        props.addProperty("AuthMethod", "RPS");
        props.addProperty("SiteName", "user.auth.xboxlive.com");
        props.addProperty("RpsTicket", "d=" + msToken);
        JsonObject body = new JsonObject();
        body.add("Properties", props);
        body.addProperty("RelyingParty", "http://auth.xboxlive.com");
        body.addProperty("TokenType", "JWT");
        RequestBody rb = RequestBody.create(gson.toJson(body), MediaType.parse("application/json"));
        Request request = new Request.Builder().url("https://user.auth.xboxlive.com/user/authenticate").post(rb).build();
        client.newCall(request).enqueue(callback);
    }
    
    public void loginWithYggdrasil(String url, String user, String pass, Callback callback) {
        JsonObject body = new JsonObject();
        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);
        body.add("agent", agent);
        body.addProperty("username", user);
        body.addProperty("password", pass);
        RequestBody rb = RequestBody.create(gson.toJson(body), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(url).post(rb).build();
        client.newCall(request).enqueue(callback);
    }
}
