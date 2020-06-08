package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

public class Migration_5_6 {
    /**
     * Adds a column to support marking notes as favorite
     */
    public Migration_5_6(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE NOTES ADD COLUMN FAVORITE INTEGER DEFAULT 0");
    }
}
