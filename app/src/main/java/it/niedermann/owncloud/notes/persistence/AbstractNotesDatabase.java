package it.niedermann.owncloud.notes.persistence;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

import it.niedermann.owncloud.notes.android.appwidget.NoteListWidget;
import it.niedermann.owncloud.notes.android.appwidget.SingleNoteWidget;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.util.DatabaseIndexUtil;
import it.niedermann.owncloud.notes.util.NoteUtil;

// Protected APIs
@SuppressWarnings("WeakerAccess")
abstract class AbstractNotesDatabase extends SQLiteOpenHelper {

    private static final String TAG = AbstractNotesDatabase.class.getSimpleName();

    private static final int database_version = 10;
    private final Context context;

    protected static final String database_name = "OWNCLOUD_NOTES";
    protected static final String table_notes = "NOTES";
    protected static final String table_accounts = "ACCOUNTS";

    protected static final String key_id = "ID";

    protected static final String key_url = "URL";
    protected static final String key_account_name = "ACCOUNT_NAME";
    protected static final String key_username = "USERNAME";

    protected static final String key_account_id = "ACCOUNT_ID";
    protected static final String key_remote_id = "REMOTEID";
    protected static final String key_status = "STATUS";
    protected static final String key_title = "TITLE";
    protected static final String key_modified = "MODIFIED";
    protected static final String key_content = "CONTENT";
    protected static final String key_excerpt = "EXCERPT";
    protected static final String key_favorite = "FAVORITE";
    protected static final String key_category = "CATEGORY";
    protected static final String key_etag = "ETAG";

