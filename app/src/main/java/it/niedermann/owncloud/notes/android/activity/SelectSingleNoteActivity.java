package it.niedermann.owncloud.notes.android.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

/**
 * Created by Daniel Bailey on 18/08/2017.
 */

public class SelectSingleNoteActivity extends NotesListViewActivity {

    private static final String TAG = SelectSingleNoteActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setResult(Activity.RESULT_CANCELED);
        findViewById(R.id.fab_create).setVisibility(View.INVISIBLE);
        getSupportActionBar().setTitle(R.string.activity_select_single_note);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ColorDrawable colorDrawable = new ColorDrawable(getColor(R.color.bg_highlighted));
            getSupportActionBar().setBackgroundDrawable(colorDrawable);
        }

        Spannable title = new SpannableString(getSupportActionBar().getTitle());
        title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.primary)), 0,
                title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        getSupportActionBar().setTitle(title);

        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
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

        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), SingleNoteWidget.class));
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
