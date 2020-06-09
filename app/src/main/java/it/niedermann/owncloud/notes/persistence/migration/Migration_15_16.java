package it.niedermann.owncloud.notes.persistence.migration;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.util.Map;

import it.niedermann.owncloud.notes.android.DarkModeSetting;

public class Migration_15_16 {

    private static final String TAG = Migration_15_16.class.getSimpleName();

    /**
     * Moves note list widget preferences from {@link SharedPreferences} to database
     * https://github.com/stefan-niedermann/nextcloud-notes/issues/832
     */
    public Migration_15_16(SQLiteDatabase db, @NonNull Context context, @NonNull Runnable notifyWidgets) {
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
                                "CATEGORIES",
                                new String[]{"CATEGORY_ID"},
                                "CATEGORY_TITLE = ? AND CATEGORY_ACCOUNT_ID = ? ",
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
        notifyWidgets.run();
    }
}
