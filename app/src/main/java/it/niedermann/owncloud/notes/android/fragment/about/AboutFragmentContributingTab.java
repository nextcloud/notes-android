package it.niedermann.owncloud.notes.android.fragment.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.FragmentAboutContributionTabBinding;
import it.niedermann.owncloud.notes.util.SupportUtil;

public class AboutFragmentContributingTab extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAboutContributionTabBinding b = FragmentAboutContributionTabBinding.inflate(inflater, container, false);
        SupportUtil.setHtml(b.aboutSource, R.string.about_source, getString(R.string.url_source));
        SupportUtil.setHtml(b.aboutIssues, R.string.about_issues, getString(R.string.url_issues));
        SupportUtil.setHtml(b.aboutTranslate, R.string.about_translate, getString(R.string.url_translations));
        return b.getRoot();
    }
}