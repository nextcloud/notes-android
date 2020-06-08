package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.util.DatabaseIndexUtil;

public class Migration_6_7 {
    /**
     * Adds columns for category support and ETags
     */
    public Migration_6_7(@NonNull SQLiteDatabase db) {
        DatabaseIndexUtil.dropIndexes(db);
        db.execSQL("ALTER TABLE NOTES ADD COLUMN CATEGORY TEXT NOT NULL DEFAULT ''");
        db.execSQL("ALTER TABLE NOTES ADD COLUMN ETAG TEXT");
        DatabaseIndexUtil.createIndex(db, "NOTES", "REMOTEID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");
    }
}
