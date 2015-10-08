package it.niedermann.owncloud.notes.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
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
            updateViews.setTextViewText(R.id.singleNoteContent, Html.fromHtml(note.getHtmlContent()));
        }
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);

        //FIXME does not work!
        Intent intent = new Intent(context, NoteActivity.class);
        intent.putExtra(NotesListViewActivity.SELECTED_NOTE, note);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        updateViews.setOnClickPendingIntent(R.id.singleNoteContent, pendingIntent);
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i : appWidgetIds) {
            int appWidgetId = appWidgetIds[i];
            Log.v("SingleNoteWidget", "onUpdate appWidgetId: " + appWidgetId);
            updateAppWidget(null, context, appWidgetManager, appWidgetId);
        }
    }
}