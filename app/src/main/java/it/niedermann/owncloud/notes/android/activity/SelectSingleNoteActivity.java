package it.niedermann.owncloud.notes.android.activity;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;

import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.widget.SingleNoteWidget;
import it.niedermann.owncloud.notes.model.Note;
import it.niedermann.owncloud.notes.model.NoteAdapter;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

/**
 * Configuration Activity to select a single note which should be displayed in the SingleNoteWidget
 * Created by stefan on 08.10.15.
 */
public class SelectSingleNoteActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private NoteSQLiteOpenHelper db = null;
    private ListView listView = null;
    private NoteAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_select_single_note);
        // Display Data
        db = new NoteSQLiteOpenHelper(this);
        db.synchronizeWithServer();
        setListView(db.getNotes());


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    /**
     * Allows other classes to set a List of Notes.
     *
     * @param noteList List&lt;Note&gt;
     */
    private void setListView(List<Note> noteList) {
        adapter = new NoteAdapter(getApplicationContext(), noteList);
        listView = (ListView) findViewById(R.id.select_single_note_list_view);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Context context = SelectSingleNoteActivity.this;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_single_note);
        appWidgetManager.updateAppWidget(appWidgetId, views);
        SingleNoteWidget.updateAppWidget(adapter.getItem(position), context, appWidgetManager, appWidgetId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
