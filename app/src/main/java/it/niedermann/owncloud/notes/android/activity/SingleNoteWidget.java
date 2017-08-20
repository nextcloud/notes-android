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

//    private DBNote note;
    private int notePosition = 0;
    public static final String WIDGET_KEY = "single_note_widget";

    // TODO Tidy this name
    public static final String INIT = "init";
    private static final String TAG = SingleNoteWidget.class.getSimpleName();

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        // TODO Confirm this is called when each _instance_ is removed by the user
        // TODO remove entry from shared prefs when widget is removed

        Log.d(TAG, "onDisabled: ");
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

        DBNote note;
        NoteSQLiteOpenHelper db = null;
        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_single_note);
        Log.d(TAG, "updateAppWidget: " + appWidgetId);
        long noteID = sharedprefs.getLong(SingleNoteWidget.WIDGET_KEY + appWidgetId, -1);
        boolean isInitialised = sharedprefs.getBoolean(SingleNoteWidget.WIDGET_KEY + appWidgetId + INIT, false);

        Log.d("updateAppWidget", "appWidgetId: " + appWidgetId);
        Log.d("updateAppWidget", "noteID: " + noteID);
        Log.d(TAG, "updateAppWidget: init: " + isInitialised);
        if (noteID >= 0 && isInitialised) {
            // Widget exists
            Log.d("updateAppWidget", "noteID: " + noteID);
            db = NoteSQLiteOpenHelper.getInstance(context);
            note = db.getNote(noteID);


            // Construct the RemoteViews object
            //        views.setTextViewText(0, "test");

            // TODO: If the user has clicked the widget and then clicked Home,
            // another click on the widget will open another edit window
            Intent intent = new Intent(context, EditNoteActivity.class);
            intent.putExtra(EditNoteActivity.PARAM_NOTE, note);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.single_note, pendingIntent);
            // Instruct the widget manager to update the widget
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

        Log.d(TAG, "onUpdate: ");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Log.d(TAG, "onReceive: ");
        int[] mAppWidgetIds = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_ID);
        if (intent.getAction() == ACTION_APPWIDGET_UPDATE) {
            onUpdate(context, appWidgetManager, mAppWidgetIds);
        }
    }
}

