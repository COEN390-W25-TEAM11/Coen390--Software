package com.example.lightingcontrol;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Base64;

public class SharedPreferencesHelper {

    // SHARED PREFERENCES SAVES: JWT token, light to open on SpecificLightActivity

    private static SharedPreferences sharedPreferences = null;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE );
    }

    // save JWT token
    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    // get JWT token
    public String getToken() {
        return sharedPreferences.getString("token", null);
    }

    // clear JWT token
    public void clearToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getToken());
        editor.apply();
    }

    private String getTokenPayload() {
        String[] parts = getToken().split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format.");
        }

        return new String(Base64.getUrlDecoder().decode(parts[1]));
    }

    private String getTokenValue(String key) {
        String payloadJson = getTokenPayload();

        String targetKey = "\"" + key + "\"";
        int keyIndex = payloadJson.indexOf(targetKey);
        if (keyIndex == -1) return null;

        int colonIndex = payloadJson.indexOf(":", keyIndex + targetKey.length());
        if (colonIndex == -1) return null;

        int startQuote = payloadJson.indexOf("\"", colonIndex + 1);
        int endQuote = payloadJson.indexOf("\"", startQuote + 1);
        if (startQuote == -1 || endQuote == -1) return null;

        return payloadJson.substring(startQuote + 1, endQuote);
    }

    public String getUsername() {
        return getTokenValue("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name");
    }

    public String getUserType() {
        return getTokenValue("http://schemas.microsoft.com/ws/2008/06/identity/claims/role");
    }
}
