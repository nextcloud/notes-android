package it.niedermann.owncloud.notes.persistence.migration;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.persistence.CapabilitiesWorker;

public class Migration_11_12 {
    public Migration_11_12(SQLiteDatabase db, int oldVersion, @NonNull Context context) {
        if (oldVersion < 12) {
            db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN API_VERSION TEXT");
            db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN COLOR VARCHAR(6) NOT NULL DEFAULT '000000'");
            db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN TEXT_COLOR VARCHAR(6) NOT NULL DEFAULT '0082C9'");
            CapabilitiesWorker.update(context);
        }
    }
}
