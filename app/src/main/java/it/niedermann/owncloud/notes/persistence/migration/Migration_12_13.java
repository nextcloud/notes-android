package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;

public class Migration_12_13 {
    public Migration_12_13(SQLiteDatabase db, int oldVersion, @NonNull Context context) {
        if (oldVersion < 13) {
            db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN CAPABILITIES_ETAG TEXT");
            WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork("it.niedermann.owncloud.notes.persistence.SyncWorker");
            WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork("SyncWorker");
        }
    }
}
