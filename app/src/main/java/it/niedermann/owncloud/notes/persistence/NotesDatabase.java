package it.niedermann.owncloud.notes.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.main.NavigationAdapter;
import it.niedermann.owncloud.notes.shared.model.ApiVersion;
import it.niedermann.owncloud.notes.shared.model.CategorySortingMethod;
import it.niedermann.owncloud.notes.shared.model.CloudNote;
import it.niedermann.owncloud.notes.shared.model.DBNote;
import it.niedermann.owncloud.notes.shared.model.DBStatus;
import it.niedermann.owncloud.notes.shared.model.ISyncCallback;
import it.niedermann.owncloud.notes.shared.model.LocalAccount;
import it.niedermann.owncloud.notes.shared.util.NoteUtil;

import static it.niedermann.owncloud.notes.shared.util.NoteUtil.generateNoteExcerpt;
import static it.niedermann.owncloud.notes.widget.notelist.NoteListWidget.updateNoteListWidgets;
import static it.niedermann.owncloud.notes.widget.singlenote.SingleNoteWidget.updateSingleNoteWidgets;

/**
 * Helps to add, get, update and delete Notes with the option to trigger a Resync with the Server.
 */
@Deprecated
public class NotesDatabase extends AbstractNotesDatabase {

    private static final String TAG = NotesDatabase.class.getSimpleName();

    private static NotesDatabase instance;

    private final NoteServerSyncHelper serverSyncHelper;

    private NotesDatabase(@NonNull Context context) {
        super(context, database_name, null);
        serverSyncHelper = NoteServerSyncHelper.getInstance(this, NotesRoomDatabase.getInstance(context));
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
     * @return changed {@link DBNote} if differs from database, otherwise the old {@link DBNote}.
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

    /**
     * Notify about changed notes.
     */
    protected void notifyWidgets() {
        updateSingleNoteWidgets(getContext());
        updateNoteListWidgets(getContext());
    }

    /**
     * @param apiVersion has to be a JSON array as a string <code>["0.2", "1.0", ...]</code>
     * @return whether or not the given {@link ApiVersion} has been written to the database
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
     * @param localAccount the {@link LocalAccount} that should be deleted
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
     * @param accountId     The user {@link LocalAccount} Id
     * @param categoryTitle The category title which will be search in the db
     * @return -1 if there is no such category else the corresponding id
     * @deprecated replaced by #{{@link NotesRoomDatabase#getOrCreateCategoryIdByTitle(long, String)}}
     */
    @NonNull
    @WorkerThread
    @Deprecated
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
}
