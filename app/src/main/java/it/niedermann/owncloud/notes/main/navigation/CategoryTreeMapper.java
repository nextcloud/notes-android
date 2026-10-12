/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.navigation;

import static it.niedermann.owncloud.notes.main.MainActivity.ADAPTER_KEY_RECENT;
import static it.niedermann.owncloud.notes.main.MainActivity.ADAPTER_KEY_STARRED;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.FAVORITES;
import static it.niedermann.owncloud.notes.shared.model.ENavigationCategoryType.RECENT;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.entity.CategoryWithNotesCount;
import it.niedermann.owncloud.notes.shared.util.DisplayUtils;

/**
 * Turns the flat list of categories into the hierarchical list of {@link NavigationItem}s shown in
 * the navigation drawer. Categories are organized as a tree along their {@code /} separator and can
 * be expanded to an arbitrary depth: a sub category is only listed when every one of its ancestors
 * is contained in {@code expandedCategories}.
 */
public final class CategoryTreeMapper {

    private static final String CATEGORY_SEPARATOR = "/";

    private CategoryTreeMapper() {
        throw new UnsupportedOperationException("Do not instantiate this util class.");
    }

    @NonNull
    public static List<NavigationItem> map(@NonNull Context context,
                                           @NonNull Set<String> expandedCategories,
                                           @NonNull List<CategoryWithNotesCount> categories,
                                           int count,
                                           int favoritesCount) {
        final var items = new ArrayList<NavigationItem>(categories.size() + 2);
        items.add(new NavigationItem(ADAPTER_KEY_RECENT, context.getString(R.string.label_all_notes), count, R.drawable.selector_all_notes, RECENT));
        items.add(new NavigationItem(ADAPTER_KEY_STARRED, context.getString(R.string.label_favorites), favoritesCount, R.drawable.selector_favorites, FAVORITES));

        final long accountId = categories.isEmpty() ? -1 : categories.get(0).getAccountId();
        final var root = buildTree(categories);
        flatten(context, root, 0, accountId, expandedCategories, items);
        return items;
    }

    @NonNull
    private static CategoryTreeNode buildTree(@NonNull List<CategoryWithNotesCount> categories) {
        final var root = new CategoryTreeNode("", "");
        for (final var category : categories) {
            final String[] segments = category.getCategory().split(CATEGORY_SEPARATOR);
            var node = root;
            final var path = new StringBuilder();
            for (final String segment : segments) {
                if (path.length() > 0) {
                    path.append(CATEGORY_SEPARATOR);
                }
                path.append(segment);
                final String currentPath = path.toString();
                node = node.children.computeIfAbsent(segment, label -> new CategoryTreeNode(currentPath, label));
            }
            node.directNotes = category.getTotalNotes();
        }
        return root;
    }

    private static void flatten(@NonNull Context context,
                                @NonNull CategoryTreeNode parent,
                                int depth,
                                long accountId,
                                @NonNull Set<String> expandedCategories,
                                @NonNull List<NavigationItem> items) {
        for (final var node : parent.children.values()) {
            final boolean hasChildren = node.hasChildren();
            final boolean expanded = hasChildren && expandedCategories.contains(node.path);
            final int notes = expanded ? node.directNotes : node.totalNotes();
            final var item = new NavigationItem.CategoryNavigationItem("category:" + node.path, node.label, notes, iconFor(context, node.label, hasChildren, expanded, depth), accountId, node.path);
            item.depth = depth;
            item.expandState = expandStateFor(hasChildren, expanded);
            items.add(item);

            if (expanded) {
                flatten(context, node, depth + 1, accountId, expandedCategories, items);
            }
        }
    }

    @NonNull
    private static CategoryExpandState expandStateFor(boolean hasChildren, boolean expanded) {
        if (!hasChildren) {
            return CategoryExpandState.NOT_EXPANDABLE;
        }
        return expanded ? CategoryExpandState.EXPANDED : CategoryExpandState.COLLAPSED;
    }

    @DrawableRes
    private static int iconFor(@NonNull Context context, @NonNull String label, boolean hasChildren, boolean expanded, int depth) {
        if (hasChildren) {
            if (expanded) {
                return NavigationAdapter.ICON_MULTIPLE_OPEN;
            }
            return depth == 0 ? NavigationAdapter.ICON_MULTIPLE : NavigationAdapter.ICON_SUB_MULTIPLE;
        }
        final int specialIcon = DisplayUtils.getCategoryIcon(context, label);
        if (specialIcon != NavigationAdapter.ICON_FOLDER) {
            return specialIcon;
        }
        return depth == 0 ? NavigationAdapter.ICON_FOLDER : NavigationAdapter.ICON_SUB_FOLDER;
    }
}
