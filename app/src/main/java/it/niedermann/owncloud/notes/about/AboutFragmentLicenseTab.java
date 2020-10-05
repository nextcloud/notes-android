package it.niedermann.owncloud.notes.about;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandedFragment;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.FragmentAboutLicenseTabBinding;
import it.niedermann.owncloud.notes.shared.util.ColorUtil;
import it.niedermann.owncloud.notes.shared.util.SupportUtil;

public class AboutFragmentLicenseTab extends BrandedFragment {

    private FragmentAboutLicenseTabBinding binding;

    private void openLicense() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_license))));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAboutLicenseTabBinding.inflate(inflater, container, false);
        binding.aboutAppLicenseButton.setOnClickListener((v) -> openLicense());
        SupportUtil.setHtml(binding.aboutIconsDisclaimer, R.string.about_icons_disclaimer, getString(R.string.about_app_icon_author));
        return binding.getRoot();
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        @ColorInt final int finalMainColor = BrandingUtil.getSecondaryForegroundColorDependingOnTheme(requireContext(), mainColor);
        DrawableCompat.setTintList(binding.aboutAppLicenseButton.getBackground(), ColorStateList.valueOf(finalMainColor));
        binding.aboutAppLicenseButton.setTextColor(ColorUtil.getForegroundColorForBackgroundColor(finalMainColor));
    }
}