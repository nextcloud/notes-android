/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.list;

import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemWithExcerptBinding;
import it.niedermann.owncloud.notes.main.items.NoteViewHolder;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;

public class NoteViewHolderWithExcerpt extends NoteViewHolder {
    @NonNull
    private final ItemNotesListNoteItemWithExcerptBinding binding;

    public NoteViewHolderWithExcerpt(@NonNull ItemNotesListNoteItemWithExcerptBinding binding, @NonNull NoteClickListener noteClickListener) {
        super(binding.getRoot(), noteClickListener);
        this.binding = binding;
    }

    public void showSwipe(boolean left) {
        binding.noteFavoriteLeft.setVisibility(left ? View.VISIBLE : View.INVISIBLE);
        binding.noteDeleteRight.setVisibility(left ? View.INVISIBLE : View.VISIBLE);
        binding.noteSwipeFrame.setBackgroundResource(left ? R.color.bg_warning : R.color.bg_attention);
    }

    public void bind(boolean isSelected, @NonNull Note note, boolean showCategory, @ColorInt int color, @Nullable CharSequence searchQuery) {
        super.bind(isSelected, note, showCategory, color, searchQuery);
        @NonNull final var context = itemView.getContext();
        binding.noteSwipeable.setAlpha(DBStatus.LOCAL_DELETED.equals(note.getStatus()) ? 0.5f : 1.0f);
        bindCategory(context, binding.noteCategory, showCategory, note.getCategory(), color);
        bindStatus(binding.noteStatus, note.getStatus(), color);
        bindFavorite(binding.noteFavorite, note.getFavorite());
        bindModified(binding.noteModified, note.getModified());

        bindSearchableContent(context, binding.noteTitle, searchQuery, note.getTitle(), color);
        bindSearchableContent(context, binding.noteExcerpt, searchQuery, note.getExcerpt(), color);
    }

    @NonNull
    public View getNoteSwipeable() {
        return binding.noteSwipeable;
    }
}