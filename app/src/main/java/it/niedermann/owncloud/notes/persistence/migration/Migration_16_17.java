package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

public class Migration_16_17 {
    /**
     * Adds a column to store the current scroll position per note
     * https://github.com/stefan-niedermann/nextcloud-notes/issues/227
     */
    public Migration_16_17(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE NOTES ADD COLUMN SCROLL_Y INTEGER DEFAULT 0");
    }
}
