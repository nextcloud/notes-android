package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.room.OnConflictStrategy;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.android.util.ColorUtil;

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
        db.execSQL("DROP INDEX ACCOUNTS_URL_idx");
        db.execSQL("DROP INDEX ACCOUNTS_USERNAME_idx");
        db.execSQL("DROP INDEX ACCOUNTS_ACCOUNT_NAME_idx");
        db.execSQL("DROP INDEX ACCOUNTS_ETAG_idx");
        db.execSQL("DROP INDEX ACCOUNTS_MODIFIED_idx");
        db.execSQL("DROP INDEX NOTES_REMOTEID_idx");
        db.execSQL("DROP INDEX NOTES_ACCOUNT_ID_idx");
        db.execSQL("DROP INDEX NOTES_STATUS_idx");
        db.execSQL("DROP INDEX NOTES_FAVORITE_idx");
        db.execSQL("DROP INDEX NOTES_CATEGORY_idx");
        db.execSQL("DROP INDEX NOTES_MODIFIED_idx");

        db.execSQL("CREATE TABLE `Account` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL DEFAULT '', `userName` TEXT NOT NULL DEFAULT '', `accountName` TEXT NOT NULL DEFAULT '', `eTag` TEXT, `modified` INTEGER, `apiVersion` TEXT, `color` INTEGER NOT NULL DEFAULT -16743735, `textColor` INTEGER NOT NULL DEFAULT -16777216, `capabilitiesETag` TEXT)");
        db.execSQL("CREATE TABLE `CategoryOptions` (`accountId` INTEGER NOT NULL, `category` TEXT NOT NULL, `sortingMethod` INTEGER, PRIMARY KEY(`accountId`, `category`), FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE `Note` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` INTEGER, `accountId` INTEGER NOT NULL, `status` TEXT NOT NULL, `title` TEXT NOT NULL DEFAULT '', `category` TEXT NOT NULL DEFAULT '', `modified` INTEGER, `content` TEXT NOT NULL DEFAULT '', `favorite` INTEGER NOT NULL DEFAULT 0, `eTag` TEXT, `excerpt` TEXT NOT NULL DEFAULT '', `scrollY` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE `NotesListWidgetData` (`mode` INTEGER NOT NULL, `category` TEXT, `id` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `themeMode` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`accountId`, `category`) REFERENCES `CategoryOptions`(`accountId`, `category`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE `SingleNoteWidgetData` (`noteId` INTEGER NOT NULL, `id` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `themeMode` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`noteId`) REFERENCES `Note`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");

        db.execSQL("CREATE INDEX `IDX_ACCOUNT_ACCOUNTNAME` ON `Account` (`accountName`)");
        db.execSQL("CREATE INDEX `IDX_ACCOUNT_ETAG` ON `Account` (`eTag`)");
        db.execSQL("CREATE INDEX `IDX_ACCOUNT_MODIFIED` ON `Account` (`modified`)");
        db.execSQL("CREATE INDEX `IDX_ACCOUNT_URL` ON `Account` (`url`)");
        db.execSQL("CREATE INDEX `IDX_ACCOUNT_USERNAME` ON `Account` (`userName`)");
        db.execSQL("CREATE INDEX `IDX_CATEGORIYOPTIONS_ACCOUNTID` ON `Category` (`accountId`)");
        db.execSQL("CREATE INDEX `IDX_CATEGORIYOPTIONS_CATEGORY` ON `Category` (`category`)");
        db.execSQL("CREATE INDEX `IDX_CATEGORIYOPTIONS_SORTING_METHOD` ON `Category` (`sortingMethod`)");
        db.execSQL("CREATE INDEX `IDX_NOTESLISTWIDGETDATA_ACCOUNTID` ON `NotesListWidgetData` (`accountId`)");
        db.execSQL("CREATE INDEX `IDX_NOTESLISTWIDGETDATA_CATEGORY` ON `NotesListWidgetData` (`category`)");
        db.execSQL("CREATE INDEX `IDX_NOTESLISTWIDGETDATA_ACCOUNT_CATEGORY` ON `NotesListWidgetData` (`accountId`, `category`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_ACCOUNTID` ON `Note` (`accountId`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_CATEGORY` ON `Note` (`category`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_FAVORITE` ON `Note` (`favorite`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_MODIFIED` ON `Note` (`modified`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_REMOTEID` ON `Note` (`remoteId`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_STATUS` ON `Note` (`status`)");
        db.execSQL("CREATE INDEX `IDX_SINGLENOTEWIDGETDATA_ACCOUNTID` ON `SingleNoteWidgetData` (`accountId`)");
        db.execSQL("CREATE INDEX `IDX_SINGLENOTEWIDGETDATA_NOTEID` ON `SingleNoteWidgetData` (`noteId`)");

        db.execSQL("CREATE UNIQUE INDEX `IDX_UNIQUE_CATEGORYOPTIONS_ACCOUNT_CATEGORY` ON `Category` (`accountId`, `sortingMethod`)");

        db.execSQL("CREATE TRIGGER TRG_CLEANUP_CATEGORIES_DEL AFTER DELETE ON Note BEGIN DELETE FROM CategoryOptions WHERE CategoryOptions.category NOT IN (SELECT Note.category FROM Note WHERE Note.accountId = CategoryOptions.accountId); END;");
        db.execSQL("CREATE TRIGGER TRG_CLEANUP_CATEGORIES_UPD AFTER UPDATE ON Note BEGIN DELETE FROM CategoryOptions WHERE CategoryOptions.category NOT IN (SELECT Note.category FROM Note WHERE Note.accountId = CategoryOptions.accountId); END;");

        Cursor tmpAccountCursor = db.query("SELECT * FROM ACCOUNTS", null);
        while (tmpAccountCursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("ID", tmpAccountCursor.getInt(0));
            values.put("URL", tmpAccountCursor.getString(1));
            values.put("USERNAME", tmpAccountCursor.getString(2));
            values.put("ACCOUNTNAME", tmpAccountCursor.getString(3));
            values.put("ETAG", tmpAccountCursor.getString(4));
            values.put("MODIFIED", tmpAccountCursor.getLong(5));
            values.put("APIVERSION", tmpAccountCursor.getString(6));
            try {
                values.put("COLOR", Color.parseColor(ColorUtil.INSTANCE.formatColorToParsableHexString(tmpAccountCursor.getString(7))));
            } catch (Exception e) {
                e.printStackTrace();
                values.put("COLOR", -16743735);
            }
            try {
                values.put("TEXTCOLOR", Color.parseColor(ColorUtil.INSTANCE.formatColorToParsableHexString(tmpAccountCursor.getString(8))));
            } catch (Exception e) {
                e.printStackTrace();
                values.put("TEXTCOLOR", -16777216);
            }
            values.put("CAPABILITIESETAG", tmpAccountCursor.getString(9));
            db.insert("ACCOUNT", OnConflictStrategy.REPLACE, values);
        }
        tmpAccountCursor.close();
        db.execSQL("DROP TABLE IF EXISTS ACCOUNTS");

        Cursor tmpCategoriesCursor = db.query("SELECT * FROM CATEGORIES", null);
        while (tmpCategoriesCursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("ACCOUNTID", tmpCategoriesCursor.getInt(1));
            values.put("CATEGORY", tmpCategoriesCursor.getString(2));
            values.put("SORTINGMETHOD", tmpCategoriesCursor.getInt(3));
            db.insert("CATEGORY", OnConflictStrategy.REPLACE, values);
        }
        tmpCategoriesCursor.close();
        db.execSQL("DROP TABLE IF EXISTS CATEGORIES");

        Cursor tmpNotesCursor = db.query("SELECT NOTES.*, CATEGORIES.title as `CAT_TITLE` FROM NOTES INNER JOIN CATEGORIES ON NOTES.categoryId = CATEGORIES.id", null);
        while (tmpNotesCursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("ID", tmpNotesCursor.getInt(0));
            values.put("REMOTEID", tmpNotesCursor.getInt(1));
            values.put("ACCOUNTID", tmpNotesCursor.getInt(2));
            values.put("STATUS", tmpNotesCursor.getString(3));
            values.put("TITLE", tmpNotesCursor.getString(4));
            values.put("MODIFIED", tmpNotesCursor.getLong(5));
            values.put("CONTENT", tmpNotesCursor.getString(6));
            values.put("FAVORITE", tmpNotesCursor.getInt(7));
            values.put("CATEGORY", tmpNotesCursor.getInt(11));
            values.put("ETAG", tmpNotesCursor.getString(9));
            values.put("EXCERPT", tmpNotesCursor.getString(10));
            db.insert("NOTE", OnConflictStrategy.REPLACE, values);
        }
        tmpNotesCursor.close();
        db.execSQL("DROP TABLE IF EXISTS NOTES");

        Cursor tmpWidgetNotesListCursor = db.query("SELECT * FROM WIDGET_NOTE_LISTS", null);
        while (tmpWidgetNotesListCursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("ID", tmpWidgetNotesListCursor.getInt(0));
            values.put("ACCOUNTID", tmpWidgetNotesListCursor.getInt(1));
            values.put("CATEGORYID", tmpWidgetNotesListCursor.getInt(2));
            values.put("MODE", tmpWidgetNotesListCursor.getInt(3));
            values.put("THEMEMODE", tmpWidgetNotesListCursor.getInt(4));
            db.insert("NOTESLISTWIDGETDATA", OnConflictStrategy.REPLACE, values);
        }
        tmpWidgetNotesListCursor.close();
        db.execSQL("DROP TABLE IF EXISTS WIDGET_NOTE_LISTS");

        Cursor tmpWidgetSinlgeNotesCursor = db.query("SELECT * FROM WIDGET_SINGLE_NOTES", null);
        while (tmpWidgetSinlgeNotesCursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("ID", tmpWidgetNotesListCursor.getInt(0));
            values.put("ACCOUNTID", tmpWidgetNotesListCursor.getInt(1));
            values.put("NOTEID", tmpWidgetNotesListCursor.getInt(2));
            values.put("THEMEMODE", tmpWidgetNotesListCursor.getInt(3));
            db.insert("SINGLENOTEWIDGETDATA", OnConflictStrategy.REPLACE, values);
        }
        tmpWidgetNotesListCursor.close();
        db.execSQL("DROP TABLE IF EXISTS WIDGET_SINGLE_NOTES");
    }
}
