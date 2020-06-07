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
    private static final String TAG = AbstractNotesDatabase.class.getSimpleName();

    private static final int database_version = 16;
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
                " UNIQUE( " + key_category_account_id + " , " + key_category_title + "), " +
                " FOREIGN KEY(" + key_category_account_id + ") REFERENCES " + table_accounts + "(" + key_id + "));");
        DatabaseIndexUtil.createIndex(db, table_category, key_category_id, key_category_account_id, key_category_title);
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

                    final String WIDGET_MODE_KEY = "NLW_mode";
                    final String ACCOUNT_ID_KEY = "NLW_account";

                    for (int appWidgetId : appWidgetIdsNLW) {
                        if (sharedPreferences.getInt(WIDGET_MODE_KEY + appWidgetId, -1) >= 0) {
                            editor.putLong(ACCOUNT_ID_KEY + appWidgetId, 1);
                        }
                    }

                    for (int appWidgetId : appWidgetIdsSNW) {
                        if (sharedPreferences.getLong("single_note_widget" + appWidgetId, -1) >= 0) {
                            editor.putLong("SNW_accountId" + appWidgetId, 1);
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
            Cursor cursor = db.query("NOTES", new String[]{"ID", "CONTENT", "TITLE"}, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put("EXCERPT", NoteUtil.generateNoteExcerpt(cursor.getString(1), cursor.getString(2)));
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
                final String DARK_THEME_KEY = "NLW_darkTheme";
                if ("darkTheme".equals(key) || key.startsWith(DARK_THEME_KEY) || key.startsWith("SNW_darkTheme")) {
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
        if (oldVersion < 14) {
            // #754 Move single note widget preferences to database
            db.execSQL("CREATE TABLE WIDGET_SINGLE_NOTES ( " +
                    "ID INTEGER PRIMARY KEY, " +
                    "ACCOUNT_ID INTEGER, " +
                    "NOTE_ID INTEGER, " +
                    "THEME_MODE INTEGER NOT NULL, " +
                    "FOREIGN KEY(ACCOUNT_ID) REFERENCES ACCOUNTS(ID), " +
                    "FOREIGN KEY(NOTE_ID) REFERENCES NOTES(ID))");

            final String SP_WIDGET_KEY = "single_note_widget";
            final String SP_ACCOUNT_ID_KEY = "SNW_accountId";
            final String SP_DARK_THEME_KEY = "SNW_darkTheme";
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Map<String, ?> prefs = sharedPreferences.getAll();
            for (Map.Entry<String, ?> pref : prefs.entrySet()) {
                final String key = pref.getKey();
                Integer widgetId = null;
                Long noteId = null;
                Long accountId = null;
                Integer themeMode = null;
                if (key != null && key.startsWith(SP_WIDGET_KEY)) {
                    try {
                        widgetId = Integer.parseInt(key.substring(SP_WIDGET_KEY.length()));
                        noteId = (Long) pref.getValue();
                        accountId = sharedPreferences.getLong(SP_ACCOUNT_ID_KEY + widgetId, -1);

                        try {
                            themeMode = DarkModeSetting.valueOf(sharedPreferences.getString(SP_DARK_THEME_KEY + widgetId, DarkModeSetting.SYSTEM_DEFAULT.name())).getModeId();
                        } catch (ClassCastException e) {
                            //DARK_THEME was a boolean in older versions of the app. We thereofre have to still support the old setting.
                            themeMode = sharedPreferences.getBoolean(SP_DARK_THEME_KEY + widgetId, false) ? DarkModeSetting.DARK.getModeId() : DarkModeSetting.LIGHT.getModeId();
                        }

                        ContentValues migratedWidgetValues = new ContentValues();
                        migratedWidgetValues.put("ID", widgetId);
                        migratedWidgetValues.put("ACCOUNT_ID", accountId);
                        migratedWidgetValues.put("NOTE_ID", noteId);
                        migratedWidgetValues.put("THEME_MODE", themeMode);
                        db.insert("WIDGET_SINGLE_NOTES", null, migratedWidgetValues);
                    } catch (Throwable t) {
                        Log.e(TAG, "Could not migrate widget {widgetId: " + widgetId + ", accountId: " + accountId + ", noteId: " + noteId + ", themeMode: " + themeMode + "}");
                        t.printStackTrace();
                    } finally {
                        // Clean up old shared preferences
                        editor.remove(SP_WIDGET_KEY + widgetId);
                        editor.remove(SP_DARK_THEME_KEY + widgetId);
                        editor.remove(SP_ACCOUNT_ID_KEY + widgetId);
                    }
                }
            }
            editor.apply();
            notifyNotesChanged();
        }
        if (oldVersion < 15) {
            // #814 normalize database (move category from string field to own table)
            // Rename a tmp_NOTES table.
            String tmpTableNotes = String.format("tmp_%s", "NOTES");
            db.execSQL("ALTER TABLE NOTES RENAME TO " + tmpTableNotes);
            db.execSQL("CREATE TABLE NOTES ( " +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "REMOTEID INTEGER, " +
                    "ACCOUNT_ID INTEGER, " +
                    "STATUS VARCHAR(50), " +
                    "TITLE TEXT, " +
                    "MODIFIED INTEGER DEFAULT 0, " +
                    "CONTENT TEXT, " +
                    "FAVORITE INTEGER DEFAULT 0, " +
                    "CATEGORY INTEGER, " +
                    "ETAG TEXT," +
                    "EXCERPT TEXT NOT NULL DEFAULT '', " +
                    "FOREIGN KEY(CATEGORY) REFERENCES CATEGORIES(CATEGORY_ID), " +
                    "FOREIGN KEY(ACCOUNT_ID) REFERENCES ACCOUNTS(ID))");
            DatabaseIndexUtil.createIndex(db, "NOTES", "REMOTEID", "ACCOUNT_ID", "STATUS", "FAVORITE", "CATEGORY", "MODIFIED");
            db.execSQL("CREATE TABLE CATEGORIES(" +
                    "CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "CATEGORY_ACCOUNT_ID INTEGER NOT NULL, " +
                    "CATEGORY_TITLE TEXT NOT NULL, " +
                    "UNIQUE( CATEGORY_ACCOUNT_ID , CATEGORY_TITLE), " +
                    "FOREIGN KEY(CATEGORY_ACCOUNT_ID) REFERENCES ACCOUNTS(ID))");
            DatabaseIndexUtil.createIndex(db, "CATEGORIES", "CATEGORY_ID", "CATEGORY_ACCOUNT_ID", "CATEGORY_TITLE");
            // A hashtable storing categoryTitle - categoryId Mapping
            // This is used to prevent too many searches in database
            Hashtable<String, Integer> categoryTitleIdMap = new Hashtable<>();
            int id = 1;
            Cursor tmpNotesCursor = db.rawQuery("SELECT * FROM " + tmpTableNotes, null);
            while (tmpNotesCursor.moveToNext()) {
                String categoryTitle = tmpNotesCursor.getString(8);
                int accountId = tmpNotesCursor.getInt(2);
                Log.e("###", accountId + "");
                Integer categoryId;
                if (categoryTitleIdMap.containsKey(categoryTitle) && categoryTitleIdMap.get(categoryTitle) != null) {
                    categoryId = categoryTitleIdMap.get(categoryTitle);
                } else {
                    // The category does not exists in the database, create it.
                    categoryId = id++;
                    ContentValues values = new ContentValues();
                    values.put("CATEGORY_ID", categoryId);
                    values.put("CATEGORY_ACCOUNT_ID", accountId);
                    values.put("CATEGORY_TITLE", categoryTitle);
                    db.insert("CATEGORIES", null, values);
                    categoryTitleIdMap.put(categoryTitle, categoryId);
                }
                // Move the data in tmp_NOTES to NOTES
                ContentValues values = new ContentValues();
                values.put("ID", tmpNotesCursor.getInt(0));
                values.put("REMOTEID", tmpNotesCursor.getInt(1));
                values.put("ACCOUNT_ID", tmpNotesCursor.getInt(2));
                values.put("STATUS", tmpNotesCursor.getString(3));
                values.put("TITLE", tmpNotesCursor.getString(4));
                values.put("MODIFIED", tmpNotesCursor.getLong(5));
                values.put("CONTENT", tmpNotesCursor.getString(6));
                values.put("FAVORITE", tmpNotesCursor.getInt(7));
                values.put("CATEGORY", categoryId);
                values.put("ETAG", tmpNotesCursor.getString(9));
                values.put("EXCERPT", tmpNotesCursor.getString(10));
                db.insert("NOTES", null, values);
            }
            tmpNotesCursor.close();
            db.execSQL("DROP TABLE IF EXISTS " + tmpTableNotes);
        }
        if (oldVersion < 16) {
            // #832 Move note list widget preferences to database
            db.execSQL("CREATE TABLE WIDGET_NOTE_LISTS ( " +
                    "ID INTEGER PRIMARY KEY, " +
                    "ACCOUNT_ID INTEGER, " +
                    "CATEGORY_ID INTEGER, " +
                    "MODE INTEGER NOT NULL, " +
                    "THEME_MODE INTEGER NOT NULL, " +
                    "FOREIGN KEY(ACCOUNT_ID) REFERENCES ACCOUNTS(ID), " +
                    "FOREIGN KEY(CATEGORY_ID) REFERENCES CATEGORIES(CATEGORY_ID))");

            final String SP_WIDGET_KEY = "NLW_mode";
            final String SP_ACCOUNT_ID_KEY = "NLW_account";
            final String SP_DARK_THEME_KEY = "NLW_darkTheme";
            final String SP_CATEGORY_KEY = "NLW_cat";

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Map<String, ?> prefs = sharedPreferences.getAll();
            for (Map.Entry<String, ?> pref : prefs.entrySet()) {
                final String key = pref.getKey();
                Integer widgetId = null;
                Integer mode = null;
                Long accountId = null;
                Integer themeMode = null;
                Integer categoryId = null;
                if (key != null && key.startsWith(SP_WIDGET_KEY)) {
                    try {
                        widgetId = Integer.parseInt(key.substring(SP_WIDGET_KEY.length()));
                        mode = (Integer) pref.getValue();
                        accountId = sharedPreferences.getLong(SP_ACCOUNT_ID_KEY + widgetId, -1);

                        try {
                            themeMode = DarkModeSetting.valueOf(sharedPreferences.getString(SP_DARK_THEME_KEY + widgetId, DarkModeSetting.SYSTEM_DEFAULT.name())).getModeId();
                        } catch (ClassCastException e) {
                            //DARK_THEME was a boolean in older versions of the app. We thereofre have to still support the old setting.
                            themeMode = sharedPreferences.getBoolean(SP_DARK_THEME_KEY + widgetId, false) ? DarkModeSetting.DARK.getModeId() : DarkModeSetting.LIGHT.getModeId();
                        }

                        if (mode == 2) {
                            final String categoryTitle = sharedPreferences.getString(SP_CATEGORY_KEY + widgetId, null);
                            Cursor cursor = db.query(
                                    table_category,
                                    new String[]{key_category_id},
                                    key_category_title + " = ? AND " + key_category_account_id + " = ? ",
                                    new String[]{categoryTitle, String.valueOf(accountId)},
                                    null,
                                    null,
                                    null);
                            if (cursor.moveToNext()) {
                                categoryId = cursor.getInt(0);
                            } else {
                                throw new IllegalStateException("No category id found for title \"" + categoryTitle + "\"");
                            }
                            cursor.close();
                        }

                        ContentValues migratedWidgetValues = new ContentValues();
                        migratedWidgetValues.put("ID", widgetId);
                        migratedWidgetValues.put("ACCOUNT_ID", accountId);
                        migratedWidgetValues.put("CATEGORY_ID", categoryId);
                        migratedWidgetValues.put("MODE", mode);
                        migratedWidgetValues.put("THEME_MODE", themeMode);
                        db.insert("WIDGET_NOTE_LISTS", null, migratedWidgetValues);
                    } catch (Throwable t) {
                        Log.e(TAG, "Could not migrate widget {widgetId: " + widgetId + ", accountId: " + accountId + ", mode: " + mode + ", categoryId: " + categoryId + ", themeMode: " + themeMode + "}");
                        t.printStackTrace();
                    } finally {
                        // Clean up old shared preferences
                        editor.remove(SP_WIDGET_KEY + widgetId);
                        editor.remove(SP_CATEGORY_KEY + widgetId);
                        editor.remove(SP_DARK_THEME_KEY + widgetId);
                        editor.remove(SP_ACCOUNT_ID_KEY + widgetId);
                    }
                }
            }
            editor.apply();
            notifyNotesChanged();
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


    protected abstract void notifyNotesChanged();
}
