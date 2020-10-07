package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;
import it.niedermann.owncloud.notes.widget.notelist.NoteListsWidgetData;

@Dao
public interface WidgetNotesListDao {

    @Insert
    void createOrUpdateNoteListWidgetData(NotesListWidgetData data);

    @Query("DELETE FROM NotesListWidgetData WHERE id = :appWidgetId")
    void removeNoteListWidget(int appWidgetId);

    @Query("SELECT * FROM NotesListWidgetData WHERE id = :appWidgetId")
    NoteListsWidgetData getNoteListWidgetData(int appWidgetId);
}
