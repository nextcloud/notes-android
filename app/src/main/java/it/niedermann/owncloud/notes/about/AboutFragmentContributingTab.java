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
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.FragmentAboutContributionTabBinding;
import it.niedermann.owncloud.notes.shared.util.SupportUtil;

public class AboutFragmentContributingTab extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final var binding = FragmentAboutContributionTabBinding.inflate(inflater, container, false);
        setTextWithURL(binding.aboutSource, getResources(), R.string.about_source, R.string.url_source, R.string.url_source);
        setTextWithURL(binding.aboutIssues, getResources(), R.string.about_issues, R.string.url_issues, R.string.url_issues);
        setTextWithURL(binding.aboutTranslate, getResources(), R.string.about_translate, R.string.url_translations, R.string.url_translations);
        return binding.getRoot();
    }
}