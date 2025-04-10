package org.sportiduino.app;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class NtagAuthKeyPreference extends DialogPreference {

    public NtagAuthKeyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSummary(NtagAuthKey.defaultKey().toString());
    }
    
    public void persistKey(NtagAuthKey key) {
        persistString(key.toString());
        setSummary(key.toString());
        notifyChanged();
    }

    public NtagAuthKey getPersistedKey() {
        return NtagAuthKey.fromString(getPersistedString(NtagAuthKey.defaultKey().toString()));
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.preference_dialog_ntag_auth_key;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        setSummary(getPersistedKey().toString());
    }
}

