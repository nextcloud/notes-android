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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;

import com.google.android.material.snackbar.Snackbar;

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
            R.drawable.selector_music, singletonList(R.string.category_music),
            R.drawable.selector_movies, asList(R.string.category_movies, R.string.category_movie),
            R.drawable.selector_work, singletonList(R.string.category_work),
            R.drawable.ic_baseline_checklist_24, asList(R.string.category_todo, R.string.category_todos, R.string.category_tasks, R.string.category_checklists),
            R.drawable.selector_food, asList(R.string.category_recipe, R.string.category_recipes, R.string.category_restaurant, R.string.category_restaurants, R.string.category_food, R.string.category_bake),
            R.drawable.selector_credentials, asList(R.string.category_key, R.string.category_keys, R.string.category_password, R.string.category_passwords, R.string.category_credentials),
            R.drawable.selector_games, asList(R.string.category_game, R.string.category_games, R.string.category_play),
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

    static public void startLinkIntent(Activity activity, @StringRes int link) {
        startLinkIntent(activity, activity.getString(link));
    }

    static public void startLinkIntent(Activity activity, String url) {
        if (!TextUtils.isEmpty(url)) {
            startLinkIntent(activity, Uri.parse(url));
        }
    }

    static public void startLinkIntent(Activity activity, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        DisplayUtils.startIntentIfAppAvailable(intent, activity, R.string.no_browser_available);
    }

    static public void startIntentIfAppAvailable(Intent intent, Activity activity, @StringRes int error) {
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            DisplayUtils.showSnackMessage(activity, error);
        }
    }

    /**
     * Show a temporary message in a {@link Snackbar} bound to the content view.
     *
     * @param activity        The {@link Activity} to which's content view the {@link Snackbar} is bound.
     * @param messageResource The resource id of the string resource to use. Can be formatted text.
     * @return The created {@link Snackbar}
     */
    public static Snackbar showSnackMessage(Activity activity, @StringRes int messageResource) {
        return showSnackMessage(activity.findViewById(android.R.id.content), messageResource);
    }

    /**
     * Show a temporary message in a {@link Snackbar} bound to the content view.
     *
     * @param activity The {@link Activity} to which's content view the {@link Snackbar} is bound.
     * @param message  Message to show.
     * @return The created {@link Snackbar}
     */
    public static Snackbar showSnackMessage(Activity activity, String message) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.show();
        return snackbar;
    }

    /**
     * Show a temporary message in a {@link Snackbar} bound to the given view.
     *
     * @param view            The view the {@link Snackbar} is bound to.
     * @param messageResource The resource id of the string resource to use. Can be formatted text.
     * @return The created {@link Snackbar}
     */
    public static Snackbar showSnackMessage(View view, @StringRes int messageResource) {
        final Snackbar snackbar = Snackbar.make(view, messageResource, Snackbar.LENGTH_LONG);
        snackbar.show();
        return snackbar;
    }

    /**
     * Show a temporary message in a {@link Snackbar} bound to the given view.
     *
     * @param view    The view the {@link Snackbar} is bound to.
     * @param message The message.
     * @return The created {@link Snackbar}
     */
    public static Snackbar showSnackMessage(View view, String message) {
        final Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
        return snackbar;
    }

}
