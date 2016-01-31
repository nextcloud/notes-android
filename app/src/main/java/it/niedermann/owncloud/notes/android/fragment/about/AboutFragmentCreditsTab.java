package it.niedermann.owncloud.notes.android.fragment.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.niedermann.owncloud.notes.R;

public class AboutFragmentCreditsTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about_credits_tab, container, false);
        ((TextView) v.findViewById(R.id.about_maintainer)).setMovementMethod(LinkMovementMethod.getInstance());
        return v;
    }
}