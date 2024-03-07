/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.selection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class ItemIdKeyProvider extends ItemKeyProvider<Long> {
    private final RecyclerView recyclerView;

    public ItemIdKeyProvider(RecyclerView recyclerView) {
        super(SCOPE_MAPPED);
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        final var adapter = recyclerView.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("RecyclerView adapter is not set!");
        }
        return adapter.getItemId(position);
    }

    @Override
    public int getPosition(@NonNull Long key) {
        final var viewHolder = recyclerView.findViewHolderForItemId(key);
        return viewHolder == null ? NO_POSITION : viewHolder.getLayoutPosition();
    }
}
