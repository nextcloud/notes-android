package it.niedermann.owncloud.notes.widget.createnote;

import static it.niedermann.owncloud.notes.shared.util.WidgetUtil.pendingIntentFlagCompat;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;

/**
 * Implementation of App Widget functionality.
 */
public class CreateNoteWidget extends AppWidgetProvider {

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        // Construct the RemoteViews object
        final var views = new RemoteViews(context.getPackageName(), R.layout.widget_create_note);
        final var intent = new Intent(context, EditNoteActivity.class);

        views.setOnClickPendingIntent(R.id.widget_create_note, PendingIntent.getActivity(context, 0, intent, pendingIntentFlagCompat(PendingIntent.FLAG_UPDATE_CURRENT)));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        for (final int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

