package it.niedermann.owncloud.notes.android.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.DarkModeSetting;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.NoteListsWidgetData;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.util.Notes;

import static it.niedermann.owncloud.notes.model.NoteListsWidgetData.MODE_DISPLAY_ALL;
import static it.niedermann.owncloud.notes.model.NoteListsWidgetData.MODE_DISPLAY_CATEGORY;
import static it.niedermann.owncloud.notes.model.NoteListsWidgetData.MODE_DISPLAY_STARRED;

public class NoteListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = NoteListWidgetFactory.class.getSimpleName();

    private final Context context;
    private final NoteListsWidgetData data;
    private final boolean darkTheme;
    private NotesDatabase db;
    private List<DBNote> dbNotes;

    NoteListWidgetFactory(Context context, Intent intent) {
        this.context = context;
        final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        db = NotesDatabase.getInstance(context);
        data = db.getNoteListWidgetData(appWidgetId);

        darkTheme = Notes.isDarkThemeActive(context, DarkModeSetting.fromModeID(data.getThemeMode()));
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        try {
            Log.v(TAG, "--- data - " + data);
            switch (data.getMode()) {
                case MODE_DISPLAY_ALL:
                    dbNotes = db.getNotes(data.getAccountId());
                    break;
                case MODE_DISPLAY_STARRED:
                    dbNotes = db.searchNotes(data.getAccountId(), null, null, true);
                    break;
                case MODE_DISPLAY_CATEGORY:
                    if (data.getCategoryId() != null) {
                        dbNotes = db.searchNotes(data.getAccountId(), null, db.getCategoryTitleById(data.getAccountId(), data.getCategoryId()), null);
                    }
                    break;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        //NoOp
    }

    /**
     * getCount()
     *
     * @return Total number of entries
     */
    @Override
    public int getCount() {
        if (dbNotes == null) {
            return 0;
        }

        return dbNotes.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews note_content;

        if (dbNotes == null || position > dbNotes.size() - 1 || dbNotes.get(position) == null) {
            Log.e(TAG, "Could not find position \"" + position + "\" in dbNotes list.");
            return null;
        }

        DBNote note = dbNotes.get(position);
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();

        extras.putLong(EditNoteActivity.PARAM_NOTE_ID, note.getId());
        extras.putLong(EditNoteActivity.PARAM_ACCOUNT_ID, note.getAccountId());
        fillInIntent.putExtras(extras);
        fillInIntent.setData(Uri.parse(fillInIntent.toUri(Intent.URI_INTENT_SCHEME)));

        if (darkTheme) {
            note_content = new RemoteViews(context.getPackageName(), R.layout.widget_entry_dark);
            note_content.setOnClickFillInIntent(R.id.widget_note_list_entry_dark, fillInIntent);
            note_content.setTextViewText(R.id.widget_entry_content_tv_dark, note.getTitle());
            note_content.setImageViewResource(R.id.widget_entry_fav_icon_dark, note.isFavorite()
                    ? R.drawable.ic_star_yellow_24dp
                    : R.drawable.ic_star_grey_ccc_24dp);
        } else {
            note_content = new RemoteViews(context.getPackageName(), R.layout.widget_entry);
            note_content.setOnClickFillInIntent(R.id.widget_note_list_entry, fillInIntent);
            note_content.setTextViewText(R.id.widget_entry_content_tv, note.getTitle());
            note_content.setImageViewResource(R.id.widget_entry_fav_icon, note.isFavorite()
                    ? R.drawable.ic_star_yellow_24dp
                    : R.drawable.ic_star_grey_ccc_24dp);
        }

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
