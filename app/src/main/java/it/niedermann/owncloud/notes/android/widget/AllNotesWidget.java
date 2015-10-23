package it.niedermann.owncloud.notes.android.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.Note;
import it.niedermann.owncloud.notes.persistence.NoteSQLiteOpenHelper;

/**
 * Widget to display a List of all notes
 * Created by stefan on 08.10.15.
 */
public class AllNotesWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, StackWidgetService.class);
            // Add the app widget ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            // Instantiate the RemoteViews object for the app widget layout.

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_all_notes);
            rv.setRemoteAdapter(appWidgetId, intent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.layout.widget_all_notes);
        }
    }

    public class StackWidgetService extends RemoteViewsService {
        @Override
        public RemoteViewsFactory onGetViewFactory(Intent intent) {
            return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
        }
    }


    class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private static final int mCount = 10;
        private List<Note> mWidgetItems = new ArrayList<>();
        private Context mContext;
        private int mAppWidgetId;

        public StackRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            NoteSQLiteOpenHelper db = new NoteSQLiteOpenHelper(mContext);
            db.synchronizeWithServer();
            mWidgetItems = db.getNotes();
            mWidgetItems.add(new Note(0, Calendar.getInstance(), "Test-Titel", "Test-Beschreibung"));
        }

        public void onCreate() {
            mWidgetItems.add(new Note(0, Calendar.getInstance(), "Test-Titel", "Test-Beschreibung"));
        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return mWidgetItems.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            // Construct a remote views item based on the app widget item XML file,
            // and set the text based on the position.
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.fragment_notes_list_note_item);
            rv.setTextViewText(R.id.list_view, mWidgetItems.get(position).getTitle());

            // Return the remote views object.
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return mWidgetItems.get(position).getId();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
