package it.niedermann.owncloud.notes.branding;

import static it.niedermann.owncloud.notes.NotesApplication.isDarkThemeActive;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.nextcloud.android.common.ui.theme.MaterialSchemes;
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase;

import it.niedermann.android.util.ColorUtil;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.shared.util.NotesColorUtil;
import scheme.Scheme;

public class NotesViewThemeUtils extends ViewThemeUtilsBase {

    private static final String TAG = NotesViewThemeUtils.class.getSimpleName();

    public NotesViewThemeUtils(@NonNull MaterialSchemes schemes) {
        super(schemes);
    }

    @ColorInt
    public int getOnPrimaryContainer(@NonNull Context context) {
        return withScheme(context, Scheme::getOnPrimaryContainer);
    }

    @Deprecated(forRemoval = true)
    public void applyBrandToPrimaryToolbar(@NonNull AppBarLayout appBarLayout, @NonNull Toolbar toolbar, @ColorInt int color) {
        // FIXME Workaround for https://github.com/nextcloud/notes-android/issues/889
        appBarLayout.setBackgroundColor(ContextCompat.getColor(appBarLayout.getContext(), R.color.primary));

        final var overflowDrawable = toolbar.getOverflowIcon();
        if (overflowDrawable != null) {
            overflowDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(overflowDrawable);
        }

        final var navigationDrawable = toolbar.getNavigationIcon();
        if (navigationDrawable != null) {
            navigationDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            toolbar.setNavigationIcon(navigationDrawable);
        }
    }

    @Deprecated(forRemoval = true)
    public void colorLayerDrawable(@NonNull LayerDrawable check, @IdRes int areaToColor, @ColorInt int mainColor) {
        final var drawable = check.findDrawableByLayerId(areaToColor);
        if (drawable == null) {
            Log.e(TAG, "Could not find areaToColor (" + areaToColor + "). Cannot apply brand.");
        } else {
            DrawableCompat.setTint(drawable, mainColor);
        }
    }

    @ColorInt
    public int getTextHighlightBackgroundColor(@NonNull Context context, @ColorInt int mainColor, @ColorInt int colorPrimary, @ColorInt int colorAccent) {
        if (isDarkThemeActive(context)) { // Dark background
            if (ColorUtil.INSTANCE.isColorDark(mainColor)) { // Dark brand color
                if (NotesColorUtil.contrastRatioIsSufficient(mainColor, colorPrimary)) { // But also dark text
                    return mainColor;
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            } else { // Light brand color
                if (NotesColorUtil.contrastRatioIsSufficient(mainColor, colorAccent)) { // But also dark text
                    return Color.argb(77, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor));
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            }
        } else { // Light background
            if (ColorUtil.INSTANCE.isColorDark(mainColor)) { // Dark brand color
                if (NotesColorUtil.contrastRatioIsSufficient(mainColor, colorAccent)) { // But also dark text
                    return Color.argb(77, Color.red(mainColor), Color.green(mainColor), Color.blue(mainColor));
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            } else { // Light brand color
                if (NotesColorUtil.contrastRatioIsSufficient(mainColor, colorPrimary)) { // But also dark text
                    return mainColor;
                } else {
                    return ContextCompat.getColor(context, R.color.defaultTextHighlightBackground);
                }
            }
        }
    }
}
