package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
        // Nothing to do...?
    }
}
