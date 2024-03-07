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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.OnConflictStrategy;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.niedermann.owncloud.notes.preferences.DarkModeSetting;
import it.niedermann.owncloud.notes.widget.notelist.NoteListWidget;
import it.niedermann.owncloud.notes.widget.singlenote.SingleNoteWidget;

public class Migration_13_14 extends Migration {

    private static final String TAG = Migration_13_14.class.getSimpleName();
    @NonNull
    private final Context context;

    public Migration_13_14(@NonNull Context context) {
        super(13, 14);
        this.context = context;
    }

    /**
     * Move single note widget preferences to database
     * https://github.com/nextcloud/notes-android/issues/754
     */
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
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
        final var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final var editor = sharedPreferences.edit();
        final var prefs = sharedPreferences.getAll();
        for (final var pref : prefs.entrySet()) {
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

                    final var migratedWidgetValues = new ContentValues();
                    migratedWidgetValues.put("ID", widgetId);
                    migratedWidgetValues.put("ACCOUNT_ID", accountId);
                    migratedWidgetValues.put("NOTE_ID", noteId);
                    migratedWidgetValues.put("THEME_MODE", themeMode);
                    db.insert("WIDGET_SINGLE_NOTES", OnConflictStrategy.REPLACE, migratedWidgetValues);
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
        context.sendBroadcast(new Intent(context, SingleNoteWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE));
        context.sendBroadcast(new Intent(context, NoteListWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE));
    }
}
