package it.niedermann.owncloud.notes.widget.notelist;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.lifecycle.LiveData;

import java.util.List;

import it.niedermann.owncloud.notes.NotesApplication;
import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.edit.EditNoteActivity;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData;
import it.niedermann.owncloud.notes.preferences.DarkModeSetting;

import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_ALL;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_CATEGORY;
import static it.niedermann.owncloud.notes.persistence.entity.NotesListWidgetData.MODE_DISPLAY_STARRED;


public class NoteListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = NoteListWidgetFactory.class.getSimpleName();

    private final Context context;
    private final NotesListWidgetData data;
    private final boolean darkTheme;
    private NotesDatabase db;
    private List<Note> noteEntities;

    NoteListWidgetFactory(Context context, Intent intent) {
        this.context = context;
        final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        db = NotesDatabase.getInstance(context);
        data = db.getWidgetNotesListDao().getNoteListWidgetData(appWidgetId);

        darkTheme = NotesApplication.isDarkThemeActive(context, DarkModeSetting.fromModeID(data.getThemeMode()));
    }

    @Override
    public void onCreate() {
        final LiveData<List<Note>> noteEntitiesLiveData;
        try {
            Log.v(TAG, "--- data - " + data);
            switch (data.getMode()) {
                case MODE_DISPLAY_ALL:
                    noteEntitiesLiveData = db.getNoteDao().searchRecentByModified(data.getAccountId(), "%");
                    break;
                case MODE_DISPLAY_STARRED:
                    noteEntitiesLiveData = db.getNoteDao().searchFavoritesByModified(data.getAccountId(), "%");
                    break;
                case MODE_DISPLAY_CATEGORY:
                default:
                    if (data.getCategory() != null) {
                        noteEntitiesLiveData = db.getNoteDao().searchCategoryByModified(data.getAccountId(), "%", data.getCategory());
                    } else {
                        noteEntitiesLiveData = db.getNoteDao().searchUncategorizedByModified(data.getAccountId(), "%");
                    }
                    break;
            }
            noteEntitiesLiveData.observeForever((notes) -> noteEntities = notes);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataSetChanged() {
        // NoOp
    }

    @Override
    public void onDestroy() {
        // NoOp
    }

    @Override
    public int getCount() {
        if (noteEntities == null) {
            return 0;
        }

        return noteEntities.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews note_content;

        if (noteEntities == null || position > noteEntities.size() - 1 || noteEntities.get(position) == null) {
            Log.e(TAG, "Could not find position \"" + position + "\" in dbNotes list.");
            return null;
        }

        Note note = noteEntities.get(position);
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
            note_content.setImageViewResource(R.id.widget_entry_fav_icon_dark, note.getFavorite()
                    ? R.drawable.ic_star_yellow_24dp
                    : R.drawable.ic_star_grey_ccc_24dp);
        } else {
            note_content = new RemoteViews(context.getPackageName(), R.layout.widget_entry);
            note_content.setOnClickFillInIntent(R.id.widget_note_list_entry, fillInIntent);
            note_content.setTextViewText(R.id.widget_entry_content_tv, note.getTitle());
            note_content.setImageViewResource(R.id.widget_entry_fav_icon, note.getFavorite()
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
