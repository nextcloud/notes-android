/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes.widget.singlenote;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.Nullable;

import com.owncloud.android.lib.common.utils.Log_OC;

import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Note;

public class SingleNoteWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private final int appWidgetId;

    private final NotesRepository repo;
    @Nullable
    private Note note;

    private static final String TAG = SingleNoteWidget.class.getSimpleName();

    SingleNoteWidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        this.repo = NotesRepository.getInstance(context);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        final var data = repo.getSingleNoteWidgetData(appWidgetId);
        if (data != null) {
            final long noteId = data.getNoteId();
            Log.v(TAG, "Fetch note with id " + noteId);
            note = repo.getNoteById(noteId);

            if (note == null) {
                Log.e(TAG, "Error: note not found");
            }
        } else {
            Log.w(TAG, "Widget with ID " + appWidgetId + " seems to be not configured yet.");
        }
    }

    @Override
    public void onDestroy() {
        //NoOp
    }

    /**
     * Returns the number of items in the data set. In this case, always 1 as a single note is
     * being displayed. Will return 0 when the note can't be displayed.
     */
    @Override
    public int getCount() {
        return (note != null) ? 1 : 0;
    }

    /**
     * Returns a RemoteView containing the note content in a TextView and
     * a fillInIntent to handle the user tapping on the item in the list view.
     *
     * @param position The position of the item in the list
     * @return The RemoteView at the specified position in the list
     */
    @Override
    public RemoteViews getViewAt(int position) {
        if (note == null) {
            return null;
        }

        final var fillInIntent = new Intent();
        fillInIntent.putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId());
        fillInIntent.putExtra(EditNoteActivity.PARAM_ACCOUNT_ID, note.getAccountId());

        final var noteContent = new RemoteViews(context.getPackageName(), R.layout.widget_single_note_content);
        noteContent.setOnClickFillInIntent(R.id.single_note_content_tv, fillInIntent);

        CharSequence rendered;
        try {
            rendered = MarkdownUtil.renderForRemoteView(context, note.getContent());
            if (rendered == null) {
                rendered = "";
            }
        } catch (Exception e) {
            Log_OC.e(TAG, "Markdown rendering failed", e);
            rendered = note.getContent();
        }
        noteContent.setTextViewText(R.id.single_note_content_tv, rendered);
        return noteContent;
    }


    // TODO Set loading view
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return note != null ? note.getId() : position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
