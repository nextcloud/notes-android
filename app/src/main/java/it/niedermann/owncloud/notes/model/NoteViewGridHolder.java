package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemGridBinding;

public class NoteViewGridHolder extends NoteViewHolder {
    @NonNull
    private final ItemNotesListNoteItemGridBinding binding;

    public NoteViewGridHolder(@NonNull ItemNotesListNoteItemGridBinding binding, @NonNull NoteClickListener noteClickListener) {
        super(binding.getRoot(), noteClickListener);
        this.binding = binding;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }


    public void showSwipe(boolean left) {

    }

    public void bind(@NonNull DBNote note, boolean showCategory, int mainColor, int textColor, @Nullable CharSequence searchQuery) {
        @NonNull final Context context = itemView.getContext();
        bindCategory(context, binding.noteCategory, showCategory, note.getCategory(), mainColor);
        binding.noteStatus.setVisibility(DBStatus.VOID.equals(note.getStatus()) ? View.INVISIBLE : View.VISIBLE);
        bindFavorite(binding.noteFavorite, note.isFavorite());
        bindTitle(context, binding.noteTitle, searchQuery, note, mainColor);
        bindExcerpt(context, binding.noteContent, searchQuery, note, mainColor);
    }

    public View getNoteSwipeable() {
        return null;
    }
}