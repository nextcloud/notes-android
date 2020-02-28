package it.niedermann.owncloud.notes.persistence;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.model.Category;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.SectionItem;
import it.niedermann.owncloud.notes.util.NoteUtil;

public class LoadNotesListTask extends AsyncTask<Void, Void, List<Item>> {

    private final Context context;
    private final NotesLoadedListener callback;
    private final Category category;
    private final CharSequence searchQuery;
    private final long accountId;

    public LoadNotesListTask(long accountId, @NonNull Context context, @NonNull NotesLoadedListener callback, @NonNull Category category, @Nullable CharSequence searchQuery) {
        this.context = context;
        this.callback = callback;
        this.category = category;
        this.searchQuery = searchQuery;
        this.accountId = accountId;
    }

    @Override
    protected List<Item> doInBackground(Void... voids) {
        List<DBNote> noteList;
        NotesDatabase db = NotesDatabase.getInstance(context);
        noteList = db.searchNotes(accountId, searchQuery, category.category, category.favorite);

        if (category.category == null) {
            return fillListByTime(noteList);
        } else {
            return fillListByCategory(noteList);
        }
    }

    private DBNote colorTheNote(DBNote dbNote) {
        if (!TextUtils.isEmpty(searchQuery)) {
            SpannableString spannableString = new SpannableString(dbNote.getTitle());
            Matcher matcher = Pattern.compile("(" + searchQuery + ")", Pattern.CASE_INSENSITIVE).matcher(spannableString);
            while (matcher.find()) {
                spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.primary_dark)),
                        matcher.start(), matcher.end(), 0);
            }

            dbNote.setTitle(Html.toHtml(spannableString));

            spannableString = new SpannableString(dbNote.getExcerpt());
            matcher = Pattern.compile("(" + searchQuery + ")", Pattern.CASE_INSENSITIVE).matcher(spannableString);
            while (matcher.find()) {
                spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.primary_dark)),
                        matcher.start(), matcher.end(), 0);
            }

            dbNote.setExcerpt(Html.toHtml(spannableString));
        }

        return dbNote;
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

            itemList.add(colorTheNote(note));
            currentCategory = note.getCategory();
        }
        return itemList;
    }

    @NonNull
    @WorkerThread
    private List<Item> fillListByTime(@NonNull List<DBNote> noteList) {
        List<Item> itemList = new ArrayList<>();
        Timeslotter timeslotter = new Timeslotter();
        String lastTimeslot = null;
        for (int i = 0; i < noteList.size(); i++) {
            DBNote currentNote = noteList.get(i);
            String timeslot = timeslotter.getTimeslot(currentNote);
            if(i > 0 && !timeslot.equals(lastTimeslot)) {
                itemList.add(new SectionItem(timeslot));
            }
            itemList.add(colorTheNote(currentNote));
            lastTimeslot = timeslot;
        }

        return itemList;
    }

    @Override
    protected void onPostExecute(List<Item> items) {
        callback.onNotesLoaded(items, category.category == null);
    }

    public interface NotesLoadedListener {
        void onNotesLoaded(List<Item> notes, boolean showCategory);
    }

    private class Timeslotter {
        private final List<Timeslot> timeslots = new ArrayList<>();
        private final Calendar lastYear;

        Timeslotter() {
            Calendar now = Calendar.getInstance();
            int month = now.get(Calendar.MONTH);
            int day = now.get(Calendar.DAY_OF_MONTH);
            int offsetWeekStart = (now.get(Calendar.DAY_OF_WEEK) - now.getFirstDayOfWeek() + 7) % 7;
            timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_today), month, day));
            timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_yesterday), month,day - 1));
            timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_this_week), month,day - offsetWeekStart));
            timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_last_week), month,day - offsetWeekStart - 7));
            timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_this_month), month,1));
            timeslots.add(new Timeslot(context.getResources().getString(R.string.listview_updated_last_month), month - 1, 1));
            lastYear = Calendar.getInstance();
            lastYear.set(now.get(Calendar.YEAR) - 1, 0, 1, 0, 0, 0);
        }

        String getTimeslot(DBNote note) {
            if (note.isFavorite()) {
                return "";
            }
            Calendar modified = note.getModified();
            for (Timeslot timeslot : timeslots) {
                if (!modified.before(timeslot.time)) {
                    return timeslot.label;
                }
            }
            if (!modified.before(this.lastYear)) {
                // use YEAR and MONTH in a format based on current locale
                return DateUtils.formatDateTime(context, modified.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY);
            } else {
                return Integer.toString(modified.get(Calendar.YEAR));
            }
        }

        private class Timeslot {
            final String label;
            final Calendar time;

            Timeslot(String label, int month, int day) {
                this.label = label;
                this.time = Calendar.getInstance();
                this.time.set(this.time.get(Calendar.YEAR), month, day, 0, 0, 0);
            }
        }
    }
}
