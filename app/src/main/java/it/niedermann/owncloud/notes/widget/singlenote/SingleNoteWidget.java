package it.niedermann.owncloud.notes.widget.singlenote;

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
import it.niedermann.owncloud.notes.preferences.DarkModeSetting;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.edit.BaseNoteFragment;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.NotesApplication;

public class SingleNoteWidget extends AppWidgetProvider {

    private static final String TAG = SingleNoteWidget.class.getSimpleName();

    static void updateAppWidget(Context context, AppWidgetManager awm, int[] appWidgetIds) {
        final Intent templateIntent = new Intent(context, EditNoteActivity.class);
        final NotesDatabase db = NotesDatabase.getInstance(context);

        for (int appWidgetId : appWidgetIds) {
            try {
                final SingleNoteWidgetData data = db.getSingleNoteWidgetData(appWidgetId);

                templateIntent.putExtra(BaseNoteFragment.PARAM_ACCOUNT_ID, data.getAccountId());

                final PendingIntent templatePendingIntent = PendingIntent.getActivity(context, appWidgetId, templateIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Intent serviceIntent = new Intent(context, SingleNoteWidgetService.class);
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

                RemoteViews views;

                if (NotesApplication.isDarkThemeActive(context, DarkModeSetting.fromModeID(data.getThemeMode()))) {
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

        updateAppWidget(context, AppWidgetManager.getInstance(context),
                (awm.getAppWidgetIds(new ComponentName(context, SingleNoteWidget.class))));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        final NotesDatabase db = NotesDatabase.getInstance(context);

        for (int appWidgetId : appWidgetIds) {
            db.removeSingleNoteWidget(appWidgetId);
        }
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * Update single note widget, if the note data was changed.
     */
    public static void updateSingleNoteWidgets(Context context) {
        context.sendBroadcast(new Intent(context, SingleNoteWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE));
    }
}
