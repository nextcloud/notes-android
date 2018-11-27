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

public class SingleNoteWidget extends AppWidgetProvider {
    private static boolean darkTheme;

    public static final String DARK_THEME_KEY = "SNW_darkTheme";
    public static final String WIDGET_KEY = "single_note_widget";

    static void updateAppWidget(Context context, AppWidgetManager awm, int[] appWidgetIds) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Intent templateIntent = new Intent(context, EditNoteActivity.class);
        templateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        for (int appWidgetId : appWidgetIds) {
            // onUpdate has been triggered before the user finished configuring the widget
            if ((sp.getLong(WIDGET_KEY + appWidgetId, -1)) == -1) {
                return;
            }

            darkTheme = sp.getBoolean(DARK_THEME_KEY + appWidgetId, false);

            PendingIntent templatePendingIntent = PendingIntent.getActivity(context, appWidgetId, templateIntent,
                                                                            PendingIntent.FLAG_UPDATE_CURRENT);

            Intent serviceIntent = new Intent(context, SingleNoteWidgetService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews views;

            if (darkTheme) {
                views = new RemoteViews(context.getPackageName(), R.layout.widget_single_note_dark);
                views.setPendingIntentTemplate(R.id.single_note_widget_lv_dark, templatePendingIntent);
                views.setRemoteAdapter(R.id.single_note_widget_lv_dark, serviceIntent);
                views.setEmptyView(R.id.single_note_widget_lv_dark, R.id.widget_single_note_placeholder_tv_dark);
                awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.single_note_widget_lv_dark);
            } else {
                views = new RemoteViews(context.getPackageName(), R.layout.widget_single_note);
                views.setPendingIntentTemplate(R.id.single_note_widget_lv, templatePendingIntent);
                views.setRemoteAdapter(R.id.single_note_widget_lv, serviceIntent);
                views.setEmptyView(R.id.single_note_widget_lv, R.id.widget_single_note_placeholder_tv);
                awm.notifyAppWidgetViewDataChanged(appWidgetId, R.id.single_note_widget_lv);
            }

            awm.updateAppWidget(appWidgetId, views);
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

        updateAppWidget(context, AppWidgetManager.getInstance(context),
                        (awm.getAppWidgetIds(new ComponentName(context, SingleNoteWidget.class))));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();

        for (int appWidgetId : appWidgetIds) {
            editor.remove(WIDGET_KEY + appWidgetId);
            editor.remove(NoteListWidget.DARK_THEME_KEY + appWidgetId);
        }

        editor.apply();
        super.onDeleted(context, appWidgetIds);
    }
}
