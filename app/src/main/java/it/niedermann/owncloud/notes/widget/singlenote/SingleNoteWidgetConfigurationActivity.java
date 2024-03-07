/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2015-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.singlenote;

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

import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.exception.ExceptionHandler;
import it.niedermann.owncloud.notes.main.MainActivity;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.entity.SingleNoteWidgetData;

public class SingleNoteWidgetConfigurationActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));
        setResult(Activity.RESULT_CANCELED);

        fabCreate.setVisibility(View.GONE);
        final var searchToolbar = binding.activityNotesListView.searchToolbar;
        final var swipeRefreshLayout = binding.activityNotesListView.swiperefreshlayout;
        searchToolbar.setTitle(R.string.activity_select_single_note);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onNoteClick(int position, View v) {
        final var note = (Note) adapter.getItem(position);
        final var args = getIntent().getExtras();

        if (args == null) {
            finish();
            return;
        }

        final int appWidgetId = args.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        executor.submit(() -> {
            try {
                mainViewModel.createOrUpdateSingleNoteWidgetData(
                        new SingleNoteWidgetData(
                                appWidgetId,
                                note.getAccountId(),
                                note.getId(),
                                NotesApplication.getAppTheme(this).getModeId()
                        )
                );
                final var updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
                        getApplicationContext(), SingleNoteWidget.class)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, updateIntent);
                getApplicationContext().sendBroadcast(updateIntent);
                finish();
            } catch (SQLException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
