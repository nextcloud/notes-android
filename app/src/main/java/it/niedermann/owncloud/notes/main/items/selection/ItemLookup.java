/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.selection;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.main.items.NoteViewHolder;

public class ItemLookup extends ItemDetailsLookup<Long> {

    @NonNull
    private final RecyclerView recyclerView;

    public ItemLookup(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        final var view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            final RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof NoteViewHolder) {
                return ((NoteViewHolder) recyclerView.getChildViewHolder(view))
                        .getItemDetails();
            } else {
                return null;
            }
        }
        return null;
    }
}
