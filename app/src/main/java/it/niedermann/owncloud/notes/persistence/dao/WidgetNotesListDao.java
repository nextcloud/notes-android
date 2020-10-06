package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.WidgetNotesListEntity;
import it.niedermann.owncloud.notes.widget.notelist.NoteListsWidgetData;

@Dao
public interface WidgetNotesListDao {

    @Insert
    void createOrUpdateNoteListWidgetData(WidgetNotesListEntity data);

    @Query("DELETE FROM WidgetNotesListEntity WHERE id = :appWidgetId")
    void removeNoteListWidget(int appWidgetId);

    @Query("SELECT * FROM WidgetNotesListEntity WHERE id = :appWidgetId")
    NoteListsWidgetData getNoteListWidgetData(int appWidgetId);
}
