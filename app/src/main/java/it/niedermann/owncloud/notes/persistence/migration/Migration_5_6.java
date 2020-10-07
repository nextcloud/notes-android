package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration_5_6 extends Migration {

    public Migration_5_6() {
        super(5, 6);
    }

    /**
     * Adds a column to support marking notes as favorite
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE NOTES ADD COLUMN FAVORITE INTEGER DEFAULT 0");
    }
}
