package it.niedermann.owncloud.notes.persistence;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.Note;
import it.niedermann.owncloud.notes.util.ICallback;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SyncService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CREATE_NOTE = "it.niedermann.owncloud.notes.persistence.action.create";
    private static final String ACTION_EDIT_NOTE = "it.niedermann.owncloud.notes.persistence.action.edit";
    private static final String ACTION_SYNC = "it.niedermann.owncloud.notes.persistence.action.edit";
    private static final String ACTION_DELETE_NOTE = "it.niedermann.owncloud.notes.persistence.action.delete";

    private static final String CONTENT = "it.niedermann.owncloud.notes.persistence.extra.content";
    private static final String NOTE = "it.niedermann.owncloud.notes.persistence.extra.note";
    private static final String NOTEID = "it.niedermann.owncloud.notes.persistence.extra.note";


    private static NoteSQLiteOpenHelper db = null;
    private static Note createdNote = null;
    private static List<Note> notes = null;


    public SyncService() {
        super("SyncService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSync(Context context) {
        Log.i(SyncService.class.getName(), "Start SycnService: ACTION_SYNC");
        //setup Database
        if (db == null) db = new NoteSQLiteOpenHelper(context);
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_SYNC);
        context.startService(intent);
    }

    public static List<Note> getNotes(Context context) {
        if(notes!= null) {
            return notes;
        }else{
            if (db == null) db = new NoteSQLiteOpenHelper(context);
            // Not started as Service to wait for the Notes!
            handleActionSync();
            return notes;
        }
    }

    public static Note getCreatedNote() {
        return createdNote;
    }

    public static void addCallback(ICallback callback) {
        if (db != null) db.getNoteServerSyncHelper().addCallback(callback);
        else throw new Resources.NotFoundException("Not Synced with Server");
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionEditNote(Context context, Note editedNote) {
        Log.i(SyncService.class.getName(), "Start SycnService: ACTION_EDIT_NOTE id: " + editedNote.getId());
        //setup Database
        if (db == null) db = new NoteSQLiteOpenHelper(context);
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_EDIT_NOTE);
        intent.putExtra(NOTE, editedNote);
        context.startService(intent);
    }


    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionCreateNote(Context context, String noteContent) {
        Log.i(SyncService.class.getName(), "Start SycnService: ACTION_CREATE_NOTE content: " + noteContent);
        //setup Database
        if (db == null) db = new NoteSQLiteOpenHelper(context);
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_CREATE_NOTE);
        intent.putExtra(CONTENT, noteContent);
        context.startService(intent);
    }

    public static void startActionDeleteNoteAndSync(Context context, long id) {
        Log.i(SyncService.class.getName(), "Start SycnService: ACTION_DELETE_NOTE id: " + id);
        //setup Database
        if (db == null) db = new NoteSQLiteOpenHelper(context);
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_DELETE_NOTE);
        intent.putExtra(NOTEID, id);
        context.startService(intent);
    }

    public static void resetLocalDatabase() {
        db = null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SYNC.equals(action)) {
                handleActionSync();
            } else if (ACTION_EDIT_NOTE.equals(action)) {
                final Note param1 = (Note) intent.getSerializableExtra(NOTE);
                handleActionEditNote(param1);
            } else if (ACTION_CREATE_NOTE.equals(action)) {
                final String param1 = intent.getStringExtra(CONTENT);
                handleActionCreateNote(param1);
            } else if (ACTION_DELETE_NOTE.equals(action)) {
                final Long param1 = intent.getLongExtra(NOTEID, -1);
                handleActionDeleteNote(param1);
            }
        }
    }

    private void handleActionDeleteNote(Long param1) {
        if (param1 != -1) {
            db.deleteNoteAndSync(param1);
        }
        notes = db.getNotes();
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private static void handleActionSync() {
        Log.i(SyncService.class.getName(), "Start SycnService: handle ACTION_SYNC");
        // TODO: Handle action ACTION_SYNC
        db.synchronizeWithServer();
        notes = db.getNotes();
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionEditNote(Note editedNote) {
        // TODO: Handle action ACTION_EDIT_NOTE
        db.updateNoteAndSync(editedNote);
        notes = db.getNotes();
    }

    private void handleActionCreateNote(String noteContent) {
        // TODO: Handle action ACTION_CREATE_NOTE
        long id = db.addNoteAndSync(noteContent);
        createdNote = db.getNote(id);
        notes = db.getNotes();
    }


}
