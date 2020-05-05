package it.niedermann.owncloud.notes.persistence;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import it.niedermann.owncloud.notes.android.DarkModeSetting;
import it.niedermann.owncloud.notes.android.appwidget.NoteListWidget;
import it.niedermann.owncloud.notes.android.appwidget.SingleNoteWidget;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.util.DatabaseIndexUtil;
import it.niedermann.owncloud.notes.util.NoteUtil;

// Protected APIs
@SuppressWarnings("WeakerAccess")
abstract class AbstractNotesDatabase extends SQLiteOpenHelper {

    // TODO : delete after testing
    // ------------------- just for testing -------------------
    public static final boolean testFlag = true;
    // ------------------ just for testing --------------------

    private static final String TAG = AbstractNotesDatabase.class.getSimpleName();

    private static final int database_version = 13;
    @NonNull
    private final Context context;

    protected static final String database_name = "OWNCLOUD_NOTES";
    protected static final String table_notes = "NOTES";
    protected static final String table_accounts = "ACCOUNTS";
    protected static final String table_category = "CATEGORIES";

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
    protected static final String key_color = "COLOR";
    protected static final String key_text_color = "TEXT_COLOR";
    protected static final String key_api_version = "API_VERSION";

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
    }

    private void createNotesTable(@NonNull SQLiteDatabase db) {
        // TODO: category ID foreign key
        db.execSQL("CREATE TABLE " + table_notes + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_remote_id + " INTEGER, " +
                key_account_id + " INTEGER, " +
                key_status + " VARCHAR(50), " +
                key_title + " TEXT, " +
                key_modified + " INTEGER DEFAULT 0, " +
                key_content + " TEXT, " +
                key_favorite + " INTEGER DEFAULT 0, " +
                key_category + " INTEGER, " +
                key_etag + " TEXT," +
                key_excerpt + " TEXT NOT NULL DEFAULT '', " +
                "FOREIGN KEY(" + key_category + ") REFERENCES " + table_category + "(" + key_id + "), " +
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
                key_modified + " INTEGER, " +
                key_api_version + " TEXT, " +
                key_color + " VARCHAR(6) NOT NULL DEFAULT '000000', " +
                key_text_color + " VARCHAR(6) NOT NULL DEFAULT '0082C9')");
        createAccountIndexes(db);
    }

    private void createCategoryTable(@NonNull SQLiteDatabase db) {
        // TODO: category ID account id
        db.execSQL("CREATE TABLE " + table_category + "(" +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_account_id + " INTEGER, " +
                key_title + " TEXT )");
        createCategoryIndexes(db);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO : delete after testing
        // ----------------- just for testing --------------------------
//        if (testFlag) {
//            recreateDatabase(db);
//            return;
//        }
        // ---------------- just for testing - end ---------------------
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
        if (oldVersion < 11) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Map<String, ?> prefs = sharedPreferences.getAll();
            for (Map.Entry<String, ?> pref : prefs.entrySet()) {
                String key = pref.getKey();
                if ("darkTheme".equals(key) || key.startsWith(NoteListWidget.DARK_THEME_KEY) || key.startsWith(SingleNoteWidget.DARK_THEME_KEY)) {
                    Boolean darkTheme = (Boolean) pref.getValue();
                    editor.putString(pref.getKey(), darkTheme ? DarkModeSetting.DARK.name() : DarkModeSetting.LIGHT.name());
                }
            }
            editor.apply();
        }
        if (oldVersion < 12) {
            db.execSQL("ALTER TABLE " + table_accounts + " ADD COLUMN " + key_api_version + " TEXT");
            db.execSQL("ALTER TABLE " + table_accounts + " ADD COLUMN " + key_color + " VARCHAR(6) NOT NULL DEFAULT '000000'");
            db.execSQL("ALTER TABLE " + table_accounts + " ADD COLUMN " + key_text_color + " VARCHAR(6) NOT NULL DEFAULT '0082C9'");
            CapabilitiesWorker.update(context);
        }
        if (oldVersion < 13) {
            String tmpTableNotes = String.format("tmp_%s", table_notes);
            db.execSQL("ALTER TABLE " + table_notes + " RENAME TO " + tmpTableNotes);
            createNotesTable(db);
            createCategoryTable(db);
            Hashtable<String, Integer> categoryTitleIdMap = new Hashtable<>();
            int id = 1;
            Cursor tmpNotesCursor = db.rawQuery("SELECT * FROM " + tmpTableNotes, null);
            while (tmpNotesCursor.moveToNext()) {
                String categoryTitle = tmpNotesCursor.getString(8);
                int accountId = tmpNotesCursor.getInt(2);
                int categoryId = 0;
                if (categoryTitleIdMap.containsKey(categoryTitle) && categoryTitleIdMap.get(categoryTitle) != null) {
                    categoryId = categoryTitleIdMap.get(categoryTitle);
                } else {
                    categoryId = id++;
                    ContentValues values = new ContentValues();
                    values.put(key_id, categoryId);
                    values.put(key_account_id, accountId);
                    values.put(key_title, categoryTitle);
                    db.insert(table_category, null, values);
//                    if (categoryTitle.trim().equals("")) {
//                        db.execSQL("INSERT INTO " + table_category + " VALUES ( " + categoryId + " , " + accountId + ", EMPTY) ");
//                    } else {
//                        db.execSQL("INSERT INTO " + table_category + " VALUES ( " + categoryId + " , " + accountId + " , '" + categoryTitle + "' ) ");
//                    }
                    categoryTitleIdMap.put(categoryTitle, categoryId);
                }
                ContentValues values = new ContentValues();
                values.put(key_id, tmpNotesCursor.getInt(0));
                values.put(key_remote_id, tmpNotesCursor.getInt(1));
                values.put(key_account_id, tmpNotesCursor.getInt(2));
                values.put(key_status, tmpNotesCursor.getString(3));
                values.put(key_title, tmpNotesCursor.getString(4));
                values.put(key_modified, tmpNotesCursor.getLong(5));
                values.put(key_content, tmpNotesCursor.getString(6));
                values.put(key_favorite, tmpNotesCursor.getInt(7));
                values.put(key_category, categoryId);
                values.put(key_etag, tmpNotesCursor.getString(9));
                values.put(key_etag, tmpNotesCursor.getString(10));
                db.insert(table_notes, null, values);
//                String values = String.format("%d, %d, %d, '%s', '%s', %d, '%s', %d, %d, ",// %s, %s",
//                        tmpNotesCursor.getInt(0), tmpNotesCursor.getInt(1), tmpNotesCursor.getInt(2),
//                        tmpNotesCursor.getString(3), tmpNotesCursor.getString(4), tmpNotesCursor.getInt(5),
//                        tmpNotesCursor.getString(6), tmpNotesCursor.getInt(7), categoryId);
//                if (tmpNotesCursor.getString(9) == null) {
//                    values = values + "null, ";
//                }
//                if (tmpNotesCursor.getString(10).trim().equals("")) {
//                    values = values + "''";
//                }
//                Log.e("###", values + " " + categoryTitle);
//                db.execSQL("INSERT INTO " + table_notes + " VALUES ( " + values + " ) ");
            }
            tmpNotesCursor.close();
            db.execSQL("DROP TABLE IF EXISTS " + tmpTableNotes);
            createCategoryIndexes(db);
            createNotesIndexes(db);
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

    private static void createNotesIndexes(@NonNull SQLiteDatabase db) {
        DatabaseIndexUtil.createIndex(db, table_notes, key_remote_id, key_account_id, key_status, key_favorite, key_category, key_modified);
    }

    private static void createAccountIndexes(@NonNull SQLiteDatabase db) {
        DatabaseIndexUtil.createIndex(db, table_accounts, key_url, key_username, key_account_name, key_etag, key_modified);
    }

    private static void createCategoryIndexes(@NonNull SQLiteDatabase db) {
        DatabaseIndexUtil.createIndex(db, table_category, key_id, key_account_id, key_title);
    }

    protected abstract void notifyNotesChanged();
}
