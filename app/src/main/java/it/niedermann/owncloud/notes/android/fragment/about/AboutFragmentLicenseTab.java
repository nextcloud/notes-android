package it.niedermann.owncloud.notes.android.fragment.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.SupportUtil;

public class AboutFragmentLicenseTab extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about_license_tab, container, false);
        Button button = (Button) v.findViewById(R.id.about_app_license_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_license))));
            }
        });
        SupportUtil.setHtml((TextView) v.findViewById(R.id.about_icons_disclaimer), R.string.about_icons_disclaimer, getString(R.string.about_app_icon_author));
        return v;
    }
}