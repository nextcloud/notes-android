package it.niedermann.owncloud.notes.branding;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import it.niedermann.owncloud.notes.R;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static it.niedermann.owncloud.notes.util.ColorUtil.isColorDark;

public abstract class BrandedActivity extends AppCompatActivity implements Branded {

    private static final String TAG = BrandedActivity.class.getSimpleName();

    public static void applyBrandToStatusbar(@NonNull Window window, @ColorInt int mainColor, @ColorInt int textColor) {
        if (SDK_INT >= LOLLIPOP) { // Set status bar color
            window.setStatusBarColor(mainColor);
            if (SDK_INT >= M) { // Set icon and text color of status bar
                final View decorView = window.getDecorView();
                if (isColorDark(mainColor)) {
                    int flags = decorView.getSystemUiVisibility();
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    decorView.setSystemUiVisibility(flags);
                } else {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        }
    }

    public static void applyBrandToPrimaryToolbar(@ColorInt int mainColor, @ColorInt int textColor, @NonNull Toolbar toolbar) {
        toolbar.setBackgroundColor(mainColor);
        toolbar.setTitleTextColor(textColor);
        final Drawable overflowDrawable = toolbar.getOverflowIcon();
        if (overflowDrawable != null) {
            overflowDrawable.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(overflowDrawable);
        }

        final Drawable navigationDrawable = toolbar.getNavigationIcon();
        if (navigationDrawable != null) {
            navigationDrawable.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
            toolbar.setNavigationIcon(navigationDrawable);
        }
    }

    public static void applyBrandToFAB(@ColorInt int mainColor, @ColorInt int textColor, @NonNull FloatingActionButton fab) {
        fab.setSupportBackgroundTintList(ColorStateList.valueOf(mainColor));
        fab.setColorFilter(textColor);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BrandingUtil.isBrandingEnabled(this)) {
            @ColorInt final int mainColor = BrandingUtil.readBrandMainColor(this);
            @ColorInt final int textColor = BrandingUtil.readBrandTextColor(this);
            setTheme(isColorDark(textColor) ? R.style.AppThemeLightBrand : R.style.AppTheme);
            applyBrandToStatusbar(getWindow(), mainColor, textColor);
        } else {
            setTheme(R.style.AppTheme);
        }
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

    // TODO maybe this can be handled in R.style.AppThemLightBrand
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        @ColorInt final int textColor = BrandingUtil.readBrandTextColor(this);
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, textColor);
                menu.getItem(i).setIcon(drawable);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    protected void applyBrandToPrimaryTabLayout(@ColorInt int mainColor, @ColorInt int textColor, @NonNull TabLayout tabLayout) {
        tabLayout.setBackgroundColor(mainColor);
        tabLayout.setTabTextColors(textColor, textColor);
        tabLayout.setTabIconTint(ColorStateList.valueOf(textColor));
        tabLayout.setSelectedTabIndicatorColor(textColor);
    }
}
