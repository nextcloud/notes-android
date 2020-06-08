package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import it.niedermann.owncloud.notes.util.NoteUtil;

public class Migration_9_10 {
    public Migration_9_10(SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 10) {
            db.execSQL("ALTER TABLE NOTES ADD COLUMN EXCERPT INTEGER NOT NULL DEFAULT ''");
            Cursor cursor = db.query("NOTES", new String[]{"ID", "CONTENT"}, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put("EXCERPT", NoteUtil.generateNoteExcerpt(cursor.getString(1)));
                db.update("NOTES", values, "ID" + " = ? ", new String[]{cursor.getString(0)});
            }
            cursor.close();
        }
    }
}
