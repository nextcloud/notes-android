package it.niedermann.owncloud.notes.android.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.fragment.about.AboutFragmentContributingTab;
import it.niedermann.owncloud.notes.android.fragment.about.AboutFragmentCreditsTab;
import it.niedermann.owncloud.notes.android.fragment.about.AboutFragmentLicenseTab;

public class AboutActivity extends AppCompatActivity {

    private FragmentTabHost mTabHost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.about_credits_tab_title)).setIndicator(getString(R.string.about_credits_tab_title)),
                AboutFragmentCreditsTab.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.about_contribution_tab_title)).setIndicator(getString(R.string.about_contribution_tab_title)),
                AboutFragmentContributingTab.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.about_license_tab_title)).setIndicator(getString(R.string.about_license_tab_title)),
                AboutFragmentLicenseTab.class, null);
    }
}