package it.niedermann.owncloud.notes.android.activity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.persistence.NoteListWidgetService;

/**
 * Created by dan0xii on 13/09/2017.
 */

public class NoteListWidget extends AppWidgetProvider {

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_note_list);

        // Launch application when user taps the header icon or app title
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(context.getPackageName(),
                                                NotesListViewActivity.class.getName()));

        PendingIntent pendingIntent = PendingIntent.getActivity(
                                                            context,
                                                            0,
                                                            intent,
                                                            PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.widget_note_header_icon, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_note_list_title, pendingIntent);

        // Launch create note activity if user taps "+" sign in header
        intent = new Intent(context, CreateNoteActivity.class);
        pendingIntent = PendingIntent.getActivity(
                                                    context,
                                                    0,
                                                    intent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.widget_note_list_create_icon, pendingIntent);

        Intent templateIntent = new Intent(context, EditNoteActivity.class);
        PendingIntent templatePI = PendingIntent.getActivity(
                                                            context,
                                                            0,
                                                            templateIntent,
                                                            PendingIntent.FLAG_UPDATE_CURRENT);
        Intent serviceIntent = new Intent(context, NoteListWidgetService.class);

        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setPendingIntentTemplate(R.id.note_list_widget_lv, templatePI);
        views.setRemoteAdapter(R.id.note_list_widget_lv, serviceIntent);
        views.setEmptyView(R.id.note_list_widget_lv, R.id.widget_note_list_placeholder_tv);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, NoteListWidget.class));

        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.note_list_widget_lv);
    }
}
