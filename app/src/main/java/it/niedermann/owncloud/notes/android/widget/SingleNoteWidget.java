package it.niedermann.owncloud.notes.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.NoteActivity;
import it.niedermann.owncloud.notes.android.activity.NotesListViewActivity;
import it.niedermann.owncloud.notes.model.Note;

/**
 * Widget which displays a single selected note.
 * Created by stefan on 08.10.15.
 */
public class SingleNoteWidget extends AppWidgetProvider {

    public static void updateAppWidget(Note note, Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_single_note);
        if (note != null) {
            updateViews.setTextViewText(R.id.single_note_content, note.getSpannableContent());
            Intent intent = new Intent(context, NoteActivity.class);
            intent.putExtra(NotesListViewActivity.SELECTED_NOTE, note);
            // http://stackoverflow.com/questions/4011178/multiple-instances-of-widget-only-updating-last-widget
            Uri data = Uri.withAppendedPath(
                    Uri.parse("notes" + "://widget/id/")
                    , String.valueOf(appWidgetId));
            intent.setData(data);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            updateViews.setOnClickPendingIntent(R.id.single_note, pendingIntent);
        }
        Log.v("Note", "Note is null: " + (note == null));
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(null, context, appWidgetManager, appWidgetId);
        }
    }
}