package com.voidlauncher;

public class MicrosoftAuth {
    public static final String CLIENT_ID = "00000000402b5328";
    public static final String SCOPE = "service::user.auth.xboxlive.com::MBI_SSL";
    public static final String REDIRECT_URL = "https://login.live.com/oauth20_desktop.srf";
    
    public static String getLoginUrl() {
        return "https://login.live.com/oauth20_authorize.srf" +
                "?client_id=" + CLIENT_ID +
                "&response_type=code" +
                "&scope=" + SCOPE +
                "&redirect_uri=" + REDIRECT_URL;
    }
}
