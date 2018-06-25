package it.niedermann.owncloud.notes.android.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class SingleNoteWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context mContext;
    private final int mAppWidgetId;

    private NoteSQLiteOpenHelper db;
    private DBNote note;

    private static final String TAG = SingleNoteWidget.class.getSimpleName();

    SingleNoteWidgetFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        db = NoteSQLiteOpenHelper.getInstance(mContext);
    }


    @Override
    public void onDataSetChanged() {
        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        long noteID = sharedprefs.getLong(SingleNoteWidget.WIDGET_KEY + mAppWidgetId, -1);

        if (noteID >= 0) {
            note = db.getNote(noteID);

            if (note == null) {
                Log.e(TAG, "Error: note not found");
            }
        }
    }

    @Override
    public void onDestroy() {

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

        RemoteViews note_content = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_single_note_content);
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();

        extras.putLong(EditNoteActivity.PARAM_NOTE_ID, note.getId());
        fillInIntent.putExtras(extras);
        fillInIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        note_content.setOnClickFillInIntent(R.id.single_note_content_tv, fillInIntent);
        note_content.setTextViewText(R.id.single_note_content_tv, note.getContent());

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
