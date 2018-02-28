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

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

public class SingleNoteWidget extends AppWidgetProvider {

    public static final String  WIDGET_KEY = "single_note_widget";

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences.Editor editor = PreferenceManager
                                            .getDefaultSharedPreferences(context).edit();

        for (int appWidgetId : appWidgetIds) {
            editor.remove(WIDGET_KEY + appWidgetId);
        }

        editor.apply();
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent templateIntent = new Intent(context, EditNoteActivity.class);
            templateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            PendingIntent templatePendingIntent = PendingIntent.getActivity(
                                                    context,
                                                    appWidgetId,
                                                    templateIntent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT);

            Intent serviceIntent = new Intent(context, SingleNoteWidgetService.class);
            RemoteViews views = new RemoteViews(context.getPackageName(),
                                                        R.layout.widget_single_note);

            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setPendingIntentTemplate(R.id.single_note_widget_lv, templatePendingIntent);
            views.setRemoteAdapter(R.id.single_note_widget_lv, serviceIntent);
            views.setEmptyView(R.id.single_note_widget_lv, R.id.widget_single_note_placeholder_tv);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int ids[] = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                                                                        SingleNoteWidget.class));

        for (int appWidgetId : ids) {
            if (ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
                                                                R.id.single_note_widget_lv);
            }
        }

        super.onReceive(context, intent);
    }
}
