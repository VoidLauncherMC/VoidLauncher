package com.voidlauncher;

import android.content.Context;
import android.content.SharedPreferences;

public class ProfileManager {
    private SharedPreferences prefs;

    public ProfileManager(Context context) {
        prefs = context.getSharedPreferences("profiles", Context.MODE_PRIVATE);
    }

    public void saveProfile(String username, String accessToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(username, accessToken);
        editor.apply();
    }

    public String getProfile(String username) {
        return prefs.getString(username, null);
    }
}
