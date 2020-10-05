package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.shared.util.DatabaseIndexUtil;

public class Migration_7_8 {
    public Migration_7_8(@NonNull SQLiteDatabase db) {
        final String table_temp = "NOTES_TEMP";
        db.execSQL("CREATE TABLE " + table_temp + " ( " +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "REMOTEID INTEGER, " +
                "STATUS VARCHAR(50), " +
                "TITLE TEXT, " +
                "MODIFIED INTEGER DEFAULT 0, " +
                "CONTENT TEXT, " +
                "FAVORITE INTEGER DEFAULT 0, " +
                "CATEGORY TEXT NOT NULL DEFAULT '', " +
                "ETAG TEXT)");
        DatabaseIndexUtil.createIndex(db, table_temp, "REMOTEID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");
        db.execSQL(String.format("INSERT INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s) ", table_temp, "ID", "REMOTEID", "STATUS", "TITLE", "MODIFIED", "CONTENT", "FAVORITE", "CATEGORY", "ETAG")
                + String.format("SELECT %s,%s,%s,%s,strftime('%%s',%s),%s,%s,%s,%s FROM %s", "ID", "REMOTEID", "STATUS", "TITLE", "MODIFIED", "CONTENT", "FAVORITE", "CATEGORY", "ETAG", "NOTES"));
        db.execSQL("DROP TABLE NOTES");
        db.execSQL(String.format("ALTER TABLE %s RENAME TO %s", table_temp, "NOTES"));
    }
}
