/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.OnConflictStrategy;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Hashtable;

public class Migration_14_15 extends Migration {

    private static final String TAG = Migration_14_15.class.getSimpleName();

    public Migration_14_15() {
        super(14, 15);
    }

    /**
     * Normalize database (move category from string field to own table)
     * https://github.com/nextcloud/notes-android/issues/814
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        // Rename a tmp_NOTES table.
        final String tmpTableNotes = String.format("tmp_%s", "NOTES");
        db.execSQL("ALTER TABLE NOTES RENAME TO " + tmpTableNotes);
        db.execSQL("CREATE TABLE NOTES ( " +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "REMOTEID INTEGER, " +
                "ACCOUNT_ID INTEGER, " +
                "STATUS VARCHAR(50), " +
                "TITLE TEXT, " +
                "MODIFIED INTEGER DEFAULT 0, " +
                "CONTENT TEXT, " +
                "FAVORITE INTEGER DEFAULT 0, " +
                "CATEGORY INTEGER, " +
                "ETAG TEXT," +
                "EXCERPT TEXT NOT NULL DEFAULT '', " +
                "FOREIGN KEY(CATEGORY) REFERENCES CATEGORIES(CATEGORY_ID), " +
                "FOREIGN KEY(ACCOUNT_ID) REFERENCES ACCOUNTS(ID))");
        createIndex(db, "NOTES", "REMOTEID", "ACCOUNT_ID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");
        db.execSQL("CREATE TABLE CATEGORIES(" +
                "CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "CATEGORY_ACCOUNT_ID INTEGER NOT NULL, " +
                "CATEGORY_TITLE TEXT NOT NULL, " +
                "UNIQUE( CATEGORY_ACCOUNT_ID , CATEGORY_TITLE), " +
                "FOREIGN KEY(CATEGORY_ACCOUNT_ID) REFERENCES ACCOUNTS(ID))");
        createIndex(db, "CATEGORIES", "CATEGORY_ID", "CATEGORY_ACCOUNT_ID", "CATEGORY_TITLE");
        // A hashtable storing categoryTitle - categoryId Mapping
        // This is used to prevent too many searches in database
        final var categoryTitleIdMap = new Hashtable<String, Integer>();
        int id = 1;
        final var tmpNotesCursor = db.query("SELECT * FROM " + tmpTableNotes, null);
        while (tmpNotesCursor.moveToNext()) {
            final String categoryTitle = tmpNotesCursor.getString(8);
            final int accountId = tmpNotesCursor.getInt(2);
            Log.e("###", accountId + "");
            final Integer categoryId;
            if (categoryTitleIdMap.containsKey(categoryTitle) && categoryTitleIdMap.get(categoryTitle) != null) {
                categoryId = categoryTitleIdMap.get(categoryTitle);
            } else {
                // The category does not exists in the database, create it.
                categoryId = id++;
                ContentValues values = new ContentValues();
                values.put("CATEGORY_ID", categoryId);
                values.put("CATEGORY_ACCOUNT_ID", accountId);
                values.put("CATEGORY_TITLE", categoryTitle);
                db.insert("CATEGORIES", OnConflictStrategy.REPLACE, values);
                categoryTitleIdMap.put(categoryTitle, categoryId);
            }
            // Move the data in tmp_NOTES to NOTES
            final ContentValues values = new ContentValues();
            values.put("ID", tmpNotesCursor.getInt(0));
            values.put("REMOTEID", tmpNotesCursor.getInt(1));
            values.put("ACCOUNT_ID", tmpNotesCursor.getInt(2));
            values.put("STATUS", tmpNotesCursor.getString(3));
            values.put("TITLE", tmpNotesCursor.getString(4));
            values.put("MODIFIED", tmpNotesCursor.getLong(5));
            values.put("CONTENT", tmpNotesCursor.getString(6));
            values.put("FAVORITE", tmpNotesCursor.getInt(7));
            values.put("CATEGORY", categoryId);
            values.put("ETAG", tmpNotesCursor.getString(9));
            values.put("EXCERPT", tmpNotesCursor.getString(10));
            db.insert("NOTES", OnConflictStrategy.REPLACE, values);
        }
        tmpNotesCursor.close();
        db.execSQL("DROP TABLE IF EXISTS " + tmpTableNotes);
    }

    private static void createIndex(@NonNull SupportSQLiteDatabase db, @NonNull String table, @NonNull String... columns) {
        for (String column : columns) {
            createIndex(db, table, column);
        }
    }

    private static void createIndex(@NonNull SupportSQLiteDatabase db, @NonNull String table, @NonNull String column) {
        final String indexName = table + "_" + column + "_idx";
        Log.v(TAG, "Creating database index: CREATE INDEX IF NOT EXISTS " + indexName + " ON " + table + "(" + column + ")");
        db.execSQL("CREATE INDEX " + indexName + " ON " + table + "(" + column + ")");
    }
}
