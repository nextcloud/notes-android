package it.niedermann.owncloud.notes.persistence.migration;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration_4_5 extends Migration {

    public Migration_4_5() {
        super(4, 5);
    }

    /**
     * Differentiate between local id and remote id
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE NOTES ADD COLUMN REMOTEID INTEGER");
        db.execSQL("UPDATE NOTES SET REMOTEID=ID WHERE (REMOTEID IS NULL OR REMOTEID=0) AND STATUS!=?", new String[]{"LOCAL_CREATED"});
        db.execSQL("UPDATE NOTES SET REMOTEID=0, STATUS=? WHERE STATUS=?", new String[]{"LOCAL_EDITED", "LOCAL_CREATED"});
    }
}
