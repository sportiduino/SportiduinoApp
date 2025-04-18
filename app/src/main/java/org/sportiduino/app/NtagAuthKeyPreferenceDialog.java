package org.sportiduino.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
    private Button resetButton;
    private NtagAuthKey key;

    private void updateResetButtonState() {
        resetButton.setSelected(getAuthKeyFromTextPass().isDefault());
    }

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

    TextWatcher editTextChangedLister = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            updateResetButtonState();
        }
    };

    private NtagAuthKey getAuthKeyFromTextPass() {
        return new NtagAuthKey(
            parseValue(editTextPass1.getText().toString()),
            parseValue(editTextPass2.getText().toString()),
            parseValue(editTextPass3.getText().toString()),
            parseValue(editTextPass4.getText().toString())
        );
    }

    private void fillTextPassFromKey(NtagAuthKey key) {
        editTextPass1.setText(String.valueOf(key.getValue(0)));
        editTextPass2.setText(String.valueOf(key.getValue(1)));
        editTextPass3.setText(String.valueOf(key.getValue(2)));
        editTextPass4.setText(String.valueOf(key.getValue(3)));
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        editTextPass1 = view.findViewById(R.id.ntag_auth_key1);
        editTextPass2 = view.findViewById(R.id.ntag_auth_key2);
        editTextPass3 = view.findViewById(R.id.ntag_auth_key3);
        editTextPass4 = view.findViewById(R.id.ntag_auth_key4);
        resetButton = view.findViewById(R.id.ntag_auth_reset_button);

        initCaretEndOnFocus(editTextPass1);
        initCaretEndOnFocus(editTextPass2);
        initCaretEndOnFocus(editTextPass3);
        initCaretEndOnFocus(editTextPass4);

        editTextPass1.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});
        editTextPass2.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});
        editTextPass3.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});
        editTextPass4.setFilters(new InputFilter[]{new MinMaxFilter(0, 255)});

        editTextPass1.addTextChangedListener(editTextChangedLister);
        editTextPass2.addTextChangedListener(editTextChangedLister);
        editTextPass3.addTextChangedListener(editTextChangedLister);
        editTextPass4.addTextChangedListener(editTextChangedLister);

        fillTextPassFromKey(key);

        updateResetButtonState();
        editTextPass1.requestFocus();

        resetButton.setOnClickListener(v -> {
            fillTextPassFromKey(NtagAuthKey.defaultKey());

            updateResetButtonState();
            editTextPass1.requestFocus();
        });
    }

    private NtagAuthKeyPreference getNtagAuthKeyPreference() {
        return (NtagAuthKeyPreference) getPreference();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            final NtagAuthKeyPreference preference = getNtagAuthKeyPreference();
            preference.persistKey(getAuthKeyFromTextPass());
        }
    }
}

