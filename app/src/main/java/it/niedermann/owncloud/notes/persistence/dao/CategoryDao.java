package it.niedermann.owncloud.notes.persistence.dao;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import it.niedermann.owncloud.notes.persistence.entity.CategoryEntity;

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
    @Query("DELETE FROM CategoryEntity WHERE accountId = :accountId AND id NOT IN (SELECT category_id FROM NoteEntity)")
    void removeEmptyCategory(long accountId);

    @Insert
    Long addCategory(@NonNull CategoryEntity entity);

    @Query("SELECT id FROM CategoryEntity WHERE accountId = :accountId AND title = :title")
    Long getCategoryIdByTitle(long accountId, @NonNull String title);

}
