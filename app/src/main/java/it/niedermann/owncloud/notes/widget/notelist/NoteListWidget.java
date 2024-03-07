/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.notelist;

import static it.niedermann.owncloud.notes.shared.util.WidgetUtil.pendingIntentFlagCompat;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;

public class NoteListWidget extends AppWidgetProvider {
    private static final String TAG = NoteListWidget.class.getSimpleName();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    static void updateAppWidget(Context context, AppWidgetManager awm, int[] appWidgetIds) {
        final var repo = NotesRepository.getInstance(context);

        RemoteViews views;

        for (int appWidgetId : appWidgetIds) {
            try {
                final var data = repo.getNoteListWidgetData(appWidgetId);

                final var serviceIntent = new Intent(context, NoteListWidgetService.class);
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

                Log.v(TAG, "-- data - " + data);

                views = new RemoteViews(context.getPackageName(), R.layout.widget_note_list);
                views.setRemoteAdapter(R.id.note_list_widget_lv, serviceIntent);
                views.setPendingIntentTemplate(R.id.note_list_widget_lv, PendingIntent.getActivity(context, 0, new Intent(), pendingIntentFlagCompat(PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_COMPONENT)));
                views.setEmptyView(R.id.note_list_widget_lv, R.id.widget_note_list_placeholder_tv);

                awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.note_list_widget_lv);
                awm.updateAppWidget(appWidgetId, views);
            } catch (NoSuchElementException e) {
                Log.i(TAG, "onUpdate has been triggered before the user finished configuring the widget");
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateAppWidget(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        final var awm = AppWidgetManager.getInstance(context);

        if (intent.getAction() != null) {
            if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
                if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                    if (intent.getExtras() != null) {
                        updateAppWidget(context, awm, new int[]{intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)});
                    } else {
                        Log.w(TAG, "intent.getExtras() is null");
                    }
                } else {
                    updateAppWidget(context, awm, awm.getAppWidgetIds(new ComponentName(context, NoteListWidget.class)));
                }
            }
        } else {
            Log.w(TAG, "intent.getAction() is null");
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        final var repo = NotesRepository.getInstance(context);

        for (final int appWidgetId : appWidgetIds) {
            executor.submit(() -> repo.removeNoteListWidget(appWidgetId));
        }
    }

    /**
     * Update note list widgets, if the note data was changed.
     */
    public static void updateNoteListWidgets(Context context) {
        context.sendBroadcast(new Intent(context, NoteListWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE));
    }
}
