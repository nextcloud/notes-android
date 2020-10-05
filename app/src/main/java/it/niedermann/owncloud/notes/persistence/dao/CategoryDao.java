package it.niedermann.owncloud.notes.persistence.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.HashMap;
import java.util.Map;

import it.niedermann.owncloud.notes.persistence.entity.CategoryEntity;
import it.niedermann.owncloud.notes.shared.model.DBStatus;

@Dao
public interface CategoryDao {

    /**
     * This function will be called when the category or note is updated.
     * Because sometime we will remove some notes in categories.
     * Such that there must be such a category without any note.
     * For these useless category, it is better to remove.
     * Move a note from a category to another may also lead to the same result.
     *
     * @param accountId The user accountId
     */
    @Query("DELETE FROM categoryentity WHERE id NOT IN (SELECT noteentity.category FROM noteentity)")
    void removeEmptyCategory(long accountId);

    @Insert
    Long addCategory(@NonNull CategoryEntity entity);

    @Query("SELECT id FROM categoryentity WHERE accountId = :accountId AND title = :title")
    Long getCategoryIdByTitle(long accountId, @NonNull String title);
}
