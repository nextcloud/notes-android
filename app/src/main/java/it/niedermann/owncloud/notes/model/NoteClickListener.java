package it.niedermann.owncloud.notes.model;

import android.view.View;

public interface NoteClickListener {
    void onNoteClick(int position, View v);

    void onNoteFavoriteClick(int position, View v);

    boolean onNoteLongClick(int position, View v);
}