package it.niedermann.owncloud.notes.android.fragment.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import it.niedermann.owncloud.notes.BuildConfig;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.FragmentAboutCreditsTabBinding;
import it.niedermann.owncloud.notes.util.SupportUtil;

public class AboutFragmentCreditsTab extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAboutCreditsTabBinding binding = FragmentAboutCreditsTabBinding.inflate(inflater, container, false);
        SupportUtil.setHtml(binding.aboutVersion, R.string.about_version, "v" + BuildConfig.VERSION_NAME);
        SupportUtil.setHtml(binding.aboutMaintainer, R.string.about_maintainer);
        SupportUtil.setHtml(binding.aboutTranslators, R.string.about_translators_transifex, getString(R.string.url_translations));
        return binding.getRoot();
    }
}