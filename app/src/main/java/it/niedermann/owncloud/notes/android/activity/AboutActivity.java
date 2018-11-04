package it.niedermann.owncloud.notes.android.activity;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.about.AboutFragmentContributingTab;
import it.niedermann.owncloud.notes.android.fragment.about.AboutFragmentCreditsTab;
import it.niedermann.owncloud.notes.android.fragment.about.AboutFragmentLicenseTab;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.pager)
    ViewPager mViewPager;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        mViewPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private class TabsPagerAdapter extends FragmentPagerAdapter {
        private final int PAGE_COUNT = 3;

        public TabsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        /**
         * return the right fragment for the given position
         */
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AboutFragmentCreditsTab();

                case 1:
                    return new AboutFragmentContributingTab();

                case 2:
                    return new AboutFragmentLicenseTab();

                default:
                    return null;
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
}