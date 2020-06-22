package it.niedermann.owncloud.notes.preferences;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.Branded;
import it.niedermann.owncloud.notes.branding.BrandedSwitchPreference;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.persistence.SyncWorker;
import it.niedermann.owncloud.notes.shared.util.DeviceCredentialUtil;
import it.niedermann.owncloud.notes.NotesApplication;

import static it.niedermann.owncloud.notes.widget.notelist.NoteListWidget.updateNoteListWidgets;

public class PreferencesFragment extends PreferenceFragmentCompat implements Branded {

    private static final String TAG = PreferencesFragment.class.getSimpleName();

    private BrandedSwitchPreference fontPref;
    private BrandedSwitchPreference lockPref;
    private BrandedSwitchPreference wifiOnlyPref;
    private BrandedSwitchPreference brandingPref;
    private BrandedSwitchPreference gridViewPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        fontPref = findPreference(getString(R.string.pref_key_font));

        brandingPref = findPreference(getString(R.string.pref_key_branding));
        if (brandingPref != null) {
            brandingPref.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
                updateNoteListWidgets(requireContext());
                final Boolean branding = (Boolean) newValue;
                Log.v(TAG, "branding: " + branding);
                requireActivity().setResult(Activity.RESULT_OK);
                requireActivity().recreate();
                return true;
            });
        } else {
            Log.e(TAG, "Could not find preference with key: \"" + getString(R.string.pref_key_branding) + "\"");
        }

        gridViewPref = findPreference(getString(R.string.pref_key_gridview));
        if (gridViewPref != null) {
            gridViewPref.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
                final Boolean gridView = (Boolean) newValue;
                Log.v(TAG, "gridView: " + gridView);
                requireActivity().setResult(Activity.RESULT_OK);
                NotesApplication.updateGridViewEnabled(gridView);
                return true;
            });
        } else {
            Log.e(TAG, "Could not find preference with key: \"" + getString(R.string.pref_key_branding) + "\"");
        }

        lockPref = findPreference(getString(R.string.pref_key_lock));
        if (lockPref != null) {
            if (!DeviceCredentialUtil.areCredentialsAvailable(requireContext())) {
                lockPref.setVisible(false);
                Preference securityCategory = findPreference(getString(R.string.pref_category_security));
                if (securityCategory != null) {
                    securityCategory.setVisible(false);
                } else {
                    Log.e(TAG, "Could not find preference " + getString(R.string.pref_category_security));
                }
            } else {
                lockPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    NotesApplication.setLockedPreference((Boolean) newValue);
                    return true;
                });
            }
        } else {
            Log.e(TAG, "Could not find \"" + getString(R.string.pref_key_lock) + "\"-preference.");
        }

        final ListPreference themePref = findPreference(getString(R.string.pref_key_theme));
        assert themePref != null;
        themePref.setOnPreferenceChangeListener((preference, newValue) -> {
            NotesApplication.setAppTheme(DarkModeSetting.valueOf((String) newValue));
            requireActivity().setResult(Activity.RESULT_OK);
            requireActivity().recreate();
            return true;
        });

        wifiOnlyPref = findPreference(getString(R.string.pref_key_wifi_only));
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


    @Override
    public void onStart() {
        super.onStart();
        @Nullable Context context = getContext();
        if (context != null) {
            @ColorInt final int mainColor = BrandingUtil.readBrandMainColor(context);
            @ColorInt final int textColor = BrandingUtil.readBrandTextColor(context);
            applyBrand(mainColor, textColor);
        }
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        fontPref.applyBrand(mainColor, textColor);
        lockPref.applyBrand(mainColor, textColor);
        wifiOnlyPref.applyBrand(mainColor, textColor);
        brandingPref.applyBrand(mainColor, textColor);
        gridViewPref.applyBrand(mainColor, textColor);
    }
}
