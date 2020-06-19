package it.niedermann.owncloud.notes.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.EditNoteActivity;
import it.niedermann.owncloud.notes.model.ApiVersion;
import it.niedermann.owncloud.notes.model.Capabilities;
import it.niedermann.owncloud.notes.model.Category;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.ISyncCallback;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.model.NavigationAdapter;
import it.niedermann.owncloud.notes.model.NoteListsWidgetData;
import it.niedermann.owncloud.notes.model.SingleNoteWidgetData;
import it.niedermann.owncloud.notes.util.CategorySortingMethod;
import it.niedermann.owncloud.notes.util.ColorUtil;
import it.niedermann.owncloud.notes.util.NoteUtil;

import static it.niedermann.owncloud.notes.android.activity.EditNoteActivity.ACTION_SHORTCUT;
import static it.niedermann.owncloud.notes.android.appwidget.NoteListWidget.updateNoteListWidgets;
import static it.niedermann.owncloud.notes.android.appwidget.SingleNoteWidget.updateSingleNoteWidgets;
import static it.niedermann.owncloud.notes.model.NoteListsWidgetData.MODE_DISPLAY_CATEGORY;
import static it.niedermann.owncloud.notes.util.NoteUtil.generateNoteExcerpt;

/**
 * Helps to add, get, update and delete Notes with the option to trigger a Resync with the Server.
 */
public class NotesDatabase extends AbstractNotesDatabase {

    private static final String TAG = NotesDatabase.class.getSimpleName();

    private static final String default_order = key_favorite + " DESC, " + key_modified + " DESC";

    private static NotesDatabase instance;

    private final NoteServerSyncHelper serverSyncHelper;

    private NotesDatabase(@NonNull Context context) {
        super(context, database_name, null);
        serverSyncHelper = NoteServerSyncHelper.getInstance(this);
    }

    public static NotesDatabase getInstance(Context context) {
        if (instance == null)
            return instance = new NotesDatabase(context);
        else
            return instance;
    }

    public NoteServerSyncHelper getNoteServerSyncHelper() {
        return serverSyncHelper;
    }

    /**
     * Creates a new Note in the Database and adds a Synchronization Flag.
     *
     * @param note Note
     */
    public long addNoteAndSync(SingleSignOnAccount ssoAccount, long accountId, CloudNote note) {
        DBNote dbNote = new DBNote(0, 0, note.getModified(), note.getTitle(), note.getContent(), note.isFavorite(), note.getCategory(), note.getEtag(), DBStatus.LOCAL_EDITED, accountId, generateNoteExcerpt(note.getContent(), note.getTitle()), 0);
        long id = addNote(accountId, dbNote);
        notifyWidgets();
        getNoteServerSyncHelper().scheduleSync(ssoAccount, true);
        return id;
    }

    /**
     * Inserts a note directly into the Database.
     * No Synchronisation will be triggered! Use addNoteAndSync()!
     *
     * @param note Note to be added. Remotely created Notes must be of type CloudNote and locally created Notes must be of Type DBNote (with DBStatus.LOCAL_EDITED)!
     */
    long addNote(long accountId, CloudNote note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(11);
        if (note instanceof DBNote) {
            DBNote dbNote = (DBNote) note;
            if (dbNote.getId() > 0) {
                values.put(key_id, dbNote.getId());
            }
            values.put(key_status, dbNote.getStatus().getTitle());
            values.put(key_account_id, dbNote.getAccountId());
            values.put(key_excerpt, dbNote.getExcerpt());
        } else {
            values.put(key_status, DBStatus.VOID.getTitle());
            values.put(key_account_id, accountId);
            values.put(key_excerpt, generateNoteExcerpt(note.getContent(), note.getTitle()));
        }
        if (note.getRemoteId() > 0) {
            values.put(key_remote_id, note.getRemoteId());
        }
        values.put(key_title, note.getTitle());
        values.put(key_modified, note.getModified().getTimeInMillis() / 1000);
        values.put(key_content, note.getContent());
        values.put(key_favorite, note.isFavorite());
        values.put(key_category, getCategoryIdByTitle(accountId, note.getCategory()));
        values.put(key_etag, note.getEtag());
        return db.insert(table_notes, null, values);
    }

    public void moveNoteToAnotherAccount(SingleSignOnAccount ssoAccount, long oldAccountId, DBNote note, long newAccountId) {
        // Add new note
        addNoteAndSync(ssoAccount, newAccountId, new CloudNote(0, note.getModified(), note.getTitle(), note.getContent(), note.isFavorite(), note.getCategory(), null));
        deleteNoteAndSync(ssoAccount, note.getId());

        notifyWidgets();
        getNoteServerSyncHelper().scheduleSync(ssoAccount, true);
    }

    /**
     * Get a single Note by ID
     *
     * @param id int - ID of the requested Note
     * @return requested Note
     */
    public DBNote getNote(long accountId, long id) {
        List<DBNote> notes = getNotesCustom(accountId, key_id + " = ? AND " + key_status + " != ? AND " + key_account_id + " = ? ", new String[]{String.valueOf(id), DBStatus.LOCAL_DELETED.getTitle(), "" + accountId}, null, false);
        return notes.isEmpty() ? null : notes.get(0);
    }

    /**
     * Gets all the remoteIds of all not deleted notes of an account
     *
     * @param accountId get the remoteIds from all notes of this account
     * @return set of remoteIds from all notes
     */
    public Set<String> getRemoteIds(long accountId) {
        Cursor cursor = getReadableDatabase()
                .query(
                        table_notes,
                        new String[]{key_remote_id},
                        key_status + " != ? AND " + key_account_id + " = ?",
                        new String[]{DBStatus.LOCAL_DELETED.getTitle(), "" + accountId},
                        null,
                        null,
                        null
                );
        Set<String> remoteIds = new HashSet<>();
        while (cursor.moveToNext()) {
            remoteIds.add(cursor.getString(0));
        }
        cursor.close();
        return remoteIds;
    }

    /**
     * Get a single Note by remote Id (aka. nextcloud file id)
     *
     * @param remoteId int - remote ID of the requested Note
     * @return requested Note
     */
    public long getLocalIdByRemoteId(long accountId, long remoteId) {
        List<DBNote> notes = getNotesCustom(accountId, key_remote_id + " = ? AND " + key_status + " != ? AND " + key_account_id + " = ? ", new String[]{String.valueOf(remoteId), DBStatus.LOCAL_DELETED.getTitle(), "" + accountId}, null, true);
        if (notes.isEmpty() || notes.get(0) == null) {
            throw new IllegalArgumentException("There is no note with remoteId \"" + remoteId + "\"");
        }
        return notes.get(0).getId();
    }

