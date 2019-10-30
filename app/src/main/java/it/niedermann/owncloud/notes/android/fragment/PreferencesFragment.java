package it.niedermann.owncloud.notes.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.Notes;

public class PreferencesFragment extends PreferenceFragment {

    private static final String TAG = PreferencesFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final SwitchPreference themePref = (SwitchPreference) findPreference(getString(R.string.pref_key_theme));
        themePref.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
            Boolean darkTheme = (Boolean) newValue;
            Notes.setAppTheme(darkTheme);
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().recreate();
            return true;
        });

        final SwitchPreference wifiOnlyPref = (SwitchPreference) findPreference(getString(R.string.pref_key_wifi_only));
        wifiOnlyPref.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
            Boolean syncOnWifiOnly = (Boolean) newValue;
            Log.v(TAG, "syncOnWifiOnly: " + syncOnWifiOnly);
            return true;
        });
    }
}
