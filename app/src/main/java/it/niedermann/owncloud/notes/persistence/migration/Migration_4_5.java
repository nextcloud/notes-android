package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import it.niedermann.owncloud.notes.model.DBStatus;

public class Migration_4_5 {
    /**
     * Differentiate between local id and remote id
     */
    public Migration_4_5(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE NOTES ADD COLUMN REMOTEID INTEGER");
        db.execSQL("UPDATE NOTES SET REMOTEID=ID WHERE (REMOTEID IS NULL OR REMOTEID=0) AND STATUS!=?", new String[]{"LOCAL_CREATED"});
        db.execSQL("UPDATE NOTES SET REMOTEID=0, STATUS=? WHERE STATUS=?", new String[]{DBStatus.LOCAL_EDITED.getTitle(), "LOCAL_CREATED"});
    }
}
