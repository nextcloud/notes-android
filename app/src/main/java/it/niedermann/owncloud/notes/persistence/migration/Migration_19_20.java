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
        db.execSQL("CREATE INDEX `IDX_CATEGORIYOPTIONS_ACCOUNTID` ON `CategoryOptions` (`accountId`)");
        db.execSQL("CREATE INDEX `IDX_CATEGORIYOPTIONS_CATEGORY` ON `CategoryOptions` (`category`)");
        db.execSQL("CREATE INDEX `IDX_CATEGORIYOPTIONS_SORTING_METHOD` ON `CategoryOptions` (`sortingMethod`)");
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

        db.execSQL("CREATE UNIQUE INDEX `IDX_UNIQUE_CATEGORYOPTIONS_ACCOUNT_CATEGORY` ON `CategoryOptions` (`accountId`, `category`)");

        db.execSQL("CREATE TRIGGER TRG_CLEANUP_CATEGORIES_DEL AFTER DELETE ON Note BEGIN DELETE FROM CategoryOptions WHERE CategoryOptions.category NOT IN (SELECT Note.category FROM Note WHERE Note.accountId = CategoryOptions.accountId); END;");
        db.execSQL("CREATE TRIGGER TRG_CLEANUP_CATEGORIES_UPD AFTER UPDATE ON Note BEGIN DELETE FROM CategoryOptions WHERE CategoryOptions.category NOT IN (SELECT Note.category FROM Note WHERE Note.accountId = CategoryOptions.accountId); END;");

        final Cursor tmpAccountCursor = db.query("SELECT * FROM ACCOUNTS", null);
        final ContentValues account_values = new ContentValues(10);
        final int account_colPos_ID = tmpAccountCursor.getColumnIndex("ID");
        final int account_colPos_URL = tmpAccountCursor.getColumnIndex("URL");
        final int account_colPos_USERNAME = tmpAccountCursor.getColumnIndex("USERNAME");
        final int account_colPos_ACCOUNT_NAME = tmpAccountCursor.getColumnIndex("ACCOUNT_NAME");
        final int account_colPos_ETAG = tmpAccountCursor.getColumnIndex("ETAG");
        final int account_colPos_MODIFIED = tmpAccountCursor.getColumnIndex("MODIFIED");
        final int account_colPos_API_VERSION = tmpAccountCursor.getColumnIndex("API_VERSION");
        final int account_colPos_COLOR = tmpAccountCursor.getColumnIndex("COLOR");
        final int account_colPos_TEXT_COLOR = tmpAccountCursor.getColumnIndex("TEXT_COLOR");
        final int account_colPos_CAPABILITIES_ETAG = tmpAccountCursor.getColumnIndex("CAPABILITIES_ETAG");
        while (tmpAccountCursor.moveToNext()) {
            account_values.put("ID", tmpAccountCursor.getInt(account_colPos_ID));
            account_values.put("URL", tmpAccountCursor.getString(account_colPos_URL));
            account_values.put("USERNAME", tmpAccountCursor.getString(account_colPos_USERNAME));
            account_values.put("ACCOUNTNAME", tmpAccountCursor.getString(account_colPos_ACCOUNT_NAME));
            account_values.put("ETAG", tmpAccountCursor.getString(account_colPos_ETAG));
            account_values.put("MODIFIED", tmpAccountCursor.getLong(account_colPos_MODIFIED));
            account_values.put("APIVERSION", tmpAccountCursor.getString(account_colPos_API_VERSION));
            try {
                account_values.put("COLOR", Color.parseColor(ColorUtil.INSTANCE.formatColorToParsableHexString(tmpAccountCursor.getString(account_colPos_COLOR))));
            } catch (Exception e) {
                e.printStackTrace();
                account_values.put("COLOR", -16743735);
            }
            try {
                account_values.put("TEXTCOLOR", Color.parseColor(ColorUtil.INSTANCE.formatColorToParsableHexString(tmpAccountCursor.getString(account_colPos_TEXT_COLOR))));
            } catch (Exception e) {
                e.printStackTrace();
                account_values.put("TEXTCOLOR", -16777216);
            }
            account_values.put("CAPABILITIESETAG", tmpAccountCursor.getString(account_colPos_CAPABILITIES_ETAG));
            db.insert("ACCOUNT", OnConflictStrategy.REPLACE, account_values);
        }
        tmpAccountCursor.close();

        final Cursor tmpCategoriesCursor = db.query("SELECT * FROM CATEGORIES", null);
        final ContentValues categories_values = new ContentValues(3);
        final int categories_colPos_ACCOUNT_ID = tmpCategoriesCursor.getColumnIndex("CATEGORY_ACCOUNT_ID");
        final int categories_colPos_TITLE = tmpCategoriesCursor.getColumnIndex("CATEGORY_TITLE");
        final int categories_colPos_SORTING_METHOD = tmpCategoriesCursor.getColumnIndex("CATEGORY_SORTING_METHOD");
        while (tmpCategoriesCursor.moveToNext()) {
            categories_values.put("ACCOUNTID", tmpCategoriesCursor.getInt(categories_colPos_ACCOUNT_ID));
            categories_values.put("CATEGORY", tmpCategoriesCursor.getString(categories_colPos_TITLE));
            categories_values.put("SORTINGMETHOD", tmpCategoriesCursor.getInt(categories_colPos_SORTING_METHOD));
            db.insert("CATEGORYOPTIONS", OnConflictStrategy.REPLACE, categories_values);
        }
        tmpCategoriesCursor.close();

        final Cursor tmpNotesCursor = db.query("SELECT NOTES.*, CATEGORIES.category_title as `CAT_TITLE` FROM NOTES INNER JOIN CATEGORIES ON NOTES.category = CATEGORIES.category_id", null);
        final ContentValues note_values = new ContentValues(12);
        final int notes_colPos_ID = tmpNotesCursor.getColumnIndex("ID");
        final int notes_colPos_REMOTEID = tmpNotesCursor.getColumnIndex("REMOTEID");
        final int notes_colPos_ACCOUNT_ID = tmpNotesCursor.getColumnIndex("ACCOUNT_ID");
        final int notes_colPos_STATUS = tmpNotesCursor.getColumnIndex("STATUS");
        final int notes_colPos_TITLE = tmpNotesCursor.getColumnIndex("TITLE");
        final int notes_colPos_MODIFIED = tmpNotesCursor.getColumnIndex("MODIFIED");
        final int notes_colPos_CONTENT = tmpNotesCursor.getColumnIndex("CONTENT");
        final int notes_colPos_FAVORITE = tmpNotesCursor.getColumnIndex("FAVORITE");
        final int notes_colPos_CAT_TITLE = tmpNotesCursor.getColumnIndex("CAT_TITLE");
        final int notes_colPos_ETAG = tmpNotesCursor.getColumnIndex("ETAG");
        final int notes_colPos_EXCERPT = tmpNotesCursor.getColumnIndex("EXCERPT");
        final int notes_colPos_SCROLL_Y = tmpNotesCursor.getColumnIndex("SCROLL_Y");
        while (tmpNotesCursor.moveToNext()) {
            note_values.put("ID", tmpNotesCursor.getInt(notes_colPos_ID));
            note_values.put("REMOTEID", tmpNotesCursor.getInt(notes_colPos_REMOTEID));
            note_values.put("ACCOUNTID", tmpNotesCursor.getInt(notes_colPos_ACCOUNT_ID));
            note_values.put("STATUS", tmpNotesCursor.getString(notes_colPos_STATUS));
            note_values.put("TITLE", tmpNotesCursor.getString(notes_colPos_TITLE));
            note_values.put("MODIFIED", tmpNotesCursor.getLong(notes_colPos_MODIFIED));
            note_values.put("CONTENT", tmpNotesCursor.getString(notes_colPos_CONTENT));
            note_values.put("FAVORITE", tmpNotesCursor.getInt(notes_colPos_FAVORITE));
            note_values.put("CATEGORY", tmpNotesCursor.getString(notes_colPos_CAT_TITLE));
            note_values.put("ETAG", tmpNotesCursor.getString(notes_colPos_ETAG));
            note_values.put("EXCERPT", tmpNotesCursor.getString(notes_colPos_EXCERPT));
            note_values.put("SCROLLY", tmpNotesCursor.getString(notes_colPos_SCROLL_Y));
            db.insert("NOTE", OnConflictStrategy.REPLACE, note_values);
        }
        tmpNotesCursor.close();

        final Cursor tmpWidgetNotesListCursor = db.query("SELECT WIDGET_NOTE_LISTS.*, CATEGORIES.category_title as `CATEGORY` FROM WIDGET_NOTE_LISTS INNER JOIN CATEGORIES ON WIDGET_NOTE_LISTS.CATEGORY_ID = CATEGORIES.category_id", null);
        final ContentValues nlw_values = new ContentValues(5);
        final int nlw_colPos_ID = tmpWidgetNotesListCursor.getColumnIndex("ID");
        final int nlw_colPos_ACCOUNT_ID = tmpWidgetNotesListCursor.getColumnIndex("ACCOUNT_ID");
        final int nlw_colPos_CATEGORY = tmpWidgetNotesListCursor.getColumnIndex("CATEGORY");
        final int nlw_colPos_MODE = tmpWidgetNotesListCursor.getColumnIndex("MODE");
        final int nlw_colPos_THEME_MODE = tmpWidgetNotesListCursor.getColumnIndex("THEME_MODE");
        while (tmpWidgetNotesListCursor.moveToNext()) {
            nlw_values.put("ID", tmpWidgetNotesListCursor.getInt(nlw_colPos_ID));
            nlw_values.put("ACCOUNTID", tmpWidgetNotesListCursor.getInt(nlw_colPos_ACCOUNT_ID));
            nlw_values.put("CATEGORY", tmpWidgetNotesListCursor.getString(nlw_colPos_CATEGORY));
            // TODO does work for categories, but not yet for FAVORITES
            nlw_values.put("MODE", tmpWidgetNotesListCursor.getInt(nlw_colPos_MODE));
            nlw_values.put("THEMEMODE", tmpWidgetNotesListCursor.getInt(nlw_colPos_THEME_MODE));
            db.insert("NOTESLISTWIDGETDATA", OnConflictStrategy.REPLACE, nlw_values);
        }
        tmpWidgetNotesListCursor.close();

        Cursor tmpWidgetSingleNotesCursor = db.query("SELECT * FROM WIDGET_SINGLE_NOTES", null);
        final ContentValues snw_values = new ContentValues(4);
        final int snw_colPos_ID = tmpWidgetSingleNotesCursor.getColumnIndex("ID");
        final int snw_colPos_ACCOUNT_ID = tmpWidgetSingleNotesCursor.getColumnIndex("ACCOUNT_ID");
        final int snw_colPos_NOTE_ID = tmpWidgetSingleNotesCursor.getColumnIndex("NOTE_ID");
        final int snw_colPos_THEME_MODE = tmpWidgetSingleNotesCursor.getColumnIndex("THEME_MODE");
        while (tmpWidgetSingleNotesCursor.moveToNext()) {
            snw_values.put("ID", tmpWidgetSingleNotesCursor.getInt(snw_colPos_ID));
            snw_values.put("ACCOUNTID", tmpWidgetSingleNotesCursor.getInt(snw_colPos_ACCOUNT_ID));
            snw_values.put("NOTEID", tmpWidgetSingleNotesCursor.getInt(snw_colPos_NOTE_ID));
            snw_values.put("THEMEMODE", tmpWidgetSingleNotesCursor.getInt(snw_colPos_THEME_MODE));
            db.insert("SINGLENOTEWIDGETDATA", OnConflictStrategy.REPLACE, snw_values);
        }
        tmpWidgetSingleNotesCursor.close();

        db.execSQL("DROP TABLE IF EXISTS WIDGET_SINGLE_NOTES");
        db.execSQL("DROP TABLE IF EXISTS WIDGET_NOTE_LISTS");
        db.execSQL("DROP TABLE IF EXISTS CATEGORIES");
        db.execSQL("DROP TABLE IF EXISTS NOTES");
        db.execSQL("DROP TABLE IF EXISTS ACCOUNTS");
    }
}
