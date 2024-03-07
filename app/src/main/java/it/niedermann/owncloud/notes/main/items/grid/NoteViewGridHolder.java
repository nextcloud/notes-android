/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.grid;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.EXCERPT_LINE_SEPARATOR;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemGridBinding;
import it.niedermann.owncloud.notes.main.items.NoteViewHolder;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;

public class NoteViewGridHolder extends NoteViewHolder {
    @NonNull
    private final ItemNotesListNoteItemGridBinding binding;

    public NoteViewGridHolder(@NonNull ItemNotesListNoteItemGridBinding binding, @NonNull NoteClickListener noteClickListener, boolean monospace, @Px float fontSize) {
        super(binding.getRoot(), noteClickListener);
        this.binding = binding;

        binding.noteTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize * 1.1f);
        binding.noteExcerpt.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize * .8f);
        if (monospace) {
            binding.noteTitle.setTypeface(Typeface.MONOSPACE);
            binding.noteExcerpt.setTypeface(Typeface.MONOSPACE);
        }
    }

    public void showSwipe(boolean left) {
        throw new UnsupportedOperationException(NoteViewGridHolder.class.getSimpleName() + " does not support swiping");
    }

    public void bind(boolean isSelected, @NonNull Note note, boolean showCategory, @ColorInt int color, @Nullable CharSequence searchQuery) {
        super.bind(isSelected, note, showCategory, color, searchQuery);
        @NonNull final Context context = itemView.getContext();
        bindCategory(context, binding.noteCategory, showCategory, note.getCategory(), color);
        bindStatus(binding.noteStatus, note.getStatus(), color);
        bindFavorite(binding.noteFavorite, note.getFavorite());
        bindSearchableContent(context, binding.noteTitle, searchQuery, note.getTitle(), color);
        bindSearchableContent(context, binding.noteExcerpt, searchQuery, note.getExcerpt().replace(EXCERPT_LINE_SEPARATOR, "\n"), color);
        binding.noteExcerpt.setVisibility(TextUtils.isEmpty(note.getExcerpt()) ? GONE : VISIBLE);
    }

    @Nullable
    public View getNoteSwipeable() {
        return null;
    }
}