package it.niedermann.owncloud.notes.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.OwnCloudNote;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.NoteUtil;

/**
 * Helps to add, get, update and delete Notes with the option to trigger a Resync with the Server.
 * <p/>
 * Created by stefan on 19.09.15.
 */
public class NoteSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final int database_version = 5;
    private static final String database_name = "OWNCLOUD_NOTES";
    private static final String table_notes = "NOTES";
    private static final String key_id = "ID";
    private static final String key_remote_id = "REMOTEID";
    private static final String key_status = "STATUS";
    private static final String key_title = "TITLE";
    private static final String key_modified = "MODIFIED";
    private static final String key_content = "CONTENT";
    private static final String[] columns = {key_id, key_remote_id, key_status, key_title, key_modified, key_content};

    private NoteServerSyncHelper serverSyncHelper = null;
    private Context context = null;

    public NoteSQLiteOpenHelper(Context context) {
        super(context, database_name, null, database_version);
        this.context = context.getApplicationContext();
        serverSyncHelper = NoteServerSyncHelper.getInstance(this);
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
        db.execSQL("CREATE TABLE '" + table_notes + "' ( '" +
                key_id + "' INTEGER PRIMARY KEY AUTOINCREMENT, '" +
                key_remote_id + "' INTEGER, '" +
                key_status + "' VARCHAR(50), '" +
                key_title + "' TEXT, '" +
                key_modified + "' TEXT, '" +
                key_content + "' TEXT)");
        // FIXME create index for status and remote_id
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion<4) {
            clearDatabase(db);
        }
        if(oldVersion<5) {
            db.execSQL("ALTER TABLE "+table_notes+" ADD COLUMN "+key_remote_id+" INTEGER");
            db.execSQL("UPDATE "+table_notes+" SET "+key_remote_id+"="+key_id+" WHERE ("+key_remote_id+" IS NULL OR "+key_remote_id+"=0) AND "+key_status+"!=?", new String[]{DBStatus.LOCAL_CREATED.getTitle()});
            db.execSQL("UPDATE "+table_notes+" SET "+key_remote_id+"=0, "+key_status+"=? WHERE "+key_status+"=?", new String[]{DBStatus.LOCAL_EDITED.getTitle(), DBStatus.LOCAL_CREATED.getTitle()});
        }
    }

    private void clearDatabase(SQLiteDatabase db) {
        db.delete(table_notes, null, null);
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
    public long addNoteAndSync(String content) {
        DBNote note = new DBNote(0, 0, Calendar.getInstance(), NoteUtil.generateNoteTitle(content), content, DBStatus.LOCAL_EDITED);
        long id = addNote(note);
        getNoteServerSyncHelper().scheduleSync(true);
        return id;
    }

    /**
     * Inserts a note directly into the Database.
     * No Synchronisation will be triggered! Use addNoteAndSync()!
     *
     * @param note Note to be added. Remotely created Notes must be of type OwnCloudNote and locally created Notes must be of Type DBNote (with DBStatus.LOCAL_EDITED)!
     */
    long addNote(OwnCloudNote note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if(note instanceof DBNote) {
            DBNote dbNote = (DBNote)note;
            if (dbNote.getId() > 0) {
                values.put(key_id, dbNote.getId());
            }
            values.put(key_status, dbNote.getStatus().getTitle());
        } else {
            values.put(key_status, DBStatus.VOID.getTitle());
        }
        if(note.getRemoteId()>0) {
            values.put(key_remote_id, note.getRemoteId());
        }
        values.put(key_title, note.getTitle());
        values.put(key_modified, note.getModified(NoteSQLiteOpenHelper.DATE_FORMAT));
        values.put(key_content, note.getContent());
        long id = db.insert(table_notes, null, values);
        db.close();
        return id;
    }

    /**
     * Get a single Note by ID
     *
     * @param id int - ID of the requested Note
     * @return requested Note
     */
    @SuppressWarnings("unused")
    public DBNote getNote(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =
                db.query(table_notes,
                        columns,
                        key_id + " = ? AND " + key_status + " != ?",
                        new String[]{String.valueOf(id), DBStatus.LOCAL_DELETED.getTitle()},
                        null,
                        null,
                        null,
                        null);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        DBNote note = getNoteFromCursor(cursor);
        cursor.close();
        return note;
    }

    /**
     * Query the database with a custom raw query.
     * @param selection      A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself).
     * @param selectionArgs  You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
     * @param orderBy        How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.
     * @return List of Notes
     */
    private List<DBNote> getNotesCustom(String selection, String[] selectionArgs, String orderBy) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table_notes, columns, selection, selectionArgs, null, null, orderBy);
        List<DBNote> notes = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                notes.add(getNoteFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }

    /**
     * Creates a DBNote object from the current row of a Cursor.
     * @param cursor    database cursor
     * @return DBNote
     */
    private DBNote getNoteFromCursor(Cursor cursor) {
        Calendar modified = Calendar.getInstance();
        try {
            String modifiedStr = cursor.getString(4);
            if (modifiedStr != null)
                modified.setTime(new SimpleDateFormat(DATE_FORMAT, Locale.GERMANY).parse(modifiedStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new DBNote(cursor.getLong(0), cursor.getLong(1), modified, cursor.getString(3), cursor.getString(5), DBStatus.parse(cursor.getString(2)));
    }

    public void debugPrintFullDB() {
        List<DBNote> notes = getNotesCustom("", new String[]{}, key_modified + " DESC");
        Log.d(getClass().getSimpleName(), "Full Database:");
        for (DBNote note : notes) {
            Log.d(getClass().getSimpleName(), "     "+note);
        }
    }

    /**
     * Returns a list of all Notes in the Database
     *
     * @return List&lt;Note&gt;
     */
    public List<DBNote> getNotes() {
        return getNotesCustom(key_status + " != ?", new String[]{DBStatus.LOCAL_DELETED.getTitle()}, key_modified + " DESC");
    }

    /**
     * Returns a list of all Notes in the Database
     *
     * @return List&lt;Note&gt;
     */
    public List<DBNote> searchNotes(String query) {
        return getNotesCustom(key_status + " != ? AND " + key_content + " LIKE ?", new String[]{DBStatus.LOCAL_DELETED.getTitle(), "%" + query + "%"}, key_modified + " DESC");
    }

    /**
     * Returns a list of all Notes in the Database with a sepcial status, e.g. Edited, Deleted,...
     *
     * @return List&lt;Note&gt;
     */
    public List<DBNote> getNotesByStatus(DBStatus status) {
        return getNotesCustom(key_status + " = ?", new String[]{status.getTitle()}, null);
    }
    /**
     * Returns a list of all Notes in the Database with were modified locally
     *
     * @return List&lt;Note&gt;
     */
    public List<DBNote> getLocalModifiedNotes() {
        return getNotesCustom(key_status + " != ?", new String[]{DBStatus.VOID.getTitle()}, null);
    }

    /**
     * Updates a single Note and sets a synchronization Flag.
     *
     * @param note Note - Note with the updated Information
     * @param callback When the synchronization is finished, this callback will be invoked (optional).
     */
    public void updateNoteAndSync(DBNote note, ICallback callback) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_status, DBStatus.LOCAL_EDITED.getTitle());
        values.put(key_title, note.getTitle());
        values.put(key_modified, note.getModified(DATE_FORMAT));
        values.put(key_content, note.getContent());
        int rows = db.update(table_notes, values, key_id + " = ? AND " + key_content + " != ?", new String[]{String.valueOf(note.getId()), note.getContent()});
        db.close();
        // if data was changed, set new status and schedule sync (with callback); otherwise invoke callback directly.
        if(rows > 0) {
            note.setStatus(DBStatus.LOCAL_EDITED);
            if(callback!=null) {
                serverSyncHelper.addCallbackPush(callback);
            }
            serverSyncHelper.scheduleSync(true);
        } else {
            if(callback!=null) {
                callback.onFinish();
            }
        }
    }

    /**
     * Updates a single Note with data from the server, (if it was not modified locally).
     * Thereby, an optimistic concurrency control is realized in order to prevent conflicts arising due to parallel changes from the UI and synchronization.
     * This is used by the synchronization task, hence no Synchronization will be triggered. Use updateNoteAndSync() instead!
     *
     * @param id local ID of Note
     * @param remoteNote Note from the server.
     * @param forceUnchangedDBNoteState is not null, then the local note is updated only if it was not modified meanwhile
     *
     * @return The number of the Rows affected.
     */
    int updateNote(long id, OwnCloudNote remoteNote, DBNote forceUnchangedDBNoteState) {
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
        values.put(key_modified, remoteNote.getModified(DATE_FORMAT));
        values.put(key_content, remoteNote.getContent());
        String whereClause;
        String[] whereArgs;
        if(forceUnchangedDBNoteState!=null) {
            // used by: NoteServerSyncHelper.SyncTask.pushLocalChanges()
            // update only, if not modified locally during the synchronization,
            // uses reference value gathered at start of synchronization
            whereClause = key_id + " = ? AND " + key_content + " = ?";
            whereArgs = new String[]{String.valueOf(id), forceUnchangedDBNoteState.getContent()};
        } else {
            // used by: NoteServerSyncHelper.SyncTask.pullRemoteChanges()
            // update only, if not modified locally
            whereClause = key_id + " = ? AND " + key_status + " = ?";
            whereArgs = new String[]{String.valueOf(id), DBStatus.VOID.getTitle()};
        }
        int i = db.update(table_notes, values, whereClause, whereArgs);
        db.close();
        Log.d(getClass().getSimpleName(), "updateNote: "+remoteNote+" || forceUnchangedDBNoteState: "+forceUnchangedDBNoteState+"  => "+i+" rows updated");
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
        db.close();
        getNoteServerSyncHelper().scheduleSync(true);
        return i;
    }

    /**
     * Delete a single Note from the Database, if it has a specific DBStatus.
     * Thereby, an optimistic concurrency control is realized in order to prevent conflicts arising due to parallel changes from the UI and synchronization.
     *
     * @param id long - ID of the Note that should be deleted.
     * @param forceDBStatus DBStatus, e.g., if Note was marked as LOCAL_DELETED (for NoteSQLiteOpenHelper.SyncTask.pushLocalChanges()) or is unchanged VOID (for NoteSQLiteOpenHelper.SyncTask.pullRemoteChanges())
     */
    void deleteNote(long id, DBStatus forceDBStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table_notes,
                key_id + " = ? AND " + key_status + " = ?",
                new String[]{String.valueOf(id), forceDBStatus.getTitle()});
        db.close();
    }
}
