package it.niedermann.owncloud.notes.model;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public abstract class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
    private final NoteClickListener noteClickListener;

    public NoteViewHolder(View v, NoteClickListener noteClickListener) {
        super(v);
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

    public abstract void showSwipe(boolean left);

    public abstract View getNoteSwipeable();
}