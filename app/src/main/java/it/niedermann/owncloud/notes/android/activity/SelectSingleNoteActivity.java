package it.niedermann.owncloud.notes.android.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.ItemAdapter;
import it.niedermann.owncloud.notes.model.SectionItem;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class SelectSingleNoteActivity extends AppCompatActivity implements
        ItemAdapter.NoteClickListener, View.OnClickListener {

    public static final String PARAM_NOTE = "note";
    public static final String PARAM_ORIGINAL_NOTE = "original_note";
    public static final String PARAM_NOTE_POSITION = "note_position";

    private DBNote note;
    private int notePosition = 0;
    private NoteSQLiteOpenHelper db = null;
    private RecyclerView listView = null;
    private ItemAdapter adapter = null;

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

        adapter = new ItemAdapter(this);
        listView = (RecyclerView) findViewById(R.id.select_single_note_list_view);
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        List<DBNote> noteList;
        noteList = db.getNotes();

        refreshList();

        Log.d("SelectSingleNote", "note: " + note.getTitle());
    }

    public void refreshList() {
        new SelectSingleNoteActivity.RefreshListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class RefreshListTask extends AsyncTask<Void, Void, List<Item>> {

        private CharSequence query = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<Item> doInBackground(Void... voids) {
            List<DBNote> noteList;
            if (query==null) {
                noteList = db.getNotes();
            } else {
                noteList = db.searchNotes(query);
            }

            final List<Item> itemList = new ArrayList<>();
            // #12 Create Sections depending on Time
            // TODO Move to ItemAdapter?
            boolean todaySet, yesterdaySet, weekSet, monthSet, earlierSet;
            todaySet = yesterdaySet = weekSet = monthSet = earlierSet = false;
            Calendar recent = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            Calendar yesterday = Calendar.getInstance();
            yesterday.set(Calendar.DAY_OF_YEAR, yesterday.get(Calendar.DAY_OF_YEAR) - 1);
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);
            yesterday.set(Calendar.MILLISECOND, 0);
            Calendar week = Calendar.getInstance();
            week.set(Calendar.DAY_OF_WEEK, week.getFirstDayOfWeek());
            week.set(Calendar.HOUR_OF_DAY, 0);
            week.set(Calendar.MINUTE, 0);
            week.set(Calendar.SECOND, 0);
            week.set(Calendar.MILLISECOND, 0);
            Calendar month = Calendar.getInstance();
            month.set(Calendar.DAY_OF_MONTH, 0);
            month.set(Calendar.HOUR_OF_DAY, 0);
            month.set(Calendar.MINUTE, 0);
            month.set(Calendar.SECOND, 0);
            month.set(Calendar.MILLISECOND, 0);
            for (int i = 0; i < noteList.size(); i++) {
                DBNote currentNote = noteList.get(i);
                if (currentNote.isFavorite()) {
                    // don't show as new section
                } else if (!todaySet && currentNote.getModified().getTimeInMillis() >= today.getTimeInMillis()) {
                    // after 00:00 today
                    if (i > 0) {
                        itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_today)));
                    }
                    todaySet = true;
                } else if (!yesterdaySet && currentNote.getModified().getTimeInMillis() < today.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= yesterday.getTimeInMillis()) {
                    // between today 00:00 and yesterday 00:00
                    if (i > 0) {
                        itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_yesterday)));
                    }
                    yesterdaySet = true;
                } else if (!weekSet && currentNote.getModified().getTimeInMillis() < yesterday.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= week.getTimeInMillis()) {
                    // between yesterday 00:00 and start of the week 00:00
                    if (i > 0) {
                        itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_this_week)));
                    }
                    weekSet = true;
                } else if (!monthSet && currentNote.getModified().getTimeInMillis() < week.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= month.getTimeInMillis()) {
                    // between start of the week 00:00 and start of the month 00:00
                    if (i > 0) {
                        itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_this_month)));
                    }
                    monthSet = true;
                } else if (!earlierSet && currentNote.getModified().getTimeInMillis() < month.getTimeInMillis()) {
                    // before start of the month 00:00
                    if (i > 0) {
                        itemList.add(new SectionItem(getResources().getString(R.string.listview_updated_earlier)));
                    }
                    earlierSet = true;
                }
                itemList.add(currentNote);
            }

            return itemList;
        }

        @Override
        protected void onPostExecute(List<Item> items) {
            adapter.setItemList(items);
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onNoteClick(int position, View v) {

    }

    @Override
    public boolean onNoteLongClick(int position, View v) {
        return false;
    }

    @Override
    public void onNoteFavoriteClick(int position, View v) {

    }
}
