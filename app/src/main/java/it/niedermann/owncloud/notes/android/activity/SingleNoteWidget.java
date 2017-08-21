package it.niedermann.owncloud.notes.android.activity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.yydcdut.rxmarkdown.RxMDConfiguration;
import com.yydcdut.rxmarkdown.RxMDEditText;
import com.yydcdut.rxmarkdown.RxMDTextView;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.callback.OnLinkClickCallback;
import com.yydcdut.rxmarkdown.factory.EditFactory;
import com.yydcdut.rxmarkdown.factory.TextFactory;
import com.yydcdut.rxmarkdown.loader.DefaultLoader;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;
import it.niedermann.owncloud.notes.util.MarkDownUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import com.yydcdut.rxmarkdown.RxMDEditText;
import com.yydcdut.rxmarkdown.RxMarkdown;
import com.yydcdut.rxmarkdown.factory.EditFactory;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;


public class SingleNoteWidget extends AppWidgetProvider {

    public static final String  WIDGET_KEY = "single_note_widget";
    public static final String  INIT = "INIT";
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
            Log.d(TAG, "Removing " + WIDGET_KEY + appWidgetId + " from sharedprefs");
            Log.d(TAG, "Removing " + WIDGET_KEY + appWidgetId + INIT + " from sharedprefs");
            sharedprefs.remove(WIDGET_KEY + appWidgetId);
            sharedprefs.remove(WIDGET_KEY + appWidgetId + INIT);
        }
        sharedprefs.apply();
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        NoteSQLiteOpenHelper    db = NoteSQLiteOpenHelper.getInstance(context);
        SharedPreferences       sharedprefs = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews             views = new RemoteViews(context.getPackageName(), R.layout.widget_single_note);
        DBNote                  note;

        Log.d(TAG, "updateAppWidget: " + appWidgetId);
        long noteID = sharedprefs.getLong(SingleNoteWidget.WIDGET_KEY + appWidgetId, -1);
        boolean isInitialised = sharedprefs.getBoolean(SingleNoteWidget.WIDGET_KEY + appWidgetId + INIT, false);

        Log.d("updateAppWidget", "appWidgetId: " + appWidgetId);
        Log.d("updateAppWidget", "noteID: " + noteID);
        Log.d(TAG, "updateAppWidget: init: " + isInitialised);

        if (noteID >= 0 && isInitialised) {

            note = db.getNote(noteID);

            // TODO: If the user has clicked the widget and then clicked Home,
            // another click on the widget will open another edit window
            Intent intent = new Intent(context, EditNoteActivity.class);
            intent.putExtra(EditNoteActivity.PARAM_NOTE, note);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.single_note, pendingIntent);
            views.setTextViewText(R.id.single_note_content, note.getContent());
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } else {

            Log.d("updateAppWidget", "Note not found");
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
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Log.d(TAG, "onReceive: ");

        if (intent.getAction() == ACTION_APPWIDGET_UPDATE) {
            int mAppWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            updateAppWidget(context, appWidgetManager, mAppWidgetId);
        }
    }


}

