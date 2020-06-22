package it.niedermann.owncloud.notes.branding;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import it.niedermann.owncloud.notes.shared.util.ColorUtil;

public class BrandedSnackbar {

    @NonNull
    public static Snackbar make(@NonNull View view, @NonNull CharSequence text, @BaseTransientBottomBar.Duration int duration) {
        final Snackbar snackbar = Snackbar.make(view, text, duration);
        if (BrandingUtil.isBrandingEnabled(view.getContext())) {
            int color = BrandingUtil.readBrandMainColor(view.getContext());
            snackbar.setActionTextColor(ColorUtil.isColorDark(color) ? Color.WHITE : color);
        }
        return snackbar;
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @StringRes int resId, @BaseTransientBottomBar.Duration int duration) {
        return make(view, view.getResources().getText(resId), duration);
    }

}