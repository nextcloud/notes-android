package it.niedermann.owncloud.notes.main.items.grid;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemGridOnlyTitleBinding;
import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;
import it.niedermann.owncloud.notes.main.items.NoteViewHolder;

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

    public void bind(@NonNull DBNote note, boolean showCategory, int mainColor, int textColor, @Nullable CharSequence searchQuery) {
        super.bind(note, showCategory, mainColor, textColor, searchQuery);
        @NonNull final Context context = itemView.getContext();
        bindStatus(binding.noteStatus, note.getStatus(), mainColor);
        bindFavorite(binding.noteFavorite, note.isFavorite());
        bindSearchableContent(context, binding.noteTitle, searchQuery, note.getTitle(), mainColor);
    }

    @Nullable
    public View getNoteSwipeable() {
        return null;
    }
}