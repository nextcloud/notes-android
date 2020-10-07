package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;

@Dao
public interface WidgetNotesListDao {

    @Insert
    void createOrUpdateNoteListWidgetData(NotesListWidgetData data);

    @Query("DELETE FROM WIDGET_NOTE_LISTS WHERE ID = :appWidgetId")
    void removeNoteListWidget(int appWidgetId);

    @Query("SELECT * FROM WIDGET_NOTE_LISTS WHERE ID = :appWidgetId")
    NotesListWidgetData getNoteListWidgetData(int appWidgetId);
}
