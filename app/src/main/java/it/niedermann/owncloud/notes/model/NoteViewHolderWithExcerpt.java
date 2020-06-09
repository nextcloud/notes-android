package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemWithExcerptBinding;

public class NoteViewHolderWithExcerpt extends NoteViewHolder {
    @NonNull
    private final ItemNotesListNoteItemWithExcerptBinding binding;

    public NoteViewHolderWithExcerpt(@NonNull ItemNotesListNoteItemWithExcerptBinding binding, @NonNull NoteClickListener noteClickListener) {
        super(binding.getRoot(), noteClickListener);
        this.binding = binding;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void showSwipe(boolean left) {
        binding.noteFavoriteLeft.setVisibility(left ? View.VISIBLE : View.INVISIBLE);
        binding.noteDeleteRight.setVisibility(left ? View.INVISIBLE : View.VISIBLE);
        binding.noteSwipeFrame.setBackgroundResource(left ? R.color.bg_warning : R.color.bg_attention);
    }

    public void bind(@NonNull DBNote note, boolean showCategory, int mainColor, int textColor, @Nullable CharSequence searchQuery) {
        @NonNull final Context context = itemView.getContext();
        binding.noteSwipeable.setAlpha(DBStatus.LOCAL_DELETED.equals(note.getStatus()) ? 0.5f : 1.0f);
        bindCategory(context, binding.noteCategory, showCategory, note.getCategory(), mainColor);
        binding.noteStatus.setVisibility(DBStatus.VOID.equals(note.getStatus()) ? View.INVISIBLE : View.VISIBLE);
        bindFavorite(binding.noteFavorite, note.isFavorite());
        bindTitleAndExcerpt(context, binding.noteTitle, binding.noteExcerpt, searchQuery, note, mainColor);
    }

    public View getNoteSwipeable() {
        return binding.noteSwipeable;
    }
}