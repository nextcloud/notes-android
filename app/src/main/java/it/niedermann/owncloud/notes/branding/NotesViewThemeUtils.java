/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.branding;

import static com.nextcloud.android.common.ui.util.ColorStateListUtilsKt.buildColorStateList;
import static com.nextcloud.android.common.ui.util.PlatformThemeUtil.isDarkMode;

import android.content.Context;
import android.content.res.ColorStateList;
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
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.nextcloud.android.common.ui.theme.MaterialSchemes;
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase;
import com.nextcloud.android.common.ui.theme.utils.MaterialViewThemeUtils;

import dynamiccolor.MaterialDynamicColors;
import it.niedermann.android.util.ColorUtil;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.shared.util.NotesColorUtil;
import kotlin.Pair;

public class NotesViewThemeUtils extends ViewThemeUtilsBase {

    private static final String TAG = NotesViewThemeUtils.class.getSimpleName();

    private final MaterialDynamicColors dynamicColor = new MaterialDynamicColors();

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
                    new Pair<>(android.R.attr.state_selected, dynamicColor.secondaryContainer().getArgb(scheme)),
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
                    new Pair<>(android.R.attr.state_selected, dynamicColor.onSecondaryContainer().getArgb(scheme)),
                    new Pair<>(-android.R.attr.state_selected, dynamicColor.onSurfaceVariant().getArgb(scheme))
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
                    new Pair<>(android.R.attr.state_selected, dynamicColor.onSecondaryContainer().getArgb(scheme)),
                    new Pair<>(-android.R.attr.state_selected, dynamicColor.onSurfaceVariant().getArgb(scheme))
            ));
            return view;
        });
    }

    /**
     * @deprecated should be replaced by {@link MaterialViewThemeUtils#themeToolbar(MaterialToolbar)}.
     */
    @Deprecated(forRemoval = true)
    public void applyBrandToPrimaryToolbar(@NonNull AppBarLayout appBarLayout,
                                           @NonNull Toolbar toolbar,
                                           @ColorInt int color) {
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
    public int getTextHighlightBackgroundColor(@NonNull Context context,
                                               @ColorInt int mainColor,
                                               @ColorInt int colorPrimary,
                                               @ColorInt int colorAccent) {
        if (isDarkMode(context)) { // Dark background
            if (ColorUtil.isColorDark(mainColor)) { // Dark brand color
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
            if (ColorUtil.isColorDark(mainColor)) { // Dark brand color
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

    /**
     * @deprecated Should be replaced with {@link com.google.android.material.search.SearchBar} component.
     */
    @Deprecated
    public void themeSearchCardView(@NonNull MaterialCardView searchBarWrapper) {
        withScheme(searchBarWrapper, scheme -> {
            searchBarWrapper.setBackgroundTintList(ColorStateList.valueOf(dynamicColor.surface().getArgb(scheme)));
            return searchBarWrapper;
        });
    }

    /**
     * @deprecated Should be replaced with {@link com.google.android.material.search.SearchBar} or
     * {@link MaterialViewThemeUtils#themeToolbar(MaterialToolbar)}
     */
    @Deprecated
    public void themeSearchToolbar(@NonNull MaterialToolbar toolbar) {
        withScheme(toolbar, scheme -> {
            toolbar.setNavigationIconTint(dynamicColor.onSurface().getArgb(scheme));
            toolbar.setTitleTextColor(dynamicColor.onSurface().getArgb(scheme));
            return toolbar;
        });
    }

    /**
     * @deprecated Should be replaced with {@link com.google.android.material.search.SearchView}
     * @see com.nextcloud.android.common.ui.theme.utils.AndroidXViewThemeUtils#themeToolbarSearchView(SearchView)
     */
    @Deprecated
    public void themeToolbarSearchView(@NonNull SearchView searchView) {
        withScheme(searchView, scheme -> {
            // hacky as no default way is provided
            final var editText = (AppCompatAutoCompleteTextView) searchView
                    .findViewById(androidx.appcompat.R.id.search_src_text);
            final var closeButton = (ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
            final var searchButton = (ImageView) searchView.findViewById(androidx.appcompat.R.id.search_button);
            editText.setHintTextColor(dynamicColor.onSurfaceVariant().getArgb(scheme));
            editText.setHighlightColor(dynamicColor.inverseOnSurface().getArgb(scheme));
            editText.setTextColor(dynamicColor.onSurface().getArgb(scheme));
            closeButton.setColorFilter(dynamicColor.onSurface().getArgb(scheme));
            searchButton.setColorFilter(dynamicColor.onSurface().getArgb(scheme));
            return searchView;
        });
    }
}
