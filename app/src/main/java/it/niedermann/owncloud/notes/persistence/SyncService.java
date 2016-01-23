package it.niedermann.owncloud.notes.persistence;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.util.ArrayList;

import it.niedermann.owncloud.notes.model.Item;
import it.niedermann.owncloud.notes.model.Note;

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

    private static final String CONTENT = "it.niedermann.owncloud.notes.persistence.extra.content";
    private static final String NOTE = "it.niedermann.owncloud.notes.persistence.extra.note";
    private static final String ADAPTER = "it.niedermann.owncloud.notes.persistence.extra.Adapter";


    private static NoteSQLiteOpenHelper db = null;



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
    public static void startActionSync(Context context, ArrayList<Item> param1) {
        //setup Database
        db = new NoteSQLiteOpenHelper(context);

        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_SYNC);
        intent.putExtra(ADAPTER, param1);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionEditNote(Context context, Note editedNote) {
        //setup Database
        db = new NoteSQLiteOpenHelper(context);

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
        //setup Database
        db = new NoteSQLiteOpenHelper(context);

        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_CREATE_NOTE);
        intent.putExtra(CONTENT, noteContent);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SYNC.equals(action)) {
                final ArrayList param1 = intent.getParcelableArrayListExtra(ADAPTER);
                handleActionSync(param1);
            } else if (ACTION_EDIT_NOTE.equals(action)) {
                final Note param1 = (Note) intent.getSerializableExtra(NOTE);
                handleActionEditNote(param1);
            } else if (ACTION_CREATE_NOTE.equals(action)) {
                final String param1 = intent.getStringExtra(CONTENT);
                handleActionCreateNote(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSync(ArrayList notes) {
        // TODO: Handle action ACTION_SYNC
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionEditNote(Note editedNote) {
        // TODO: Handle action ACTION_EDIT_NOTE
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionCreateNote(String noteContent) {
        // TODO: Handle action ACTION_CREATE_NOTE
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
