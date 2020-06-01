package it.niedermann.owncloud.notes.model;

import android.graphics.Color;
import android.text.Html;
import android.view.View;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemBinding;
import it.niedermann.owncloud.notes.util.Notes;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
    private final ItemNotesListNoteItemBinding binding;
    private final NoteClickListener noteClickListener;

    public NoteViewHolder(View v, NoteClickListener noteClickListener) {
        super(v);
        binding = ItemNotesListNoteItemBinding.bind(v);
        this.noteClickListener = noteClickListener;
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int adapterPosition = getAdapterPosition();
        if (adapterPosition != NO_POSITION) {
            noteClickListener.onNoteClick(adapterPosition, v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return noteClickListener.onNoteLongClick(getAdapterPosition(), v);
    }

    public void showSwipe(boolean left) {
        binding.noteFavoriteLeft.setVisibility(left ? View.VISIBLE : View.INVISIBLE);
        binding.noteDeleteRight.setVisibility(left ? View.INVISIBLE : View.VISIBLE);
        binding.noteSwipeFrame.setBackgroundResource(left ? R.color.bg_warning : R.color.bg_attention);
    }

    public void bind(DBNote note, NoteClickListener noteClickListener, boolean showCategory, int mainColor, int textColor) {
        binding.noteSwipeable.setAlpha(DBStatus.LOCAL_DELETED.equals(note.getStatus()) ? 0.5f : 1.0f);
        binding.noteTitle.setText(Html.fromHtml(note.getTitle()));

        // FIXME coloring when searching
        binding.noteCategory.setVisibility(showCategory && !note.getCategory().isEmpty() ? View.VISIBLE : View.GONE);
        binding.noteCategory.setText(Html.fromHtml(note.getCategory()));

        DrawableCompat.setTint(binding.noteCategory.getBackground(), mainColor);
        binding.noteCategory.setTextColor(Notes.isDarkThemeActive(binding.getRoot().getContext()) ? textColor : Color.BLACK);

        binding.noteExcerpt.setText(Html.fromHtml(note.getExcerpt()));
        binding.noteStatus.setVisibility(DBStatus.VOID.equals(note.getStatus()) ? View.INVISIBLE : View.VISIBLE);
        binding.noteFavorite.setImageResource(note.isFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp);
        binding.noteFavorite.setOnClickListener(view -> noteClickListener.onNoteFavoriteClick(getAdapterPosition(), view));
    }

    public View getNoteSwipeable() {
        return binding.noteSwipeable;
    }
}