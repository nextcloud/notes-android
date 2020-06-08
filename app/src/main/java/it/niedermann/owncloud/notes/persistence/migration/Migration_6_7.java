package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import it.niedermann.owncloud.notes.util.DatabaseIndexUtil;

public class Migration_6_7 {
    public Migration_6_7(SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 7) {
            DatabaseIndexUtil.dropIndexes(db);
            db.execSQL("ALTER TABLE NOTES ADD COLUMN CATEGORY TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE NOTES ADD COLUMN ETAG TEXT");
            DatabaseIndexUtil.createIndex(db, "NOTES", "REMOTEID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");
        }
    }
}
