package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Hashtable;

import it.niedermann.owncloud.notes.util.DatabaseIndexUtil;

public class Migration_14_15 {
    /**
     * Normalize database (move category from string field to own table)
     * https://github.com/stefan-niedermann/nextcloud-notes/issues/814
     */
    public Migration_14_15(SQLiteDatabase db) {
        // Rename a tmp_NOTES table.
        String tmpTableNotes = String.format("tmp_%s", "NOTES");
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
        DatabaseIndexUtil.createIndex(db, "NOTES", "REMOTEID", "ACCOUNT_ID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");
        db.execSQL("CREATE TABLE CATEGORIES(" +
                "CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "CATEGORY_ACCOUNT_ID INTEGER NOT NULL, " +
                "CATEGORY_TITLE TEXT NOT NULL, " +
                "UNIQUE( CATEGORY_ACCOUNT_ID , CATEGORY_TITLE), " +
                "FOREIGN KEY(CATEGORY_ACCOUNT_ID) REFERENCES ACCOUNTS(ID))");
        DatabaseIndexUtil.createIndex(db, "CATEGORIES", "CATEGORY_ID", "CATEGORY_ACCOUNT_ID", "CATEGORY_TITLE");
        // A hashtable storing categoryTitle - categoryId Mapping
        // This is used to prevent too many searches in database
        Hashtable<String, Integer> categoryTitleIdMap = new Hashtable<>();
        int id = 1;
        Cursor tmpNotesCursor = db.rawQuery("SELECT * FROM " + tmpTableNotes, null);
        while (tmpNotesCursor.moveToNext()) {
            String categoryTitle = tmpNotesCursor.getString(8);
            int accountId = tmpNotesCursor.getInt(2);
            Log.e("###", accountId + "");
            Integer categoryId;
            if (categoryTitleIdMap.containsKey(categoryTitle) && categoryTitleIdMap.get(categoryTitle) != null) {
                categoryId = categoryTitleIdMap.get(categoryTitle);
            } else {
                // The category does not exists in the database, create it.
                categoryId = id++;
                ContentValues values = new ContentValues();
                values.put("CATEGORY_ID", categoryId);
                values.put("CATEGORY_ACCOUNT_ID", accountId);
                values.put("CATEGORY_TITLE", categoryTitle);
                db.insert("CATEGORIES", null, values);
                categoryTitleIdMap.put(categoryTitle, categoryId);
            }
            // Move the data in tmp_NOTES to NOTES
            ContentValues values = new ContentValues();
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
            db.insert("NOTES", null, values);
        }
        tmpNotesCursor.close();
        db.execSQL("DROP TABLE IF EXISTS " + tmpTableNotes);
    }
}
