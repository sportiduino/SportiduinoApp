package org.sportiduino.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat  implements PreferenceManager.OnPreferenceTreeClickListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals("night_mode")) {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            return true;
        }

        return false;
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof PasswordPreference) {
            dialogFragment = PasswordPreferenceDialog.newInstance(preference.getKey());
        } else if (preference instanceof NtagAuthKeyPreference) {
            dialogFragment = NtagAuthKeyPreferenceDialog.newInstance(preference.getKey());
        } else if (preference instanceof NumberPickerPreference) {
            dialogFragment = NumberPickerPreferenceDialog.newInstance(preference.getKey());
        } else if (preference instanceof CardDataUrlPreference) {
            dialogFragment = CardDataUrlPreferenceDialog.newInstance(preference.getKey());
        }

        // If it was one of our custom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(),"androidx.preference.PreferenceFragment.DIALOG");
        }
        // Could not be handled here. Try with the super method.
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}

