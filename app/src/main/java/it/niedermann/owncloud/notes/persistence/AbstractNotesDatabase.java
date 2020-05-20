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
import androidx.work.WorkManager;

import java.net.MalformedURLException;
import java.net.URL;
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

    private static final String TAG = AbstractNotesDatabase.class.getSimpleName();

    private static final int database_version = 13;
    @NonNull
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
    protected static final String key_capabilities_etag = "CAPABILITIES_ETAG";
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
                key_capabilities_etag + " TEXT)");
        DatabaseIndexUtil.createIndex(db, table_accounts, key_url, key_username, key_account_name, key_etag, key_modified);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            recreateDatabase(db);
            return;
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE NOTES ADD COLUMN REMOTEID INTEGER");
            db.execSQL("UPDATE NOTES SET REMOTEID=ID WHERE (REMOTEID IS NULL OR REMOTEID=0) AND STATUS!=?", new String[]{"LOCAL_CREATED"});
            db.execSQL("UPDATE NOTES SET REMOTEID=0, STATUS=? WHERE STATUS=?", new String[]{DBStatus.LOCAL_EDITED.getTitle(), "LOCAL_CREATED"});
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE NOTES ADD COLUMN FAVORITE INTEGER DEFAULT 0");
        }
        if (oldVersion < 7) {
            DatabaseIndexUtil.dropIndexes(db);
            db.execSQL("ALTER TABLE NOTES ADD COLUMN CATEGORY TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE NOTES ADD COLUMN ETAG TEXT");
            DatabaseIndexUtil.createIndex(db, "NOTES", "REMOTEID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");
        }
        if (oldVersion < 8) {
            final String table_temp = "NOTES_TEMP";
            db.execSQL("CREATE TABLE " + table_temp + " ( " +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "REMOTEID INTEGER, " +
                    "STATUS VARCHAR(50), " +
                    "TITLE TEXT, " +
                    "MODIFIED INTEGER DEFAULT 0, " +
                    "CONTENT TEXT, " +
                    "FAVORITE INTEGER DEFAULT 0, " +
                    "CATEGORY TEXT NOT NULL DEFAULT '', " +
                    "ETAG TEXT)");
            DatabaseIndexUtil.createIndex(db, table_temp, "REMOTEID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");
            db.execSQL(String.format("INSERT INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s) ", table_temp, "ID", "REMOTEID", "STATUS", "TITLE", "MODIFIED", "CONTENT", "FAVORITE", "CATEGORY", "ETAG")
                    + String.format("SELECT %s,%s,%s,%s,strftime('%%s',%s),%s,%s,%s,%s FROM %s", "ID", "REMOTEID", "STATUS", "TITLE", "MODIFIED", "CONTENT", "FAVORITE", "CATEGORY", "ETAG", "NOTES"));
            db.execSQL("DROP TABLE NOTES");
            db.execSQL(String.format("ALTER TABLE %s RENAME TO %s", table_temp, "NOTES"));
        }
        if (oldVersion < 9) {
            // Create accounts table
            db.execSQL("CREATE TABLE ACCOUNTS ( " +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "URL TEXT, " +
                    "USERNAME TEXT, " +
                    "ACCOUNT_NAME TEXT UNIQUE, " +
                    "ETAG TEXT, " +
                    "MODIFIED INTEGER)");
            DatabaseIndexUtil.createIndex(db, "ACCOUNTS", "URL", "USERNAME", "ACCOUNT_NAME", "ETAG", "MODIFIED");

            // Add accountId to notes table
            db.execSQL("ALTER TABLE NOTES ADD COLUMN ACCOUNT_ID INTEGER NOT NULL DEFAULT 0");
            DatabaseIndexUtil.createIndex(db, "NOTES", "ACCOUNT_ID");

            // Migrate existing account from SharedPreferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String username = sharedPreferences.getString("settingsUsername", "");
            String url = sharedPreferences.getString("settingsUrl", "");
            if (!url.isEmpty() && url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
                try {
                    String accountName = username + "@" + new URL(url).getHost();

                    ContentValues migratedAccountValues = new ContentValues();
                    migratedAccountValues.put("URL", url);
                    migratedAccountValues.put("USERNAME", username);
                    migratedAccountValues.put("ACCOUNT_NAME", accountName);
                    db.insert("ACCOUNTS", null, migratedAccountValues);

                    // After successful insertion of migrated account, set accountId to 1 in each note
                    ContentValues values = new ContentValues();
                    values.put("ACCOUNT_ID", 1);
                    db.update("NOTES", values, "ACCOUNT_ID = ?", new String[]{"NULL"});

                    // Add FOREIGN_KEY constraint
                    final String table_temp = "NOTES_TEMP";
                    db.execSQL(String.format("ALTER TABLE %s RENAME TO %s", "NOTES", table_temp));

                    db.execSQL("CREATE TABLE NOTES ( " +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "REMOTEID INTEGER, " +
                            "ACCOUNT_ID INTEGER, " +
                            "STATUS VARCHAR(50), " +
                            "TITLE TEXT, " +
                            "MODIFIED INTEGER DEFAULT 0, " +
                            "CONTENT TEXT, " +
                            "FAVORITE INTEGER DEFAULT 0, " +
                            "CATEGORY TEXT NOT NULL DEFAULT '', " +
                            "ETAG TEXT," +
                            "FOREIGN KEY(ACCOUNT_ID) REFERENCES ACCOUNTS(ID))");
                    DatabaseIndexUtil.createIndex(db, "NOTES", "REMOTEID", "ACCOUNT_ID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");

                    db.execSQL(String.format("INSERT INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) ", "NOTES", "ID", "ACCOUNT_ID", "REMOTEID", "STATUS", "TITLE", "MODIFIED", "CONTENT", "FAVORITE", "CATEGORY", "ETAG")
                            + String.format("SELECT %s,%s,%s,%s,%s,%s,%s,%s,%s,%s FROM %s", "ID", values.get("ACCOUNT_ID"), "REMOTEID", "STATUS", "TITLE", "MODIFIED", "CONTENT", "FAVORITE", "CATEGORY", "ETAG", table_temp));
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
            db.execSQL("ALTER TABLE NOTES ADD COLUMN EXCERPT INTEGER NOT NULL DEFAULT ''");
            Cursor cursor = db.query("NOTES", new String[]{"ID", "CONTENT"}, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put("EXCERPT", NoteUtil.generateNoteExcerpt(cursor.getString(1)));
                db.update("NOTES", values, "ID" + " = ? ", new String[]{cursor.getString(0)});
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
            db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN API_VERSION TEXT");
            db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN COLOR VARCHAR(6) NOT NULL DEFAULT '000000'");
            db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN TEXT_COLOR VARCHAR(6) NOT NULL DEFAULT '0082C9'");
            CapabilitiesWorker.update(context);
        }
        if (oldVersion < 13) {
            db.execSQL("ALTER TABLE ACCOUNTS ADD COLUMN CAPABILITIES_ETAG TEXT");
            WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork("it.niedermann.owncloud.notes.persistence.SyncWorker");
            WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork("SyncWorker");
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

    protected abstract void notifyNotesChanged();
}
