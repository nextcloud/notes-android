package it.niedermann.owncloud.notes.shared.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseIndexUtil {

    private static final String TAG = DatabaseIndexUtil.class.getSimpleName();

    private DatabaseIndexUtil() {

    }

    public static void createIndex(@NonNull SupportSQLiteDatabase db, @NonNull String table, @NonNull String... columns) {
        for (String column : columns) {
            createIndex(db, table, column);
        }
    }

    public static void createIndex(@NonNull SupportSQLiteDatabase db, @NonNull String table, @NonNull String column) {
        String indexName = table + "_" + column + "_idx";
        Log.v(TAG, "Creating database index: CREATE INDEX IF NOT EXISTS " + indexName + " ON " + table + "(" + column + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + indexName + " ON " + table + "(" + column + ")");
    }

    public static void dropIndexes(@NonNull SupportSQLiteDatabase db) {
        try (Cursor c = db.query("SELECT name, sql FROM sqlite_master WHERE type = 'index'")) {
            while (c.moveToNext()) {
                // Skip automatic indexes which we can't drop manually
                if (c.getString(1) != null) {
                    Log.v(TAG, "Deleting database index: DROP INDEX " + c.getString(0));
                    db.execSQL("DROP INDEX " + c.getString(0));
                }
            }
        }
    }
}
