/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.room.OnConflictStrategy;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.android.util.ColorUtil;

public final class Migration_20_21 extends Migration {

    public Migration_20_21() {
        super(20, 21);
    }

    /**
     * From {@link SQLiteOpenHelper} to {@link RoomDatabase}
     * https://github.com/stefan-niedermann/nextcloud-deck/issues/531
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        dropOldIndices(db);

        createNewTables(db);
        createNewIndices(db);

        migrateAccounts(db);
        migrateCategories(db);
        migrateNotes(db);
        migrateNotesListWidgets(db);
        migrateSingleNotesWidgets(db);

        dropOldTables(db);
    }

    private static void dropOldIndices(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("DROP INDEX IF EXISTS ACCOUNTS_URL_idx");
        db.execSQL("DROP INDEX IF EXISTS ACCOUNTS_USERNAME_idx");
        db.execSQL("DROP INDEX IF EXISTS ACCOUNTS_ACCOUNT_NAME_idx");
        db.execSQL("DROP INDEX IF EXISTS ACCOUNTS_ETAG_idx");
        db.execSQL("DROP INDEX IF EXISTS ACCOUNTS_MODIFIED_idx");
        db.execSQL("DROP INDEX IF EXISTS NOTES_REMOTEID_idx");
        db.execSQL("DROP INDEX IF EXISTS NOTES_ACCOUNT_ID_idx");
        db.execSQL("DROP INDEX IF EXISTS NOTES_STATUS_idx");
        db.execSQL("DROP INDEX IF EXISTS NOTES_FAVORITE_idx");
        db.execSQL("DROP INDEX IF EXISTS NOTES_CATEGORY_idx");
        db.execSQL("DROP INDEX IF EXISTS NOTES_MODIFIED_idx");
    }

    private static void createNewTables(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE `Account` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL DEFAULT '', `userName` TEXT NOT NULL DEFAULT '', `accountName` TEXT NOT NULL DEFAULT '', `eTag` TEXT, `modified` INTEGER, `apiVersion` TEXT, `color` INTEGER NOT NULL DEFAULT -16743735, `textColor` INTEGER NOT NULL DEFAULT -16777216, `capabilitiesETag` TEXT)");
        db.execSQL("CREATE TABLE `CategoryOptions` (`accountId` INTEGER NOT NULL, `category` TEXT NOT NULL, `sortingMethod` INTEGER, PRIMARY KEY(`accountId`, `category`), FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE `Note` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` INTEGER, `accountId` INTEGER NOT NULL, `status` TEXT NOT NULL, `title` TEXT NOT NULL DEFAULT '', `category` TEXT NOT NULL DEFAULT '', `modified` INTEGER, `content` TEXT NOT NULL DEFAULT '', `favorite` INTEGER NOT NULL DEFAULT 0, `eTag` TEXT, `excerpt` TEXT NOT NULL DEFAULT '', `scrollY` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE `NotesListWidgetData` (`mode` INTEGER NOT NULL, `category` TEXT, `id` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `themeMode` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE `SingleNoteWidgetData` (`noteId` INTEGER NOT NULL, `id` INTEGER NOT NULL, `accountId` INTEGER NOT NULL, `themeMode` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`noteId`) REFERENCES `Note`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
    }

    private static void createNewIndices(@NonNull SupportSQLiteDatabase db) {
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

    }

    private static void migrateAccounts(@NonNull SupportSQLiteDatabase db) {
        final var cursor = db.query("SELECT * FROM ACCOUNTS", null);
        final var values = new ContentValues(10);

        final int COLUMN_POSITION_ID = cursor.getColumnIndex("ID");
        final int COLUMN_POSITION_URL = cursor.getColumnIndex("URL");
        final int COLUMN_POSITION_USERNAME = cursor.getColumnIndex("USERNAME");
        final int COLUMN_POSITION_ACCOUNT_NAME = cursor.getColumnIndex("ACCOUNT_NAME");
        final int COLUMN_POSITION_ETAG = cursor.getColumnIndex("ETAG");
        final int COLUMN_POSITION_MODIFIED = cursor.getColumnIndex("MODIFIED");
        final int COLUMN_POSITION_API_VERSION = cursor.getColumnIndex("API_VERSION");
        final int COLUMN_POSITION_COLOR = cursor.getColumnIndex("COLOR");
        final int COLUMN_POSITION_TEXT_COLOR = cursor.getColumnIndex("TEXT_COLOR");
        final int COLUMN_POSITION_CAPABILITIES_ETAG = cursor.getColumnIndex("CAPABILITIES_ETAG");

        while (cursor.moveToNext()) {
            values.put("ID", cursor.getInt(COLUMN_POSITION_ID));
            values.put("URL", cursor.getString(COLUMN_POSITION_URL));
            values.put("USERNAME", cursor.getString(COLUMN_POSITION_USERNAME));
            values.put("ACCOUNTNAME", cursor.getString(COLUMN_POSITION_ACCOUNT_NAME));
            values.put("ETAG", cursor.getString(COLUMN_POSITION_ETAG));
            values.put("MODIFIED", cursor.getLong(COLUMN_POSITION_MODIFIED) * 1_000);
            values.put("APIVERSION", cursor.getString(COLUMN_POSITION_API_VERSION));
            try {
                values.put("COLOR", Color.parseColor(ColorUtil.formatColorToParsableHexString(cursor.getString(COLUMN_POSITION_COLOR))));
            } catch (Exception e) {
                e.printStackTrace();
                values.put("COLOR", -16743735);
            }
            try {
                values.put("TEXTCOLOR", Color.parseColor(ColorUtil.formatColorToParsableHexString(cursor.getString(COLUMN_POSITION_TEXT_COLOR))));
            } catch (Exception e) {
                e.printStackTrace();
                values.put("TEXTCOLOR", -16777216);
            }
            values.put("CAPABILITIESETAG", cursor.getString(COLUMN_POSITION_CAPABILITIES_ETAG));
            db.insert("ACCOUNT", OnConflictStrategy.REPLACE, values);
        }
        cursor.close();
    }

    private static void migrateCategories(@NonNull SupportSQLiteDatabase db) {
        final var cursor = db.query("SELECT * FROM CATEGORIES", null);
        final var values = new ContentValues(3);

        final int COLUMN_POSITION_ACCOUNT_ID = cursor.getColumnIndex("CATEGORY_ACCOUNT_ID");
        final int COLUMN_POSITION_TITLE = cursor.getColumnIndex("CATEGORY_TITLE");
        final int COLUMN_POSITION_SORTING_METHOD = cursor.getColumnIndex("CATEGORY_SORTING_METHOD");

        while (cursor.moveToNext()) {
            values.put("ACCOUNTID", cursor.getInt(COLUMN_POSITION_ACCOUNT_ID));
            values.put("CATEGORY", cursor.getString(COLUMN_POSITION_TITLE));
            values.put("SORTINGMETHOD", cursor.getInt(COLUMN_POSITION_SORTING_METHOD));
            db.insert("CATEGORYOPTIONS", OnConflictStrategy.REPLACE, values);
        }
        cursor.close();
    }

    private static void migrateNotes(@NonNull SupportSQLiteDatabase db) {
        final var cursor = db.query("SELECT NOTES.*, CATEGORIES.category_title as `CAT_TITLE` FROM NOTES LEFT JOIN CATEGORIES ON NOTES.category = CATEGORIES.category_id", null);
        final var values = new ContentValues(12);

        final int COLUMN_POSITION_ID = cursor.getColumnIndex("ID");
        final int COLUMN_POSITION_REMOTEID = cursor.getColumnIndex("REMOTEID");
        final int COLUMN_POSITION_ACCOUNT_ID = cursor.getColumnIndex("ACCOUNT_ID");
        final int COLUMN_POSITION_STATUS = cursor.getColumnIndex("STATUS");
        final int COLUMN_POSITION_TITLE = cursor.getColumnIndex("TITLE");
        final int COLUMN_POSITION_MODIFIED = cursor.getColumnIndex("MODIFIED");
        final int COLUMN_POSITION_CONTENT = cursor.getColumnIndex("CONTENT");
        final int COLUMN_POSITION_FAVORITE = cursor.getColumnIndex("FAVORITE");
        final int COLUMN_POSITION_CAT_TITLE = cursor.getColumnIndex("CAT_TITLE");
        final int COLUMN_POSITION_ETAG = cursor.getColumnIndex("ETAG");
        final int COLUMN_POSITION_EXCERPT = cursor.getColumnIndex("EXCERPT");
        final int COLUMN_POSITION_SCROLL_Y = cursor.getColumnIndex("SCROLL_Y");

        while (cursor.moveToNext()) {
            values.put("ID", cursor.getInt(COLUMN_POSITION_ID));
            values.put("REMOTEID", cursor.getInt(COLUMN_POSITION_REMOTEID));
            values.put("ACCOUNTID", cursor.getInt(COLUMN_POSITION_ACCOUNT_ID));
            values.put("STATUS", cursor.getString(COLUMN_POSITION_STATUS));
            values.put("TITLE", cursor.getString(COLUMN_POSITION_TITLE));
            values.put("MODIFIED", cursor.getLong(COLUMN_POSITION_MODIFIED) * 1_000);
            values.put("CONTENT", cursor.getString(COLUMN_POSITION_CONTENT));
            values.put("FAVORITE", cursor.getInt(COLUMN_POSITION_FAVORITE));
            values.put("CATEGORY", cursor.getString(COLUMN_POSITION_CAT_TITLE));
            values.put("ETAG", cursor.getString(COLUMN_POSITION_ETAG));
            values.put("EXCERPT", cursor.getString(COLUMN_POSITION_EXCERPT));
            values.put("SCROLLY", cursor.getString(COLUMN_POSITION_SCROLL_Y));
            db.insert("NOTE", OnConflictStrategy.REPLACE, values);
        }
        cursor.close();
    }

    private static void migrateNotesListWidgets(@NonNull SupportSQLiteDatabase db) {
        final var cursor = db.query("SELECT WIDGET_NOTE_LISTS.*, CATEGORIES.category_title as `CATEGORY` FROM WIDGET_NOTE_LISTS LEFT JOIN CATEGORIES ON WIDGET_NOTE_LISTS.CATEGORY_ID = CATEGORIES.category_id", null);
        final var values = new ContentValues(5);

        final int COLUMN_POSITION_ID = cursor.getColumnIndex("ID");
        final int COLUMN_POSITION_ACCOUNT_ID = cursor.getColumnIndex("ACCOUNT_ID");
        final int COLUMN_POSITION_CATEGORY = cursor.getColumnIndex("CATEGORY");
        final int COLUMN_POSITION_MODE = cursor.getColumnIndex("MODE");
        final int COLUMN_POSITION_THEME_MODE = cursor.getColumnIndex("THEME_MODE");

        while (cursor.moveToNext()) {
            values.put("ID", cursor.getInt(COLUMN_POSITION_ID));
            values.put("ACCOUNTID", cursor.getInt(COLUMN_POSITION_ACCOUNT_ID));
            values.put("CATEGORY", cursor.getString(COLUMN_POSITION_CATEGORY));
            values.put("MODE", cursor.getInt(COLUMN_POSITION_MODE));
            values.put("THEMEMODE", cursor.getInt(COLUMN_POSITION_THEME_MODE));
            db.insert("NOTESLISTWIDGETDATA", OnConflictStrategy.REPLACE, values);
        }
        cursor.close();
    }

    private static void migrateSingleNotesWidgets(@NonNull SupportSQLiteDatabase db) {
        final var cursor = db.query("SELECT * FROM WIDGET_SINGLE_NOTES", null);
        final var values = new ContentValues(4);

        final int COLUMN_POSITION_ID = cursor.getColumnIndex("ID");
        final int COLUMN_POSITION_ACCOUNT_ID = cursor.getColumnIndex("ACCOUNT_ID");
        final int COLUMN_POSITION_NOTE_ID = cursor.getColumnIndex("NOTE_ID");
        final int COLUMN_POSITION_THEME_MODE = cursor.getColumnIndex("THEME_MODE");

        while (cursor.moveToNext()) {
            values.put("ID", cursor.getInt(COLUMN_POSITION_ID));
            values.put("ACCOUNTID", cursor.getInt(COLUMN_POSITION_ACCOUNT_ID));
            values.put("NOTEID", cursor.getInt(COLUMN_POSITION_NOTE_ID));
            values.put("THEMEMODE", cursor.getInt(COLUMN_POSITION_THEME_MODE));
            db.insert("SINGLENOTEWIDGETDATA", OnConflictStrategy.REPLACE, values);
        }
        cursor.close();
    }

    private static void dropOldTables(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS WIDGET_SINGLE_NOTES");
        db.execSQL("DROP TABLE IF EXISTS WIDGET_NOTE_LISTS");
        db.execSQL("DROP TABLE IF EXISTS CATEGORIES");
        db.execSQL("DROP TABLE IF EXISTS NOTES");
        db.execSQL("DROP TABLE IF EXISTS ACCOUNTS");
    }
}
