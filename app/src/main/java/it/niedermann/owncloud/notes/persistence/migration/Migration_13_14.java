package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.util.Map;

import it.niedermann.owncloud.notes.preferences.DarkModeSetting;

public class Migration_13_14 {

    private static final String TAG = Migration_13_14.class.getSimpleName();

    /**
     * Move single note widget preferences to database
     * https://github.com/stefan-niedermann/nextcloud-notes/issues/754
     */
    public Migration_13_14(@NonNull SQLiteDatabase db, @NonNull Context context, @NonNull Runnable notifyWidgets) {
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
        notifyWidgets.run();
    }
}
