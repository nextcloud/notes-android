package it.niedermann.owncloud.notes.android.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import androidx.annotation.Nullable;
import android.widget.Toast;

import at.bitfire.cert4android.CustomCertManager;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.Notes;

public class PreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference resetTrust = findPreference(getString(R.string.pref_key_reset_trust));
        resetTrust.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CustomCertManager.Companion.resetCertificates(getActivity());
                Toast.makeText(getActivity(), getString(R.string.settings_cert_reset_toast), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        final SwitchPreference themePref = (SwitchPreference) findPreference(getString(R.string.pref_key_theme));
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        themePref.setSummary(sp.getBoolean(getString(R.string.pref_key_theme), false) ?
                            getString(R.string.pref_value_theme_dark) : getString(R.string.pref_value_theme_light));
        themePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean darkTheme = (Boolean) newValue;
                Notes.setAppTheme(darkTheme);
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();

                return true;
            }
        });
    }
}
