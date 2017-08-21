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
import android.view.View;

import java.util.Locale;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.ItemAdapter;

/**
 * Created by Daniel Bailey on 18/08/2017.
 */

public class SelectSingleNoteActivity extends NotesListViewActivity {

    private static final String TAG = SelectSingleNoteActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

  //      db = NoteSQLiteOpenHelper.getInstance(this);
        this.setResult(Activity.RESULT_CANCELED);
        findViewById(R.id.fab_create).setVisibility(View.INVISIBLE);
        getSupportActionBar().setTitle(R.string.activity_select_single_note);

        initList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { return true; }

    @Override
    public void onNoteClick(int position, View v) {

        ItemAdapter adapter = getItemAdapter();
        Item item = adapter.getItem(position);
        DBNote note = (DBNote) item;
        long noteID = note.getId();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int mAppWidgetId = -1;

        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(this).edit();

        sp.putLong(SingleNoteWidget.WIDGET_KEY + mAppWidgetId, noteID);
        sp.putBoolean(SingleNoteWidget.WIDGET_KEY + mAppWidgetId + SingleNoteWidget.INIT, true);
        sp.apply();

        Intent retIntent = new Intent(this, SingleNoteWidget.class);
        retIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        retIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        this.sendBroadcast(retIntent);
        this.setResult(RESULT_OK, retIntent);
        finish();
    }

    @Override
    public boolean onNoteLongClick(int position, View v) { return false; }

    @Override
    public void onNoteFavoriteClick(int position, View view) { }
}
