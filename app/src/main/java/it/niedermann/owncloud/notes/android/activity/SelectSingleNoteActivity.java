package it.niedermann.owncloud.notes.android.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.View;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.ItemAdapter;

public class SelectSingleNoteActivity extends NotesListViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorDrawable color;
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();

        setResult(Activity.RESULT_CANCELED);
        findViewById(R.id.fab_create).setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = new ColorDrawable(getColor(R.color.bg_highlighted));
        } else {
            color = new ColorDrawable(ContextCompat.getColor(getApplicationContext(),
                                                                R.color.bg_highlighted));
        }

        ab.setBackgroundDrawable(color);
        ab.setTitle(R.string.activity_select_single_note);
        Spannable title = new SpannableString(ab.getTitle());
        title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.primary)),
                                                0,
                                                title.length(),
                                                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ab.setTitle(title);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
        initList();
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
