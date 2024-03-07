/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.persistence.migration;

import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.OnConflictStrategy;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.owncloud.notes.preferences.DarkModeSetting;
import it.niedermann.owncloud.notes.widget.notelist.NoteListWidget;
import it.niedermann.owncloud.notes.widget.singlenote.SingleNoteWidget;

public class Migration_15_16 extends Migration {

    private static final String TAG = Migration_15_16.class.getSimpleName();
    @NonNull
    private final Context context;

    public Migration_15_16(@NonNull Context context) {
        super(15, 16);
        this.context = context;
    }

    /**
     * Moves note list widget preferences from {@link SharedPreferences} to database
     * https://github.com/nextcloud/notes-android/issues/832
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
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

        final var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final var editor = sharedPreferences.edit();
        final var prefs = sharedPreferences.getAll();
        for (final var pref : prefs.entrySet()) {
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
                        final var cursor = db.query("SELECT CATEGORY_ID FROM CATEGORIES WHERE CATEGORY_TITLE = ? AND CATEGORY_ACCOUNT_ID = ?", new String[]{categoryTitle, String.valueOf(accountId)});
                        if (cursor.moveToNext()) {
                            categoryId = cursor.getInt(0);
                        } else {
                            throw new IllegalStateException("No category id found for title \"" + categoryTitle + "\"");
                        }
                        cursor.close();
                    }

                    final var migratedWidgetValues = new ContentValues();
                    migratedWidgetValues.put("ID", widgetId);
                    migratedWidgetValues.put("ACCOUNT_ID", accountId);
                    migratedWidgetValues.put("CATEGORY_ID", categoryId);
                    migratedWidgetValues.put("MODE", mode);
                    migratedWidgetValues.put("THEME_MODE", themeMode);
                    db.insert("WIDGET_NOTE_LISTS", OnConflictStrategy.REPLACE, migratedWidgetValues);
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
        context.sendBroadcast(new Intent(context, SingleNoteWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE));
        context.sendBroadcast(new Intent(context, NoteListWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE));
    }
}
