package it.niedermann.owncloud.notes.persistence.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName", "Detekt.ClassNaming", "Detekt.MagicNumber")
class Migration_24_25 : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Account ADD COLUMN directEditingAvailable INTEGER DEFAULT 0 NOT NULL")
        // remove capabilities etag to force refresh
        db.execSQL("UPDATE Account SET capabilitiesETag = NULL")
    }
}
