/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.selection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.main.items.ItemAdapter;

public class ItemSelectionTracker {

    private ItemSelectionTracker() {
        // Use build() method
    }

    public static SelectionTracker<Long> build(@NonNull RecyclerView recyclerView, @NonNull ItemAdapter adapter) {
        return new SelectionTracker.Builder<>(
                ItemSelectionTracker.class.getSimpleName(),
                recyclerView,
                new ItemIdKeyProvider(recyclerView),
                new ItemLookup(recyclerView),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
                new SelectionTracker.SelectionPredicate<>() {
                    @Override
                    public boolean canSetStateForKey(@NonNull Long key, boolean nextState) {
                        return true;
                    }

                    @Override
                    public boolean canSetStateAtPosition(int position, boolean nextState) {
                        @Nullable Integer swipedPosition = adapter.getSwipedPosition();
                        if (!adapter.hasItemPosition(position)) {
                            return false;
                        }
                        return (swipedPosition == null || swipedPosition != position) && !adapter.getItem(position).isSection();
                    }

                    @Override
                    public boolean canSelectMultiple() {
                        return true;
                    }
                }).build();
    }
}
