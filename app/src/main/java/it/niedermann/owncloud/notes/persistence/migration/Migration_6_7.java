package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.owncloud.notes.shared.util.DatabaseIndexUtil;

public class Migration_6_7 extends Migration {

    public Migration_6_7() {
        super(6, 7);
    }

    /**
     * Adds columns for category support and ETags
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        DatabaseIndexUtil.dropIndexes(db);
        db.execSQL("ALTER TABLE NOTES ADD COLUMN CATEGORY TEXT NOT NULL DEFAULT ''");
        db.execSQL("ALTER TABLE NOTES ADD COLUMN ETAG TEXT");
        DatabaseIndexUtil.createIndex(db, "NOTES", "REMOTEID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");
    }
}
