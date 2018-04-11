package it.niedermann.owncloud.notes.android.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.appwidget.SingleNoteWidget;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.ItemAdapter;

public class SelectSingleNoteActivity extends NotesListViewActivity {

    @BindView(R.id.fab_create)
    View fabCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();

        setResult(Activity.RESULT_CANCELED);

        ButterKnife.bind(this);
        fabCreate.setVisibility(View.GONE);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.activity_select_single_note);
        }
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

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
        sp.apply();

        Intent retIntent = new Intent(this, SingleNoteWidget.class);
        retIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        retIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        sendBroadcast(retIntent);
        setResult(RESULT_OK, retIntent);
        finish();
    }

    @Override
    public boolean onNoteLongClick(int position, View v) {
        return false;
    }

    @Override
    public void onNoteFavoriteClick(int position, View view) {
    }
}
