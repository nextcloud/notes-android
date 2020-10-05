package it.niedermann.owncloud.notes.main.items.list;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemWithExcerptBinding;
import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;
import it.niedermann.owncloud.notes.main.items.NoteViewHolder;

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

    public void bind(@NonNull DBNote note, boolean showCategory, int mainColor, int textColor, @Nullable CharSequence searchQuery) {
        super.bind(note, showCategory, mainColor, textColor, searchQuery);
        @NonNull final Context context = itemView.getContext();
        binding.noteSwipeable.setAlpha(DBStatus.LOCAL_DELETED.equals(note.getStatus()) ? 0.5f : 1.0f);
        bindCategory(context, binding.noteCategory, showCategory, note.getCategory(), mainColor);
        bindStatus(binding.noteStatus, note.getStatus(), mainColor);
        bindFavorite(binding.noteFavorite, note.isFavorite());

        bindSearchableContent(context, binding.noteTitle, searchQuery, note.getTitle(), mainColor);
        bindSearchableContent(context, binding.noteExcerpt, searchQuery, note.getExcerpt(), mainColor);
    }

    @NonNull
    public View getNoteSwipeable() {
        return binding.noteSwipeable;
    }
}