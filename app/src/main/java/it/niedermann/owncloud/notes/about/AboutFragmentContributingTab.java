/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2016-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2016-2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.about;

import static it.niedermann.owncloud.notes.shared.util.SupportUtil.setTextWithURL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.nextcloud.android.common.ui.theme.utils.ColorRole;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedFragment;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.FragmentAboutContributionTabBinding;

public class AboutFragmentContributingTab extends BrandedFragment {

    private FragmentAboutContributionTabBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAboutContributionTabBinding.inflate(inflater, container, false);
        setTextWithURL(binding.aboutSource, getResources(), R.string.about_source, R.string.url_source, R.string.url_source);
        setTextWithURL(binding.aboutIssues, getResources(), R.string.about_issues, R.string.url_issues, R.string.url_issues);
        setTextWithURL(binding.aboutTranslate, getResources(), R.string.about_translate, R.string.url_translations, R.string.url_translations);
        return binding.getRoot();
    }

    @Override
    public void applyBrand(int color) {
        final var util = BrandingUtil.of(color, requireContext());
        util.platform.colorTextView(binding.aboutIssuesHeadline);
        util.platform.colorTextView(binding.aboutIssues, ColorRole.ON_SURFACE_VARIANT);
        util.platform.colorTextView(binding.aboutSourceHeadline);
        util.platform.colorTextView(binding.aboutSource, ColorRole.ON_SURFACE_VARIANT);
        util.platform.colorTextView(binding.aboutTranslateHeadline);
        util.platform.colorTextView(binding.aboutTranslate, ColorRole.ON_SURFACE_VARIANT);
    }
}