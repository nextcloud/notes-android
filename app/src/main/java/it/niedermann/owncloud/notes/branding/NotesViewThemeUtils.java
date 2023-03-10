package it.niedermann.owncloud.notes.branding;

import static com.nextcloud.android.common.ui.util.ColorStateListUtilsKt.buildColorStateList;
import static com.nextcloud.android.common.ui.util.PlatformThemeUtil.isDarkMode;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.nextcloud.android.common.ui.theme.MaterialSchemes;
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase;
import com.nextcloud.android.common.ui.theme.utils.ColorRole;
import com.nextcloud.android.common.ui.theme.utils.MaterialViewThemeUtils;

import it.niedermann.android.util.ColorUtil;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.shared.util.NotesColorUtil;
import kotlin.Pair;
import scheme.Scheme;

public class NotesViewThemeUtils extends ViewThemeUtilsBase {

    private static final String TAG = NotesViewThemeUtils.class.getSimpleName();

    public NotesViewThemeUtils(@NonNull MaterialSchemes schemes) {
        super(schemes);
    }

    /**
     * The Notes app uses custom navigation view items because they have several features which are
     * not covered by {@link NavigationItem}.
     */
    public void colorNavigationViewItem(@NonNull View view) {
        withScheme(view, scheme -> {
            view.setBackgroundTintList(buildColorStateList(
                    new Pair<>(android.R.attr.state_selected, scheme.getSecondaryContainer()),
                    new Pair<>(-android.R.attr.state_selected, Color.TRANSPARENT)
            ));
            return view;
        });
    }

    /**
     * The Notes app uses custom navigation view items because they have several features which are
     * not covered by {@link NavigationItem}.
     */
    public void colorNavigationViewItemIcon(@NonNull ImageView view) {
        withScheme(view, scheme -> {
            view.setImageTintList(buildColorStateList(
                    new Pair<>(android.R.attr.state_selected, scheme.getOnSecondaryContainer()),
                    new Pair<>(-android.R.attr.state_selected, scheme.getOnSurfaceVariant())
            ));
            return view;
        });
    }

    /**
     * The Notes app uses custom navigation view items because they have several features which are
     * not covered by {@link NavigationItem}.
     */
    public void colorNavigationViewItemText(@NonNull TextView view) {
        withScheme(view, scheme -> {
            view.setTextColor(buildColorStateList(
                    new Pair<>(android.R.attr.state_selected, scheme.getOnSecondaryContainer()),
                    new Pair<>(-android.R.attr.state_selected, scheme.getOnSurfaceVariant())
            ));
            return view;
        });
    }

    /**
     * @deprecated should be replaced by {@link MaterialViewThemeUtils#themeToolbar(MaterialToolbar)}.
     */
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

    /**
     * Colorizes only a specific part of a drawable
     */
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
        if (isDarkMode(context)) { // Dark background
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