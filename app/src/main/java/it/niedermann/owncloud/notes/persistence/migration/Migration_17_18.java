package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

public class Migration_17_18 {
    /**
     * Add a new column to store the sorting method for a category note list
     */
    public Migration_17_18(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE CATEGORIES ADD COLUMN CATEGORY_SORTING_METHOD INTEGER DEFAULT 0");
    }
}
