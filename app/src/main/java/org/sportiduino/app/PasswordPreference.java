package org.sportiduino.app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.preference.DialogPreference;

import java.util.Arrays;

public class PasswordPreference extends DialogPreference {

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void persistPassword(Password password) {
        persistString(password.toString());
        setSummary(password.toString());
        notifyChanged();
    }

    public Password getPersistedPassword() {
        return Password.fromString(getPersistedString(new Password().toString()));
    }

    @Override
    public int getDialogLayoutResource() {
        Log.i("PasswordPreference", "getDialogLayoutResource");
        return R.layout.preference_password;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        setSummary(getPersistedPassword().toString());
    }
}

