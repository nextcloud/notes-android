package it.niedermann.owncloud.notes.persistence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.android.activity.SettingsActivity;
import it.niedermann.owncloud.notes.model.DBNote;
import it.niedermann.owncloud.notes.model.DBStatus;
import it.niedermann.owncloud.notes.model.OwnCloudNote;
import it.niedermann.owncloud.notes.util.ICallback;
import it.niedermann.owncloud.notes.util.NotesClient;
import it.niedermann.owncloud.notes.util.NotesClientUtil.LoginStatus;

/**
 * Helps to synchronize the Database to the Server.
 * <p/>
 * Created by stefan on 20.09.15.
 */
public class NoteServerSyncHelper {

    private static NoteServerSyncHelper instance;

    /**
     * Get (or create) instance from NoteServerSyncHelper.
     * This has to be a singleton in order to realize correct registering and unregistering of
     * the BroadcastReceiver, which listens on changes of network connectivity.
     * @param dbHelper NoteSQLiteOpenHelper
     * @return NoteServerSyncHelper
     */
    public static synchronized NoteServerSyncHelper getInstance(NoteSQLiteOpenHelper dbHelper) {
        if(instance==null) {
            instance = new NoteServerSyncHelper(dbHelper);
        }
        return instance;
    }

    private final NoteSQLiteOpenHelper dbHelper;
    private final Context appContext;

