package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.Category;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.SectionItem;
import it.niedermann.owncloud.notes.util.NoteUtil;

public class LoadNotesListTask extends AsyncTask<Void, Void, List<Item>> {

    public interface NotesLoadedListener {
        void onNotesLoaded(List<Item> notes, boolean showCategory);
    }

    private final Context context;
    private final NotesLoadedListener callback;
    private final Category category;
    private final CharSequence searchQuery;

    public LoadNotesListTask(@NonNull Context context, @NonNull NotesLoadedListener callback, @NonNull Category category, @Nullable CharSequence searchQuery) {
        this.context = context;
        this.callback = callback;
        this.category = category;
        this.searchQuery = searchQuery;
    }

    @Override
    protected List<Item> doInBackground(Void... voids) {
        List<DBNote> noteList;
        NoteSQLiteOpenHelper db = NoteSQLiteOpenHelper.getInstance(context);
        noteList = db.searchNotes(searchQuery, category.category, category.favorite);

        if (category.category == null) {
            return fillListByTime(noteList);
        } else {
            return fillListByCategory(noteList);
        }
    }

    @NonNull
    @WorkerThread
    private List<Item> fillListByCategory(@NonNull List<DBNote> noteList) {
        List<Item> itemList = new ArrayList<>();
        String currentCategory = category.category;
        for (DBNote note : noteList) {
            if (currentCategory != null && !currentCategory.equals(note.getCategory())) {
                itemList.add(new SectionItem(NoteUtil.extendCategory(note.getCategory())));
            }
            itemList.add(note);
            currentCategory = note.getCategory();
        }
        return itemList;
    }

    @NonNull
    @WorkerThread
    private List<Item> fillListByTime(@NonNull List<DBNote> noteList) {
        List<Item> itemList = new ArrayList<>();
        // #12 Create Sections depending on Time
        boolean todaySet, yesterdaySet, weekSet, monthSet, earlierSet;
        todaySet = yesterdaySet = weekSet = monthSet = earlierSet = false;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Calendar yesterday = Calendar.getInstance();
        yesterday.set(Calendar.DAY_OF_YEAR, yesterday.get(Calendar.DAY_OF_YEAR) - 1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);
        Calendar week = Calendar.getInstance();
        week.set(Calendar.DAY_OF_WEEK, week.getFirstDayOfWeek());
        week.set(Calendar.HOUR_OF_DAY, 0);
        week.set(Calendar.MINUTE, 0);
        week.set(Calendar.SECOND, 0);
        week.set(Calendar.MILLISECOND, 0);
        Calendar month = Calendar.getInstance();
        month.set(Calendar.DAY_OF_MONTH, 0);
        month.set(Calendar.HOUR_OF_DAY, 0);
        month.set(Calendar.MINUTE, 0);
        month.set(Calendar.SECOND, 0);
        month.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < noteList.size(); i++) {
            DBNote currentNote = noteList.get(i);
            if (currentNote.isFavorite()) {
                // don't show as new section
            } else if (!todaySet && currentNote.getModified().getTimeInMillis() >= today.getTimeInMillis()) {
                // after 00:00 today
                if (i > 0) {
                    itemList.add(new SectionItem(context.getResources().getString(R.string.listview_updated_today)));
                }
                todaySet = true;
            } else if (!yesterdaySet && currentNote.getModified().getTimeInMillis() < today.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= yesterday.getTimeInMillis()) {
                // between today 00:00 and yesterday 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(context.getResources().getString(R.string.listview_updated_yesterday)));
                }
                yesterdaySet = true;
            } else if (!weekSet && currentNote.getModified().getTimeInMillis() < yesterday.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= week.getTimeInMillis()) {
                // between yesterday 00:00 and start of the week 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(context.getResources().getString(R.string.listview_updated_this_week)));
                }
                weekSet = true;
            } else if (!monthSet && currentNote.getModified().getTimeInMillis() < week.getTimeInMillis() && currentNote.getModified().getTimeInMillis() >= month.getTimeInMillis()) {
                // between start of the week 00:00 and start of the month 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(context.getResources().getString(R.string.listview_updated_this_month)));
                }
                monthSet = true;
            } else if (!earlierSet && currentNote.getModified().getTimeInMillis() < month.getTimeInMillis()) {
                // before start of the month 00:00
                if (i > 0) {
                    itemList.add(new SectionItem(context.getResources().getString(R.string.listview_updated_earlier)));
                }
                earlierSet = true;
            }
            itemList.add(currentNote);
        }

        return itemList;
    }

    @Override
    protected void onPostExecute(List<Item> items) {
        callback.onNotesLoaded(items, category.category == null);
    }
}
