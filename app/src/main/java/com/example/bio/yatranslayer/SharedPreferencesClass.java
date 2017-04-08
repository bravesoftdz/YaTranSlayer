package com.example.bio.yatranslayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class SharedPreferencesClass {
    SharedPreferences sPref;

    static void saveVariable(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    static String loadVariable(String key, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String token = prefs.getString(key, null);

        return token;
    }
}

