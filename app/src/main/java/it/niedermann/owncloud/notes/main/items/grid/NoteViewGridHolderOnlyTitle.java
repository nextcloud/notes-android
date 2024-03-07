/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.grid;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemGridOnlyTitleBinding;
import it.niedermann.owncloud.notes.main.items.NoteViewHolder;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;

public class NoteViewGridHolderOnlyTitle extends NoteViewHolder {
    @NonNull
    private final ItemNotesListNoteItemGridOnlyTitleBinding binding;

    public NoteViewGridHolderOnlyTitle(@NonNull ItemNotesListNoteItemGridOnlyTitleBinding binding, @NonNull NoteClickListener noteClickListener, boolean monospace, @Px float fontSize) {
        super(binding.getRoot(), noteClickListener);
        this.binding = binding;

        binding.noteTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize * 1.1f);
        if (monospace) {
            binding.noteTitle.setTypeface(Typeface.MONOSPACE);
        }
    }

    public void showSwipe(boolean left) {
        throw new UnsupportedOperationException(NoteViewGridHolderOnlyTitle.class.getSimpleName() + " does not support swiping");
    }

    public void bind(boolean isSelected, @NonNull Note note, boolean showCategory, int color, @Nullable CharSequence searchQuery) {
        super.bind(isSelected, note, showCategory, color, searchQuery);
        @NonNull final Context context = itemView.getContext();
        bindStatus(binding.noteStatus, note.getStatus(), color);
        bindFavorite(binding.noteFavorite, note.getFavorite());
        bindSearchableContent(context, binding.noteTitle, searchQuery, note.getTitle(), color);
    }

    @Nullable
    public View getNoteSwipeable() {
        return null;
    }
}