package it.niedermann.owncloud.notes.android.fragment.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.util.SupportUtil;

public class AboutFragmentLicenseTab extends Fragment {

    @BindView(R.id.about_icons_disclaimer)
    TextView iconsDisclaimer;
    @BindView(R.id.about_app_license_button)
    Button appLicenseButton;

    @OnClick(R.id.about_app_license_button)
    void openLicense() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_license))));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about_license_tab, container, false);
        ButterKnife.bind(this, v);
        SupportUtil.setHtml(iconsDisclaimer, R.string.about_icons_disclaimer, getString(R.string.about_app_icon_author));
        return v;
    }
}