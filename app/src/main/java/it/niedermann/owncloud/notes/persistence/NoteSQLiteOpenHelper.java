package it.niedermann.owncloud.notes.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.appwidget.NoteListWidget;
import it.niedermann.owncloud.notes.android.appwidget.SingleNoteWidget;
import it.niedermann.owncloud.notes.model.CloudNote;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.LocalAccount;
import it.niedermann.owncloud.notes.model.NavigationAdapter;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.NoteUtil;

/**
 * Helps to add, get, update and delete Notes with the option to trigger a Resync with the Server.
 */
public class NoteSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final int database_version = 9;
    private static final String database_name = "OWNCLOUD_NOTES";
    private static final String table_notes = "NOTES";
    private static final String table_accounts = "ACCOUNTS";
    private static final String key_id = "ID";
    private static final String key_account_id = "ACCOUNT_ID";
    private static final String key_remote_id = "REMOTEID";

    private static final String key_url = "URL";
    private static final String key_account_name = "ACCOUNT_NAME";
    private static final String key_username = "USERNAME";
    private static final String key_display_name = "DISPLAY_NAME";
    private static final String key_token = "TOKEN";

    private static final String key_status = "STATUS";
    private static final String key_title = "TITLE";
    private static final String key_modified = "MODIFIED";
    private static final String key_content = "CONTENT";
    private static final String key_favorite = "FAVORITE";
    private static final String key_category = "CATEGORY";
    private static final String key_etag = "ETAG";

    private static final String[] columns = {key_id, key_remote_id, key_status, key_title, key_modified, key_content, key_favorite, key_category, key_etag};
    private static final String default_order = key_favorite + " DESC, " + key_modified + " DESC";

    private static NoteSQLiteOpenHelper instance;

    private NoteServerSyncHelper serverSyncHelper;
    private Context context;

    private NoteSQLiteOpenHelper(Context context) {
        super(context, database_name, null, database_version);
        this.context = context.getApplicationContext();
        serverSyncHelper = NoteServerSyncHelper.getInstance(this);
    }

    public static NoteSQLiteOpenHelper getInstance(Context context) {
        if (instance == null)
            return instance = new NoteSQLiteOpenHelper(context.getApplicationContext());
        else
            return instance;
    }

    public NoteServerSyncHelper getNoteServerSyncHelper() {
        return serverSyncHelper;
    }

    /**
     * Creates initial the Database
     *
     * @param db Database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        createNotesTable(db, table_notes);
        createAccountTable(db, table_accounts);
    }

    private void createNotesTable(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_remote_id + " INTEGER, " +
                key_account_id + " INTEGER, " +
                key_status + " VARCHAR(50), " +
                key_title + " TEXT, " +
                key_modified + " INTEGER DEFAULT 0, " +
                key_content + " TEXT, " +
                key_favorite + " INTEGER DEFAULT 0, " +
                key_category + " TEXT NOT NULL DEFAULT '', " +
                key_etag + " TEXT)");
        createNotesIndexes(db);
    }

    private void createAccountTable(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_url + " TEXT, " +
                key_username + " TEXT, " +
                key_account_name + " TEXT UNIQUE, " +
                key_display_name + " TEXT, " +
                key_token + " TEXT)");
        createAccountIndexes(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            recreateDatabase(db);
        }
        if (oldVersion < 4) {
            clearDatabase(db);
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_remote_id + " INTEGER");
            db.execSQL("UPDATE " + table_notes + " SET " + key_remote_id + "=" + key_id + " WHERE (" + key_remote_id + " IS NULL OR " + key_remote_id + "=0) AND " + key_status + "!=?", new String[]{DBStatus.LOCAL_CREATED.getTitle()});
            db.execSQL("UPDATE " + table_notes + " SET " + key_remote_id + "=0, " + key_status + "=? WHERE " + key_status + "=?", new String[]{DBStatus.LOCAL_EDITED.getTitle(), DBStatus.LOCAL_CREATED.getTitle()});
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_favorite + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 7) {
            dropIndexes(db);
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_category + " TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_etag + " TEXT");
            createNotesIndexes(db);
        }
        if (oldVersion < 8) {
            final String table_temp = "NOTES_TEMP";
            createNotesTable(db, table_temp);
            db.execSQL(String.format("INSERT INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s) ", table_temp, key_id, key_remote_id, key_status, key_title, key_modified, key_content, key_favorite, key_category, key_etag)
                    + String.format("SELECT %s,%s,%s,%s,strftime('%%s',%s),%s,%s,%s,%s FROM %s", key_id, key_remote_id, key_status, key_title, key_modified, key_content, key_favorite, key_category, key_etag, table_notes));
            db.execSQL(String.format("DROP TABLE %s", table_notes));
            db.execSQL(String.format("ALTER TABLE %s RENAME TO %s", table_temp, table_notes));
        }
        if (oldVersion < 9) {
            db.execSQL("ALTER TABLE " + table_notes + " ADD COLUMN " + key_account_id + " INTEGER NOT NULL DEFAULT 0");
            createIndex(db, table_notes, key_account_id);
            createAccountTable(db, table_accounts);
            ContentValues values = new ContentValues();
            values.put(key_account_id, 1);
            db.update(table_notes, values, key_account_id + " = ?", new String[] {"NULL"});
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        recreateDatabase(db);
    }

    private void clearDatabase(SQLiteDatabase db) {
        db.delete(table_notes, null, null);
    }

    private void recreateDatabase(SQLiteDatabase db) {
        dropIndexes(db);
        db.execSQL("DROP TABLE " + table_notes);
        onCreate(db);
    }

    private void dropIndexes(SQLiteDatabase db) {
        Cursor c = db.query("sqlite_master", new String[]{"name"}, "type=?", new String[]{"index"}, null, null, null);
        while (c.moveToNext()) {
            db.execSQL("DROP INDEX " + c.getString(0));
        }
        c.close();
    }

    private void createNotesIndexes(SQLiteDatabase db) {
        createIndex(db, table_notes, key_remote_id);
        createIndex(db, table_notes, key_account_id);
        createIndex(db, table_notes, key_status);
        createIndex(db, table_notes, key_favorite);
        createIndex(db, table_notes, key_category);
        createIndex(db, table_notes, key_modified);
    }

    private void createAccountIndexes(SQLiteDatabase db) {
        createIndex(db, table_accounts, key_account_name);
    }

    private void createIndex(SQLiteDatabase db, String table, String column) {
        String indexName = table + "_" + column + "_idx";
        db.execSQL("CREATE INDEX IF NOT EXISTS " + indexName + " ON " + table + "(" + column + ")");
    }

    public Context getContext() {
        return context;
    }

    /**
     * Creates a new Note in the Database and adds a Synchronization Flag.
     *
     * @param content String
     */
    @SuppressWarnings("UnusedReturnValue")
    public long addNoteAndSync(String content, String category, boolean favorite, long accountId) {
        CloudNote note = new CloudNote(0, Calendar.getInstance(), NoteUtil.generateNonEmptyNoteTitle(content, getContext()), content, favorite, category, null);
        return addNoteAndSync(accountId, note);
    }

    /**
     * Creates a new Note in the Database and adds a Synchronization Flag.
     *
     * @param note Note
     */
    @SuppressWarnings("UnusedReturnValue")
    public long addNoteAndSync(long accountId, CloudNote note) {
        DBNote dbNote = new DBNote(0, 0, note.getModified(), note.getTitle(), note.getContent(), note.isFavorite(), note.getCategory(), note.getEtag(), DBStatus.LOCAL_EDITED, accountId);
        long id = addNote(accountId, dbNote);
        notifyNotesChanged();
        getNoteServerSyncHelper().scheduleSync(true);
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
        ContentValues values = new ContentValues();
        if (note instanceof DBNote) {
            DBNote dbNote = (DBNote) note;
            if (dbNote.getId() > 0) {
                values.put(key_id, dbNote.getId());
            }
            values.put(key_status, dbNote.getStatus().getTitle());
            values.put(key_account_id, dbNote.getAccountId());
        } else {
            values.put(key_status, DBStatus.VOID.getTitle());
            values.put(key_account_id, accountId);
        }
        if (note.getRemoteId() > 0) {
            values.put(key_remote_id, note.getRemoteId());
        }
        values.put(key_title, note.getTitle());
        values.put(key_modified, note.getModified().getTimeInMillis() / 1000);
        values.put(key_content, note.getContent());
        values.put(key_favorite, note.isFavorite());
        values.put(key_category, note.getCategory());
        values.put(key_etag, note.getEtag());
        return db.insert(table_notes, null, values);
    }

    /**
     * Get a single Note by ID
     *
     * @param id int - ID of the requested Note
     * @return requested Note
     */
    public DBNote getNote(long accountId, long id) {
        List<DBNote> notes = getNotesCustom(accountId, key_id + " = ? AND " + key_status + " != ? AND " + key_account_id + " = ? ", new String[]{String.valueOf(id), DBStatus.LOCAL_DELETED.getTitle(), "" + accountId}, null);
        return notes.isEmpty() ? null : notes.get(0);
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
    private List<DBNote> getNotesCustom(long accountId, @NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy) {
        return this.getNotesCustom(accountId, selection, selectionArgs, orderBy, null);
    }

    @NonNull
    @WorkerThread
    private List<DBNote> getNotesCustom(long accountId, @NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy, @Nullable String limit) {
        SQLiteDatabase db = getReadableDatabase();
        if (selectionArgs.length > 2) {
            Log.v("Note", selection + "   ----   " + selectionArgs[0] + " " + selectionArgs[1] + " " + selectionArgs[2]);
        }
        Cursor cursor = db.query(table_notes, columns, selection, selectionArgs, null, null, orderBy, limit);
        List<DBNote> notes = new ArrayList<>();
        while (cursor.moveToNext()) {
            notes.add(getNoteFromCursor(accountId, cursor));
        }
        cursor.close();
        return notes;
    }

    /**
     * Creates a DBNote object from the current row of a Cursor.
     *
     * @param cursor database cursor
     * @return DBNote
     */
    @NonNull
    private DBNote getNoteFromCursor(long accountId, @NonNull Cursor cursor) {
        Calendar modified = Calendar.getInstance();
        modified.setTimeInMillis(cursor.getLong(4) * 1000);
        return new DBNote(cursor.getLong(0), cursor.getLong(1), modified, cursor.getString(3), cursor.getString(5), cursor.getInt(6) > 0, cursor.getString(7), cursor.getString(8), DBStatus.parse(cursor.getString(2)), accountId);
    }

    public void debugPrintFullDB(long accountId) {
        List<DBNote> notes = getNotesCustom(accountId, "", new String[]{}, default_order);
        Log.v(getClass().getSimpleName(), "Full Database (" + notes.size() + " notes):");
        for (DBNote note : notes) {
            Log.v(getClass().getSimpleName(), "     " + note);
        }
    }

    @NonNull
    @WorkerThread
    public Map<Long, Long> getIdMap(long accountId) {
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
        return getNotesCustom(accountId, key_status + " != ? AND " + key_account_id + " = ?", new String[]{DBStatus.LOCAL_DELETED.getTitle(), "" + accountId}, default_order);
    }

    @NonNull
    @WorkerThread
    public List<DBNote> getRecentNotes(long accountId) {
        return getNotesCustom(accountId, key_status + " != ? AND " + key_account_id + " = ?", new String[]{DBStatus.LOCAL_DELETED.getTitle(), "" + accountId}, key_modified + " DESC", "4");
    }

    /**
     * Returns a list of all Notes in the Database
     *
     * @return List&lt;Note&gt;
     */
    @NonNull
    @WorkerThread
    public List<DBNote> searchNotes(long accountId, @Nullable CharSequence query, @Nullable String category, @Nullable Boolean favorite) {
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
            where.add("(" + key_category + "=? OR " + key_category + " LIKE ? )");
            args.add(category);
            args.add(category + "/%");
        }

        if (favorite != null) {
            where.add(key_favorite + "=?");
            args.add(favorite ? "1" : "0");
        }

        String order = category == null ? default_order : key_category + ", " + key_title;
        return getNotesCustom(accountId, TextUtils.join(" AND ", where), args.toArray(new String[]{}), order);
    }

    /**
     * Returns a list of all Notes in the Database with were modified locally
     *
     * @return List&lt;Note&gt;
     */
    @NonNull
    @WorkerThread
    public List<DBNote> getLocalModifiedNotes(long accountId) {
        return getNotesCustom(accountId, key_status + " != ? AND " + key_account_id + " = ?", new String[]{DBStatus.VOID.getTitle(), "" + accountId}, null);
    }

    @NonNull
    @WorkerThread
    public Map<String, Integer> getFavoritesCount(long accountId) {
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

    @NonNull
    @WorkerThread
    public List<NavigationAdapter.NavigationItem> getCategories(long accountId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                table_notes,
                new String[]{key_category, "COUNT(*)"},
                key_status + " != ? AND " + key_account_id + " = ?",
                new String[]{DBStatus.LOCAL_DELETED.getTitle(), "" + accountId},
                key_category,
                null,
                key_category);
        List<NavigationAdapter.NavigationItem> categories = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Resources res = context.getResources();
            String category = cursor.getString(0).toLowerCase();
            int icon = NavigationAdapter.ICON_FOLDER;
            if(category.equals(res.getString(R.string.category_music).toLowerCase())) {
                icon = R.drawable.ic_library_music_grey600_24dp;
            } else if(category.equals(res.getString(R.string.category_movies).toLowerCase()) || category.equals(res.getString(R.string.category_movie).toLowerCase())) {
                icon = R.drawable.ic_local_movies_grey600_24dp;
            } else if(category.equals(res.getString(R.string.category_work).toLowerCase())) {
                icon = R.drawable.ic_work_grey600_24dp;
            }
            categories.add(new NavigationAdapter.NavigationItem("category:" + cursor.getString(0), cursor.getString(0), cursor.getInt(1), icon));
        }
        cursor.close();
        return categories;
    }

    public void toggleFavorite(@NonNull DBNote note, @Nullable ICallback callback) {
        note.setFavorite(!note.isFavorite());
        note.setStatus(DBStatus.LOCAL_EDITED);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_status, note.getStatus().getTitle());
        values.put(key_favorite, note.isFavorite() ? "1" : "0");
        db.update(table_notes, values, key_id + " = ?", new String[]{String.valueOf(note.getId())});
        if (callback != null) {
            serverSyncHelper.addCallbackPush(callback);
        }
        serverSyncHelper.scheduleSync(true);
    }

    public void setCategory(@NonNull DBNote note, @NonNull String category, @Nullable ICallback callback) {
        note.setCategory(category);
        note.setStatus(DBStatus.LOCAL_EDITED);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_status, note.getStatus().getTitle());
        values.put(key_category, note.getCategory());
        db.update(table_notes, values, key_id + " = ?", new String[]{String.valueOf(note.getId())});
        if (callback != null) {
            serverSyncHelper.addCallbackPush(callback);
        }
        serverSyncHelper.scheduleSync(true);
    }

    /**
     * Updates a single Note with a new content.
     * The title is derived from the new content automatically, and modified date as well as DBStatus are updated, too -- if the content differs to the state in the database.
     *
     * @param oldNote    Note to be changed
     * @param newContent New content. If this is <code>null</code>, then <code>oldNote</code> is saved again (useful for undoing changes).
     * @param callback   When the synchronization is finished, this callback will be invoked (optional).
     * @return changed note if differs from database, otherwise the old note.
     */
    public DBNote updateNoteAndSync(long accountId, @NonNull DBNote oldNote, @Nullable String newContent, @Nullable ICallback callback) {
        //debugPrintFullDB();
        DBNote newNote;
        if (newContent == null) {
            newNote = new DBNote(oldNote.getId(), oldNote.getRemoteId(), oldNote.getModified(), oldNote.getTitle(), oldNote.getContent(), oldNote.isFavorite(), oldNote.getCategory(), oldNote.getEtag(), DBStatus.LOCAL_EDITED, accountId);
        } else {
            newNote = new DBNote(oldNote.getId(), oldNote.getRemoteId(), Calendar.getInstance(), NoteUtil.generateNonEmptyNoteTitle(newContent, getContext()), newContent, oldNote.isFavorite(), oldNote.getCategory(), oldNote.getEtag(), DBStatus.LOCAL_EDITED, accountId);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_status, newNote.getStatus().getTitle());
        values.put(key_title, newNote.getTitle());
        values.put(key_category, newNote.getCategory());
        values.put(key_modified, newNote.getModified().getTimeInMillis() / 1000);
        values.put(key_content, newNote.getContent());
        int rows = db.update(table_notes, values, key_id + " = ? AND (" + key_content + " != ? OR " + key_category + " != ?)", new String[]{String.valueOf(newNote.getId()), newNote.getContent(), newNote.getCategory()});
        // if data was changed, set new status and schedule sync (with callback); otherwise invoke callback directly.
        if (rows > 0) {
            notifyNotesChanged();
            if (callback != null) {
                serverSyncHelper.addCallbackPush(callback);
            }
            serverSyncHelper.scheduleSync(true);
            return newNote;
        } else {
            if (callback != null) {
                callback.onFinish();
            }
            return oldNote;
        }
    }

    /**
     * Updates a single Note with data from the server, (if it was not modified locally).
     * Thereby, an optimistic concurrency control is realized in order to prevent conflicts arising due to parallel changes from the UI and synchronization.
     * This is used by the synchronization task, hence no Synchronization will be triggered. Use updateNoteAndSync() instead!
     *
     * @param id                        local ID of Note
     * @param remoteNote                Note from the server.
     * @param forceUnchangedDBNoteState is not null, then the local note is updated only if it was not modified meanwhile
     * @return The number of the Rows affected.
     */
    int updateNote(long id, @NonNull CloudNote remoteNote, @Nullable DBNote forceUnchangedDBNoteState) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First, update the remote ID, since this field cannot be changed in parallel, but have to be updated always.
        ContentValues values = new ContentValues();
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
        values.put(key_category, remoteNote.getCategory());
        values.put(key_etag, remoteNote.getEtag());
        String whereClause;
        String[] whereArgs;
        if (forceUnchangedDBNoteState != null) {
            // used by: NoteServerSyncHelper.SyncTask.pushLocalChanges()
            // update only, if not modified locally during the synchronization
            // (i.e. all (!) user changeable columns (content, favorite) should still have the same value),
            // uses reference value gathered at start of synchronization
            whereClause = key_id + " = ? AND " + key_content + " = ? AND " + key_favorite + " = ? AND " + key_category + " = ?";
            whereArgs = new String[]{String.valueOf(id), forceUnchangedDBNoteState.getContent(), forceUnchangedDBNoteState.isFavorite() ? "1" : "0", forceUnchangedDBNoteState.getCategory()};
        } else {
            // used by: NoteServerSyncHelper.SyncTask.pullRemoteChanges()
            // update only, if not modified locally (i.e. STATUS="") and if modified remotely (i.e. any (!) column has changed)
            whereClause = key_id + " = ? AND " + key_status + " = ? AND (" + key_modified + "!=? OR " + key_title + "!=? OR " + key_favorite + "!=? OR " + key_category + "!=? OR " + (remoteNote.getEtag() != null ? key_etag + " IS NULL OR " : "") + key_etag + "!=? OR " + key_content + "!=?)";
            whereArgs = new String[]{String.valueOf(id), DBStatus.VOID.getTitle(), Long.toString(remoteNote.getModified().getTimeInMillis() / 1000), remoteNote.getTitle(), remoteNote.isFavorite() ? "1" : "0", remoteNote.getCategory(), remoteNote.getEtag(), remoteNote.getContent()};
        }
        int i = db.update(table_notes, values, whereClause, whereArgs);
        Log.d(getClass().getSimpleName(), "updateNote: " + remoteNote + " || forceUnchangedDBNoteState: " + forceUnchangedDBNoteState + "  => " + i + " rows updated");
        return i;
    }

    /**
     * Marks a Note in the Database as Deleted. In the next Synchronization it will be deleted
     * from the Server.
     *
     * @param id long - ID of the Note that should be deleted
     * @return Affected rows
     */
    @SuppressWarnings("UnusedReturnValue")
    public int deleteNoteAndSync(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_status, DBStatus.LOCAL_DELETED.getTitle());
        int i = db.update(table_notes,
                values,
                key_id + " = ?",
                new String[]{String.valueOf(id)});
        notifyNotesChanged();
        getNoteServerSyncHelper().scheduleSync(true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            shortcutManager.getPinnedShortcuts().forEach((shortcut) -> {
                String shortcutId = id + "";
                if (shortcut.getId().equals(shortcutId)) {
                    Log.v(NoteSQLiteOpenHelper.class.getSimpleName(), "Removing shortcut for " + shortcutId);
                    shortcutManager.disableShortcuts(Collections.singletonList(shortcutId), context.getResources().getString(R.string.note_has_been_deleted));
                }
            });
        }
        return i;
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
    }

    /**
     * Notify about changed notes.
     */
    void notifyNotesChanged() {
        updateSingleNoteWidgets();
        updateNoteListWidgets();
    }

    /**
     * Update single note widget, if the note data was changed.
     */
    private void updateSingleNoteWidgets() {
        Intent intent = new Intent(getContext(), SingleNoteWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        getContext().sendBroadcast(intent);
    }

    /**
     * Update note list widgets, if the note data was changed.
     */
    private void updateNoteListWidgets() {
        Intent intent = new Intent(getContext(), NoteListWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        getContext().sendBroadcast(intent);
    }

    public boolean hasAccounts() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), table_accounts) > 0;
    }

    public long addAccount(String url, String username, String accountName, String token) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_url, url);
        values.put(key_username, username);
        values.put(key_account_name, accountName);
        values.put(key_token, token);
        return db.insert(table_accounts, null, values);
    }

    public LocalAccount getAccount(long i) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table_accounts, new String[]{key_id, key_url, key_account_name, key_username, key_display_name, key_token}, key_id + " = ?", new String[]{i + ""}, null, null, null, null);
        LocalAccount account = new LocalAccount();
        while (cursor.moveToNext()) {
            account.setId(cursor.getLong(0));
            account.setUrl(cursor.getString(1));
            account.setAccountName(cursor.getString(2));
            account.setUserName(cursor.getString(3));
            account.setDisplayName(cursor.getString(4));
            account.setToken(cursor.getString(5));
        }
        cursor.close();
        return account;
    }

    public List<LocalAccount> getAccounts() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table_accounts, new String[]{key_id, key_url, key_account_name, key_username, key_display_name, key_token}, null, null, null, null, null);
        List<LocalAccount> accounts = new ArrayList<>();
        while (cursor.moveToNext()) {
            LocalAccount account = new LocalAccount();
            account.setId(cursor.getLong(0));
            account.setUrl(cursor.getString(1));
            account.setAccountName(cursor.getString(2));
            account.setUserName(cursor.getString(3));
            account.setDisplayName(cursor.getString(4));
            account.setToken(cursor.getString(5));
            accounts.add(account);
        }
        cursor.close();
        return accounts;
    }

    public LocalAccount getLocalAccountByAccountName(String accountName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table_accounts, new String[]{key_id, key_url, key_account_name, key_username, key_display_name, key_token}, key_account_name + " = ?", new String[]{accountName}, null, null, null, null);
        LocalAccount account = new LocalAccount();
        while (cursor.moveToNext()) {
            account.setId(cursor.getLong(0));
            account.setUrl(cursor.getString(1));
            account.setAccountName(cursor.getString(2));
            account.setUserName(cursor.getString(3));
            account.setDisplayName(cursor.getString(4));
            account.setToken(cursor.getString(5));
        }
        cursor.close();
        return account;
    }

    public void deleteAccount(long accountId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedAccounts = db.delete(table_accounts, key_id + " = ?", new String[] {accountId + ""});
        if(deletedAccounts < 1) {
            throw new IllegalArgumentException("The given accountId does not delete any row");
        } else if(deletedAccounts > 1) {
            Log.e(NoteSQLiteOpenHelper.class.getSimpleName(), "AccountId '" + accountId + "' deleted unexpectedly '" + deletedAccounts + "' accounts");
        }
        int deletedNotes = db.delete(table_notes, key_account_id + " = ?", new String[]{accountId + ""});
        Log.v(NoteSQLiteOpenHelper.class.getSimpleName(), "Deleted " + deletedNotes + " notes from account " + accountId);
    }
}
