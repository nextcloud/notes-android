package it.niedermann.owncloud.notes.main.items.grid;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemGridBinding;
import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.model.NoteClickListener;
import it.niedermann.owncloud.notes.main.items.NoteViewHolder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static it.niedermann.owncloud.notes.shared.util.NoteUtil.EXCERPT_LINE_SEPARATOR;

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

    public void bind(@NonNull DBNote note, boolean showCategory, int mainColor, int textColor, @Nullable CharSequence searchQuery) {
        super.bind(note, showCategory, mainColor, textColor, searchQuery);
        @NonNull final Context context = itemView.getContext();
        bindCategory(context, binding.noteCategory, showCategory, note.getCategory(), mainColor);
        bindStatus(binding.noteStatus, note.getStatus(), mainColor);
        bindFavorite(binding.noteFavorite, note.isFavorite());
        bindSearchableContent(context, binding.noteTitle, searchQuery, note.getTitle(), mainColor);
        bindSearchableContent(context, binding.noteExcerpt, searchQuery, note.getExcerpt().replace(EXCERPT_LINE_SEPARATOR, "\n"), mainColor);
        binding.noteExcerpt.setVisibility(TextUtils.isEmpty(note.getExcerpt()) ? GONE : VISIBLE);
    }

    @Nullable
    public View getNoteSwipeable() {
        return null;
    }
}