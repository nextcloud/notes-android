package it.niedermann.owncloud.notes.persistence.migration;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.preference.PreferenceManager;

import java.net.MalformedURLException;
import java.net.URL;

import it.niedermann.owncloud.notes.widget.notelist.NoteListWidget;
import it.niedermann.owncloud.notes.widget.singlenote.SingleNoteWidget;
import it.niedermann.owncloud.notes.shared.util.DatabaseIndexUtil;

public class Migration_8_9 {

    private static final String TAG = Migration_8_9.class.getSimpleName();

    /**
     * Adds an account table for multi account usage in combination with SingleSignOn
     */
    public Migration_8_9(@NonNull SQLiteDatabase db, @NonNull Context context, @NonNull Consumer<SQLiteDatabase> recreateDatabase, @NonNull Runnable notifyWidgets) {
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

                notifyWidgets.run();

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
                recreateDatabase.accept(db);
            }
        } else {
            Log.e(TAG, "Previous URL is empty or does not end with a '/' character. Recreating database...");
            recreateDatabase.accept(db);
        }
    }
}
