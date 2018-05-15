package it.niedermann.owncloud.notes.android.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.android.activity.NotesListViewActivity;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

public class NoteListWidget extends AppWidgetProvider {
    public static final String WIDGET_MODE_KEY = "NLW_mode";
    public static final String WIDGET_CATEGORY_KEY = "NLW_cat";
    public static final int NLW_DISPLAY_ALL = 0;
    public static final int NLW_DISPLAY_STARRED = 1;
    public static final int NLW_DISPLAY_CATEGORY = 2;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            int displayMode = sp.getInt(NoteListWidget.WIDGET_MODE_KEY + appWidgetId, -1);
            String category = sp.getString(NoteListWidget.WIDGET_CATEGORY_KEY + appWidgetId, null);
            Intent serviceIntent = new Intent(context, NoteListWidgetService.class);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_note_list);

            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.putExtra(NoteListWidget.WIDGET_MODE_KEY + appWidgetId, displayMode);

            // Launch application when user taps the header icon or app title
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName(context.getPackageName(),
                                                  NotesListViewActivity.class.getName()));

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                                                                    intent,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_note_header_icon, pendingIntent);
            views.setOnClickPendingIntent(R.id.widget_note_list_title_tv, pendingIntent);

            // Launch create note activity if user taps "+" sign in header
            intent = new Intent(context, EditNoteActivity.class);
            pendingIntent = PendingIntent.getActivity(context,0,
                                                        intent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_note_list_create_icon, pendingIntent);

            Intent templateIntent = new Intent(context, EditNoteActivity.class);
            PendingIntent templatePI = PendingIntent.getActivity(context,
                                                                0, templateIntent,
                                                                PendingIntent.FLAG_UPDATE_CURRENT);

            if (displayMode == 2) {
                serviceIntent.putExtra(NoteListWidget.WIDGET_CATEGORY_KEY + appWidgetId, category);
            }
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setPendingIntentTemplate(R.id.note_list_widget_lv, templatePI);
            views.setRemoteAdapter(appWidgetId, R.id.note_list_widget_lv, serviceIntent);
            views.setEmptyView(R.id.note_list_widget_lv, R.id.widget_note_list_placeholder_tv);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = awm.getAppWidgetIds(new ComponentName(context, NoteListWidget.class));

        for (int appWidgetId : appWidgetIds) {
            if (ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
                awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.note_list_widget_lv);
            }
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        SharedPreferences.Editor editor = PreferenceManager
                                            .getDefaultSharedPreferences(context).edit();

        for (int appWidgetId : appWidgetIds) {
            editor.remove(WIDGET_MODE_KEY + appWidgetId);
            editor.remove(WIDGET_CATEGORY_KEY + appWidgetId);
        }

        editor.apply();
    }
}
