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
            switch (position) {
                default: // Fall-through to credits tab
                case POS_CREDITS:
                    tab.setText(R.string.about_credits_tab_title);
                    break;
                case POS_CONTRIB:
                    tab.setText(R.string.about_contribution_tab_title);
                    break;
                case POS_LICENSE:
                    tab.setText(R.string.about_license_tab_title);
                    break;
            }
        }).attach();
    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToPrimaryToolbar(binding.appBar, binding.toolbar);
        @ColorInt int finalMainColor = BrandingUtil.getSecondaryForegroundColorDependingOnTheme(this, mainColor);
        binding.tabs.setSelectedTabIndicatorColor(finalMainColor);
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
            switch (position) {
                default: // Fall-through to credits tab
                case POS_CREDITS:
                    return new AboutFragmentCreditsTab();

                case POS_CONTRIB:
                    return new AboutFragmentContributingTab();

                case POS_LICENSE:
                    return new AboutFragmentLicenseTab();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity as oppose to navigating up
        return true;
    }
}