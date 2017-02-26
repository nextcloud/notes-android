package it.niedermann.owncloud.notes.android.fragment.about;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.SupportUtil;

public class AboutFragmentCreditsTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about_credits_tab, container, false);
        String versionName;
        try {
            versionName = "v"+getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "(error)";
        }
        SupportUtil.setHtml((TextView) v.findViewById(R.id.about_version), R.string.about_version, versionName);
        SupportUtil.setHtml((TextView) v.findViewById(R.id.about_maintainer), R.string.about_maintainer);
        SupportUtil.setHtml((TextView) v.findViewById(R.id.about_translators), R.string.about_translators_transifex, getString(R.string.url_translations));
        return v;
    }
}