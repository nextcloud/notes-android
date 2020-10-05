package it.niedermann.owncloud.notes.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.WidgetSingleNoteEntity;

@Dao
public interface WidgetSingleNoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createOrUpdateSingleNoteWidgetData(WidgetSingleNoteEntity data);

    @Query("DELETE FROM WidgetSingleNoteEntity WHERE id = :id")
    void removeSingleNoteWidget(int id);

    @Query("SELECT * FROM WidgetSingleNoteEntity WHERE id = :id")
    WidgetSingleNoteEntity getSingleNoteWidgetData(int id);
}
