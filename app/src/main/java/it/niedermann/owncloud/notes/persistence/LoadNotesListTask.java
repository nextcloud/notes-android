//package it.niedermann.owncloud.notes.persistence;
//
//import android.content.Context;
//import android.os.AsyncTask;
//import android.text.TextUtils;
//import android.text.format.DateUtils;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.annotation.WorkerThread;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
//import it.niedermann.owncloud.notes.R;
//import it.niedermann.owncloud.notes.main.items.section.SectionItem;
//import it.niedermann.owncloud.notes.persistence.entity.Note;
//import it.niedermann.owncloud.notes.persistence.entity.NoteWithCategory;
//import it.niedermann.owncloud.notes.shared.model.OldCategory;
//import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
//import it.niedermann.owncloud.notes.shared.model.Item;
//import it.niedermann.owncloud.notes.shared.util.NoteUtil;
//
//public class LoadNotesListTask extends AsyncTask<Void, Void, List<Item>> {
//
//    private final Context context;
//    private final NotesLoadedListener callback;
//    private final OldCategory category;
//    private final String searchQuery;
//    private final long accountId;
//
//    public LoadNotesListTask(long accountId, @NonNull Context context, @NonNull NotesLoadedListener callback, @NonNull OldCategory category, @Nullable CharSequence searchQuery) {
//        this.context = context;
//        this.callback = callback;
//        this.category = category;
//        this.searchQuery = searchQuery == null ? "%" : "%" + searchQuery + "%";
//        this.accountId = accountId;
//    }
//
//    @Override
//    protected List<Item> doInBackground(Void... voids) {
//        List<NoteWithCategory> noteList;
//        NotesDatabase db = NotesDatabase.getInstance(context);
//        CategorySortingMethod sortingMethod = db.getCategoryOrder(accountId, category);
//
//        if(Boolean.TRUE.equals(category.favorite)) {
//            noteList = db.getNoteDao().searchNotesByCategoryFavoritesDirectly(accountId, searchQuery, sortingMethod);
//        } else if(TextUtils.isEmpty(category.category)) {
//            noteList = db.getNoteDao().searchNotesByUncategorizedDirectly(accountId, searchQuery, sortingMethod);
//        } else {
//            noteList = db.getNoteDao().searchNotesByCategoryDirectly(accountId, searchQuery, category.category, sortingMethod);
//        }
//
////        if (category.category == null) {
////            if (sortingMethod == CategorySortingMethod.SORT_MODIFIED_DESC) {
////                return fillListByTime(noteList);
////            } else {
////                return fillListByInitials(noteList);
////            }
////        } else {
////            return fillListByCategory(noteList);
////        }
//    }
//
//    @Override
//    protected void onPostExecute(List<Item> items) {
//        callback.onNotesLoaded(items, category.category == null, searchQuery);
//    }
//
//    public interface NotesLoadedListener {
//        void onNotesLoaded(List<Item> notes, boolean showCategory, CharSequence searchQuery);
//    }
//    }
//}
