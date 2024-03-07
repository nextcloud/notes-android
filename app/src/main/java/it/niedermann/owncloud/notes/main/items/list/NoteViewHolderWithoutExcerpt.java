/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.list;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemWithoutExcerptBinding;
import it.niedermann.owncloud.notes.main.items.NoteViewHolder;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;

public class NoteViewHolderWithoutExcerpt extends NoteViewHolder {
    @NonNull
    private final ItemNotesListNoteItemWithoutExcerptBinding binding;

    public NoteViewHolderWithoutExcerpt(@NonNull ItemNotesListNoteItemWithoutExcerptBinding binding, @NonNull NoteClickListener noteClickListener) {
        super(binding.getRoot(), noteClickListener);
        this.binding = binding;
    }

    public void showSwipe(boolean left) {
        binding.noteFavoriteLeft.setVisibility(left ? View.VISIBLE : View.INVISIBLE);
        binding.noteDeleteRight.setVisibility(left ? View.INVISIBLE : View.VISIBLE);
        binding.noteSwipeFrame.setBackgroundResource(left ? R.color.bg_warning : R.color.bg_attention);
    }

    public void bind(boolean isSelected, @NonNull Note note, boolean showCategory, int color, @Nullable CharSequence searchQuery) {
        super.bind(isSelected, note, showCategory, color, searchQuery);
        @NonNull final Context context = itemView.getContext();
        binding.noteSwipeable.setAlpha(DBStatus.LOCAL_DELETED.equals(note.getStatus()) ? 0.5f : 1.0f);
        bindCategory(context, binding.noteCategory, showCategory, note.getCategory(), color);
        bindStatus(binding.noteStatus, note.getStatus(), color);
        bindFavorite(binding.noteFavorite, note.getFavorite());
        bindSearchableContent(context, binding.noteTitle, searchQuery, note.getTitle(), color);
    }

    @NonNull
    public View getNoteSwipeable() {
        return binding.noteSwipeable;
    }
}