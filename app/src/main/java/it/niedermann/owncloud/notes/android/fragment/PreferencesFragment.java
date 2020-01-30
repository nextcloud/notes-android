package it.niedermann.owncloud.notes.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.SyncWorker;
import it.niedermann.owncloud.notes.util.Notes;

public class PreferencesFragment extends PreferenceFragmentCompat {

    private static final String TAG = PreferencesFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        final SwitchPreference themePref = findPreference(getString(R.string.pref_key_theme));
        themePref.setOnPreferenceChangeListener((preference, newValue) -> {
            Boolean darkTheme = (Boolean) newValue;
            Notes.setAppTheme(darkTheme);
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().recreate();
            return true;
        });

        final SwitchPreference wifiOnlyPref = findPreference(getString(R.string.pref_key_wifi_only));
        wifiOnlyPref.setOnPreferenceChangeListener((preference, newValue) -> {
            Boolean syncOnWifiOnly = (Boolean) newValue;
            Log.v(TAG, "syncOnWifiOnly: " + syncOnWifiOnly);
            return true;
        });

        final ListPreference syncPref = findPreference(getString(R.string.pref_key_background_sync));
        syncPref.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.v(TAG, "syncPref: " + preference + " - newValue: " + newValue);
            SyncWorker.update(getContext(), newValue.toString());
            return true;
        });
    }
}
