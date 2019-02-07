package it.niedermann.owncloud.notes.android.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class NoteListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final int displayMode;
    private final int appWidgetId;
    private final boolean darkTheme;
    private String category;
    private final SharedPreferences sp;
    private NoteSQLiteOpenHelper db;
    private List<DBNote> dbNotes;

    NoteListWidgetFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                            AppWidgetManager.INVALID_APPWIDGET_ID);
        sp = PreferenceManager.getDefaultSharedPreferences(this.context);
        displayMode = sp.getInt(NoteListWidget.WIDGET_MODE_KEY + appWidgetId, -1);
        darkTheme = sp.getBoolean(NoteListWidget.DARK_THEME_KEY + appWidgetId, false);
        category = sp.getString(NoteListWidget.WIDGET_CATEGORY_KEY + appWidgetId, "");
    }

    @Override
    public void onCreate() {
        db = NoteSQLiteOpenHelper.getInstance(context);
    }

    @Override
    public void onDataSetChanged() {
        if (displayMode == NoteListWidget.NLW_DISPLAY_ALL) {
            dbNotes = db.getNotes();
        } else if (displayMode == NoteListWidget.NLW_DISPLAY_STARRED) {
            dbNotes = db.searchNotes(null,null, true);
        } else if (displayMode == NoteListWidget.NLW_DISPLAY_CATEGORY) {
            dbNotes = db.searchNotes(null, category,  null);
        }
    }

    @Override
    public void onDestroy() {

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
    public RemoteViews getViewAt(int i) {
        RemoteViews note_content;

        if (dbNotes == null || dbNotes.get(i) == null) {
            return null;
        }

        DBNote note = dbNotes.get(i);
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();

        extras.putLong(EditNoteActivity.PARAM_NOTE_ID, note.getId());
        fillInIntent.putExtras(extras);
        fillInIntent.setData(Uri.parse(fillInIntent.toUri(Intent.URI_INTENT_SCHEME)));

        if (darkTheme) {
            note_content = new RemoteViews(context.getPackageName(), R.layout.widget_entry_dark);
            note_content.setOnClickFillInIntent(R.id.widget_note_list_entry_dark, fillInIntent);
            note_content.setTextViewText(R.id.widget_entry_content_tv_dark, note.getTitle());

            if (note.isFavorite()) {
                note_content.setImageViewResource(R.id.widget_entry_fav_icon_dark, R.drawable.ic_star_yellow_24dp);
            } else {
                note_content.setImageViewResource(R.id.widget_entry_fav_icon_dark, R.drawable.ic_star_grey_ccc_24dp);
            }
        } else {
            note_content = new RemoteViews(context.getPackageName(), R.layout.widget_entry);
            note_content.setOnClickFillInIntent(R.id.widget_note_list_entry, fillInIntent);
            note_content.setTextViewText(R.id.widget_entry_content_tv, note.getTitle());

            if (note.isFavorite()) {
                note_content.setImageViewResource(R.id.widget_entry_fav_icon, R.drawable.ic_star_yellow_24dp);
            } else {
                note_content.setImageViewResource(R.id.widget_entry_fav_icon, R.drawable.ic_star_grey_ccc_24dp);
            }
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
