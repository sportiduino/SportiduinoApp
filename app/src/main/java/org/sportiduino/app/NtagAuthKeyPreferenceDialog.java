package org.sportiduino.app;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import static org.sportiduino.app.NtagAuthKey.parseValue;
import static org.sportiduino.app.ViewUtils.initCaretEndOnFocus;

public class NtagAuthKeyPreferenceDialog extends PreferenceDialogFragmentCompat {

    private static final String SAVE_STATE_TEXT = "NtagAuthKeyPreferenceDialogFragment.text";
    private EditText editTextPass1;
    private EditText editTextPass2;
    private EditText editTextPass3;
    private EditText editTextPass4;
    private NtagAuthKey key;

    public static NtagAuthKeyPreferenceDialog newInstance(String key) {
        final NtagAuthKeyPreferenceDialog
                fragment = new NtagAuthKeyPreferenceDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            key = getNtagAuthKeyPreference().getPersistedKey();
        } else {
            key = NtagAuthKey.fromString(savedInstanceState.getCharSequence(SAVE_STATE_TEXT).toString());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(SAVE_STATE_TEXT, key.toString());
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        editTextPass1 = view.findViewById(R.id.ntag_auth_key1);
        editTextPass2 = view.findViewById(R.id.ntag_auth_key2);
        editTextPass3 = view.findViewById(R.id.ntag_auth_key3);
        editTextPass4 = view.findViewById(R.id.ntag_auth_key4);

        initCaretEndOnFocus(editTextPass1);
        initCaretEndOnFocus(editTextPass2);
        initCaretEndOnFocus(editTextPass3);
        initCaretEndOnFocus(editTextPass4);

        editTextPass1.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});
        editTextPass2.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});
        editTextPass3.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});
        editTextPass4.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});
        editTextPass1.setText(String.valueOf(key.getValue(0)));
        editTextPass2.setText(String.valueOf(key.getValue(1)));
        editTextPass3.setText(String.valueOf(key.getValue(2)));
        editTextPass4.setText(String.valueOf(key.getValue(3)));

        editTextPass1.requestFocus();

        Button resetButton = view.findViewById(R.id.button_ntag_auth_key_reset);
        resetButton.setOnClickListener(v -> {
            key = NtagAuthKey.defaultKey();
            editTextPass1.setText(String.valueOf(key.getValue(0)));
            editTextPass2.setText(String.valueOf(key.getValue(1)));
            editTextPass3.setText(String.valueOf(key.getValue(2)));
            editTextPass4.setText(String.valueOf(key.getValue(3)));
        });
    }

    private NtagAuthKeyPreference getNtagAuthKeyPreference() {
        return (NtagAuthKeyPreference) getPreference();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            NtagAuthKey value = new NtagAuthKey(
                    parseValue(editTextPass1.getText().toString()),
                    parseValue(editTextPass2.getText().toString()),
                    parseValue(editTextPass3.getText().toString()),
                    parseValue(editTextPass4.getText().toString()));
            final NtagAuthKeyPreference preference = getNtagAuthKeyPreference();
            preference.persistKey(value);
        }
    }
}

