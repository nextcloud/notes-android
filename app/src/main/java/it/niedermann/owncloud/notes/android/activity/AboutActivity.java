package it.niedermann.owncloud.notes.android.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import it.niedermann.nextcloud.exception.ExceptionHandler;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.about.AboutFragmentContributingTab;
import it.niedermann.owncloud.notes.android.fragment.about.AboutFragmentCreditsTab;
import it.niedermann.owncloud.notes.android.fragment.about.AboutFragmentLicenseTab;
import it.niedermann.owncloud.notes.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));
        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.pager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));
        binding.tabs.setupWithViewPager(binding.pager);
    }

    private class TabsPagerAdapter extends FragmentPagerAdapter {

        TabsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return 3;
        }

        /**
         * return the right fragment for the given position
         */
        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return new AboutFragmentContributingTab();

                case 2:
                    return new AboutFragmentLicenseTab();

                default:
                    return new AboutFragmentCreditsTab();
            }
        }

        /**
         * generate title based on given position
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.about_credits_tab_title);

                case 1:
                    return getString(R.string.about_contribution_tab_title);

                case 2:
                    return getString(R.string.about_license_tab_title);

                default:
                    return null;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity as oppose to navigating up
        return true;
    }
}