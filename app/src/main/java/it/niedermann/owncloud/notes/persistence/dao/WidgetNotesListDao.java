package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface WidgetNotesListDao {

    @Query("DELETE FROM WidgetNotesListEntity WHERE id = :appWidgetId")
    void removeNoteListWidget(int appWidgetId);

}
