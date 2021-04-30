package it.niedermann.owncloud.notes.persistence.migration;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration_16_17 extends Migration {

    public Migration_16_17() {
        super(16, 17);
    }

    /**
     * Adds a column to store the current scroll position per note
     * https://github.com/stefan-niedermann/nextcloud-notes/issues/227
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE NOTES ADD COLUMN SCROLL_Y INTEGER DEFAULT 0");
    }
}
