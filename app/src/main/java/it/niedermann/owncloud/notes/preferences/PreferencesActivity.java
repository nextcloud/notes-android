/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.preferences;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ActivityPreferencesBinding;

public class PreferencesActivity extends LockedActivity {

    private ActivityPreferencesBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferencesViewModel viewModel = new ViewModelProvider(this).get(PreferencesViewModel.class);
        viewModel.resultCode$.observe(this, this::setResult);

        binding = ActivityPreferencesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, new PreferencesFragment())
                .commit();
    }

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, this);
        util.platform.themeStatusBar(this);
        util.material.themeToolbar(binding.toolbar);
    }
}
