package it.niedermann.owncloud.notes.android.activity;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RemoteViews;

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
        setResult(RESULT_CANCELED);

        db = NoteSQLiteOpenHelper.getInstance(this);

        int noteID = 6;

        for (int i = 6; note == null; i++) {
            note = db.getNote(i);
        }


        // TODO Ask the user which note they want to be displayed
//        note = db.getNote(noteID);


        // Notify the widget of the extra data to be displayed

        // note = originalNote = (DBNote);
        // notePosition = getIntent().getIntExtra(PARAM_NOTE_POSITION, 0);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int mAppWidgetId = 0;

        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_single_note);
        views.setTextViewText(R.id.single_note_content, note.getContent());
        appWidgetManager.updateAppWidget(mAppWidgetId, views);

        Intent retIntent = new Intent();
        retIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, retIntent);

        Log.d("SelectSingleNote", "note: " + note.getTitle());
        finish();
    }
}
