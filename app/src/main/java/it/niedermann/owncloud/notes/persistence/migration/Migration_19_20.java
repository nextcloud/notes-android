package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.OnConflictStrategy;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.owncloud.notes.shared.util.DatabaseIndexUtil;

public class Migration_19_20 extends Migration {

    public Migration_19_20() {
        super(19, 20);
    }

    /**
     * From {@link SQLiteOpenHelper} to {@link RoomDatabase}
     * https://github.com/stefan-niedermann/nextcloud-deck/issues/531
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        // Migrate from VARCHAR to TEXT in ACCOUNTS

        DatabaseIndexUtil.dropIndexes(db, "ACCOUNTS");
        db.execSQL("ALTER TABLE ACCOUNTS RENAME TO ACCOUNTS_TEMP");
        db.execSQL("CREATE TABLE ACCOUNTS ( " +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "URL TEXT, " +
                "USERNAME TEXT, " +
                "ACCOUNT_NAME TEXT UNIQUE, " +
                "ETAG TEXT, " +
                "MODIFIED INTEGER, " +
                "API_VERSION TEXT, " +
                "COLOR TEXT NOT NULL DEFAULT '000000', " +
                "TEXT_COLOR TEXT NOT NULL DEFAULT '0082C9', " +
                "CAPABILITIES_ETAG TEXT);");
        DatabaseIndexUtil.createIndex(db, "ACCOUNTS", "URL", "USERNAME", "ACCOUNT_NAME", "ETAG", "MODIFIED");

        Cursor tmpAccountCursor = db.query("SELECT * FROM ACCOUNTS_TEMP", null);
        while (tmpAccountCursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("ID", tmpAccountCursor.getInt(0));
            values.put("URL", tmpAccountCursor.getInt(1));
            values.put("USERNAME", tmpAccountCursor.getInt(2));
            values.put("ACCOUNT_NAME", tmpAccountCursor.getString(3));
            values.put("ETAG", tmpAccountCursor.getString(4));
            values.put("MODIFIED", tmpAccountCursor.getLong(5));
            values.put("API_VERSION", tmpAccountCursor.getString(6));
            values.put("COLOR", tmpAccountCursor.getInt(7));
            values.put("TEXT_COLOR", tmpAccountCursor.getInt(8));
            values.put("CAPABILITIES_ETAG", tmpAccountCursor.getString(9));
            db.insert("ACCOUNTS", OnConflictStrategy.REPLACE, values);
        }
        tmpAccountCursor.close();
        db.execSQL("DROP TABLE IF EXISTS ACCOUNTS_TEMP");

        // Migrate from VARCHAR to TEXT in NOTES

        DatabaseIndexUtil.dropIndexes(db, "NOTES");
        db.execSQL("ALTER TABLE NOTES RENAME TO NOTES_TEMP");
        db.execSQL("CREATE TABLE NOTES ( " +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "REMOTEID INTEGER, " +
                "ACCOUNT_ID INTEGER, " +
                "STATUS TEXT, " +
                "TITLE TEXT, " +
                "MODIFIED INTEGER DEFAULT 0, " +
                "CONTENT TEXT, " +
                "FAVORITE INTEGER DEFAULT 0, " +
                "CATEGORY INTEGER, " +
                "ETAG TEXT," +
                "EXCERPT TEXT NOT NULL DEFAULT '', " +
                "SCROLL_Y INTEGER DEFAULT 0, " +
                "FOREIGN KEY(CATEGORY) REFERENCES CATEGORIES(CATEGORY_ID), " +
                "FOREIGN KEY(ACCOUNT_ID) REFERENCES ACCOUNTS(ID) ON DELETE CASCADE)");
        DatabaseIndexUtil.createIndex(db, "NOTES", "REMOTEID", "ACCOUNT_ID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");

        Cursor tmpNotesCursor = db.query("SELECT * FROM NOTES_TEMP", null);
        while (tmpNotesCursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("ID", tmpNotesCursor.getInt(0));
            values.put("REMOTEID", tmpNotesCursor.getInt(1));
            values.put("ACCOUNT_ID", tmpNotesCursor.getInt(2));
            values.put("STATUS", tmpNotesCursor.getString(3));
            values.put("TITLE", tmpNotesCursor.getString(4));
            values.put("MODIFIED", tmpNotesCursor.getLong(5));
            values.put("CONTENT", tmpNotesCursor.getString(6));
            values.put("FAVORITE", tmpNotesCursor.getInt(7));
            values.put("CATEGORY", tmpNotesCursor.getInt(8));
            values.put("ETAG", tmpNotesCursor.getString(9));
            values.put("EXCERPT", tmpNotesCursor.getString(10));
            db.insert("NOTES", OnConflictStrategy.REPLACE, values);
        }
        tmpNotesCursor.close();
        db.execSQL("DROP TABLE IF EXISTS NOTES_TEMP");
    }
}
