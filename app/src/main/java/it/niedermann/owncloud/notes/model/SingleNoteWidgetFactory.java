package it.niedermann.owncloud.notes.model;

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
import it.niedermann.owncloud.notes.android.activity.SingleNoteWidget;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

public class SingleNoteWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mAppWidgetId;
    private static final String TAG = SingleNoteWidget.class.getSimpleName();

    public SingleNoteWidgetFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                            AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public boolean hasStableIds() {
        return true;
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
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    /**
     * getCount() always runs 1 because the list view only ever has a
     * single note displayed
     * @return
     */
    @Override
    public int getCount() {
        return 1;
    }

    /**
     * getViewAt -          Returns a RemoteView containing the note content in a TextView and
     *                      a fillInIntent to handle the user tapping on the item in the list
     *                      view.
     *
     * @param   position    The position of the item in the list
     * @return              The RemoteView at the specified position in the list
     */
    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews note_content = new RemoteViews(mContext.getPackageName(),
                                                    R.layout.widget_single_note_content);

        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        long noteID = sharedprefs.getLong(SingleNoteWidget.WIDGET_KEY + mAppWidgetId, -1);

        if (noteID >= 0) {
            NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(mContext);
            DBNote note = db.getNote(noteID);

            final Intent fillInIntent = new Intent();
            final Bundle extras = new Bundle();

            extras.putSerializable(EditNoteActivity.PARAM_NOTE, note);

            fillInIntent.putExtras(extras);
            fillInIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            note_content.setOnClickFillInIntent(R.id.single_note_content_tv, fillInIntent);
            note_content.setTextViewText(R.id.single_note_content_tv, note.getContent());
        } else {
            Log.e(TAG, "Note not found");
            note_content.setTextViewText(R.id.single_note_content_tv, "Note not found");
        }

        return note_content;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
