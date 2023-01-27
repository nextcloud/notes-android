package it.niedermann.owncloud.notes.about;

import static it.niedermann.owncloud.notes.shared.util.SupportUtil.setTextWithURL;
import static it.niedermann.owncloud.notes.shared.util.SupportUtil.strong;
import static it.niedermann.owncloud.notes.shared.util.SupportUtil.url;

import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import it.niedermann.owncloud.notes.BuildConfig;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.FragmentAboutCreditsTabBinding;

public class AboutFragmentCreditsTab extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final var binding = FragmentAboutCreditsTabBinding.inflate(inflater, container, false);
        binding.aboutVersion.setText(getString(R.string.about_version, strong(BuildConfig.VERSION_NAME)));
        final var founderText = getString(R.string.about_developers_stefan, getString(R.string.about_developers_founder));
        setTextWithURL(binding.aboutDevelopers, getResources(), R.string.about_developers, founderText, R.string.url_niedermann_it);
        setTextWithURL(binding.aboutTranslators, getResources(), R.string.about_translators_transifex, R.string.about_translators_transifex_label, R.string.url_translations);
        return binding.getRoot();
    }
}
