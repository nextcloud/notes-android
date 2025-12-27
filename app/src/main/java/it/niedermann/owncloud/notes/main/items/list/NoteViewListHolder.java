/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.main.items.list;

import android.content.res.Resources;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemWithExcerptBinding;
import it.niedermann.owncloud.notes.main.items.NoteViewHolder;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;

public class NoteViewListHolder extends NoteViewHolder {
    @NonNull
    private final ItemNotesListNoteItemWithExcerptBinding binding;

    private final int defaultSwipeBackgroundColor;
    private final int starSwipeBackgroundColor;
    private final int deleteSwipeBackgroundColor;


    public NoteViewListHolder(@NonNull ItemNotesListNoteItemWithExcerptBinding binding, @NonNull NoteClickListener noteClickListener) {
        super(binding.getRoot(), noteClickListener);
        this.binding = binding;
        Resources resources = binding.getRoot().getContext().getResources();
        this.defaultSwipeBackgroundColor = ResourcesCompat.getColor(resources, R.color.transparent, null);
        this.starSwipeBackgroundColor = ResourcesCompat.getColor(resources, R.color.bg_warning, null);
        this.deleteSwipeBackgroundColor = ResourcesCompat.getColor(resources, R.color.bg_attention, null);
    }

    public void showSwipe(float dX) {
        if (dX == 0.0f) {
            binding.noteFavoriteLeft.setVisibility(View.INVISIBLE);
            binding.noteDeleteRight.setVisibility(View.INVISIBLE);
            binding.noteSwipeFrame.setCardBackgroundColor(defaultSwipeBackgroundColor);
        } else {
            boolean left = dX > 0;
            binding.noteFavoriteLeft.setVisibility(left ? View.VISIBLE : View.INVISIBLE);
            binding.noteDeleteRight.setVisibility(left ? View.INVISIBLE : View.VISIBLE);
            binding.noteSwipeFrame.setCardBackgroundColor(left ? starSwipeBackgroundColor : deleteSwipeBackgroundColor);
        }
    }

    public void bind(boolean isSelected, @NonNull Note note, boolean showCategory, @ColorInt int color, @Nullable CharSequence searchQuery) {
        super.bind(isSelected, note, showCategory, color, searchQuery);
        @NonNull final var context = itemView.getContext();
        binding.noteCard.setAlpha(DBStatus.LOCAL_DELETED.equals(note.getStatus()) ? 0.5f : 1.0f);
        bindCategory(context, binding.noteCategory, showCategory, note.getCategory(), color);
        bindStatus(binding.noteSyncStatus, note.getStatus(), color);
        bindFavorite(binding.noteFavorite, note.getFavorite());
        bindModified(binding.noteModified, note.getModified());

        bindSearchableContent(context, binding.noteTitle, searchQuery, note.getTitle(), color);
        if (note.getExcerpt().isEmpty()) {
            bindSearchableContent(
                    context,
                    binding.noteExcerpt,
                    searchQuery,
                    context.getString(R.string.listview_no_content),
                    color
            );
        } else {
            bindSearchableContent(context, binding.noteExcerpt, searchQuery, note.getExcerpt(), color);
        }
    }

    @NonNull
    public View getNoteSwipeable() {
        return binding.noteCard;
    }
}
