package it.niedermann.owncloud.notes.widget.singlenote;

import static it.niedermann.android.markdown.remoteviews.RemoteViewElement.TYPE_CHECKBOX_CHECKED;
import static it.niedermann.android.markdown.remoteviews.RemoteViewElement.TYPE_CHECKBOX_UNCHECKED;
import static it.niedermann.android.markdown.remoteviews.RemoteViewElement.TYPE_TEXT;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import it.niedermann.android.markdown.MarkdownUtil;
import it.niedermann.android.markdown.remoteviews.RemoteViewElement;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.persistence.NotesRepository;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.entity.SingleNoteWidgetData;
import it.niedermann.owncloud.notes.reciever.WidgetCheckboxReciever;

public class SingleNoteWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private final int appWidgetId;

    private final NotesRepository repo;
    @Nullable
    private Note note;

    private ArrayList<RemoteViewElement> noteElements = new ArrayList<>();

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
        noteElements = MarkdownUtil.getRenderedElementsForRemoteView(context, note.getContent());
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
        return (note != null) ? this.noteElements.size() : 0;
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
        final var args = new Bundle();

        args.putLong(EditNoteActivity.PARAM_NOTE_ID, note.getId());
        args.putLong(EditNoteActivity.PARAM_ACCOUNT_ID, note.getAccountId());
        fillInIntent.putExtras(args);

        var item = noteElements.get(position);

        final var note_content = new RemoteViews(context.getPackageName(), R.layout.widget_single_note_content);
        var content = item.getCurrentLineBlock();
        int type = item.getType();


        if (type == TYPE_TEXT || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            note_content.setOnClickFillInIntent(R.id.single_note_content_tv, fillInIntent);
            note_content.setTextViewText(R.id.single_note_content_tv, MarkdownUtil.renderForRemoteView(context, content).toString().trim());
            note_content.setViewVisibility(R.id.single_note_content_tv, View.VISIBLE);
        }

        if (type == TYPE_CHECKBOX_CHECKED || type == TYPE_CHECKBOX_UNCHECKED ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                note_content.setTextViewText(R.id.single_note_content_cb, content);
                if(type == TYPE_CHECKBOX_CHECKED) {
                    note_content.setCompoundButtonChecked(R.id.single_note_content_cb, true);
                }
                note_content.setViewVisibility(R.id.single_note_content_cb, View.VISIBLE);
                note_content.setOnClickPendingIntent(R.id.single_note_content_cb, getPendingIntent());
            }
        }

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

    private PendingIntent getPendingIntent() {
        Intent checkboxIntent = new Intent(context, WidgetCheckboxReciever.class);
        checkboxIntent.setAction("toggle");
        return PendingIntent.getBroadcast(context, 0, checkboxIntent, PendingIntent.FLAG_IMMUTABLE);

    }

}
