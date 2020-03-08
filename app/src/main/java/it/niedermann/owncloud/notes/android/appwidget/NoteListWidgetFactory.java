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
import it.niedermann.owncloud.notes.android.DarkModeSetting;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NotesDatabase;
import it.niedermann.owncloud.notes.util.Notes;

import static it.niedermann.owncloud.notes.android.appwidget.NoteListWidget.DARK_THEME_KEY;

public class NoteListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final int displayMode;
    private final boolean darkTheme;
    private final String category;
    private final long accountId;
    private NotesDatabase db;
    private List<DBNote> dbNotes;

    NoteListWidgetFactory(Context context, Intent intent) {
        this.context = context;
        final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.context);
        displayMode = sp.getInt(NoteListWidget.WIDGET_MODE_KEY + appWidgetId, -1);
        DarkModeSetting theme = NoteWidgetHelper.getDarkThemeSetting(sp, DARK_THEME_KEY, appWidgetId);
        darkTheme = Notes.isDarkThemeActive(context, theme);
        category = sp.getString(NoteListWidget.WIDGET_CATEGORY_KEY + appWidgetId, "");
        accountId = sp.getLong(NoteListWidget.ACCOUNT_ID_KEY + appWidgetId, -1);
    }

    @Override
    public void onCreate() {
        db = NotesDatabase.getInstance(context);
    }

    @Override
    public void onDataSetChanged() {
        try {
            if (displayMode == NoteListWidget.NLW_DISPLAY_ALL) {
                dbNotes = db.getNotes(accountId);
            } else if (displayMode == NoteListWidget.NLW_DISPLAY_STARRED) {
                dbNotes = db.searchNotes(accountId, null, null, true);
            } else if (displayMode == NoteListWidget.NLW_DISPLAY_CATEGORY) {
                dbNotes = db.searchNotes(accountId, null, category, null);
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
    public RemoteViews getViewAt(int i) {
        RemoteViews note_content;

        if (dbNotes == null || dbNotes.get(i) == null) {
            return null;
        }

        DBNote note = dbNotes.get(i);
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
