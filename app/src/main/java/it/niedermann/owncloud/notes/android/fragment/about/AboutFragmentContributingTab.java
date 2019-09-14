package it.niedermann.owncloud.notes.android.fragment.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.SupportUtil;

public class AboutFragmentContributingTab extends Fragment {

    @BindView(R.id.about_source)
    TextView aboutSource;
    @BindView(R.id.about_issues)
    TextView aboutIssues;
    @BindView(R.id.about_translate)
    TextView aboutTranslate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about_contribution_tab, container, false);
        ButterKnife.bind(this, v);
        SupportUtil.setHtml(aboutSource, R.string.about_source, getString(R.string.url_source));
        SupportUtil.setHtml(aboutIssues, R.string.about_issues, getString(R.string.url_issues));
        SupportUtil.setHtml(aboutTranslate, R.string.about_translate, getString(R.string.url_translations));
        return v;
    }
}