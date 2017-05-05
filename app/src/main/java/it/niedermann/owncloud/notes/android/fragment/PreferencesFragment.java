package it.niedermann.owncloud.notes.android.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.widget.Toast;

import at.bitfire.cert4android.CustomCertManager;
import it.niedermann.owncloud.notes.R;

public class PreferencesFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference resetTrust = findPreference("resetTrust");
        resetTrust.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CustomCertManager ccm = new CustomCertManager(getActivity(), true);
                ccm.resetCertificates();
                ccm.close();
                Toast.makeText(getActivity(), getString(R.string.settings_cert_reset_toast), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}