    /**
     * Query the database with a custom raw query.
     *
     * @param selection     A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself).
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
     * @param orderBy       How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.
     * @return List of Notes
     */
    @NonNull
    @WorkerThread
    private List<DBNote> getNotesCustom(long accountId, @NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy, boolean pruneContent) {
        return this.getNotesCustom(accountId, selection, selectionArgs, orderBy, null, pruneContent);
    }

    @NonNull
    @WorkerThread
    private List<DBNote> getNotesCustom(long accountId, @NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy, @Nullable String limit, boolean pruneContent) {
        if (selectionArgs.length > 2) {
            Log.v(TAG, selection + "   ----   " + selectionArgs[0] + " " + selectionArgs[1] + " " + selectionArgs[2]);
        }
        String cols = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
                key_id, key_remote_id, key_status, key_title, key_modified, key_favorite, key_category_title, key_etag, key_excerpt, key_scroll_y);
        if (!pruneContent) {
            cols = String.format("%s, %s", cols, key_content);
        }
        String rawQuery = "SELECT " + cols + " FROM " + table_notes + " INNER JOIN " + table_category + " ON " + key_category + " = " + key_category_id +
                " WHERE " + selection + (orderBy == null ? "" : " ORDER BY " + orderBy) + (limit == null ? "" : " LIMIT " + limit);
        Cursor cursor = getReadableDatabase().rawQuery(rawQuery, selectionArgs);

