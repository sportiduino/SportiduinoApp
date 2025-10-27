package org.sportiduino.app;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class CardDataUrlPreference extends DialogPreference {

    public CardDataUrlPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setSummary(getPlaceholderValue());
    }

    private String getPlaceholderValue() {
        return "(" + App.str(R.string.not_specify) + ")";
    }

    private String getValueOrPlaceholderValue() {
        String value = getValue();

        if (value.isEmpty()) {
            value = getPlaceholderValue();
        }

        return value;
    }

    public String getValue() {
        return getPersistedString(null);
    }

    public void setValue(String value) {
        persistString(value);

        setSummary(getValueOrPlaceholderValue());

        notifyChanged();
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.preference_card_data_url;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);

        setSummary(getValueOrPlaceholderValue());
    }
}

