package it.niedermann.owncloud.notes.widget.notelist;

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

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;

public class NoteListWidget extends AppWidgetProvider {
    private static final String TAG = NoteListWidget.class.getSimpleName();

    public static final int PENDING_INTENT_NEW_NOTE_RQ = 0;
    public static final int PENDING_INTENT_EDIT_NOTE_RQ = 1;
    public static final int PENDING_INTENT_OPEN_APP_RQ = 2;

    static void updateAppWidget(Context context, AppWidgetManager awm, int[] appWidgetIds) {
        final NotesDatabase db = NotesDatabase.getInstance(context);

        RemoteViews views;

        for (int appWidgetId : appWidgetIds) {
            try {
                final NoteListsWidgetData data = db.getNoteListWidgetData(appWidgetId);

                Intent serviceIntent = new Intent(context, NoteListWidgetService.class);
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

                PendingIntent templatePI = PendingIntent.getActivity(context, PENDING_INTENT_EDIT_NOTE_RQ,
                        new Intent(context, EditNoteActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Log.v(TAG, "-- data - " + data);

                views = new RemoteViews(context.getPackageName(), R.layout.widget_note_list);
                views.setPendingIntentTemplate(R.id.note_list_widget_lv, templatePI);
                views.setRemoteAdapter(R.id.note_list_widget_lv, serviceIntent);
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
        AppWidgetManager awm = AppWidgetManager.getInstance(context);

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
        final NotesDatabase db = NotesDatabase.getInstance(context);

        for (int appWidgetId : appWidgetIds) {
            db.removeNoteListWidget(appWidgetId);
        }
    }

    /**
     * Update note list widgets, if the note data was changed.
     */
    public static void updateNoteListWidgets(Context context) {
        context.sendBroadcast(new Intent(context, NoteListWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE));
    }
}
