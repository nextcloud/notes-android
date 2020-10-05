package it.niedermann.owncloud.notes.persistence.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.WidgetNotesListEntity;

@Dao
public interface WidgetNotesListDao {

    @Query("DELETE FROM WidgetNotesListEntity WHERE id = :appWidgetId")
    void removeNoteListWidget(int appWidgetId);

    @Insert
    void createOrUpdateNoteListWidgetData(@NonNull WidgetNotesListEntity data);
}
