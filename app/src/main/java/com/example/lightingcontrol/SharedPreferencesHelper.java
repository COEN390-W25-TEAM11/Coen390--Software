package com.example.lightingcontrol;

import android.content.Context;
import android.content.SharedPreferences;

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

    // save lightToOpen
    public void saveLight(String lightId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lightId", lightId);
        editor.apply();
    }

    // get JWT token
    public String getLight() {
        return sharedPreferences.getString("lightId", null);
    }

}
