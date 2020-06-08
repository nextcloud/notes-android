package it.niedermann.owncloud.notes.persistence.migration;

import android.database.sqlite.SQLiteDatabase;

import it.niedermann.owncloud.notes.model.DBStatus;

public class Migration_4_5 {
    public Migration_4_5(SQLiteDatabase db, int oldVersion) {
        db.execSQL("ALTER TABLE NOTES ADD COLUMN REMOTEID INTEGER");
        db.execSQL("UPDATE NOTES SET REMOTEID=ID WHERE (REMOTEID IS NULL OR REMOTEID=0) AND STATUS!=?", new String[]{"LOCAL_CREATED"});
        db.execSQL("UPDATE NOTES SET REMOTEID=0, STATUS=? WHERE STATUS=?", new String[]{DBStatus.LOCAL_EDITED.getTitle(), "LOCAL_CREATED"});
    }
}
