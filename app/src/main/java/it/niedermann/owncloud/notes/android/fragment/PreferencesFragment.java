package it.niedermann.owncloud.notes.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.DarkModeSetting;
import it.niedermann.owncloud.notes.persistence.SyncWorker;
import it.niedermann.owncloud.notes.util.DeviceCredentialUtil;
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

        final SwitchPreference lockPref = findPreference(getString(R.string.pref_key_lock));
        if (lockPref != null) {
            if (!DeviceCredentialUtil.areCredentialsAvailable(requireContext())) {
                lockPref.setVisible(false);
            } else {
                lockPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    Notes.setLockedPreference((Boolean) newValue);
                    return true;
                });
            }
        } else {
            Log.e(TAG, "Could not find \"" + getString(R.string.pref_key_lock) + "\"-preference.");
        }

        final ListPreference themePref = findPreference(getString(R.string.pref_key_theme));
        assert themePref != null;
        themePref.setOnPreferenceChangeListener((preference, newValue) -> {
            Notes.setAppTheme(DarkModeSetting.valueOf((String) newValue));
            requireActivity().setResult(Activity.RESULT_OK);
            requireActivity().recreate();
            return true;
        });

        final SwitchPreference wifiOnlyPref = findPreference(getString(R.string.pref_key_wifi_only));
        assert wifiOnlyPref != null;
        wifiOnlyPref.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.i(TAG, "syncOnWifiOnly: " + newValue);
            return true;
        });

        final ListPreference syncPref = findPreference(getString(R.string.pref_key_background_sync));
        assert syncPref != null;
        syncPref.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.i(TAG, "syncPref: " + preference + " - newValue: " + newValue);
            SyncWorker.update(requireContext(), newValue.toString());
            return true;
        });
    }
}
