package it.niedermann.owncloud.notes.android.fragment.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.niedermann.owncloud.notes.R;

public class AboutFragmentContributingTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about_contribution_tab, container, false);
        ((TextView) v.findViewById(R.id.about_source)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) v.findViewById(R.id.about_issues)).setMovementMethod(LinkMovementMethod.getInstance());
        return v;
    }
}