package it.niedermann.owncloud.notes.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

public class DatabaseIndexUtil {

    private DatabaseIndexUtil() {

    }

    public static void createIndex(@NonNull SQLiteDatabase db, @NonNull String table, @NonNull String ...columns) {
        for (String column: columns) {
            createIndex(db, table, column);
        }
    }

    public static void createIndex(@NonNull SQLiteDatabase db, @NonNull String table, @NonNull String column) {
        String indexName = table + "_" + column + "_idx";
        db.execSQL("CREATE INDEX IF NOT EXISTS " + indexName + " ON " + table + "(" + column + ")");
    }

    public static void dropIndexes(@NonNull SQLiteDatabase db) {
        Cursor c = db.query("sqlite_master", new String[]{"name"}, "type=?", new String[]{"index"}, null, null, null);
        while (c.moveToNext()) {
            db.execSQL("DROP INDEX " + c.getString(0));
        }
        c.close();
    }
}
