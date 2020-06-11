package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemGridBinding;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class NoteViewGridHolder extends NoteViewHolder {
    @NonNull
    private final ItemNotesListNoteItemGridBinding binding;

    public NoteViewGridHolder(@NonNull ItemNotesListNoteItemGridBinding binding, @NonNull NoteClickListener noteClickListener) {
        super(binding.getRoot(), noteClickListener);
        this.binding = binding;
    }

    public void showSwipe(boolean left) {
        throw new UnsupportedOperationException(NoteViewGridHolder.class.getSimpleName() + " does not support swiping");
    }

    public void bind(@NonNull DBNote note, boolean showCategory, int mainColor, int textColor, @Nullable CharSequence searchQuery) {
        super.bind(note, showCategory, mainColor, textColor, searchQuery);
        @NonNull final Context context = itemView.getContext();
        bindCategory(context, binding.noteCategory, showCategory, note.getCategory(), mainColor);
        binding.noteStatus.setVisibility(DBStatus.VOID.equals(note.getStatus()) ? INVISIBLE : VISIBLE);
        bindFavorite(binding.noteFavorite, note.isFavorite());
        bindSearchableContent(context, binding.noteTitle, searchQuery, note.getTitle(), mainColor);
        bindSearchableContent(context, binding.noteExcerpt, searchQuery, note.getExcerpt().replace("   ", "\n"), mainColor);
        binding.noteExcerpt.setVisibility(TextUtils.isEmpty(note.getExcerpt()) ? GONE : VISIBLE);
    }

    @Nullable
    public View getNoteSwipeable() {
        return null;
    }
}