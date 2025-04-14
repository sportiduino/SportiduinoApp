package org.sportiduino.app;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.sportiduino.app.NtagAuthKey;

public class NtagAuthKeyManager {
    public static NtagAuthKey getAuthKey(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String keyStr = sharedPref.getString("ntag_auth_key", NtagAuthKey.defaultKey().toString());
        return NtagAuthKey.fromString(keyStr);
    }
}

