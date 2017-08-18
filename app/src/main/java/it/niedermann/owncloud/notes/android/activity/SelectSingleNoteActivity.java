package it.niedermann.owncloud.notes.android.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class SelectSingleNoteActivity extends Activity {

    public static final String PARAM_NOTE = "note";
    public static final String PARAM_ORIGINAL_NOTE = "original_note";
    public static final String PARAM_NOTE_POSITION = "note_position";

    private DBNote note, originalNote;
    private int notePosition = 0;
    private NoteSQLiteOpenHelper db = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_single_note);
        setResult(RESULT_CANCELED);

        db = NoteSQLiteOpenHelper.getInstance(this);
        int noteID = 6;

        for (int i = 6; note == null; i++) {
            note = db.getNote(i);
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int mAppWidgetId = 0;

        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        Log.d("SelectSingleNote", "mAppWidgetId: " + mAppWidgetId);

        // TODO Ask the user which note they want to be displayed
//        note = db.getNote(noteID);

        SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(this).edit();

        Log.d("SelectSingleNote", "Adding noteID (" + noteID + ") to sharedPrefs");
        // TODO Add widgetID to this key
        sp.putInt(SingleNoteWidget.WIDGET_KEY + mAppWidgetId, noteID);
        sp.apply();

        Intent retIntent = new Intent(this, SingleNoteWidget.class);
        retIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        retIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        setResult(RESULT_OK, retIntent);

        Log.d("SelectSingleNote", "note: " + note.getTitle());
    }
}
