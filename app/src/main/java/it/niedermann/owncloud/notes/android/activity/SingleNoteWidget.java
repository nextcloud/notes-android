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


public class SingleNoteWidget extends AppWidgetProvider {

    private DBNote note, originalNote;
    private int notePosition = 0;
    private NoteSQLiteOpenHelper db = null;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        // TODO Confirm this is called when each _instance_ is removed by the user

        // TODO remove entry from shared prefs when widget is removed
    }

    /**
     *
     * @param noteID    The noteID of the single note to be displayed by this widget
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, int noteID) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_single_note);




        // Construct the RemoteViews object
        //        views.setTextViewText(0, "test");
        Intent intent = new Intent(context, EditNoteActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.single_note, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(context);
            int noteID = sharedprefs.getInt("widget" + appWidgetId, -1);

            if (noteID >= 0) {
                // Widget exists


                db = NoteSQLiteOpenHelper.getInstance(context);



                // TODO Ask the user which note they want to be displayed
                note = db.getNote(noteID);


                // Notify the widget of the extra data to be displayed

                // note = originalNote = (DBNote);
                // notePosition = getIntent().getIntExtra(PARAM_NOTE_POSITION, 0);

                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                int mAppWidgetId = 0;

                if (extras != null) {
                    mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                }



                updateAppWidget(context, appWidgetManager, appWidgetId, noteID);
            }
        }
    }
}
