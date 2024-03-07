/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.preferences;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.Branded;
import it.niedermann.owncloud.notes.branding.BrandedSwitchPreference;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.persistence.SyncWorker;
import it.niedermann.owncloud.notes.shared.util.DeviceCredentialUtil;

public class PreferencesFragment extends PreferenceFragmentCompat implements Branded {

    private static final String TAG = PreferencesFragment.class.getSimpleName();

    private PreferencesViewModel viewModel;

    private BrandedSwitchPreference fontPref;
    private BrandedSwitchPreference lockPref;
    private BrandedSwitchPreference wifiOnlyPref;
    private BrandedSwitchPreference gridViewPref;
    private BrandedSwitchPreference preventScreenCapturePref;
    private BrandedSwitchPreference backgroundSyncPref;
    private BrandedSwitchPreference keepScreenOnPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        viewModel = new ViewModelProvider(requireActivity()).get(PreferencesViewModel.class);

        fontPref = findPreference(getString(R.string.pref_key_font));

        gridViewPref = findPreference(getString(R.string.pref_key_gridview));
        if (gridViewPref != null) {
            gridViewPref.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
                final Boolean gridView = (Boolean) newValue;
                Log.v(TAG, "gridView: " + gridView);
                viewModel.resultCode$.setValue(Activity.RESULT_OK);
                NotesApplication.updateGridViewEnabled(gridView);
                return true;
            });
        } else {
            Log.e(TAG, "Could not find preference with key: \"" + getString(R.string.pref_key_gridview) + "\"");
        }

        keepScreenOnPref = findPreference(getString(R.string.pref_key_keep_screen_on));
        if (keepScreenOnPref != null) {
            keepScreenOnPref.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
                Log.v(TAG, "keepScreenOnPref: " + keepScreenOnPref);
                return true;
            });
        } else {
            Log.e(TAG, "Could not find preference with key: \"" + getString(R.string.pref_key_gridview) + "\"");
        }

        preventScreenCapturePref = findPreference(getString(R.string.pref_key_prevent_screen_capture));
        if (preventScreenCapturePref == null) {
            Log.e(TAG, "Could not find \"" + getString(R.string.pref_key_prevent_screen_capture) + "\"-preference.");
        }

        lockPref = findPreference(getString(R.string.pref_key_lock));
        if (lockPref != null) {
            if (!DeviceCredentialUtil.areCredentialsAvailable(requireContext())) {
                lockPref.setVisible(false);
            } else {
                lockPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    NotesApplication.setLockedPreference((Boolean) newValue);
                    return true;
                });
            }
        } else {
            Log.e(TAG, "Could not find \"" + getString(R.string.pref_key_lock) + "\"-preference.");
        }

        final var themePref = findPreference(getString(R.string.pref_key_theme));
        assert themePref != null;
        themePref.setOnPreferenceChangeListener((preference, newValue) -> {
            NotesApplication.setAppTheme(DarkModeSetting.valueOf((String) newValue));
            viewModel.resultCode$.setValue(Activity.RESULT_OK);
            ActivityCompat.recreate(requireActivity());
            return true;
        });

        wifiOnlyPref = findPreference(getString(R.string.pref_key_wifi_only));
        assert wifiOnlyPref != null;
        wifiOnlyPref.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.i(TAG, "syncOnWifiOnly: " + newValue);
            return true;
        });

        backgroundSyncPref = findPreference(getString(R.string.pref_key_background_sync));
        assert backgroundSyncPref != null;
        backgroundSyncPref.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.i(TAG, "backgroundSync: " + newValue);
            SyncWorker.update(requireContext(), (Boolean) newValue);
            return true;
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        final var context = requireContext();
        @ColorInt final int color = BrandingUtil.readBrandMainColor(context);
        applyBrand(color);
    }

    /**
     * Change color for backgroundSyncPref as well
     * https://github.com/stefan-niedermann/nextcloud-deck/issues/531
     *
     * @param color color of main brand
     */

    @Override
    public void applyBrand(int color) {
        fontPref.applyBrand(color);
        lockPref.applyBrand(color);
        wifiOnlyPref.applyBrand(color);
        gridViewPref.applyBrand(color);
        preventScreenCapturePref.applyBrand(color);
        backgroundSyncPref.applyBrand(color);
        keepScreenOnPref.applyBrand(color);
    }
}
