package org.sportiduino.app;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class NumberPickerPreferenceDialog extends PreferenceDialogFragmentCompat {

    private NumberPicker picker;

    public static NumberPickerPreferenceDialog newInstance(String key) {
        final NumberPickerPreferenceDialog fragment = new NumberPickerPreferenceDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected View onCreateDialogView(@NonNull Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.preference_number_picker, null);

        picker = view.findViewById(R.id.pref_num_picker);
        NumberPickerPreference preference = (NumberPickerPreference) getPreference();
        picker.setMinValue(getResources().getInteger(R.integer.countdown_timer_min));
        picker.setMaxValue(getResources().getInteger(R.integer.countdown_timer_max));
        picker.setValue(preference.getValue());
        Log.d("onCreateDialogView", String.valueOf(preference.getValue()));

        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult && picker != null) {
            int value = picker.getValue();
            Log.d("onDialogClosed", String.valueOf(value));
            NumberPickerPreference preference = (NumberPickerPreference) getPreference();
            if (preference.callChangeListener(value)) {
                preference.setValue(value);
            }
        }
    }
}