    protected AbstractNotesDatabase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory, database_version);
        this.context = context;
    }


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
    }

    private void createNotesTable(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + table_notes + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_remote_id + " INTEGER, " +
                key_account_id + " INTEGER, " +
                key_status + " VARCHAR(50), " +
                key_title + " TEXT, " +
                key_modified + " INTEGER DEFAULT 0, " +
                key_content + " TEXT, " +
                key_favorite + " INTEGER DEFAULT 0, " +
                key_category + " TEXT NOT NULL DEFAULT '', " +
                key_etag + " TEXT," +
                key_excerpt + " TEXT NOT NULL DEFAULT '', " +
                "FOREIGN KEY(" + key_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "))");
        createNotesIndexes(db);
    }

    private void createAccountTable(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + table_accounts + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_url + " TEXT, " +
                key_username + " TEXT, " +
                key_account_name + " TEXT UNIQUE, " +
                key_etag + " TEXT, " +
                key_modified + " INTEGER)");
        createAccountIndexes(db);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            recreateDatabase(db);
            return;
        }
        if (oldVersion < 4) {
            db.delete(table_notes, null, null);
            db.delete(table_accounts, null, null);
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_remote_id + " INTEGER");
            db.execSQL("UPDATE " + table_notes + " SET " + key_remote_id + "=" + key_id + " WHERE (" + key_remote_id + " IS NULL OR " + key_remote_id + "=0) AND " + key_status + "!=?", new String[]{DBStatus.LOCAL_CREATED.getTitle()});
            db.execSQL("UPDATE " + table_notes + " SET " + key_remote_id + "=0, " + key_status + "=? WHERE " + key_status + "=?", new String[]{DBStatus.LOCAL_EDITED.getTitle(), DBStatus.LOCAL_CREATED.getTitle()});
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_favorite + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 7) {
            DatabaseIndexUtil.dropIndexes(db);
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_category + " TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_etag + " TEXT");
            DatabaseIndexUtil.createIndex(db, table_notes, key_remote_id, key_status, key_favorite, key_category, key_modified);
        }
        if (oldVersion < 8) {
            final String table_temp = "NOTES_TEMP";
            db.execSQL("CREATE TABLE " + table_temp + " ( " +
                    key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    key_remote_id + " INTEGER, " +
                    key_status + " VARCHAR(50), " +
                    key_title + " TEXT, " +
                    key_modified + " INTEGER DEFAULT 0, " +
                    key_content + " TEXT, " +
                    key_favorite + " INTEGER DEFAULT 0, " +
                    key_category + " TEXT NOT NULL DEFAULT '', " +
                    key_etag + " TEXT)");
            DatabaseIndexUtil.createIndex(db, table_temp, key_remote_id, key_status, key_favorite, key_category, key_modified);
            db.execSQL(String.format("INSERT INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s) ", table_temp, key_id, key_remote_id, key_status, key_title, key_modified, key_content, key_favorite, key_category, key_etag)
                    + String.format("SELECT %s,%s,%s,%s,strftime('%%s',%s),%s,%s,%s,%s FROM %s", key_id, key_remote_id, key_status, key_title, key_modified, key_content, key_favorite, key_category, key_etag, table_notes));
            db.execSQL(String.format("DROP TABLE %s", table_notes));
            db.execSQL(String.format("ALTER TABLE %s RENAME TO %s", table_temp, table_notes));
        }
        if (oldVersion < 9) {
            // Create accounts table
            createAccountTable(db);

            // Add accountId to notes table
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_account_id + " INTEGER NOT NULL DEFAULT 0");
            DatabaseIndexUtil.createIndex(db, table_notes, key_account_id);

            // Migrate existing account from SharedPreferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String username = sharedPreferences.getString("settingsUsername", "");
            String url = sharedPreferences.getString("settingsUrl", "");
            if (!url.isEmpty() && url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
                try {
                    String accountName = username + "@" + new URL(url).getHost();

                    ContentValues migratedAccountValues = new ContentValues();
                    migratedAccountValues.put(key_url, url);
                    migratedAccountValues.put(key_username, username);
                    migratedAccountValues.put(key_account_name, accountName);
                    db.insert(table_accounts, null, migratedAccountValues);

                    // After successful insertion of migrated account, set accountId to 1 in each note
                    ContentValues values = new ContentValues();
                    values.put(key_account_id, 1);
                    db.update(table_notes, values, key_account_id + " = ?", new String[]{"NULL"});

                    // Add FOREIGN_KEY constraint
                    final String table_temp = "NOTES_TEMP";
                    db.execSQL(String.format("ALTER TABLE %s RENAME TO %s", table_notes, table_temp));

                    db.execSQL("CREATE TABLE " + table_notes + " ( " +
                            key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            key_remote_id + " INTEGER, " +
                            key_account_id + " INTEGER, " +
                            key_status + " VARCHAR(50), " +
                            key_title + " TEXT, " +
                            key_modified + " INTEGER DEFAULT 0, " +
                            key_content + " TEXT, " +
                            key_favorite + " INTEGER DEFAULT 0, " +
                            key_category + " TEXT NOT NULL DEFAULT '', " +
                            key_etag + " TEXT," +
                            "FOREIGN KEY(" + key_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "))");
                    DatabaseIndexUtil.createIndex(db, table_notes, key_remote_id, key_account_id, key_status, key_favorite, key_category, key_modified);

                    db.execSQL(String.format("INSERT INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) ", table_notes, key_id, key_account_id, key_remote_id, key_status, key_title, key_modified, key_content, key_favorite, key_category, key_etag)
                            + String.format("SELECT %s,%s,%s,%s,%s,%s,%s,%s,%s,%s FROM %s", key_id, values.get(key_account_id), key_remote_id, key_status, key_title, key_modified, key_content, key_favorite, key_category, key_etag, table_temp));
                    db.execSQL(String.format("DROP TABLE %s;", table_temp));

                    AppWidgetManager awm = AppWidgetManager.getInstance(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    // Add accountId '1' to any existing (and configured) appwidgets
                    int[] appWidgetIdsNLW = awm.getAppWidgetIds(new ComponentName(context, NoteListWidget.class));
                    int[] appWidgetIdsSNW = awm.getAppWidgetIds(new ComponentName(context, SingleNoteWidget.class));

                    for (int appWidgetId : appWidgetIdsNLW) {
                        if (sharedPreferences.getInt(NoteListWidget.WIDGET_MODE_KEY + appWidgetId, -1) >= 0) {
                            editor.putLong(NoteListWidget.ACCOUNT_ID_KEY + appWidgetId, 1);
                        }
                    }

                    for (int appWidgetId : appWidgetIdsSNW) {
                        if (sharedPreferences.getLong(SingleNoteWidget.WIDGET_KEY + appWidgetId, -1) >= 0) {
                            editor.putLong(SingleNoteWidget.ACCOUNT_ID_KEY + appWidgetId, 1);
                        }
                    }

                    notifyNotesChanged();

                    // Clean up no longer needed SharedPreferences
                    editor.remove("notes_last_etag");
                    editor.remove("notes_last_modified");
                    editor.remove("settingsUrl");
                    editor.remove("settingsUsername");
                    editor.remove("settingsPassword");
                    editor.apply();
                } catch (MalformedURLException e) {
                    Log.e(TAG, "Previous URL could not be parsed. Recreating database...");
                    e.printStackTrace();
                    recreateDatabase(db);
                    return;
                }
            } else {
                Log.e(TAG, "Previous URL is empty or does not end with a '/' character. Recreating database...");
                recreateDatabase(db);
                return;
            }
        }
        if (oldVersion < 10) {
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_excerpt + " INTEGER NOT NULL DEFAULT ''");
            Cursor cursor = db.query(table_notes, new String[]{key_id, key_content}, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put(key_excerpt, NoteUtil.generateNoteExcerpt(cursor.getString(1)));
                db.update(table_notes, values, key_id + " = ? ", new String[]{cursor.getString(0)});
            }
            cursor.close();
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
        onCreate(db);
    }

    private static void createNotesIndexes(@NonNull SQLiteDatabase db) {
        DatabaseIndexUtil.createIndex(db, table_notes, key_remote_id, key_account_id, key_status, key_favorite, key_category, key_modified);
    }

    private static void createAccountIndexes(@NonNull SQLiteDatabase db) {
        DatabaseIndexUtil.createIndex(db, table_accounts, key_url, key_username, key_account_name, key_etag, key_modified);
    }

    protected abstract void notifyNotesChanged();
}
