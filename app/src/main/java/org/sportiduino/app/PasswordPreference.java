package org.sportiduino.app;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class PasswordPreference extends DialogPreference {

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSummary(Password.defaultPassword().toString());
    }
    
    public void persistPassword(Password password) {
        persistString(password.toString());
        setSummary(password.toString());
        notifyChanged();
    }

    public Password getPersistedPassword() {
        return Password.fromString(getPersistedString(Password.defaultPassword().toString()));
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.preference_password;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        setSummary(getPersistedPassword().toString());
    }
}

