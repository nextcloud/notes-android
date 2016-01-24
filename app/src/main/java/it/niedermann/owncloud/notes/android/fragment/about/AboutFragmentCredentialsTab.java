package it.niedermann.owncloud.notes.android.fragment.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.niedermann.owncloud.notes.R;

public class AboutFragmentCredentialsTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about_credentials_tab, container, false);
    }
}