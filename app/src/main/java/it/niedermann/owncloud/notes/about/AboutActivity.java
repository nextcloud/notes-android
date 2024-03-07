/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2015-2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2021 TacoTheDank <skytkrsfan3895@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.about;

import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;

import it.niedermann.owncloud.notes.LockedActivity;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ActivityAboutBinding;

public class AboutActivity extends LockedActivity {

    private ActivityAboutBinding binding;
    private final static int POS_CREDITS = 0;
    private final static int POS_CONTRIB = 1;
    private final static int POS_LICENSE = 2;
    private final static int TOTAL_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.pager.setAdapter(new TabsStateAdapter(this));
        // generate title based on given position
        new TabLayoutMediator(binding.tabs, binding.pager, (tab, position) -> {
            switch (position) { // Fall-through to credits tab
                default ->
                        tab.setText(R.string.about_credits_tab_title);
                case POS_CONTRIB ->
                        tab.setText(R.string.about_contribution_tab_title);
                case POS_LICENSE ->
                        tab.setText(R.string.about_license_tab_title);
            }
        }).attach();
    }

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, this);
        util.platform.themeStatusBar(this);
        util.material.themeToolbar(binding.toolbar);
        util.material.themeTabLayoutOnSurface(binding.tabs);
    }

    private static class TabsStateAdapter extends FragmentStateAdapter {

        TabsStateAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public int getItemCount() {
            return TOTAL_COUNT;
        }

        /**
         * return the right fragment for the given position
         */
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return switch (position) { // Fall-through to credits tab
                default -> new AboutFragmentCreditsTab();
                case POS_CONTRIB -> new AboutFragmentContributingTab();
                case POS_LICENSE -> new AboutFragmentLicenseTab();
            };
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity as oppose to navigating up
        return true;
    }
}