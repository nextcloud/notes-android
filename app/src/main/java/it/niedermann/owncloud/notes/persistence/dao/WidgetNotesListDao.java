package it.niedermann.owncloud.notes.persistence.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.NoSuchElementException;

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
