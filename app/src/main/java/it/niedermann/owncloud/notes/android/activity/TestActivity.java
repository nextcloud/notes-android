package it.niedermann.owncloud.notes.android.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import java.util.Locale;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.ItemAdapter;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

import static android.R.string.cancel;

/**
 * Created by dbailey on 18/08/2017.
 * TODO replace all "dbailey" occurences
 */

public class TestActivity extends NotesListViewActivity {

//    private NoteSQLiteOpenHelper db = null;
//    private ItemAdapter adapter = null;

    private static final String TAG = TestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

  //      db = NoteSQLiteOpenHelper.getInstance(this);
        this.setResult(Activity.RESULT_CANCELED);
        findViewById(R.id.fab_create).setVisibility(View.INVISIBLE);
        getSupportActionBar().setTitle(R.string.activity_select_single_note);
        // TODO fix colouring
        final int colorPrimary = getResources().getColor(R.color.fg_default);
        String htmlColor = String.format(Locale.US, "#%06X", (0xFFFFFF & Color.argb(0, Color.red(colorPrimary), Color.green(colorPrimary), Color.blue(colorPrimary))));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + htmlColor + "\">" + getString(R.string.app_name) + "</font>"));

        initList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { return true; }

    @Override
    public void onNoteClick(int position, View v) {

        ItemAdapter adapter = getItemAdapter();
        Item        item = adapter.getItem(position);
        DBNote      note = (DBNote) item;
        long        noteID = note.getId();
        Intent      intent = getIntent();
        Bundle      extras = intent.getExtras();
        int         mAppWidgetId = 0;

        Log.d(TAG, "onNoteClick: Position: " + position);
        Log.d(TAG, "onNoteClick: noteID: " + noteID);

        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        Log.d("SelectSingleNote", "mAppWidgetId: " + mAppWidgetId);

        SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(this).edit();

        Log.d("SelectSingleNote", "Adding noteID (" + noteID + ") to sharedPrefs");
        sp.putLong(SingleNoteWidget.WIDGET_KEY + mAppWidgetId, noteID);
        sp.putBoolean(SingleNoteWidget.WIDGET_KEY + mAppWidgetId + SingleNoteWidget.INIT, true);
        sp.apply();


        Intent retIntent = new Intent(this, SingleNoteWidget.class);
        retIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mAppWidgetId);
        retIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        this.sendBroadcast(retIntent);
        this.setResult(RESULT_OK, retIntent);

        Log.d(TAG, "onNoteClick: Sent return intent, RESULT_OK");
        finish();
    }

    @Override
    public boolean onNoteLongClick(int position, View v) { return false; }

    @Override
    public void onNoteFavoriteClick(int position, View view) { }
}
