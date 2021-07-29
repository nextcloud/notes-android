package it.niedermann.owncloud.notes.about;

import android.os.Bundle;
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
        SupportUtil.setHtml(binding.aboutSource, R.string.about_source, getString(R.string.url_source));
        SupportUtil.setHtml(binding.aboutIssues, R.string.about_issues, getString(R.string.url_issues));
        SupportUtil.setHtml(binding.aboutTranslate, R.string.about_translate, getString(R.string.url_translations));
        return binding.getRoot();
    }
}