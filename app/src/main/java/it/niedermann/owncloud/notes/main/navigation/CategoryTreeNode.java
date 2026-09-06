/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.navigation;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.TreeMap;

/**
 * A single node in the category hierarchy. The tree is built from the flat list of categories where
 * each category path is separated by {@code /}. Children are kept sorted by their label so the
 * resulting navigation is deterministic regardless of the order the categories arrive in.
 */
class CategoryTreeNode {

    @NonNull
    final String path;
    @NonNull
    final String label;
    int directNotes = 0;
    @NonNull
    final Map<String, CategoryTreeNode> children = new TreeMap<>();

    CategoryTreeNode(@NonNull String path, @NonNull String label) {
        this.path = path;
        this.label = label;
    }

    boolean hasChildren() {
        return !children.isEmpty();
    }

    int totalNotes() {
        int total = directNotes;
        for (final var child : children.values()) {
            total += child.totalNotes();
        }
        return total;
    }
}