    // Track network connection changes using a BroadcastReceiver
    private boolean networkConnected = false;
    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr = (ConnectivityManager)appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
            if(activeInfo != null && activeInfo.isConnected()) {
                Log.d(NoteServerSyncHelper.class.getSimpleName(), "Network connection established.");
                networkConnected = true;
                scheduleSync(false);
            } else {
                networkConnected = false;
                Log.d(NoteServerSyncHelper.class.getSimpleName(), "No network connection.");
            }
        }
    };

    // current state of the synchronization
    private boolean syncActive = false;
    private boolean syncScheduled = false;

    // list of callbacks for both parts of synchronziation
    private List<ICallback> callbacksPush = new ArrayList<>();
    private List<ICallback> callbacksPull = new ArrayList<>();



    private NoteServerSyncHelper(NoteSQLiteOpenHelper db) {
        this.dbHelper = db;
        this.appContext = db.getContext().getApplicationContext();

        // Registers BroadcastReceiver to track network connection changes.
        appContext.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void finalize() throws Throwable {
        appContext.unregisterReceiver(networkReceiver);
        super.finalize();
    }


    /**
     * Synchronization is only possible, if there is an active network connection.
     * NoteServerSyncHelper observes changes in the network connection.
     * The current state can be retrieved with this method.
     * @return true if sync is possible, otherwise false.
     */
    public boolean isSyncPossible() {
        return networkConnected;
    }


    /**
     * Adds a callback method to the NoteServerSyncHelper for the synchronization part push local changes to the server.
     * All callbacks will be executed once the synchronization operations are done.
     * After execution the callback will be deleted, so it has to be added again if it shall be
     * executed the next time all synchronize operations are finished.
     *
     * @param callback Implementation of ICallback, contains one method that shall be executed.
     */
    public void addCallbackPush(ICallback callback) {
        callbacksPush.add(callback);
    }
    /**
     * Adds a callback method to the NoteServerSyncHelper for the synchronization part pull remote changes from the server.
     * All callbacks will be executed once the synchronization operations are done.
     * After execution the callback will be deleted, so it has to be added again if it shall be
     * executed the next time all synchronize operations are finished.
     *
     * @param callback Implementation of ICallback, contains one method that shall be executed.
     */
    public void addCallbackPull(ICallback callback) {
        callbacksPull.add(callback);
    }


    /**
     * Schedules a synchronization and start it directly, if the network is connected and no
     * synchronization is currently running.
     * @param onlyLocalChanges Whether to only push local changes to the server or to also load the whole list of notes from the server.
     */
    public void scheduleSync(boolean onlyLocalChanges) {
        Log.d(getClass().getSimpleName(), "Sync requested ("+(onlyLocalChanges?"onlyLocalChanges":"full")+"; "+(networkConnected?"network connected":"network NOT connected")+", "+(syncActive?"sync active":"sync NOT active")+") ...");
        if(isSyncPossible() && (!syncActive || onlyLocalChanges)) {
            Log.d(getClass().getSimpleName(), "... starting now");
            SyncTask syncTask = new SyncTask(onlyLocalChanges);
            syncTask.addCallbacks(callbacksPush);
            callbacksPush = new ArrayList<>();
            if(!onlyLocalChanges) {
                syncTask.addCallbacks(callbacksPull);
                callbacksPull = new ArrayList<>();
            }
            syncTask.execute();
        } else if(!onlyLocalChanges) {
            Log.d(getClass().getSimpleName(), "... scheduled");
            syncScheduled = true;
        } else {
            Log.d(getClass().getSimpleName(), "... do nothing");
        }
    }

    /**
     * SyncTask is an AsyncTask which performs the synchronization in a background thread.
     * Synchronization consists of two parts: pushLocalChanges and pullRemoteChanges.
     */
    private class SyncTask extends AsyncTask<Void, Void, LoginStatus> {
        private final boolean onlyLocalChanges;
        private final List<ICallback> callbacks = new ArrayList<>();
        private NotesClient client;

        public SyncTask(boolean onlyLocalChanges) {
            this.onlyLocalChanges = onlyLocalChanges;
        }

        public void addCallbacks(List<ICallback> callbacks) {
            this.callbacks.addAll(callbacks);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!onlyLocalChanges && syncScheduled) {
                syncScheduled = false;
            }
            syncActive = true;
        }

        @Override
        protected LoginStatus doInBackground(Void... voids) {
            client = createNotesClient(); // recreate NoteClients on every sync in case the connection settings was changed
            Log.d(getClass().getSimpleName(), "STARTING SYNCHRONIZATION");
            dbHelper.debugPrintFullDB();
            LoginStatus status = LoginStatus.OK;
            pushLocalChanges();
            if(!onlyLocalChanges) {
                status = pullRemoteChanges();
            }
            dbHelper.debugPrintFullDB();
            Log.d(getClass().getSimpleName(), "SYNCHRONIZATION FINISHED");
            return status;
        }

        /**
         * Push local changes: for each locally created/edited/deleted Note, use NotesClient in order to push the changed to the server.
         */
        private void pushLocalChanges() {
            Log.d(getClass().getSimpleName(), "pushLocalChanges()");
            List<DBNote> notes = dbHelper.getLocalModifiedNotes();
            for (DBNote note : notes) {
                Log.d(getClass().getSimpleName(), "   Process Local Note: "+note);
                try {
                    OwnCloudNote remoteNote=null;
                    switch(note.getStatus()) {
                        case LOCAL_EDITED:
                            Log.d(getClass().getSimpleName(), "   ...create/edit");
                            // if note is not new, try to edit it.
                            if (note.getRemoteId()>0) {
                                Log.d(getClass().getSimpleName(), "   ...try to edit");
                                remoteNote = client.editNote(note.getRemoteId(), note.getContent());
                            }
                            // However, the note may be deleted on the server meanwhile; or was never synchronized -> (re)create
                            // Please note, thas dbHelper.updateNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                            if (remoteNote == null) {
                                Log.d(getClass().getSimpleName(), "   ...Note does not exist on server -> (re)create");
                                remoteNote = client.createNote(note.getContent());
                                dbHelper.updateNote(note.getId(), remoteNote, note);
                            } else {
                                dbHelper.updateNote(note.getId(), remoteNote, note);
                            }
                            break;
                        case LOCAL_DELETED:
                            if(note.getRemoteId()>0) {
                                Log.d(getClass().getSimpleName(), "   ...delete (from server and local)");
                                try {
                                    client.deleteNote(note.getRemoteId());
                                } catch (FileNotFoundException e) {
                                    Log.d(getClass().getSimpleName(), "   ...Note does not exist on server (anymore?) -> delete locally");
                                }
                            } else {
                                Log.d(getClass().getSimpleName(), "   ...delete (only local, since it was not synchronized)");
                            }
                            // Please note, thas dbHelper.deleteNote() realizes an optimistic conflict resolution, which is required for parallel changes of this Note from the UI.
                            dbHelper.deleteNote(note.getId(), DBStatus.LOCAL_DELETED);
                            break;
                        default:
                            throw new IllegalStateException("Unknown State of Note: "+note);
                    }
                } catch (IOException | JSONException e) {
                    // FIXME make some errors visible in the UI
                    e.printStackTrace();
                }
            }
        }

        /**
         * Pull remote Changes: update or create each remote note (if local pendant has no changes) and remove remotely deleted notes.
         */
        private LoginStatus pullRemoteChanges() {
            Log.d(getClass().getSimpleName(), "pullRemoteChanges()");
            LoginStatus status = null;
            try {
                List<DBNote> localNotes = dbHelper.getNotes();
                Map<Long, Long> localIDmap = new HashMap<>();
                for (DBNote note : localNotes) {
                    localIDmap.put(note.getRemoteId(), note.getId());
                }
                List<OwnCloudNote> remoteNotes = client.getNotes();
                Set<Long> remoteIDs = new HashSet<>();
                // pull remote changes: update or create each remote note
                for (OwnCloudNote remoteNote : remoteNotes) {
                    Log.d(getClass().getSimpleName(), "   Process Remote Note: "+remoteNote);
                    remoteIDs.add(remoteNote.getRemoteId());
                    if(localIDmap.containsKey(remoteNote.getRemoteId())) {
                        Log.d(getClass().getSimpleName(), "   ... found -> Update");
                        dbHelper.updateNote(localIDmap.get(remoteNote.getRemoteId()), remoteNote, null);
                    } else {
                        Log.d(getClass().getSimpleName(), "   ... create");
                        dbHelper.addNote(remoteNote);
                    }
                }
                Log.d(getClass().getSimpleName(), "   Remove remotely deleted Notes (only those without local changes)");
                // remove remotely deleted notes (only those without local changes)
                for (DBNote note : localNotes) {
                    if(note.getStatus()==DBStatus.VOID && !remoteIDs.contains(note.getRemoteId())) {
                        Log.d(getClass().getSimpleName(), "   ... remove "+note);
                        dbHelper.deleteNote(note.getId(), DBStatus.VOID);
                    }
                }
                status = LoginStatus.OK;
            } catch (IOException e) {
                status = LoginStatus.CONNECTION_FAILED;
            } catch (JSONException e) {
                status = LoginStatus.JSON_FAILED;
            }
            return status;
        }

        @Override
        protected void onPostExecute(LoginStatus status) {
            super.onPostExecute(status);
            if (status!=LoginStatus.OK) {
                Toast.makeText(appContext, appContext.getString(R.string.error_sync, appContext.getString(status.str)), Toast.LENGTH_LONG).show();
            }
            syncActive = false;
            // notify callbacks
            for (ICallback callback : callbacks) {
                callback.onFinish();
            }
            // start next sync if scheduled meanwhile
            if(syncScheduled) {
                scheduleSync(false);
            }
        }
    }

    private NotesClient createNotesClient() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext.getApplicationContext());
        String url = preferences.getString(SettingsActivity.SETTINGS_URL, SettingsActivity.DEFAULT_SETTINGS);
        String username = preferences.getString(SettingsActivity.SETTINGS_USERNAME, SettingsActivity.DEFAULT_SETTINGS);
        String password = preferences.getString(SettingsActivity.SETTINGS_PASSWORD, SettingsActivity.DEFAULT_SETTINGS);
        return new NotesClient(url, username, password);
    }
}
