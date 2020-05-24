package it.niedermann.owncloud.notes.android.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import it.niedermann.owncloud.notes.ExceptionHandler;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.appwidget.SingleNoteWidget;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.SingleNoteWidgetData;
import it.niedermann.owncloud.notes.util.Notes;

public class SelectSingleNoteActivity extends NotesListViewActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));
        setResult(Activity.RESULT_CANCELED);

        fabCreate.setVisibility(View.GONE);
        Toolbar toolbar = binding.activityNotesListView.toolbar;
        SwipeRefreshLayout swipeRefreshLayout = binding.activityNotesListView.swiperefreshlayout;
        toolbar.setTitle(R.string.activity_select_single_note);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onNoteClick(int position, View v) {
        final DBNote note = (DBNote) adapter.getItem(position);
        final Bundle extras = getIntent().getExtras();

        if (extras == null) {
            finish();
            return;
        }

        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        try {
            db.createOrUpdateSingleNoteWidgetData(
                    new SingleNoteWidgetData(
                            appWidgetId,
                            note.getAccountId(),
                            note.getId(),
                            Notes.getAppTheme(this).getModeId()
                    )
            );
            final Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
                    getApplicationContext(), SingleNoteWidget.class)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, updateIntent);
            getApplicationContext().sendBroadcast(updateIntent);
        } catch (SQLException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        finish();
    }
}
