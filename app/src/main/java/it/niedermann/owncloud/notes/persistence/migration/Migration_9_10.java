package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.shared.util.NoteUtil;

public class Migration_9_10 {
    /**
     * Adds a column to store excerpt instead of regenerating it each time
     * https://github.com/stefan-niedermann/nextcloud-notes/issues/528
     */
    public Migration_9_10(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE NOTES ADD COLUMN EXCERPT INTEGER NOT NULL DEFAULT ''");
        Cursor cursor = db.query("NOTES", new String[]{"ID", "CONTENT", "TITLE"}, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("EXCERPT", NoteUtil.generateNoteExcerpt(cursor.getString(1), cursor.getString(2)));
            db.update("NOTES", values, "ID" + " = ? ", new String[]{cursor.getString(0)});
        }
        cursor.close();
    }
}
