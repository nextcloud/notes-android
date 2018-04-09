package it.niedermann.owncloud.notes.android.appwidget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class NoteListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context mContext;
    private NoteSQLiteOpenHelper db;
    private List<DBNote> dbNotes;

    NoteListWidgetFactory(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        db = NoteSQLiteOpenHelper.getInstance(mContext);
    }

    @Override
    public void onDataSetChanged() {
        // Stores all db notes using the default sort order (starred, modified)
        // which is how they are displayed by the widget
        dbNotes = db.getNotes();
    }

    @Override
    public void onDestroy() {

    }

    /**
     * getCount()
     *
     * @return  Total number of entries
     */
    @Override
    public int getCount() {
        if (dbNotes == null) {
            return 0;
        }

        return dbNotes.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        if (dbNotes == null || dbNotes.get(i) == null) {
            return null;
        }

        RemoteViews note_content = new RemoteViews(mContext.getPackageName(),
                                                    R.layout.widget_entry);
        DBNote note = dbNotes.get(i);
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();

        extras.putLong(EditNoteActivity.PARAM_NOTE_ID, note.getId());
        fillInIntent.putExtras(extras);
        fillInIntent.setData(Uri.parse(fillInIntent.toUri(Intent.URI_INTENT_SCHEME)));
        note_content.setOnClickFillInIntent(R.id.widget_note_list_entry, fillInIntent);

        if (note.isFavorite()) {
            note_content.setViewVisibility(R.id.widget_entry_fav_icon, View.VISIBLE);
        } else {
            note_content.setViewVisibility(R.id.widget_entry_fav_icon, View.INVISIBLE);
        }
        note_content.setTextViewText(R.id.widget_entry_content_tv, note.getTitle());

        return note_content;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
