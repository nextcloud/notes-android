package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.niedermann.owncloud.notes.persistence.migration.Migration_10_11;
import it.niedermann.owncloud.notes.persistence.migration.Migration_11_12;
import it.niedermann.owncloud.notes.persistence.migration.Migration_12_13;
import it.niedermann.owncloud.notes.persistence.migration.Migration_13_14;
import it.niedermann.owncloud.notes.persistence.migration.Migration_14_15;
import it.niedermann.owncloud.notes.persistence.migration.Migration_15_16;
import it.niedermann.owncloud.notes.persistence.migration.Migration_16_17;
import it.niedermann.owncloud.notes.persistence.migration.Migration_17_18;
import it.niedermann.owncloud.notes.persistence.migration.Migration_18_19;
import it.niedermann.owncloud.notes.persistence.migration.Migration_4_5;
import it.niedermann.owncloud.notes.persistence.migration.Migration_5_6;
import it.niedermann.owncloud.notes.persistence.migration.Migration_6_7;
import it.niedermann.owncloud.notes.persistence.migration.Migration_7_8;
import it.niedermann.owncloud.notes.persistence.migration.Migration_8_9;
import it.niedermann.owncloud.notes.persistence.migration.Migration_9_10;
import it.niedermann.owncloud.notes.shared.util.DatabaseIndexUtil;

abstract class AbstractNotesDatabase extends SQLiteOpenHelper {

    private static final int database_version = 19;
    @NonNull
    protected final Context context;

    protected static final String database_name = "OWNCLOUD_NOTES";
    protected static final String table_notes = "NOTES";
    protected static final String table_accounts = "ACCOUNTS";
    protected static final String table_category = "CATEGORIES";
    protected static final String table_widget_single_notes = "WIDGET_SINGLE_NOTES";
    protected static final String table_widget_note_list = "WIDGET_NOTE_LISTS";

    protected static final String key_id = "ID";
    protected static final String key_url = "URL";
    protected static final String key_account_name = "ACCOUNT_NAME";
    protected static final String key_username = "USERNAME";
    protected static final String key_account_id = "ACCOUNT_ID";
    protected static final String key_note_id = "NOTE_ID";
    protected static final String key_remote_id = "REMOTEID";
    protected static final String key_status = "STATUS";
    protected static final String key_title = "TITLE";
    protected static final String key_modified = "MODIFIED";
    protected static final String key_content = "CONTENT";
    protected static final String key_excerpt = "EXCERPT";
    protected static final String key_favorite = "FAVORITE";
    protected static final String key_category = "CATEGORY";
    protected static final String key_etag = "ETAG";
    protected static final String key_capabilities_etag = "CAPABILITIES_ETAG";
    protected static final String key_color = "COLOR";
    protected static final String key_text_color = "TEXT_COLOR";
    protected static final String key_api_version = "API_VERSION";
    protected static final String key_category_id = "CATEGORY_ID";
    protected static final String key_category_title = "CATEGORY_TITLE";
    protected static final String key_category_account_id = "CATEGORY_ACCOUNT_ID";
    protected static final String key_theme_mode = "THEME_MODE";
    protected static final String key_mode = "MODE";
    protected static final String key_scroll_y = "SCROLL_Y";
    protected static final String key_category_sorting_method = "CATEGORY_SORTING_METHOD";

