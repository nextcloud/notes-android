package it.niedermann.owncloud.notes.widget.singlenote;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.Nullable;

import java.util.NoSuchElementException;

import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.shared.model.DBNote;

public class SingleNoteWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private final int appWidgetId;

    private final NotesDatabase db;
    @Nullable
    private DBNote note;

    private static final String TAG = SingleNoteWidget.class.getSimpleName();

    SingleNoteWidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        this.db = NotesDatabase.getInstance(context);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        try {
            final SingleNoteWidgetData data = db.getSingleNoteWidgetData(appWidgetId);
            final long noteId = data.getNoteId();
            Log.v(TAG, "Fetch note with id " + noteId);
            note = db.getNote(data.getAccountId(), noteId);

            if (note == null) {
                Log.e(TAG, "Error: note not found");
            }
        } catch (NoSuchElementException e) {
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

        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();

        extras.putLong(EditNoteActivity.PARAM_NOTE_ID, note.getId());
        extras.putLong(EditNoteActivity.PARAM_ACCOUNT_ID, note.getAccountId());
        fillInIntent.putExtras(extras);

        final RemoteViews note_content = new RemoteViews(context.getPackageName(), R.layout.widget_single_note_content);
        note_content.setOnClickFillInIntent(R.id.single_note_content_tv, fillInIntent);
        note_content.setTextViewText(R.id.single_note_content_tv, MarkdownUtil.renderForRemoteView(context, note.getContent()));

        return note_content;
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
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
