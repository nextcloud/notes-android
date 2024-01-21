package it.niedermann.owncloud.notes.branding;

import static it.niedermann.owncloud.notes.branding.BrandingUtil.readBrandMainColorLiveData;

import android.util.TypedValue;
import android.view.Menu;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.R;

public abstract class BrandedActivity extends AppCompatActivity implements Branded {

    @ColorInt
    protected int colorAccent;

    @Override
    protected void onStart() {
        super.onStart();

        final var typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        colorAccent = typedValue.data;

        readBrandMainColorLiveData(this).observe(this, this::applyBrand);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final var utils = BrandingUtil.of(colorAccent, this);

        for (int i = 0; i < menu.size(); i++) {
            utils.platform.colorToolbarMenuIcon(this, menu.getItem(i));
        }

        return super.onCreateOptionsMenu(menu);
    }
}
