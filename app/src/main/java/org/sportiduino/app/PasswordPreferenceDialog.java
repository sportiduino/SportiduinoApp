package org.sportiduino.app;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import static org.sportiduino.app.Password.parseValue;

public class PasswordPreferenceDialog extends PreferenceDialogFragmentCompat {

    private static final String SAVE_STATE_TEXT = "PasswordPreferenceDialogFragment.text";
    private EditText editTextPass1;
    private EditText editTextPass2;
    private EditText editTextPass3;
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
        editTextPass1 = view.findViewById(R.id.password1);
        editTextPass2 = view.findViewById(R.id.password2);
        editTextPass3 = view.findViewById(R.id.password3);
        editTextPass1.requestFocus();
        editTextPass1.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});
        editTextPass2.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});
        editTextPass3.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});
        editTextPass1.setText(String.valueOf(password.getValue(0)));
        editTextPass2.setText(String.valueOf(password.getValue(1)));
        editTextPass3.setText(String.valueOf(password.getValue(2)));
    }

    private PasswordPreference getPasswordPreference() {
        return (PasswordPreference) getPreference();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Password value = new Password(
                    parseValue(editTextPass1.getText().toString()),
                    parseValue(editTextPass2.getText().toString()),
                    parseValue(editTextPass3.getText().toString()));
            final PasswordPreference preference = getPasswordPreference();
            preference.persistPassword(value);
        }
    }
}