    protected AbstractNotesDatabase(@NonNull Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, database_version);
        this.context = context;
    }


    @NonNull
    public Context getContext() {
        return context;
    }

    /**
     * Creates initial the Database
     *
     * @param db Database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        createAccountTable(db);
        createNotesTable(db);
        createCategoryTable(db);
        createWidgetSingleNoteTable(db);
        createWidgetNoteListTable(db);
    }

    private void createNotesTable(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + table_notes + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_remote_id + " INTEGER, " +
                key_account_id + " INTEGER, " + // TODO NOT NULL
                key_status + " VARCHAR(50), " +
                key_title + " TEXT, " +
                key_modified + " INTEGER DEFAULT 0, " +
                key_content + " TEXT, " +
                key_favorite + " INTEGER DEFAULT 0, " +
                key_category + " INTEGER, " +
                key_etag + " TEXT," +
                key_excerpt + " TEXT NOT NULL DEFAULT '', " +
                key_scroll_y + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + key_category + ") REFERENCES " + table_category + "(" + key_category_id + "), " +
                "FOREIGN KEY(" + key_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "))");
        DatabaseIndexUtil.createIndex(db, table_notes, key_remote_id, key_account_id, key_status, key_favorite, key_category, key_modified);
    }

    private void createAccountTable(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + table_accounts + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_url + " TEXT, " +
                key_username + " TEXT, " +
                key_account_name + " TEXT UNIQUE, " +
                key_etag + " TEXT, " +
                key_modified + " INTEGER, " +
                key_api_version + " TEXT, " +
                key_color + " VARCHAR(6) NOT NULL DEFAULT '000000', " +
                key_text_color + " VARCHAR(6) NOT NULL DEFAULT '0082C9', " +
                key_capabilities_etag + " TEXT);");
        DatabaseIndexUtil.createIndex(db, table_accounts, key_url, key_username, key_account_name, key_etag, key_modified);
    }

    private void createCategoryTable(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + table_category + "(" +
                key_category_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_category_account_id + " INTEGER NOT NULL, " +
                key_category_title + " TEXT NOT NULL, " +
                key_category_sorting_method + " INTEGER DEFAULT 0, " +
                " UNIQUE( " + key_category_account_id + " , " + key_category_title + "), " +
                " FOREIGN KEY(" + key_category_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "));");
        DatabaseIndexUtil.createIndex(db, table_category, key_category_id, key_category_account_id, key_category_title, key_category_sorting_method);
    }

    private void createWidgetSingleNoteTable(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + table_widget_single_notes + " ( " +
                key_id + " INTEGER PRIMARY KEY, " +
                key_account_id + " INTEGER, " +
                key_note_id + " INTEGER, " +
                key_theme_mode + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + key_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "), " +
                "FOREIGN KEY(" + key_note_id + ") REFERENCES " + table_notes + "(" + key_id + "))");
    }

    private void createWidgetNoteListTable(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + table_widget_note_list + " ( " +
                key_id + " INTEGER PRIMARY KEY, " +
                key_account_id + " INTEGER, " +
                key_category_id + " INTEGER, " +
                key_mode + " INTEGER NOT NULL, " +
                key_theme_mode + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + key_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "), " +
                "FOREIGN KEY(" + key_category_id + ") REFERENCES " + table_category + "(" + key_category_id + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 0:
            case 1:
            case 2:
            case 3:
                recreateDatabase(db);
                return;
            case 4:
                new Migration_4_5(db);
            case 5:
                new Migration_5_6(db);
            case 6:
                new Migration_6_7(db);
            case 7:
                new Migration_7_8(db);
            case 8:
                new Migration_8_9(db, context, this::recreateDatabase, this::notifyWidgets);
            case 9:
                new Migration_9_10(db);
            case 10:
                new Migration_10_11(context);
            case 11:
                new Migration_11_12(db, context);
            case 12:
                new Migration_12_13(db, context);
            case 13:
                new Migration_13_14(db, context, this::notifyWidgets);
            case 14:
                new Migration_14_15(db);
            case 15:
                new Migration_15_16(db, context, this::notifyWidgets);
            case 16:
                new Migration_16_17(db);
            case 17:
                new Migration_17_18(db);
            case 18:
                new Migration_18_19(context);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        recreateDatabase(db);
    }

    private void recreateDatabase(SQLiteDatabase db) {
        DatabaseIndexUtil.dropIndexes(db);
        db.execSQL("DROP TABLE IF EXISTS " + table_notes);
        db.execSQL("DROP TABLE IF EXISTS " + table_accounts);
        db.execSQL("DROP TABLE IF EXISTS " + table_category);
        onCreate(db);
    }

    protected abstract void notifyWidgets();
}
