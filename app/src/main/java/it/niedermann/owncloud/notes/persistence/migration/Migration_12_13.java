package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;

import it.niedermann.owncloud.notes.model.Capabilities;

public class Migration_12_13 {
    /**
     * Adds a column to store the ETag of the server {@link Capabilities}
     */
    public Migration_12_13(@NonNull SQLiteDatabase db, @NonNull Context context) {
        db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN CAPABILITIES_ETAG TEXT");
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork("it.niedermann.owncloud.notes.persistence.SyncWorker");
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork("SyncWorker");
    }
}
