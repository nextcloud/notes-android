package it.niedermann.owncloud.notes.shared.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

public class DatabaseIndexUtil {

    private static final String TAG = DatabaseIndexUtil.class.getSimpleName();

    private DatabaseIndexUtil() {

    }

    public static void createIndex(@NonNull SQLiteDatabase db, @NonNull String table, @NonNull String ...columns) {
        for (String column: columns) {
            createIndex(db, table, column);
        }
    }

    public static void createIndex(@NonNull SQLiteDatabase db, @NonNull String table, @NonNull String column) {
        String indexName = table + "_" + column + "_idx";
        Log.v(TAG, "Creating database index: CREATE INDEX IF NOT EXISTS " + indexName + " ON " + table + "(" + column + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + indexName + " ON " + table + "(" + column + ")");
    }

    public static void dropIndexes(@NonNull SQLiteDatabase db) {
        try (Cursor c = db.query("sqlite_master", new String[]{"name", "sql"}, "type=?", new String[]{"index"}, null, null, null)) {
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
