package org.sportiduino.app;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class PasswordPreferenceDialog extends PreferenceDialogFragmentCompat {

    private static final String SAVE_STATE_TEXT = "PasswordPreferenceDialogFragment.text";
    private EditText pass1;
    private EditText pass2;
    private EditText pass3;
    private Password password;

    public static PasswordPreferenceDialog newInstance(String key) {
        final PasswordPreferenceDialog
                fragment = new PasswordPreferenceDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            password = getPasswordPreference().getPersistedPassword();
        } else {
            password = Password.fromString(savedInstanceState.getCharSequence(SAVE_STATE_TEXT).toString());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(SAVE_STATE_TEXT, password.toString());
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        pass1 = view.findViewById(R.id.password1);
        pass2 = view.findViewById(R.id.password2);
        pass3 = view.findViewById(R.id.password3);
        pass1.requestFocus();
        pass1.setText(String.valueOf(password.getValue(0)));
        pass2.setText(String.valueOf(password.getValue(1)));
        pass3.setText(String.valueOf(password.getValue(2)));
    }

    private PasswordPreference getPasswordPreference() {
        return (PasswordPreference) getPreference();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Password value = new Password(
                    Integer.parseInt(pass1.getText().toString()),
                    Integer.parseInt(pass2.getText().toString()),
                    Integer.parseInt(pass3.getText().toString()));
            final PasswordPreference preference = getPasswordPreference();
            preference.persistPassword(value);
        }
    }
}