        List<DBNote> notes = new ArrayList<>();
        while (cursor.moveToNext()) {
            notes.add(getNoteFromCursor(accountId, cursor, pruneContent));
        }
        cursor.close();
        return notes;
    }

    /**
     * Creates a DBNote object from the current row of a Cursor.
     *
     * @param cursor       database cursor
     * @param pruneContent whether or not the content should be pruned for performance reasons
     * @return DBNote
     */
    @NonNull
    private DBNote getNoteFromCursor(long accountId, @NonNull Cursor cursor, boolean pruneContent) {
        validateAccountId(accountId);
        Calendar modified = Calendar.getInstance();
        modified.setTimeInMillis(cursor.getLong(4) * 1000);
        return new DBNote(
                cursor.getLong(0),
                cursor.getLong(1),
                modified,
                cursor.getString(3),
                pruneContent ? "" : cursor.getString(10),
                cursor.getInt(5) > 0,
                cursor.getString(6),
                cursor.getString(7),
                DBStatus.parse(cursor.getString(2)),
                accountId,
                cursor.getString(8),
                cursor.getInt(9)
        );
    }

    @NonNull
    @WorkerThread
    public Map<Long, Long> getIdMap(long accountId) {
        validateAccountId(accountId);
        Map<Long, Long> result = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table_notes, new String[]{key_remote_id, key_id}, key_status + " != ? AND " + key_account_id + " = ? ", new String[]{DBStatus.LOCAL_DELETED.getTitle(), "" + accountId}, null, null, null);
        while (cursor.moveToNext()) {
            result.put(cursor.getLong(0), cursor.getLong(1));
        }
        cursor.close();
        return result;
    }

    /**
     * Returns a list of all Notes in the Database
     *
     * @return List&lt;Note&gt;
     */
    @NonNull
    @WorkerThread
    public List<DBNote> getNotes(long accountId) {
        validateAccountId(accountId);
        return getNotesCustom(accountId, key_status + " != ? AND " + key_account_id + " = ?", new String[]{DBStatus.LOCAL_DELETED.getTitle(), "" + accountId}, default_order, false);
    }

    @NonNull
    @WorkerThread
    public List<DBNote> getRecentNotes(long accountId) {
        validateAccountId(accountId);
        return getNotesCustom(accountId, key_status + " != ? AND " + key_account_id + " = ?", new String[]{DBStatus.LOCAL_DELETED.getTitle(), "" + accountId}, key_modified + " DESC", "4", true);
    }

    /**
     * This method is overloading searchNotes method.
     * In order to keep the original code (called this method) still work.
     *
     * @return List&lt;Note&gt;
     */
    @NonNull
    @WorkerThread
    public List<DBNote> searchNotes(long accountId, @Nullable CharSequence query,
                                    @Nullable String category, @Nullable Boolean favorite) {
        return searchNotes(accountId, query, category, favorite, null);
    }

    /**
     * Returns a list of all Notes in the Database
     * This method only supports to return the notes in the categories with the matched title or the notes in the categories whose ancestor category matches
     * For example, three categories with the title "abc", "abc/aaa" and "abcd"
     * If search with "abc", then only notes in "abc" and "abc/aaa" will be returned.
     *
     * @return List&lt;Note&gt;
     */
    @NonNull
    @WorkerThread
    public List<DBNote> searchNotes(long accountId, @Nullable CharSequence query,
                                    @Nullable String category, @Nullable Boolean favorite,
                                    @Nullable CategorySortingMethod sortingMethod) {
        validateAccountId(accountId);
        List<String> where = new ArrayList<>();
        List<String> args = new ArrayList<>();

        where.add(key_status + " != ?");
        args.add(DBStatus.LOCAL_DELETED.getTitle());

        where.add(key_account_id + " = ?");
        args.add("" + accountId);

        if (query != null) {
            where.add(key_status + " != ?");
            args.add(DBStatus.LOCAL_DELETED.getTitle());

            where.add("(" + key_title + " LIKE ? OR " + key_content + " LIKE ? OR " + key_category + " LIKE ?" + ")");
            args.add("%" + query + "%");
            args.add("%" + query + "%");
            args.add("%" + query + "%");
        }

        if (category != null) {
            where.add(key_category + " IN (SELECT " + key_category_id + " FROM " + table_category +
                    " WHERE " + key_category_title + " =? OR " + key_category_title + " LIKE ?)");
            args.add(category);
            args.add(category + "/%");
        }

        if (favorite != null) {
            where.add(key_favorite + "=?");
            args.add(favorite ? "1" : "0");
        }

        String order = category == null ? default_order : key_category + ", " + key_title;
        // TODO: modify here, need to test
        if (sortingMethod != null) {
            order = key_favorite + " DESC," + sortingMethod.getSorder();
        }
        return getNotesCustom(accountId, TextUtils.join(" AND ", where), args.toArray(new String[]{}), order, true);
    }

    /**
     * Returns a list of all Notes in the Database which were modified locally
     *
     * @return List&lt;Note&gt;
     */
    @NonNull
    @WorkerThread
    List<DBNote> getLocalModifiedNotes(long accountId) {
        validateAccountId(accountId);
        return getNotesCustom(accountId, key_status + " != ? AND " + key_account_id + " = ?", new String[]{DBStatus.VOID.getTitle(), "" + accountId}, null, false);
    }

    @NonNull
    @WorkerThread
    public Map<String, Integer> getFavoritesCount(long accountId) {
        validateAccountId(accountId);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                table_notes,
                new String[]{key_favorite, "COUNT(*)"},
                key_status + " != ? AND " + key_account_id + " = ?",
                new String[]{DBStatus.LOCAL_DELETED.getTitle(), "" + accountId},
                key_favorite,
                null,
                key_favorite);
        Map<String, Integer> favorites = new HashMap<>(cursor.getCount());
        while (cursor.moveToNext()) {
            favorites.put(cursor.getString(0), cursor.getInt(1));
        }
        cursor.close();
        return favorites;
    }

    /**
     * This method return all of the categories with given accountId
     *
     * @param accountId The user account Id
     * @return All of the categories with given accountId
     */
    @NonNull
    @WorkerThread
    public List<NavigationAdapter.CategoryNavigationItem> getCategories(long accountId) {
        return searchCategories(accountId, null);
    }

    /**
     * This method return the category list containing all of the categories containing the
     * search pattern and matched with the given accountId
     * The join operation is used because it is needed that the number of notes in each category
     * If search pattern is null, this method will return all of the categories for corresponding accountId
     *
     * @param accountId The user account ID
     * @param search    The search pattern
     * @return The category list containing all of the categories matched
     */
    @NonNull
    @WorkerThread
    public List<NavigationAdapter.CategoryNavigationItem> searchCategories(long accountId, String search) {
        validateAccountId(accountId);
        String columns = key_category_id + ", " + key_category_title + ", COUNT(*)";
        String selection = key_status + " != ?  AND " +
                key_category_account_id + " = ? AND " +
                key_category_title + " LIKE ? " +
                (search == null ? "" : " AND " + key_category_title + " != \"\"");
        String rawQuery = "SELECT " + columns +
                " FROM " + table_category +
                " INNER JOIN " + table_notes +
                " ON " + key_category + " = " + key_category_id +
                " WHERE " + selection +
                " GROUP BY " + key_category_title;

        Cursor cursor = getReadableDatabase().rawQuery(rawQuery,
                new String[]{DBStatus.LOCAL_DELETED.getTitle(), String.valueOf(accountId),
                        search == null ? "%" : "%" + search.trim() + "%"});

        List<NavigationAdapter.CategoryNavigationItem> categories = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Resources res = getContext().getResources();
            String category = cursor.getString(1).toLowerCase();
            int icon = NavigationAdapter.ICON_FOLDER;
            if (category.equals(res.getString(R.string.category_music).toLowerCase())) {
                icon = R.drawable.ic_library_music_grey600_24dp;
            } else if (category.equals(res.getString(R.string.category_movies).toLowerCase()) || category.equals(res.getString(R.string.category_movie).toLowerCase())) {
                icon = R.drawable.ic_local_movies_grey600_24dp;
            } else if (category.equals(res.getString(R.string.category_work).toLowerCase())) {
                icon = R.drawable.ic_work_grey600_24dp;
            }
            categories.add(new NavigationAdapter.CategoryNavigationItem("category:" + cursor.getString(1), cursor.getString(1), cursor.getInt(2), icon, cursor.getLong(0)));
        }

        cursor.close();
        return categories;
    }

    public String getCategoryTitleById(long accountId, long categoryId) {
        validateAccountId(accountId);
        final String categoryTitle;
        final Cursor cursor = getReadableDatabase().query(table_category, new String[]{key_category_title}, key_category_id + " = ?", new String[]{String.valueOf(categoryId)}, null, null, null);
        if (cursor.moveToFirst()) {
            categoryTitle = cursor.getString(0);
        } else {
            categoryTitle = null;
        }
        cursor.close();
        return categoryTitle;
    }

    public void toggleFavorite(SingleSignOnAccount ssoAccount, @NonNull DBNote note, @Nullable ISyncCallback callback) {
        note.setFavorite(!note.isFavorite());
        note.setStatus(DBStatus.LOCAL_EDITED);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(2);
        values.put(key_status, note.getStatus().getTitle());
        values.put(key_favorite, note.isFavorite() ? "1" : "0");
        db.update(table_notes, values, key_id + " = ?", new String[]{String.valueOf(note.getId())});
        if (callback != null) {
            serverSyncHelper.addCallbackPush(ssoAccount, callback);
        }
        serverSyncHelper.scheduleSync(ssoAccount, true);
    }

    /**
     * Set the category for a given note.
     * This method will search in the database to find out the category id in the db.
     * If there is no such category existing, this method will create it and search again.
     *
     * @param ssoAccount The single sign on account
     * @param note       The note which will be updated
     * @param category   The category title which should be used to find the category id.
     * @param callback   When the synchronization is finished, this callback will be invoked (optional).
     */
    public void setCategory(SingleSignOnAccount ssoAccount, @NonNull DBNote note, @NonNull String category, @Nullable ISyncCallback callback) {
        note.setCategory(category);
        note.setStatus(DBStatus.LOCAL_EDITED);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(2);
        values.put(key_status, note.getStatus().getTitle());
        int id = getCategoryIdByTitle(note.getAccountId(), note.getCategory());
        values.put(key_category, id);
        db.update(table_notes, values, key_id + " = ?", new String[]{String.valueOf(note.getId())});
        removeEmptyCategory(note.getAccountId());
        if (callback != null) {
            serverSyncHelper.addCallbackPush(ssoAccount, callback);
        }
        serverSyncHelper.scheduleSync(ssoAccount, true);
    }

    private long addCategory(long accountId, @NonNull String title) {
        validateAccountId(accountId);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues(2);
        values.put(key_category_account_id, accountId);
        values.put(key_category_title, title);
        return db.insert(table_category, null, values);
    }

    public DBNote updateNoteAndSync(SingleSignOnAccount ssoAccount, @NonNull LocalAccount localAccount, @NonNull DBNote oldNote, @Nullable String newContent, @Nullable ISyncCallback callback) {
        return updateNoteAndSync(ssoAccount, localAccount, oldNote, newContent, null, callback);
    }

    /**
     * Updates a single Note with a new content.
     * The title is derived from the new content automatically, and modified date as well as DBStatus are updated, too -- if the content differs to the state in the database.
     *
     * @param oldNote    Note to be changed
     * @param newContent New content. If this is <code>null</code>, then <code>oldNote</code> is saved again (useful for undoing changes).
     * @param newTitle   New title. If this is <code>null</code>, then either the old title is reused (in case the note has been synced before) or a title is generated (in case it is a new note)
     * @param callback   When the synchronization is finished, this callback will be invoked (optional).
     * @return changed note if differs from database, otherwise the old note.
     */
    public DBNote updateNoteAndSync(SingleSignOnAccount ssoAccount, @NonNull LocalAccount localAccount, @NonNull DBNote oldNote, @Nullable String newContent, @Nullable String newTitle, @Nullable ISyncCallback callback) {
        DBNote newNote;
        if (newContent == null) {
            newNote = new DBNote(oldNote.getId(), oldNote.getRemoteId(), oldNote.getModified(), oldNote.getTitle(), oldNote.getContent(), oldNote.isFavorite(), oldNote.getCategory(), oldNote.getEtag(), DBStatus.LOCAL_EDITED, localAccount.getId(), oldNote.getExcerpt(), oldNote.getScrollY());
        } else {
            final String title;
            if (newTitle != null) {
                title = newTitle;
            } else {
                if (oldNote.getRemoteId() == 0 || localAccount.getPreferredApiVersion() == null || localAccount.getPreferredApiVersion().compareTo(new ApiVersion("1.0", 0, 0)) < 0) {
                    title = NoteUtil.generateNonEmptyNoteTitle(newContent, getContext());
                } else {
                    title = oldNote.getTitle();
                }
            }
            newNote = new DBNote(oldNote.getId(), oldNote.getRemoteId(), Calendar.getInstance(), title, newContent, oldNote.isFavorite(), oldNote.getCategory(), oldNote.getEtag(), DBStatus.LOCAL_EDITED, localAccount.getId(), generateNoteExcerpt(newContent, title), oldNote.getScrollY());
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(7);
        values.put(key_status, newNote.getStatus().getTitle());
        values.put(key_title, newNote.getTitle());
        values.put(key_category, getCategoryIdByTitle(newNote.getAccountId(), newNote.getCategory()));
        values.put(key_modified, newNote.getModified().getTimeInMillis() / 1000);
        values.put(key_content, newNote.getContent());
        values.put(key_excerpt, newNote.getExcerpt());
        values.put(key_scroll_y, newNote.getScrollY());
        int rows = db.update(table_notes, values, key_id + " = ? AND (" + key_content + " != ? OR " + key_category + " != ?)", new String[]{String.valueOf(newNote.getId()), newNote.getContent(), newNote.getCategory()});
        removeEmptyCategory(localAccount.getId());
        // if data was changed, set new status and schedule sync (with callback); otherwise invoke callback directly.
        if (rows > 0) {
            notifyWidgets();
            if (callback != null) {
                serverSyncHelper.addCallbackPush(ssoAccount, callback);
            }
            serverSyncHelper.scheduleSync(ssoAccount, true);
            return newNote;
        } else {
            if (callback != null) {
                callback.onFinish();
            }
            return oldNote;
        }
    }

    public void updateScrollY(long noteId, int scrollY) {
        Log.e(TAG, "Updated scrollY: " + scrollY);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(1);
        values.put(key_scroll_y, scrollY);
        db.update(table_notes, values, key_id + " = ? ", new String[]{String.valueOf(noteId)});
    }

    /**
     * Updates a single Note with data from the server, (if it was not modified locally).
     * Thereby, an optimistic concurrency control is realized in order to prevent conflicts arising due to parallel changes from the UI and synchronization.
     * This is used by the synchronization task, hence no Synchronization will be triggered. Use updateNoteAndSync() instead!
     *
     * @param id                        local ID of Note
     * @param remoteNote                Note from the server.
     * @param forceUnchangedDBNoteState is not null, then the local note is updated only if it was not modified meanwhile
     */
    void updateNote(LocalAccount localAccount, long id, @NonNull CloudNote remoteNote, @Nullable DBNote forceUnchangedDBNoteState) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First, update the remote ID, since this field cannot be changed in parallel, but have to be updated always.
        ContentValues values = new ContentValues(8);
        values.put(key_remote_id, remoteNote.getRemoteId());
        db.update(table_notes, values, key_id + " = ?", new String[]{String.valueOf(id)});

        // The other columns have to be updated in dependency of forceUnchangedDBNoteState,
        // since the Synchronization-Task must not overwrite locales changes!
        values.clear();
        values.put(key_status, DBStatus.VOID.getTitle());
        values.put(key_title, remoteNote.getTitle());
        values.put(key_modified, remoteNote.getModified().getTimeInMillis() / 1000);
        values.put(key_content, remoteNote.getContent());
        values.put(key_favorite, remoteNote.isFavorite());
        values.put(key_category, getCategoryIdByTitle(localAccount.getId(), remoteNote.getCategory()));
        values.put(key_etag, remoteNote.getEtag());
        values.put(key_excerpt, generateNoteExcerpt(remoteNote.getContent(), remoteNote.getTitle()));
        String whereClause;
        String[] whereArgs;
        if (forceUnchangedDBNoteState != null) {
            // used by: NoteServerSyncHelper.SyncTask.pushLocalChanges()
            // update only, if not modified locally during the synchronization
            // (i.e. all (!) user changeable columns (content, favorite, category) must still have the same value),
            // uses reference value gathered at start of synchronization
            whereClause = key_id + " = ? AND " + key_content + " = ? AND " + key_favorite + " = ? AND " + key_category + " = ?";
            whereArgs = new String[]{String.valueOf(id), forceUnchangedDBNoteState.getContent(), forceUnchangedDBNoteState.isFavorite() ? "1" : "0", String.valueOf(getCategoryIdByTitle(localAccount.getId(), forceUnchangedDBNoteState.getCategory()))};
        } else {
            // used by: NoteServerSyncHelper.SyncTask.pullRemoteChanges()
            // update only, if not modified locally (i.e. STATUS="") and if modified remotely (i.e. any (!) column has changed)
            whereClause = key_id + " = ? AND " + key_status + " = ? AND (" + key_modified + "!=? OR " + key_title + "!=? OR " + key_favorite + "!=? OR " + key_category + "!=? OR " + (remoteNote.getEtag() != null ? key_etag + " IS NULL OR " : "") + key_etag + "!=? OR " + key_content + "!=?)";
            whereArgs = new String[]{String.valueOf(id), DBStatus.VOID.getTitle(), Long.toString(remoteNote.getModified().getTimeInMillis() / 1000), remoteNote.getTitle(), remoteNote.isFavorite() ? "1" : "0", remoteNote.getCategory(), remoteNote.getEtag(), remoteNote.getContent()};
        }
        int i = db.update(table_notes, values, whereClause, whereArgs);
        removeEmptyCategory(id);
        Log.d(TAG, "updateNote: " + remoteNote + " || forceUnchangedDBNoteState: " + forceUnchangedDBNoteState + "  => " + i + " rows updated");
    }

    /**
     * Marks a Note in the Database as Deleted. In the next Synchronization it will be deleted
     * from the Server.
     *
     * @param id long - ID of the Note that should be deleted
     */
    public void deleteNoteAndSync(SingleSignOnAccount ssoAccount, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(1);
        values.put(key_status, DBStatus.LOCAL_DELETED.getTitle());
        db.update(table_notes,
                values,
                key_id + " = ?",
                new String[]{String.valueOf(id)});
        notifyWidgets();
        getNoteServerSyncHelper().scheduleSync(ssoAccount, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = getContext().getSystemService(ShortcutManager.class);
            if (shortcutManager != null) {
                shortcutManager.getPinnedShortcuts().forEach((shortcut) -> {
                    String shortcutId = id + "";
                    if (shortcut.getId().equals(shortcutId)) {
                        Log.v(TAG, "Removing shortcut for " + shortcutId);
                        shortcutManager.disableShortcuts(Collections.singletonList(shortcutId), getContext().getResources().getString(R.string.note_has_been_deleted));
                    }
                });
            } else {
                Log.e(TAG, ShortcutManager.class.getSimpleName() + "is null.");
            }
        }
    }

    /**
     * Delete a single Note from the Database, if it has a specific DBStatus.
     * Thereby, an optimistic concurrency control is realized in order to prevent conflicts arising due to parallel changes from the UI and synchronization.
     *
     * @param id            long - ID of the Note that should be deleted.
     * @param forceDBStatus DBStatus, e.g., if Note was marked as LOCAL_DELETED (for NoteSQLiteOpenHelper.SyncTask.pushLocalChanges()) or is unchanged VOID (for NoteSQLiteOpenHelper.SyncTask.pullRemoteChanges())
     */
    void deleteNote(long id, @NonNull DBStatus forceDBStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table_notes,
                key_id + " = ? AND " + key_status + " = ?",
                new String[]{String.valueOf(id), forceDBStatus.getTitle()});
        removeEmptyCategory(id);
    }

    /**
     * Notify about changed notes.
     */
    protected void notifyWidgets() {
        updateSingleNoteWidgets(getContext());
        updateNoteListWidgets(getContext());
    }

    void updateDynamicShortcuts(long accountId) {
        new Thread(() -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                ShortcutManager shortcutManager = getContext().getApplicationContext().getSystemService(ShortcutManager.class);
                if (shortcutManager != null) {
                    if (!shortcutManager.isRateLimitingActive()) {
                        List<ShortcutInfo> newShortcuts = new ArrayList<>();

                        for (DBNote note : getRecentNotes(accountId)) {
                            if (!TextUtils.isEmpty(note.getTitle())) {
                                Intent intent = new Intent(getContext().getApplicationContext(), EditNoteActivity.class);
                                intent.putExtra(EditNoteActivity.PARAM_NOTE_ID, note.getId());
                                intent.setAction(ACTION_SHORTCUT);

                                newShortcuts.add(new ShortcutInfo.Builder(getContext().getApplicationContext(), note.getId() + "")
                                        .setShortLabel(note.getTitle() + "")
                                        .setIcon(Icon.createWithResource(getContext().getApplicationContext(), note.isFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp))
                                        .setIntent(intent)
                                        .build());
                            } else {
                                // Prevent crash https://github.com/stefan-niedermann/nextcloud-notes/issues/613
                                Log.e(TAG, "shortLabel cannot be empty " + note);
                            }
                        }
                        Log.d(TAG, "Update dynamic shortcuts");
                        shortcutManager.removeAllDynamicShortcuts();
                        shortcutManager.addDynamicShortcuts(newShortcuts);
                    }
                }
            }
        }).start();
    }

    public boolean hasAccounts() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), table_accounts) > 0;
    }

    /**
     * @param url          URL to the root of the used Nextcloud instance without trailing slash
     * @param username     Username of the account
     * @param accountName  Composed by the username and the host of the URL, separated by @-sign
     * @param capabilities {@link Capabilities} object containing information about the brand colors, supported API versions, etc...
     * @throws SQLiteConstraintException in case accountName already exists
     */
    public void addAccount(@NonNull String url, @NonNull String username, @NonNull String accountName, @NonNull Capabilities capabilities) throws SQLiteConstraintException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(4);
        values.put(key_url, url);
        values.put(key_username, username);
        values.put(key_account_name, accountName);
        values.put(key_capabilities_etag, capabilities.getETag());
        long accountId = db.insertOrThrow(table_accounts, null, values);
        updateBrand(accountId, capabilities);
    }

    /**
     * @param accountId account which should be read
     * @return a LocalAccount object for the given accountId
     */
    public LocalAccount getAccount(long accountId) {
        validateAccountId(accountId);
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(table_accounts, new String[]{key_id, key_url, key_account_name, key_username, key_etag, key_modified, key_api_version, key_color, key_text_color, key_capabilities_etag}, key_id + " = ?", new String[]{String.valueOf(accountId)}, null, null, null, null);
        final LocalAccount account = new LocalAccount();
        while (cursor.moveToNext()) {
            account.setId(cursor.getLong(0));
            account.setUrl(cursor.getString(1));
            account.setAccountName(cursor.getString(2));
            account.setUserName(cursor.getString(3));
            account.setETag(cursor.getString(4));
            account.setModified(cursor.getLong(5));
            account.setPreferredApiVersion(cursor.getString(6));
            account.setColor(Color.parseColor('#' + cursor.getString(7)));
            account.setTextColor(Color.parseColor('#' + cursor.getString(8)));
            account.setCapabilitiesETag(cursor.getString(9));
        }
        cursor.close();
        return account;
    }

    public List<LocalAccount> getAccounts() {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(table_accounts, new String[]{key_id, key_url, key_account_name, key_username, key_etag, key_modified, key_api_version, key_color, key_text_color, key_capabilities_etag}, null, null, null, null, null);
        final List<LocalAccount> accounts = new ArrayList<>();
        while (cursor.moveToNext()) {
            LocalAccount account = new LocalAccount();
            account.setId(cursor.getLong(0));
            account.setUrl(cursor.getString(1));
            account.setAccountName(cursor.getString(2));
            account.setUserName(cursor.getString(3));
            account.setETag(cursor.getString(4));
            account.setModified(cursor.getLong(5));
            account.setPreferredApiVersion(cursor.getString(6));
            account.setColor(Color.parseColor('#' + cursor.getString(7)));
            account.setTextColor(Color.parseColor('#' + cursor.getString(8)));
            account.setCapabilitiesETag(cursor.getString(9));
            accounts.add(account);
        }
        cursor.close();
        return accounts;
    }

    @Nullable
    public LocalAccount getLocalAccountByAccountName(String accountName) throws IllegalArgumentException {
        if (accountName == null) {
            Log.e(TAG, "accountName is null");
            return null;
        }
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(table_accounts, new String[]{key_id, key_url, key_account_name, key_username, key_etag, key_modified, key_api_version, key_color, key_text_color, key_capabilities_etag}, key_account_name + " = ?", new String[]{accountName}, null, null, null, null);
        final LocalAccount account = new LocalAccount();
        int numberEntries = 0;
        while (cursor.moveToNext()) {
            numberEntries++;
            account.setId(cursor.getLong(0));
            account.setUrl(cursor.getString(1));
            account.setAccountName(cursor.getString(2));
            account.setUserName(cursor.getString(3));
            account.setETag(cursor.getString(4));
            account.setModified(cursor.getLong(5));
            account.setPreferredApiVersion(cursor.getString(6));
            account.setColor(Color.parseColor('#' + cursor.getString(7)));
            account.setTextColor(Color.parseColor('#' + cursor.getString(8)));
            account.setCapabilitiesETag(cursor.getString(9));
        }
        cursor.close();
        switch (numberEntries) {
            case 0:
                Log.w(TAG, "Could not find any account for \"" + accountName + "\". Returning null.");
                return null;
            case 1:
                return account;
            default:
                Log.e(TAG, "", new IllegalArgumentException("Expected to find 1 account for name \"" + accountName + "\", but found " + numberEntries + "."));
                return null;
        }
    }

    public void updateBrand(long accountId, @NonNull Capabilities capabilities) throws IllegalArgumentException {
        validateAccountId(accountId);

        String color;
        try {
            color = ColorUtil.formatColorToParsableHexString(capabilities.getColor()).substring(1);
        } catch (Exception e) {
            color = String.format("%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.defaultBrand)));
        }

        String textColor;
        try {
            textColor = ColorUtil.formatColorToParsableHexString(capabilities.getTextColor()).substring(1);
        } catch (Exception e) {
            textColor = String.format("%06X", (0xFFFFFF & ContextCompat.getColor(context, android.R.color.white)));
        }

        final SQLiteDatabase db = this.getWritableDatabase();
        final ContentValues values = new ContentValues(2);

        values.put(key_color, color);
        values.put(key_text_color, textColor);

        final int updatedRows = db.update(table_accounts, values, key_id + " = ?", new String[]{String.valueOf(accountId)});
        if (updatedRows == 1) {
            Log.v(TAG, "Updated " + key_color + " to " + capabilities.getColor() + " and " + key_text_color + " to " + capabilities.getTextColor() + " for " + key_account_id + " = " + accountId);
        } else {
            Log.e(TAG, "Updated " + updatedRows + " but expected only 1 for accountId = " + accountId + " and " + key_color + " = " + capabilities.getColor() + " and " + key_text_color + " = " + capabilities.getTextColor());
        }
    }

    /**
     * @param apiVersion has to be a JSON array as a string <code>["0.2", "1.0", ...]</code>
     * @return whether or not the given apiVersion have been written to the database
     * @throws IllegalArgumentException if the apiVersion does not match the expected format
     */
    public boolean updateApiVersion(long accountId, @Nullable String apiVersion) throws IllegalArgumentException {
        validateAccountId(accountId);
        if (apiVersion != null) {
            try {
                JSONArray apiVersions = new JSONArray(apiVersion);
                for (int i = 0; i < apiVersions.length(); i++) {
                    ApiVersion.of(apiVersions.getString(i));
                }
                if (apiVersions.length() > 0) {
                    final SQLiteDatabase db = this.getWritableDatabase();
                    final ContentValues values = new ContentValues(1);
                    values.put(key_api_version, apiVersion);
                    final int updatedRows = db.update(table_accounts, values, key_id + " = ?", new String[]{String.valueOf(accountId)});
                    if (updatedRows == 1) {
                        Log.i(TAG, "Updated " + key_api_version + " to \"" + apiVersion + "\" for accountId = " + accountId);
                    } else {
                        Log.e(TAG, "Updated " + updatedRows + " but expected only 1 for accountId = " + accountId + " and apiVersion = \"" + apiVersion + "\"");
                    }
                    return true;
                } else {
                    Log.i(TAG, "Given API version is a valid JSON array but does not contain any valid API versions. Do not update database.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("API version does contain a non-valid version: " + apiVersion);
            } catch (JSONException e) {
                throw new IllegalArgumentException("API version must contain be a JSON array: " + apiVersion);
            }
        } else {
            Log.v(TAG, "Given API version is null. Do not update database");
        }
        return false;
    }

    /**
     * @param localAccount the account that should be deleted
     * @throws IllegalArgumentException if no account has been deleted by the given accountId
     */
    public void deleteAccount(@NonNull LocalAccount localAccount) throws IllegalArgumentException {
        validateAccountId(localAccount.getId());
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedAccounts = db.delete(table_accounts, key_id + " = ?", new String[]{String.valueOf(localAccount.getId())});
        if (deletedAccounts < 1) {
            Log.e(TAG, "AccountId '" + localAccount.getId() + "' did not delete any account");
            throw new IllegalArgumentException("The given accountId does not delete any row");
        } else if (deletedAccounts > 1) {
            Log.e(TAG, "AccountId '" + localAccount.getId() + "' deleted unexpectedly '" + deletedAccounts + "' accounts");
        }

        try {
            SSOClient.invalidateAPICache(AccountImporter.getSingleSignOnAccount(getContext(), localAccount.getAccountName()));
        } catch (NextcloudFilesAppAccountNotFoundException e) {
            e.printStackTrace();
            SSOClient.invalidateAPICache();
        }

        final int deletedNotes = db.delete(table_notes, key_account_id + " = ?", new String[]{String.valueOf(localAccount.getId())});
        Log.v(TAG, "Deleted " + deletedNotes + " notes from account " + localAccount.getId());
    }

    void updateETag(long accountId, String etag) {
        validateAccountId(accountId);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(1);
        values.put(key_etag, etag);
        final int updatedRows = db.update(table_accounts, values, key_id + " = ?", new String[]{String.valueOf(accountId)});
        if (updatedRows == 1) {
            Log.v(TAG, "Updated etag to " + etag + " for accountId = " + accountId);
        } else {
            Log.e(TAG, "Updated " + updatedRows + " but expected only 1 for accountId = " + accountId + " and etag = " + etag);
        }
    }

    public void updateCapabilitiesETag(long accountId, String capabilitiesETag) {
        validateAccountId(accountId);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(1);
        values.put(key_capabilities_etag, capabilitiesETag);
        final int updatedRows = db.update(table_accounts, values, key_id + " = ?", new String[]{String.valueOf(accountId)});
        if (updatedRows == 1) {
            Log.v(TAG, "Updated etag to " + capabilitiesETag + " for accountId = " + accountId);
        } else {
            Log.e(TAG, "Updated " + updatedRows + " but expected only 1 for accountId = " + accountId + " and capabilitiesETag = " + capabilitiesETag);
        }
    }

    void updateModified(long accountId, long modified) {
        validateAccountId(accountId);
        if (modified < 0) {
            throw new IllegalArgumentException("modified must be greater or equal 0");
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues(1);
        values.put(key_modified, modified);
        final int updatedRows = db.update(table_accounts, values, key_id + " = ?", new String[]{String.valueOf(accountId)});
        if (updatedRows == 1) {
            Log.v(TAG, "Updated modified to " + modified + " for accountId = " + accountId);
        } else {
            Log.e(TAG, "Updated " + updatedRows + " but expected only 1 for accountId = " + accountId + " and modified = " + modified);
        }
    }

    /**
     * @param appWidgetId the id of the widget
     * @return {@link SingleNoteWidgetData}
     * @throws NoSuchElementException in case there is no {@link SingleNoteWidgetData} for the given appWidgetId
     */
    @NonNull
    public SingleNoteWidgetData getSingleNoteWidgetData(int appWidgetId) throws NoSuchElementException {
        SingleNoteWidgetData data = new SingleNoteWidgetData();
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(table_widget_single_notes, new String[]{key_account_id, key_note_id, key_theme_mode}, key_id + " = ?", new String[]{String.valueOf(appWidgetId)}, null, null, null, null);
        if (cursor.moveToNext()) {
            data.setAppWidgetId(appWidgetId);
            data.setAccountId(cursor.getLong(0));
            data.setNoteId(cursor.getLong(1));
            data.setThemeMode(cursor.getInt(2));
        } else {
            throw new NoSuchElementException();
        }
        cursor.close();
        return data;
    }

    public void removeSingleNoteWidget(int appWidgetId) {
        final SQLiteDatabase db = getWritableDatabase();
        db.delete(table_widget_single_notes, key_id + " = ?", new String[]{String.valueOf(appWidgetId)});
    }

    public void createOrUpdateSingleNoteWidgetData(@NonNull SingleNoteWidgetData data) throws SQLException {
        validateAccountId(data.getAccountId());
        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues(4);
        values.put(key_id, data.getAppWidgetId());
        values.put(key_account_id, data.getAccountId());
        values.put(key_note_id, data.getNoteId());
        values.put(key_theme_mode, data.getThemeMode());
        db.replaceOrThrow(table_widget_single_notes, null, values);
    }

    @NonNull
    public NoteListsWidgetData getNoteListWidgetData(int appWidgetId) throws NoSuchElementException {
        NoteListsWidgetData data = new NoteListsWidgetData();
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(table_widget_note_list, new String[]{key_account_id, key_category_id, key_theme_mode, key_mode}, key_id + " = ?", new String[]{String.valueOf(appWidgetId)}, null, null, null, null);
        if (cursor.moveToNext()) {
            data.setAppWidgetId(appWidgetId);
            data.setAccountId(cursor.getLong(0));
            data.setCategoryId(cursor.getLong(1));
            data.setThemeMode(cursor.getInt(2));
            data.setMode(cursor.getInt(3));
        } else {
            throw new NoSuchElementException();
        }
        cursor.close();
        return data;
    }

    public void removeNoteListWidget(int appWidgetId) {
        final SQLiteDatabase db = getWritableDatabase();
        db.delete(table_widget_note_list, key_id + " = ?", new String[]{String.valueOf(appWidgetId)});
    }

    public void createOrUpdateNoteListWidgetData(@NonNull NoteListsWidgetData data) throws SQLException {
        validateAccountId(data.getAccountId());
        final SQLiteDatabase db = getWritableDatabase();
        final ContentValues values = new ContentValues(5);
        if (data.getMode() != MODE_DISPLAY_CATEGORY && data.getCategoryId() != null) {
            throw new UnsupportedOperationException("Cannot create a widget with a categoryId when mode is not " + MODE_DISPLAY_CATEGORY);
        }
        values.put(key_id, data.getAppWidgetId());
        values.put(key_account_id, data.getAccountId());
        values.put(key_category_id, data.getCategoryId());
        values.put(key_theme_mode, data.getThemeMode());
        values.put(key_mode, data.getMode());
        db.replaceOrThrow(table_widget_note_list, null, values);
    }

    private static void validateAccountId(long accountId) {
        if (accountId < 1) {
            throw new IllegalArgumentException("accountId must be greater than 0");
        }
    }

    /**
     * Get the category if with the given category title
     * The method does not support fuzzy search.
     * Because the category title in database is unique, there will not at most one result.
     * If there is no such category, database will create it if create flag is set.
     * Otherwise this method will return -1 as default value.
     *
     * @param accountId     The user account Id
     * @param categoryTitle The category title which will be search in the db
     * @return -1 if there is no such category else the corresponding id
     */
    @NonNull
    @WorkerThread
    private Integer getCategoryIdByTitle(long accountId, @NonNull String categoryTitle) {
        validateAccountId(accountId);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                table_category,
                new String[]{key_category_id},
                key_category_title + " = ? AND " + key_category_account_id + " = ? ",
                new String[]{categoryTitle, String.valueOf(accountId)},
                null,
                null,
                null);
        int id;
        if (cursor.moveToNext()) {
            id = cursor.getInt(0);
        } else {
            id = (int) addCategory(accountId, categoryTitle);
            if (id == -1) {
                Log.e(TAG, String.format("Error occurs when creating category: %s", categoryTitle));
            }
        }
        cursor.close();
        return id;
    }

    /**
     * This function will be called when the category or note is updated.
     * Because sometime we will remove some notes in categories.
     * Such that there must be such a category without any note.
     * For these useless category, it is better to remove.
     * Move a note from a category to another may also lead to the same result.
     *
     * @param accountId The user accountId
     */
    private void removeEmptyCategory(long accountId) {
        validateAccountId(accountId);
        getReadableDatabase().delete(table_category,
                key_category_id + " NOT IN (SELECT " + key_category + " FROM " + table_notes + ")",
                null);
    }

    /**
     * This function is used to get the sorting method of a category by title.
     * The sorting method of the category can be used to decide
     * to use which sorting method to show the notes for each categories.
     *
     * @param accountId     The user accountID
     * @param categoryTitle The category title
     * @return The sorting method in CategorySortingMethod enum format
     */
    public CategorySortingMethod getCategoryOrderByTitle(long accountId, String categoryTitle) {
        validateAccountId(accountId);

        long categoryId = getCategoryIdByTitle(accountId, categoryTitle);

        SQLiteDatabase db = getReadableDatabase();
        int orderIndex;
        try (Cursor cursor = db.query(table_category, new String[]{key_category_sorting_method},
                key_category_id + " = ?", new String[]{String.valueOf(categoryId)},
                null, null, null)) {
            orderIndex = 0;
            while (cursor.moveToNext()) {
                orderIndex = cursor.getInt(0);
            }
        }

        return CategorySortingMethod.getCSM(orderIndex);
    }

    /**
     * This method is used to modify the sorting method for one category by title.
     * The user can determine use which sorting method to show the notes for a category.
     * When the user changes the sorting method, this method should be called.
     *
     * @param accountId     The user accountID
     * @param categoryTitle The category title
     * @param sortingMethod The sorting method in CategorySortingMethod enum format
     */
    public void modifyCategoryOrderByTitle(
            long accountId, String categoryTitle, CategorySortingMethod sortingMethod) {
        validateAccountId(accountId);

        long categoryId = getCategoryIdByTitle(accountId, categoryTitle);

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(key_category_sorting_method, sortingMethod.getCSMID());
        db.update(table_category, values,
                key_category_id + " = ?", new String[]{String.valueOf(categoryId)});
    }

    /**
     * Gets the sorting method of a category, the category can be normal category or
     * one of "All notes", "Favorite", and "Uncategorized".
     * If category is one of these three, sorting method will be got from android.content.SharedPreference.
     * The sorting method of the category can be used to decide
     * to use which sorting method to show the notes for each categories.
     *
     * @param accountId The user accountID
     * @param category  The category
     * @return The sorting method in CategorySortingMethod enum format
     */
    public CategorySortingMethod getCategoryOrder(long accountId, Category category) {
        validateAccountId(accountId);

        final Context ctx = getContext().getApplicationContext();
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        int orderIndex;

        if (category.category == null) {
            if (category.favorite != null && category.favorite) {
                // Favorite
                orderIndex = sp.getInt(ctx.getString(R.string.action_sorting_method) +
                                ' ' + ctx.getString(R.string.label_favorites),
                        0);
            } else {
                // All notes
                orderIndex = sp.getInt(ctx.getString(R.string.action_sorting_method) +
                                ' ' + ctx.getString(R.string.label_all_notes),
                        0);
            }
        } else if (category.category.isEmpty()) {
            // Uncategorized
            orderIndex = sp.getInt(ctx.getString(R.string.action_sorting_method) +
                            ' ' + ctx.getString(R.string.action_uncategorized),
                    0);
        } else {
            return getCategoryOrderByTitle(accountId, category.category);
        }

        return CategorySortingMethod.getCSM(orderIndex);
    }

    /**
     * Modifies the sorting method for one category, the category can be normal category or
     * one of "All notes", "Favorite", and "Uncategorized".
     * If category is one of these three, sorting method will be modified in android.content.SharedPreference.
     * The user can determine use which sorting method to show the notes for a category.
     * When the user changes the sorting method, this method should be called.
     *
     * @param accountId     The user accountID
     * @param category      The category to be modified
     * @param sortingMethod The sorting method in CategorySortingMethod enum format
     */
    public void modifyCategoryOrder(
            long accountId, Category category, CategorySortingMethod sortingMethod) {
        validateAccountId(accountId);

        final Context ctx = getContext().getApplicationContext();
        final SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        int orderIndex = sortingMethod.getCSMID();
        if (category.category == null) {
            if (category.favorite != null && category.favorite) {
                // Favorite
                sp.putInt(ctx.getString(R.string.action_sorting_method) +
                                ' ' + ctx.getString(R.string.label_favorites),
                        orderIndex);
            } else {
                // All notes
                sp.putInt(ctx.getString(R.string.action_sorting_method) +
                                ' ' + ctx.getString(R.string.label_all_notes),
                        orderIndex);
            }
        } else if (category.category.isEmpty()) {
            // Uncategorized
            sp.putInt(ctx.getString(R.string.action_sorting_method) +
                            ' ' + ctx.getString(R.string.action_uncategorized),
                    orderIndex);
        } else {
            modifyCategoryOrderByTitle(accountId, category.category, sortingMethod);
            return;
        }
        sp.apply();
    }

}
