package it.niedermann.owncloud.notes.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import at.bitfire.cert4android.CustomCertManager;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.Notes;

public class PreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference resetTrust = findPreference(getString(R.string.pref_key_reset_trust));
        resetTrust.setOnPreferenceClickListener((Preference preference) -> {
            CustomCertManager.Companion.resetCertificates(getActivity());
            Toast.makeText(getActivity(), getString(R.string.settings_cert_reset_toast), Toast.LENGTH_SHORT).show();
            return true;
        });

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
            Log.v("Notes", "syncOnWifiOnly: " + syncOnWifiOnly);
            return true;
        });
    }
}
