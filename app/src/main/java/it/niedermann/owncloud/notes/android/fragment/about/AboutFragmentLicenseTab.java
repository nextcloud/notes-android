package it.niedermann.owncloud.notes.android.fragment.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.FragmentAboutLicenseTabBinding;
import it.niedermann.owncloud.notes.util.SupportUtil;

public class AboutFragmentLicenseTab extends Fragment {

    private void openLicense() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_license))));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAboutLicenseTabBinding binding = FragmentAboutLicenseTabBinding.inflate(inflater, container, false);
        binding.aboutAppLicenseButton.setOnClickListener((v) -> openLicense());
        SupportUtil.setHtml(binding.aboutIconsDisclaimer, R.string.about_icons_disclaimer, getString(R.string.about_app_icon_author));
        return binding.getRoot();
    }
}