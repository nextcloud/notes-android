package it.niedermann.owncloud.notes.persistence.dao;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.CategoryEntity;

@Dao
public interface WidgetSingleNoteDao {

    @Query("DELETE FROM WidgetSingleNoteEntity WHERE id = :appWidgetId")
    void removeSingleNoteWidget(int appWidgetId);

}
