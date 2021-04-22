package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration_17_18 extends Migration {

    public Migration_17_18() {
        super(17, 18);
    }

    /**
     * Add a new column to store the sorting method for a category note list
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE CATEGORIES ADD COLUMN CATEGORY_SORTING_METHOD INTEGER DEFAULT 0");
    }
}
