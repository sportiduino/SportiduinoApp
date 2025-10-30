package org.sportiduino.app;

import static org.sportiduino.app.ViewUtils.initCaretEndOnFocus;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

public class CardDataUrlPreferenceDialog extends PreferenceDialogFragmentCompat {

    private static final String SAVE_STATE_TEXT = "CardDataUrlPreferenceDialogFragment.text";
    private EditText cardDataUrl;

    public static CardDataUrlPreferenceDialog newInstance(String key) {
        final CardDataUrlPreferenceDialog
                fragment = new CardDataUrlPreferenceDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    private CardDataUrlPreference getCardDataUrlPreference() {
        return (CardDataUrlPreference) getPreference();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(SAVE_STATE_TEXT, cardDataUrl.getText().toString());
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        cardDataUrl = view.findViewById(R.id.pref_card_data_url);

        initCaretEndOnFocus(cardDataUrl);
    }

    @Nullable
    @Override
    protected View onCreateDialogView(@NonNull Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.preference_card_data_url, null);

        cardDataUrl = view.findViewById(R.id.pref_card_data_url);

        CardDataUrlPreference preference = getCardDataUrlPreference();

        cardDataUrl.setText(preference.getValue());

        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = cardDataUrl.getText().toString();

            CardDataUrlPreference preference = getCardDataUrlPreference();

            if (preference.callChangeListener(value)) {
                preference.setValue(value);
            }
        }
    }
}
