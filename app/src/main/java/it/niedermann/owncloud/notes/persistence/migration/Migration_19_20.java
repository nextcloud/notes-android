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

import it.niedermann.owncloud.notes.shared.util.ColorUtil;

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

        db.execSQL("CREATE TABLE `Account` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `url` TEXT, `userName` TEXT, `accountName` TEXT, `eTag` TEXT, `modified` INTEGER, `apiVersion` TEXT, `color` INTEGER DEFAULT -16743735, `textColor` INTEGER DEFAULT -16777216, `capabilitiesETag` TEXT)");
        db.execSQL("CREATE TABLE `Category` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `accountId` INTEGER NOT NULL, `title` TEXT, `sortingMethod` INTEGER, FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE `Note` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `remoteId` INTEGER, `accountId` INTEGER, `status` TEXT, `title` TEXT, `modified` INTEGER DEFAULT 0, `content` TEXT, `favorite` INTEGER DEFAULT 0, `categoryId` INTEGER, `eTag` TEXT, `excerpt` TEXT NOT NULL DEFAULT '', `scrollY` INTEGER DEFAULT 0, FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`categoryId`) REFERENCES `Category`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");
        db.execSQL("CREATE TABLE `NotesListWidgetData` (`mode` INTEGER NOT NULL, `categoryId` INTEGER, `id` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `themeMode` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`categoryId`) REFERENCES `Category`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE `SingleNoteWidgetData` (`noteId` INTEGER NOT NULL, `id` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `themeMode` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`noteId`) REFERENCES `Note`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");

        db.execSQL("CREATE INDEX `IDX_ACCOUNT_ACCOUNTNAME` ON `Account` (`accountName`)");
        db.execSQL("CREATE INDEX `IDX_ACCOUNT_URL` ON `Account` (`url`)");
        db.execSQL("CREATE INDEX `IDX_ACCOUNT_USERNAME` ON `Account` (`userName`)");
        db.execSQL("CREATE INDEX `IDX_ACCOUNT_MODIFIED` ON `Account` (`modified`)");
        db.execSQL("CREATE INDEX `IDX_ACCOUNT_ETAG` ON `Account` (`eTag`)");
        db.execSQL("CREATE INDEX `IDX_CATEGORIES_ACCOUNTID` ON `Category` (`accountId`)");
        db.execSQL("CREATE INDEX `IDX_CATEGORIES_ID` ON `Category` (`id`)");
        db.execSQL("CREATE INDEX `IDX_CATEGORIES_SORTING_METHOD` ON `Category` (`sortingMethod`)");
        db.execSQL("CREATE INDEX `IDX_CATEGORIES_TITLE` ON `Category` (`title`)");
        db.execSQL("CREATE INDEX `IDX_NOTESLISTWIDGETDATA_ACCOUNTID` ON `NotesListWidgetData` (`accountId`)");
        db.execSQL("CREATE INDEX `IDX_NOTESLISTWIDGETDATA_CATEGORYID` ON `NotesListWidgetData` (`categoryId`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_ACCOUNTID` ON `Note` (`accountId`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_CATEGORY` ON `Note` (`categoryId`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_FAVORITE` ON `Note` (`favorite`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_MODIFIED` ON `Note` (`modified`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_REMOTEID` ON `Note` (`remoteId`)");
        db.execSQL("CREATE INDEX `IDX_NOTE_STATUS` ON `Note` (`status`)");
        db.execSQL("CREATE INDEX `IDX_SINGLENOTEWIDGETDATA_ACCOUNTID` ON `SingleNoteWidgetData` (`accountId`)");
        db.execSQL("CREATE INDEX `IDX_SINGLENOTEWIDGETDATA_NOTEID` ON `SingleNoteWidgetData` (`noteId`)");

        db.execSQL("CREATE TRIGGER TRG_CLEANUP_CATEGORIES_DEL AFTER DELETE ON Note BEGIN DELETE FROM Category WHERE Category.id NOT IN (SELECT Note.categoryId FROM Note); END;");
        db.execSQL("CREATE TRIGGER TRG_CLEANUP_CATEGORIES_UPD AFTER UPDATE ON Note BEGIN DELETE FROM Category WHERE Category.id NOT IN (SELECT Note.categoryId FROM Note); END;");

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
                values.put("COLOR", Color.parseColor(ColorUtil.formatColorToParsableHexString(tmpAccountCursor.getString(7))));
            } catch (Exception e) {
                e.printStackTrace();
                values.put("COLOR", -16743735);
            }
            try {
                values.put("TEXTCOLOR", Color.parseColor(ColorUtil.formatColorToParsableHexString(tmpAccountCursor.getString(8))));
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
            values.put("ID", tmpCategoriesCursor.getInt(0));
            values.put("ACCOUNTID", tmpCategoriesCursor.getInt(1));
            values.put("TITLE", tmpCategoriesCursor.getString(2));
            values.put("SORTINGMETHOD", tmpCategoriesCursor.getInt(3));
            db.insert("CATEGORY", OnConflictStrategy.REPLACE, values);
        }
        tmpCategoriesCursor.close();
        db.execSQL("DROP TABLE IF EXISTS CATEGORIES");

        Cursor tmpNotesCursor = db.query("SELECT * FROM NOTES", null);
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
            values.put("CATEGORYID", tmpNotesCursor.getInt(8));
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
        while (tmpWidgetNotesListCursor.moveToNext()) {
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
