package it.niedermann.owncloud.notes.android.activity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

public class SingleNoteWidget extends AppWidgetProvider {

    public static final String  WIDGET_KEY = "single_note_widget";
    private static final String TAG = SingleNoteWidget.class.getSimpleName();

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        SharedPreferences.Editor sharedprefs = PreferenceManager.getDefaultSharedPreferences(context).edit();

        for (int appWidgetId : appWidgetIds) {
            sharedprefs.remove(WIDGET_KEY + appWidgetId);
        }
        sharedprefs.apply();
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_single_note);
        long noteID = sharedprefs.getLong(SingleNoteWidget.WIDGET_KEY + appWidgetId, -1);

        if (noteID >= 0) {
            NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(context);
            DBNote note = db.getNote(noteID);
            Intent intent = new Intent(context, EditNoteActivity.class);
            intent.putExtra(EditNoteActivity.PARAM_NOTE, note);
            intent.putExtra(EditNoteActivity.PARAM_WIDGET_SRC, true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_single_note, pendingIntent);
            views.setTextViewText(R.id.single_note_content, note.getContent());
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } else {
            Log.e(TAG, "Note not found");
            views.setTextViewText(R.id.single_note_content, "Note not found");
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int ids[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, SingleNoteWidget.class));

        for (int appWidgetId : ids) {
            if (ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
        super.onReceive(context, intent);
    }
}
