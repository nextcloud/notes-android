/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.shared.util;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.main.navigation.NavigationAdapter;
import it.niedermann.owncloud.notes.main.navigation.NavigationItem;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;

public class DisplayUtils {

    private static final Map<Integer, Collection<Integer>> SPECIAL_CATEGORY_REPLACEMENTS = Map.of(
            R.drawable.ic_library_music_grey600_24dp, singletonList(R.string.category_music),
            R.drawable.ic_local_movies_grey600_24dp, asList(R.string.category_movies, R.string.category_movie),
            R.drawable.ic_work_grey600_24dp, singletonList(R.string.category_work),
            R.drawable.ic_baseline_checklist_24, asList(R.string.category_todo, R.string.category_todos, R.string.category_tasks, R.string.category_checklists),
            R.drawable.ic_baseline_fastfood_24, asList(R.string.category_recipe, R.string.category_recipes, R.string.category_restaurant, R.string.category_restaurants, R.string.category_food, R.string.category_bake),
            R.drawable.ic_baseline_vpn_key_24, asList(R.string.category_key, R.string.category_keys, R.string.category_password, R.string.category_passwords, R.string.category_credentials),
            R.drawable.ic_baseline_games_24, asList(R.string.category_game, R.string.category_games, R.string.category_play),
            R.drawable.ic_baseline_card_giftcard_24, asList(R.string.category_gift, R.string.category_gifts, R.string.category_present, R.string.category_presents)
    );

    private DisplayUtils() {
        throw new UnsupportedOperationException("Do not instantiate this util class.");
    }

    public static List<NavigationItem.CategoryNavigationItem> convertToCategoryNavigationItem(@NonNull Context context, @NonNull Collection<CategoryWithNotesCount> counter) {
        return counter.stream()
                .map(ctr -> convertToCategoryNavigationItem(context, ctr))
                .collect(Collectors.toList());
    }

    public static NavigationItem.CategoryNavigationItem convertToCategoryNavigationItem(@NonNull Context context, @NonNull CategoryWithNotesCount counter) {
        final var res = context.getResources();
        final var englishRes = getEnglishResources(context);
        final String category = counter.getCategory().replaceAll("\\s+", "");
        int icon = NavigationAdapter.ICON_FOLDER;

        for (Map.Entry<Integer, Collection<Integer>> replacement : SPECIAL_CATEGORY_REPLACEMENTS.entrySet()) {
            if (Stream.concat(
                    replacement.getValue().stream().map(res::getString),
                    replacement.getValue().stream().map(englishRes::getString)
            ).map(str -> str.replaceAll("\\s+", ""))
                    .anyMatch(r -> r.equalsIgnoreCase(category))) {
                icon = replacement.getKey();
                break;
            }
        }
        return new NavigationItem.CategoryNavigationItem("category:" + counter.getCategory(), counter.getCategory(), counter.getTotalNotes(), icon, counter.getAccountId(), counter.getCategory());
    }

    @NonNull
    private static Resources getEnglishResources(@NonNull Context context) {
        final var config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(new Locale("en"));
        return context.createConfigurationContext(config).getResources();
    }

    /**
     * Detect if the soft keyboard is open.
     * On API prior to 30 we fall back to workaround which might be less reliable
     *
     * @param parentView View
     * @return keyboardVisibility Boolean
     */
    @SuppressLint("WrongConstant")
    public static boolean isSoftKeyboardVisible(@NonNull View parentView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final var insets = ViewCompat.getRootWindowInsets(parentView);
            if (insets != null) {
                return insets.isVisible(WindowInsets.Type.ime());
            }
        }

        //Arbitrary keyboard height
        final int defaultKeyboardHeightDP = 100;
        final int EstimatedKeyboardDP = defaultKeyboardHeightDP + 48;
        final var rect = new Rect();
        final int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, parentView.getResources().getDisplayMetrics());
        parentView.getWindowVisibleDisplayFrame(rect);
        final int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
        return heightDiff >= estimatedKeyboardHeight;
    }
}
