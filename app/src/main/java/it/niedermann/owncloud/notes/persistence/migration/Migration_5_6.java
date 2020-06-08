package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

public class Migration_5_6 {
    public Migration_5_6(SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE NOTES ADD COLUMN FAVORITE INTEGER DEFAULT 0");
        }
    }
}
