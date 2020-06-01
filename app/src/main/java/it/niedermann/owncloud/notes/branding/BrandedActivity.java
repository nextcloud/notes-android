package it.niedermann.owncloud.notes.branding;

import android.content.res.ColorStateList;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public abstract class BrandedActivity extends AppCompatActivity implements Branded {

    private static final String TAG = BrandedActivity.class.getSimpleName();

    public static void applyBrandToFAB(@ColorInt int mainColor, @ColorInt int textColor, @NonNull FloatingActionButton fab) {
        fab.setSupportBackgroundTintList(ColorStateList.valueOf(mainColor));
        fab.setColorFilter(textColor);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (BrandingUtil.isBrandingEnabled(this)) {
            @ColorInt final int mainColor = BrandingUtil.readBrandMainColor(this);
            @ColorInt final int textColor = BrandingUtil.readBrandTextColor(this);
            applyBrand(mainColor, textColor);
        }
    }

    protected void applyBrandToPrimaryTabLayout(@ColorInt int mainColor, @ColorInt int textColor, @NonNull TabLayout tabLayout) {
        tabLayout.setBackgroundColor(mainColor);
        tabLayout.setTabTextColors(textColor, textColor);
        tabLayout.setTabIconTint(ColorStateList.valueOf(textColor));
        tabLayout.setSelectedTabIndicatorColor(textColor);
    }
}
