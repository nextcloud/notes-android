package it.niedermann.owncloud.notes.android.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
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
    }

    @Override
    public void onCreate() {
        db = NoteSQLiteOpenHelper.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_note_list);
        AppWidgetManager awm = AppWidgetManager.getInstance(context);

        switch (displayMode)
        {
            case 0:
                views.setTextViewText(R.id.widget_note_list_title_tv, context.getString(R.string.app_name));
                break;
            case 1:
                views.setTextViewText(R.id.widget_note_list_title_tv, "Starred");
                break;
            case 2:
                category = sp.getString(NoteListWidget.WIDGET_CATEGORY_KEY + appWidgetId, null);
                if (category.equals("")) {
                    views.setTextViewText(R.id.widget_note_list_title_tv, context.getString(R.string.action_uncategorized));
                } else {
                    views.setTextViewText(R.id.widget_note_list_title_tv, category);
                }
                break;
        }

        awm.updateAppWidget(appWidgetId, views);
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
        if (dbNotes == null || dbNotes.get(i) == null) {
            return null;
        }

        RemoteViews note_content = new RemoteViews(context.getPackageName(),
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
