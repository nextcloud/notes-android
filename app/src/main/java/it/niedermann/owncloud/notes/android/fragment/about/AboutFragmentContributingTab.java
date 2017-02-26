package it.niedermann.owncloud.notes.android.fragment.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.SupportUtil;

public class AboutFragmentContributingTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about_contribution_tab, container, false);
        SupportUtil.setHtml((TextView) v.findViewById(R.id.about_source), R.string.about_source, getString(R.string.url_source));
        SupportUtil.setHtml((TextView) v.findViewById(R.id.about_issues), R.string.about_issues, getString(R.string.url_issues));
        SupportUtil.setHtml((TextView) v.findViewById(R.id.about_translate), R.string.about_translate, getString(R.string.url_translations));
        return v;
    }
}