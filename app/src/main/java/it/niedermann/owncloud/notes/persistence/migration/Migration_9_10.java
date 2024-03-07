/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;

import androidx.annotation.NonNull;
import androidx.room.OnConflictStrategy;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.owncloud.notes.shared.util.NoteUtil;

public class Migration_9_10 extends Migration {

    public Migration_9_10() {
        super(9, 10);
    }

    /**
     * Adds a column to store excerpt instead of regenerating it each time
     * https://github.com/nextcloud/notes-android/issues/528
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE NOTES ADD COLUMN EXCERPT INTEGER NOT NULL DEFAULT ''");
        final var cursor = db.query("NOTES", new String[]{"ID", "CONTENT", "TITLE"});
        while (cursor.moveToNext()) {
            final var values = new ContentValues();
            values.put("EXCERPT", NoteUtil.generateNoteExcerpt(cursor.getString(1), cursor.getString(2)));
            db.update("NOTES", OnConflictStrategy.REPLACE, values, "ID" + " = ? ", new String[]{cursor.getString(0)});
        }
        cursor.close();
    }
}
