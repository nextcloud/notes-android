package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.model.ApiVersion;
import it.niedermann.owncloud.notes.persistence.CapabilitiesWorker;

public class Migration_11_12 {
    /**
     * Adds columns to store the {@link ApiVersion} and the theme colors
     */
    public Migration_11_12(@NonNull SQLiteDatabase db, @NonNull Context context) {
        db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN API_VERSION TEXT");
        db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN COLOR VARCHAR(6) NOT NULL DEFAULT '000000'");
        db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN TEXT_COLOR VARCHAR(6) NOT NULL DEFAULT '0082C9'");
        CapabilitiesWorker.update(context);
    }
}
